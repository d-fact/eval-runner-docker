package ca.uwaterloo.cs.jgrok.fb;

class OperationDegree {
    
    /**
     * <pre>
     * Expressions:
     *     degree(rel, col) : rel 
     * Precondition:
     *     list is sorted into ascending order in col
     * Postcondition:
     *     A correct degree relation without any duplicate tuples.
     *     All tuples in degree are sorted into ascending order in DOM.
     * Example:
     *    >> print R
     *    1 x a
     *    2 x b
     *    3 y l
     *    4 y m
     *    5 y n
     *    >> degree(R, 1)
     *    x 2
     *    y 3
     * </pre>
     */
    static TupleList degree(TupleList list, int col) {
        int deg;
        int elem, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            deg = 1;
            elem = list.get(0).get(col);
            for(int i = 1; i < count; i++) {
                next = list.get(i).get(col);
                if(elem == next) {
                    deg++;
                    continue;
                }
                
                l.add(new Tuple4Edge(elem, IDManager.getID(""+deg)));
                deg = 1;
                elem = next;
            }
            
            l.add(new Tuple4Edge(elem, IDManager.getID(""+deg)));
        }
        return l;
    }
    
    /**
     * <pre>
     * Expressions:
     *     degree(rel, cols) : rel 
     * Precondition:
     *     list is sorted into ascending order in cols
     * Postcondition:
     *     A correct degree relation without any duplicate tuples.
     *     All tuples in degree are sorted into ascending order in DOM.
     * Example:
     *    >> print R
     *    1 x a
     *    2 x a
     *    3 y m
     *    4 y m
     *    5 y m
     *    >> degree(R, 1, 2)
     *    x a 2
     *    y m 3
     * </pre>
     */
    static TupleList degree(TupleList list, int[] cols) {
        int deg;
        int[] elem, next;
        int count = list.size();
        TupleList l = new TupleList();
        
        if(count > 0) {
            deg = 1;
            elem = list.get(0).get(cols);
            
            for(int i = 1; i < count; i++) {
                next = list.get(i).get(cols);
                
                boolean equal = true;
                for(int k = 0; k < cols.length; k++) {
                    if(elem[k] != next[k]) {
                        equal = false;
                        break;
                    }
                }
                
                if(equal) {
                    deg++;
                    continue;
                }
                
                l.add(new TupleImpl(elem, IDManager.getID(""+deg)));
                deg = 1;
                elem = next;
            }
            
            l.add(new TupleImpl(elem, IDManager.getID(""+deg)));
        }
        return l;
    }
    
    static TupleList outdegree(TupleList list, int col) {
        if(list.size() > 0) {
            int[] cols;
            cols = new int[list.get(0).size()-col];
            for(int i = 0; i < cols.length; i++) {
                cols[i] = col+i;
            }
            
            TupleList t_l;
            t_l= list.getTupleList(cols);
            t_l.sort();
            t_l.removeDuplicates();
            
            return degree(t_l, 0);
        } else {
            return new TupleList();
        }
    }
    
    static TupleList indegree(TupleList list, int col) {
        if(list.size() > 0) {
            int[] cols;
            cols = new int[col+1];
            for(int i = 0; i < cols.length; i++) {
                cols[i] = col-i;
            }
            
            TupleList t_l;
            t_l= list.getTupleList(cols);
            t_l.sort();
            t_l.removeDuplicates();
            
            return degree(t_l, 0);
        } else {
            return new TupleList();
        }
    }
}
