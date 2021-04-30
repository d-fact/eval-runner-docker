package ca.uwaterloo.cs.jgrok.fb;

import java.util.StringTokenizer;

public class SpecialOperation {
    
    public static int encode(NodeSet nSet) {
        nSet.trySort(0);        
        return encodeID(nSet.data);
    }
    
    public static NodeSet decode(int setID) {
        NodeSet nSet = new NodeSet();
        nSet.data = decodeID(setID);
        nSet.sortLevel = 0;
        return nSet;
    }
    
    public static NodeSet basket(String exp) {
        NodeSet result = null;
        
        if(exp.length() > 0) {
            if(exp.charAt(0) == '(' && exp.charAt(exp.length()-1) == ')') {
                String s = exp.substring(1, exp.length()-1);
                StringTokenizer st = new StringTokenizer(s);
                NodeSet tokenSet = new NodeSet();
                while(st.hasMoreTokens()) {
                    tokenSet.add(st.nextToken());
                }
                if(tokenSet.size() > 0)
                    result = NodeSet.singleton(encode(tokenSet));
            }
        }
        
        if(result == null) result = new NodeSet();
        return result;
    }
    
    public static NodeSet basket(NodeSet set) {
        NodeSet result;
        
        if(set.size() > 0) {
            result = NodeSet.singleton(encode(set));
            result.sortLevel = 0;
        } else {
            result = new NodeSet();
        }
        
        result.setHasDuplicates(false);
        
        // Return a singleton.
        return result;
    }
    
    public static EdgeSet basket(EdgeSet eSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        eSet.trySort(0);
        result.data = basket(eSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static NodeSet unbasket(String exp) {
        NodeSet result = new NodeSet();
        
        if(exp.length() > 0) {
            if(exp.charAt(0) == '(' && exp.charAt(exp.length()-1) == ')') {
                String s = exp.substring(1, exp.length()-1);
                StringTokenizer st = new StringTokenizer(s);
                while(st.hasMoreTokens()) {
                    result.add(st.nextToken());
                }
            }
        }
        
        return result;
    }
    
    public static NodeSet unbasket(NodeSet set) {
        int id;
        int[] ids;
        NodeSet result = new NodeSet();
        
        TupleList t_l = set.data;
        for(int i = 0; i < t_l.size(); i++) {
            id = t_l.get(i).getDom();
            ids = IDManager.parse(id);
            for(int j = 0; j < ids.length; j++) {
                result.add(ids[j]);
            }
        }
        
        return result;
    }

    public static EdgeSet unbasket(EdgeSet eSet) {
        int dom;
        int rng;
        int[] ids;
        EdgeSet result = new EdgeSet();
        
        Tuple t;
        TupleList t_l = eSet.data;
        for(int i = 0; i < t_l.size(); i++) {
            t = t_l.get(i);
            dom = t.getDom();
            rng = t.getRng();
            ids = IDManager.parse(rng);
            for(int j = 0; j < ids.length; j++) {
                result.add(dom, ids[j]);
            }
        }
            
        return result;
    }
    
    public static EdgeSet lattice(NodeSet nSet) {
        TupleList t_l;
        int i, j, count;
        EdgeSet result = new EdgeSet();
        
        t_l = nSet.data;
        count = t_l.size();
        
        int currID, compID;
        NodeSet curr, comp;
        for(i = 0; i < count - 1; i++) {
            currID = t_l.get(i).getDom();
            curr = decode(currID);
            for(j = i+1; j < count; j++) {
                compID = t_l.get(j).getDom();
                comp = decode(compID);
                int val = Operation.comparison(curr.data, comp.data);
                if(val == -1)
                    result.data.add(new Tuple4Edge(compID, currID));
                else if(val == 1)
                    result.data.add(new Tuple4Edge(currID, compID));
            }
        }
        
        return AlgebraOperation.unclosure(result);
    }
    
    /////////////////////////////////////////////////////////////////
    
    /**
     * <pre>
     * Expressions:
     *     encodeID(set) : int
     * Precondition:
     *     set is sorted into ascending order
     * Postcondition:
     *     A unique number identifying the set
     * </pre>
     */
    static int encodeID(TupleList setL) {
        int count;
        count = setL.size();
        
        if(count > 0) {
            int[] ids = new int[count];
            for(int i = 0; i < count; i++) {
                ids[i] = setL.get(i).getDom();
            }
            return IDManager.getID(ids);
        }
        
        return 0;
    }
    
    /**
     * <pre>
     * Expressions:
     *     decodeID(int) : set
     * Precondition:
     *     none
     * Postcondition:
     *     A set identified by the given unique number
     * </pre>
     */
    static TupleList decodeID(int setID) {
        int[] ids = IDManager.parse(setID);
        TupleList t_l = new TupleList(ids.length);
        
        for(int i= 0; i < ids.length; i++) {
            t_l.add(new Tuple4Node(ids[i]));
        }
        
        return t_l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     basket(eset) : eset
     * Precondition:
     *     eset is sorted into ascending order in DOM
     * Postcondition:
     *     A tuple list sorted into asceding order in DOM.
     * Example:
     *     Input
     *         x   A
     *         x   B
     *         x   C
     *         y   U
     *         y   V
     *     Output
     *         x   (A B C)
     *         y   (U V)
     * </pre>
     */
    static TupleList basket(TupleList list) {
        int curr, next;
        int count = list.size();
        TupleList l = new TupleList();
        TupleList v = new TupleList();
        
        Tuple t;
        if(count > 0) {
            t = (Tuple)list.get(0);
            curr = t.getDom();
            v.add(new Tuple4Node(t.getRng()));
            
            for(int i = 1; i < count; i++) {
                t = (Tuple)list.get(i);
                next = t.getDom();
                if(curr == next) {
                    v.add(new Tuple4Node(t.getRng()));
                } else {
                    RadixSorter.sort(v, 0, v);
                    l.add(new Tuple4Edge(curr, encodeID(v)));
                    
                    v.clear();
                    curr = next;
                    v.add(new Tuple4Node(t.getRng()));
                }
            }
            
            RadixSorter.sort(v, 0, v);
            l.add(new Tuple4Edge(curr, encodeID(v)));
        }
        return l;
    }
}
