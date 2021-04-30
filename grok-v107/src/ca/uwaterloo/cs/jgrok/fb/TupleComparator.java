package ca.uwaterloo.cs.jgrok.fb;

import java.util.Comparator;

public abstract class TupleComparator implements Comparator<Tuple> {
    
    public abstract int compare(Tuple t1, Tuple t2);
    
}
