package ca.uwaterloo.cs.jgrok.fb;

public class UtilityOperation {
    
    public static EdgeSet subtree(NodeSet roots, EdgeSet contain) {
        EdgeSet Do = AlgebraOperation.reflectiveClosure(contain);
        NodeSet useSet = AlgebraOperation.project(roots, Do);
        NodeSet entSet = AlgebraOperation.entityOf(contain);
        NodeSet delSet = AlgebraOperation.difference(entSet, useSet);
        EdgeSet subtree = UtilityOperation.delset(contain, delSet);
        return subtree;
    }
    
    public static EdgeSet head(EdgeSet edgeSet) {
        EdgeSet result;
        //??? UtilityOperation.head
        result = new EdgeSet();
        System.out.println("Not implemented yet: head()");
        
        return result;
    }
    
    public static EdgeSet tail(EdgeSet edgeSet) {
        EdgeSet result;
        //??? UtilityOperation.tail
        result = new EdgeSet();
        System.out.println("Not implemented yet: tail()");
        
        return result;
    }
    
    public static NodeSet sinkOf(EdgeSet edgeSet) {
        NodeSet result;
        
        edgeSet.trySort(0);
        result = new NodeSet();
        result.data = Operation.difference(Operation.rangeOf(edgeSet.shadow()),
                                           Operation.domainOf(edgeSet.data));
        result.sortLevel = 0;
        result.setHasDuplicates(false);        
        
        return result;
    }
    
    public static NodeSet sourceOf(EdgeSet edgeSet) {
        NodeSet result;
        
        edgeSet.trySort(0);
        result = new NodeSet();
        result.data = Operation.difference(Operation.domainOf(edgeSet.data),
                                           Operation.rangeOf(edgeSet.shadow()));
        result.sortLevel = 0;
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet localof(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        if(edgeSet.sortLevel == 1)
            result.data = Operation.localof(edgeSet.data);
        else 
            result.data = Operation.localof(edgeSet.shadow());
        
        result.sortLevel = 0;
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet outdegree(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        edgeSet.trySort(0);
        result.data = Operation.outdegree(edgeSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }

    public static EdgeSet degree(TupleSet tSet, int col) {
        EdgeSet result;
        
        result = new EdgeSet();
        tSet.trySort(col);
        result.data = OperationDegree.degree(tSet.data, col);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }

    public static TupleSet degree(TupleSet tSet, int[] cols) {
        TupleSet result;
        
        result = new TupleSet();
        for(int i = cols.length-1; i >= 0; i--) {
            tSet.trySort(cols[i]);
        }
        result.data = OperationDegree.degree(tSet.data, cols);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet indegree(TupleSet tSet, int col) {
        EdgeSet result;
        
        result = new EdgeSet();
        tSet.trySort(col);
        result.data = OperationDegree.indegree(tSet.data, col);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet outdegree(TupleSet tSet, int col) {
        EdgeSet result;
        
        result = new EdgeSet();
        tSet.trySort(col);
        result.data = OperationDegree.outdegree(tSet.data, col);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet delset(EdgeSet edgeSet, NodeSet set) {
        EdgeSet result;
        
        result = new EdgeSet();
        edgeSet.trySort(0);
        set.trySort(0);
        result.data = Operation.delset(edgeSet.data, set.data);
        result.setHasDuplicates(edgeSet.hasDuplicates());
        result.trySort(0);
        
        return result;
    }
    
    public static EdgeSet reach(NodeSet nodeSet,
                                EdgeSet edgeSet) {
        EdgeSet result;
        
        nodeSet.trySort(0);
        edgeSet.trySort(0);
        result = new EdgeSet();
        result.data = Operation.reach(nodeSet.data, edgeSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static EdgeSet reach(NodeSet srcSet,
                                NodeSet sinkSet,
                                EdgeSet edgeSet) {
        EdgeSet invSet;
        EdgeSet result;
        
        result = reach(srcSet, edgeSet);
        invSet = AlgebraOperation.inverse(result);
        result = reach(sinkSet, invSet);
        result = AlgebraOperation.inverse(result);
        result.sortLevel = 1;
        result.setHasDuplicates(false);

        return result;
    }
}
