package ca.uwaterloo.cs.jgrok.fb;

import java.util.ArrayList;

public class ShowDB {
    TupleList shadow;
    TupleList attList;
    TupleList relList;
    TupleList nameList;
    StringBuffer buffer;
    
    int name_ID = IDManager.getID("@name");
    int inst_ID = IDManager.getID("$INSTANCE");
    
    public ShowDB() {
        buffer = new StringBuffer();
        attList = new TupleList(1000);
        relList = new TupleList(1000);
    }
    
    public void addRels(EdgeSet eSet) {
        Tuple t, t3;
        TupleList t_l;
        t_l = eSet.data;
        
        ////////////////////
        // relame src trg //
        ////////////////////
        int size = t_l.size();
        int[] data = new int[3];
        data[0] = IDManager.getID(eSet.getName());
        
        for(int i = 0; i < size; i++) {
            t = t_l.get(i);
            data[1] = t.getDom();
            data[2] = t.getRng();
            t3 = new TupleImpl(data);
            relList.add(t3);
        }
    }
    
    public void addAtts(EdgeSet eSet) {
        Tuple t, t3;
        TupleList t_l;
        t_l = eSet.data;
        
        /////////////////////
        // attname src val //
        /////////////////////
        int size = t_l.size();
        int[] data = new int[3];
        data[0] = IDManager.getID(eSet.getName());
        if(data[0] == name_ID) {
            nameList = (TupleList)t_l.clone();
        }
        
        for(int i = 0; i < size; i++) {
            t = t_l.get(i);
            data[1] = t.getDom();
            data[2] = t.getRng();
            t3 = new TupleImpl(data);
            attList.add(t3);
        }
    }
    
    public void setup() {
        TupleList t_l;
        
        if(nameList != null) {
            t_l = new TupleList(nameList.size());
            RadixSorter.sort(nameList, 0, t_l);
            nameList = t_l;
        }
        
        t_l = new TupleList(attList.size());
        RadixSorter.sort(attList, 0, t_l);
        attList = new TupleList(t_l.size());
        RadixSorter.sort(t_l, 1, attList);
        
        t_l = new TupleList(relList.size());
        RadixSorter.sort(relList, 0, t_l);
        relList = new TupleList(t_l.size());
        RadixSorter.sort(t_l, 1, relList);
        shadow = new TupleList(t_l.size());
        RadixSorter.sort(t_l, 2, shadow);
    }
    
    /**
     * Gets a node's name.
     */
    public String getName(int nodeID) {
        if(nameList == null) return null;
        int ind = BinarySearch.search(nameList, nodeID, 0);
        if(ind < 0) return null;
        return IDManager.get(nameList.get(ind).get(1));
    }
    
    /**
     * Gets a node's attributes.
     * @param nodeID the node's unique ID.
     * @return an array of three strings, where
     * <pre>
     *     string[0] - the node's name
     *     string[1] - the node's instance
     *     string[2] - the node's other attributes.
     * </pre>
     */
    public String[] getAtts(int nodeID) {
        int ind;
        String[] result;
        
        result = new String[3];
        result[0] = null;
        result[1] = null;
        result[2] = null;
        
        ind = BinarySearch.search(attList, nodeID, 1);
        if(ind < 0) return result;
        
        buffer.delete(0, buffer.length());
        buffer.append("{ ");
        
        int t0;
        Tuple t;
               while(ind < attList.size()) {
            t = attList.get(ind);
            if(t.get(1) != nodeID) break;
            
            t0 = t.get(0);
            if(t0 == name_ID)
                result[0] = IDManager.get(t.get(2));
            else if(t0 == inst_ID)
                result[1] = IDManager.get(t.get(2));
            else {
                buffer.append(IDManager.get(t0).substring(1));
                buffer.append('=');
                buffer.append(IDManager.get(t.get(2)));
                buffer.append(' ');
            }
            ind++;
        }
        
        if(buffer.length() > 2) {
            buffer.append("}");
            result[2] = buffer.toString();
        }
        
        return result;
    }
    
    public ArrayList<String> getRels(int nodeID) {
        int ind;
        int count;
        Tuple t;
        String name;
        
        ArrayList<String> result = new ArrayList<String>();
        
        ind = BinarySearch.search(relList, nodeID, 1);
        if(ind >= 0) {
            buffer.delete(0, buffer.length());
            buffer.append("( ");
            
            count = relList.size();
            while(ind < count) {
                t = relList.get(ind);
                if(t.get(1) != nodeID) break;
                
                buffer.delete(2, buffer.length());
                buffer.append(IDManager.get(t.get(0)));
                buffer.append(" -> ");
                name = getName(t.get(2));
                if(name != null) {
                    buffer.append(name);
                    buffer.append(" @ ");
                }
                buffer.append(IDManager.get(t.get(2)));
                buffer.append(" )");
                result.add(buffer.toString());
                
                ind++;
            }
        }
        
        ind = BinarySearch.search(shadow, nodeID, 2);
        if(ind >= 0) {
            buffer.delete(0, buffer.length());
            buffer.append("( ");
            
            count = shadow.size();
            while(ind < count) {
                t = shadow.get(ind);
                if(t.get(2) != nodeID) break;
                
                buffer.delete(2, buffer.length());
                buffer.append(IDManager.get(t.get(0)));
                buffer.append(" <- ");
                name = getName(t.get(1));
                if(name != null) {
                    buffer.append(name);
                    buffer.append(" @ ");
                }
                buffer.append(IDManager.get(t.get(1)));
                buffer.append(" )");
                result.add(buffer.toString());
                
                ind++;
            }
        }
        
        return result;
    }
    
    /**
     * Gets a node's attribute value.
     */
    public static String getAtt(int nodeID,
                                EdgeSet eSet) {
        int ind;
        TupleList t_l;
        
        if(eSet == null) return null;
        
        eSet.trySort(0);
        t_l = eSet.data;
        ind = BinarySearch.search(t_l, nodeID, 0);
        if(ind < 0) return null;
        return IDManager.get(t_l.get(ind).get(1));
    }
    
    public static ArrayList<String> getRels(int nodeID,
                                            EdgeSet eSet,
                                            EdgeSet attName) {
        int ind;
        int prev;
        int count;
        String name;
        TupleList domList;  // dom sorted.
        TupleList rngList;  // rng sorted.
        ArrayList<String> result = new ArrayList<String>();
        
        rngList = eSet.shadow();
        if((eSet.sortLevel % 2) != 0) {
            TupleList t_l = new TupleList(eSet.size());
            RadixSorter.sort(rngList, 0, t_l);
            eSet.data = t_l;
            eSet.sortLevel = 2;
        }
        
        domList = eSet.data;
        
        StringBuffer buffer;
        buffer = new StringBuffer();
        buffer.append("( ");
        buffer.append(eSet.getName());
        int constLen = buffer.length();
        
        // Get ( rel -> node @ nodeID )
        ind = BinarySearch.search(domList, nodeID, 0);
        if(ind >= 0) {
            buffer.append(" -> ");
            int len = buffer.length();
            
            while(ind > 0) {
                prev = ind - 1;
                if(domList.get(prev).get(0) == nodeID) ind = prev;
                else break;
            }
            
            Tuple t;
            count = domList.size();
            while(ind < count) {
                t = domList.get(ind);
                if(t.get(0) != nodeID) break;
                
                buffer.delete(len, buffer.length());
                name = ShowDB.getAtt(t.get(1), attName);
                if(name != null) {
                    buffer.append(name);
                    buffer.append(" @ ");
                }
                buffer.append(IDManager.get(t.get(1)));
                buffer.append(" )");
                result.add(buffer.toString());
                
                ind++;
            }
        }
        
        // Get ( rel <- node @ nodeID )
        ind = BinarySearch.search(rngList, nodeID, 1);
        if(ind >= 0) {
            buffer.delete(constLen, buffer.length());
            buffer.append(" <- ");
            int len = buffer.length();
            
            Tuple t;
            count = rngList.size();
            while(ind < count) {
                t = rngList.get(ind);
                if(t.get(1) != nodeID) break;
                
                buffer.delete(len, buffer.length());
                name = ShowDB.getAtt(t.get(0), attName);
                if(name != null) {
                    buffer.append(name);
                    buffer.append(" @ ");
                }
                buffer.append(IDManager.get(t.get(0)));
                buffer.append(" )");
                result.add(buffer.toString());
                
                ind++;
            }
        }
        return result;
    }
}
