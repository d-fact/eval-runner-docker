package ca.uwaterloo.cs.jgrok.fb;

class TupleListFactory {
    
    static TupleList newTupleList() {
        return new TupleList();
    }
    
    static TupleList newTupleList(int capacity) {
        return new TupleList(capacity);
    }
    
    static TupleList newNodeList() {
        return new TupleList();
    }
    
    static TupleList newNodeList(int capacity) {
        return new TupleList(capacity);
    }
    
    static TupleList newEdgeList() {
        return new TupleList();
    }
    
    static TupleList newEdgeList(int capacity) {
        return new TupleList(capacity);
    }
}
