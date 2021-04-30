package ca.uwaterloo.cs.jgrok.fb;

class Tuple4Edge extends TupleImpl {
    
    Tuple4Edge(int from, int to) {
        super(from, to);
    }
    
    public Object clone() {
        return new Tuple4Edge(getDom(), getRng());
    }
}
