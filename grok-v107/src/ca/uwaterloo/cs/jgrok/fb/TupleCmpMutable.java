package ca.uwaterloo.cs.jgrok.fb;

public class TupleCmpMutable extends TupleComparator {
    int[] cols;
    
    public TupleCmpMutable() {
        cols = null;
    }
    
    public TupleCmpMutable(int[] cols) {
        this.cols = cols;
    }
    
    public int compare(Tuple t1, Tuple t2) {
        if(cols == null)
            return fullCompare(t1, t2);
        else
            return partCompare(t1, t2);
    }
    
    private int fullCompare(Tuple t1, Tuple t2) {
        int dim;
        int dim1 = t1.size();
        int dim2 = t1.size();
        
        if(dim1 < dim2)
            dim = dim1;
        else
            dim = dim2;
        
        for(int col = 0; col < dim; col++) {
            if(t1.get(col) > t2.get(col)) return 1;
            else if(t1.get(col) < t2.get(col)) return -1;
        }
        
        if(dim1 == dim2) return 0;
        else if (dim1 == dim)  return -1;
        else return 1;
    }
    
    private int partCompare(Tuple t1, Tuple t2) {
        int col;
        int dim1 = t1.size();
        int dim2 = t2.size();
        
        for(int i = 0; i < cols.length; i++) {
            col = cols[i];
            
            if(col < dim1) {
                if(col < dim2) {
                    if(t1.get(col) > t2.get(col)) return 1;
                    else if(t1.get(col) < t2.get(col)) return -1;
                } else return 1;
            } else {
                if(col < dim2) return -1;
            }
        }
        
        return 0;
    }
}
