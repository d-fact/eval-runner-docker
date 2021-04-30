package ca.uwaterloo.cs.jgrok.fb;

public class TupleSelectorSimple extends TupleSelector {
    int col1;
    int col2;
    
    public TupleSelectorSimple(int col1, int col2) {
        this.col1 = col1;
        this.col2 = col2;
    }
    
    public boolean select(Tuple t) {
        return t.get(col1) == t.get(col2);
    }
}
