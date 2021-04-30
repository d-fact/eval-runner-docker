package ca.uwaterloo.cs.jgrok.fb;

public class TupleCmpSimple extends TupleComparator {
    int[] cols;
    
    public TupleCmpSimple(int[] cols) {
        this.cols = cols;
    }
    
    public int compare(Tuple t1, Tuple t2) {
        int col;
        
        for(int i = 0; i < cols.length; i++) {
            col = cols[i];
            if(t1.get(col) > t2.get(col))
                return 1;
            else if(t1.get(col) < t2.get(col))
                return -1;
        }
        
        return 0;
    }
}
