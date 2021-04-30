package ca.uwaterloo.cs.jgrok.fb;

public class BinarySearch {
    
    public static int search(TupleList list, int data, int col) {
        int cmp;
        int mid, low, high;
        
        low = 0;
        high = list.size() - 1;
        while (low <= high) {
            mid = (low + high) / 2;
            cmp = data - list.get(mid).get(col);
            if(cmp < 0)
                high = mid - 1;
            else if(cmp > 0)
                low = mid + 1;
            else {
                while(mid > 0) {
                    if(list.get(mid-1).get(col) == data) mid--;
                    else break;
                }
                return mid;
            }
        }
        
        return -1;
    }
    
    public static int search(TupleList list, Tuple tuple, TupleComparator tcmp) {
        int cmp;
        int mid, low, high;
        Tuple t;
        
        low = 0;
        high = list.size() - 1;
        while (low <= high) {
            mid = (low + high) / 2;
            t = list.get(mid);
            cmp = tcmp.compare(tuple, t);
            if(cmp < 0)
                high = mid - 1;
            else if(cmp > 0)
                low = mid + 1;
            else {
                while(mid > 0) {
                    t = list.get(mid-1);
                    if(tcmp.compare(tuple, t) == 0) mid--;
                    else break;
                }
                return mid;
            }
        }
        
        return -1;
    }
}
