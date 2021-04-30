package ca.uwaterloo.cs.jgrok.fb;

public class AlgebraOperation {
    
    /**
     * "+="
     */
    public static TupleSet append(TupleSet tSet1,
                                  TupleSet tSet2) {
        TupleSet result;
        
        if(tSet1.columns() == tSet2.columns()) {
            result = tSet1.newSet();
            tSet1.data.addAll(tSet2.data);
            result.data = tSet1.data;
            result.setHasDuplicates(true);
            result.sortLevel = -1;
        } else {
            result = new TupleSet();
            tSet1.data.addAll(tSet2.data);
            result.data = tSet1.data;
            result.setHasDuplicates(true);
            result.sortLevel = -1;
        }
        
        return result;
    }
    
    public static TupleSet inverse(TupleSet tSet) {
        TupleSet result;
        
        if(tSet instanceof NodeSet) {
            result = (TupleSet)tSet.clone();
        } else {
            result = tSet.newSet();
            result.data = Operation.inverse(tSet.data);
        }
        result.setHasDuplicates(tSet.hasDuplicates());
        
        return result;
    }

    public static TupleSet union(TupleSet tSet1,
                                 TupleSet tSet2) {
        TupleSet result;
        
        if(tSet1.size() == 0) return (TupleSet)tSet2.clone();
        if(tSet2.size() == 0) return (TupleSet)tSet1.clone();
        
        if(tSet1.columns() == tSet2.columns()) {
            result = tSet1.newSet();
            result.data = Operation.union(tSet1.data, tSet2.data);
            result.data.sort_removeDuplicates();
            result.setHasDuplicates(false);
            result.sortLevel = 0;
        } else {
            result = (TupleSet)tSet1.clone();
        }
        
        return result;
    }
    
    public static TupleSet difference(TupleSet tSet1,
                                      TupleSet tSet2) {
        TupleSet result;
        
        if(tSet1.size() == 0) return new TupleSet();
        if(tSet2.size() == 0) return (TupleSet)tSet1.clone();
        
        if(tSet1.columns() == tSet2.columns()) {
            result = tSet1.newSet();
            tSet1.sort();
            tSet2.sort();
            result.data = Operation.difference(tSet1.data, tSet2.data);
            result.data.sort_removeDuplicates();
            result.setHasDuplicates(false);
            result.sortLevel = 0;
        } else {
            result = (TupleSet)tSet1.clone();
        }
        
        return result;
    }
    
    public static TupleSet intersection(TupleSet tSet1,
                                        TupleSet tSet2) {
        TupleSet result;
        
        if(tSet1.size() == 0) return new TupleSet();
        if(tSet2.size() == 0) return new TupleSet();
        
        result = tSet1.newSet();
        if(tSet1.columns() == tSet2.columns()) {
            tSet1.sort();
            tSet2.sort();
            result.data = Operation.intersection(tSet1.data, tSet2.data);
            result.data.sort_removeDuplicates();
            result.setHasDuplicates(false);
            result.sortLevel = 0;
        }
        
        return result;
    }
    
    public static NodeSet union(NodeSet set1,
                                NodeSet set2) {
        NodeSet result;
        
        result = new NodeSet();
        result.data = Operation.union(set1.data, set2.data);
        result.data.sort_removeDuplicates();
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet union(EdgeSet edgeSet1,
                                EdgeSet edgeSet2) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.union(edgeSet1.data, edgeSet2.data);
        result.data.sort_removeDuplicates();
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static NodeSet difference(NodeSet set1,
                                     NodeSet set2) {
        NodeSet result;
        
        set1.trySort(0);
        set2.trySort(0);
        result = new NodeSet();
        result.data = Operation.difference(set1.data, set2.data);
        result.sortLevel = 0;
        
        if(!set1.hasDuplicates())
            result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet difference(EdgeSet edgeSet1,
                                     EdgeSet edgeSet2) {
        EdgeSet result;
        
        edgeSet1.trySort(2);
        edgeSet2.trySort(2);
        result = new EdgeSet();
        result.data = Operation.difference(edgeSet1.data, edgeSet2.data);
        result.sortLevel = 2;
        
        if(!edgeSet1.hasDuplicates())
            result.setHasDuplicates(false);
        
        return result;
    }
    
    public static NodeSet intersection(NodeSet set1,
                                       NodeSet set2) {
        NodeSet result;
        
        result = new NodeSet();
        set1.trySort(0);
        set2.trySort(0);
        result.data = Operation.intersection(set1.data, set2.data);
        result.sortLevel = 0;
        
        if(!set1.hasDuplicates() || !set2.hasDuplicates())
            result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet intersection(EdgeSet edgeSet1,
                                       EdgeSet edgeSet2) {
        EdgeSet result;
        
        result = new EdgeSet();
        edgeSet1.trySort(2);
        edgeSet2.trySort(2);
        result.data = Operation.intersection(edgeSet1.data, edgeSet2.data);
        result.sortLevel = 2;
        
        if(!edgeSet1.hasDuplicates() || !edgeSet2.hasDuplicates())
            result.setHasDuplicates(false);
        
        return result;
    }
    
    public static NodeSet project(NodeSet nodeSet,
                                  EdgeSet edgeSet) {
        NodeSet result;
        
        nodeSet.trySort(0);
        edgeSet.trySort(0);
        result = new NodeSet();
        result.data = Operation.forwardProjection(nodeSet.data, edgeSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static NodeSet project(EdgeSet edgeSet,
                                  NodeSet nodeSet) {
        NodeSet result;
        
        nodeSet.trySort(0);
        result = new NodeSet();
        if(edgeSet.sortLevel == 1)
            result.data = Operation.backwardProjection(edgeSet.data,
                                                       nodeSet.data);
        else
            result.data = Operation.backwardProjection(edgeSet.shadow(),
                                                       nodeSet.data);
        
        result.sortLevel = 0;
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static TupleSet project(NodeSet set, TupleSet rel) {
        TupleSet result;
        
        if(rel.columns() == 3)
            result = new EdgeSet();
        else
            result = new TupleSet();
        
        set.trySort(0);
        rel.trySort(0);
        result.data = OperationRel.forwardProjection(set.data, rel.data);
        result.sortLevel = 0;
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static TupleSet project(TupleSet rel, NodeSet set) {
        TupleSet result;

        if(rel.columns() == 3)
            result = new EdgeSet();
        else
            result = new TupleSet();
        
        set.trySort(0);
        rel.trySort(rel.columns() - 1);
        result.data = OperationRel.backwardProjection(rel.data, set.data);
        result.sortLevel = 0;
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet id(TupleSet tSet) {
        EdgeSet result;
        
        result = new EdgeSet();        
        result.data = Operation.id(tSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static NodeSet entityOf(TupleSet tSet) {
        NodeSet result;
        
        if(tSet instanceof NodeSet) {
            result = (NodeSet)tSet.clone();
            result.trySort(0);
            result.removeDuplicates();
        } else {
            result = new NodeSet();
            result.data = Operation.entityOf(tSet.data);
        }
        
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        return result;
    }
    
    public static NodeSet domainOf(TupleSet tSet) {
        NodeSet result;
        
        tSet.trySort(0);
        result = new NodeSet();
        result.data = Operation.domainOf(tSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static NodeSet rangeOf(TupleSet tSet) {
        NodeSet result;
        
        tSet.sortRng();        
        result = new NodeSet();
        result.data = Operation.rangeOf(tSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet crossProduct(NodeSet set1,
                                       NodeSet set2) {
        EdgeSet result;
        
        set1.trySort(0);
        if(set1.hasDuplicates())
            set1.data.removeDuplicates();
        
        if(set1 != set2) {
            set2.trySort(0);
            if(set2.hasDuplicates())
                set2.data.removeDuplicates();
        }
        
        result = new EdgeSet();
        result.data = Operation.crossProduct(set1.data, set2.data);
        result.setHasDuplicates(false);
        result.sortLevel = 0;
        
        return result;
    }
    
    public static EdgeSet composition(EdgeSet edgeSet, int pow) {
        if(pow < 0) {
            throw new IllegalArgumentException("Illegal Argument: pow = " + pow);
        }
        
        if(pow == 0) {
            return id(entityOf(edgeSet));
        } else if(pow == 1) {
            return (EdgeSet)edgeSet.clone();
        } else {
            EdgeSet result = edgeSet;
            
            for(int i = 1; i < pow; i++) {
                result = composition(result, edgeSet);
            }
            
            result.setHasDuplicates(false);
            
            return result;
        }
    }
    
    public static EdgeSet composition(NodeSet set, EdgeSet edgeSet) {
        EdgeSet result;
        
        set.trySort(0);
        edgeSet.trySort(0);
        result = new EdgeSet();
        result.data = Operation.composition(set.data, edgeSet.data);
        
        result.trySort(2);
        result.data.removeDuplicates();
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet composition(EdgeSet edgeSet, NodeSet set) {
        EdgeSet result;
        
        set.trySort(0);
        result = new EdgeSet();
        if(edgeSet.sortLevel == 1)
            result.data = Operation.composition(edgeSet.data, set.data);
        else
            result.data = Operation.composition(edgeSet.shadow(), set.data);
        
        result.trySort(2);
        result.data.removeDuplicates();
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static EdgeSet composition(EdgeSet edgeSet1,
                                      EdgeSet edgeSet2) {
        EdgeSet result;
        
        result = new EdgeSet();
        if(edgeSet1.data == edgeSet2.data) {
            TupleList shadow;
            shadow = edgeSet1.shadow();
            edgeSet1.trySort(0);
            result.data = Operation.composition(shadow, edgeSet1.data);
        } else {
            edgeSet1.trySort(1);
            edgeSet2.trySort(0);
            result.data = Operation.composition(edgeSet1.data, edgeSet2.data);
        }
        
        result.trySort(2);
        result.data.removeDuplicates();
        result.setHasDuplicates(false);
        
        return result;
    }
    
    public static TupleSet composition(TupleSet rel, NodeSet set) {
        return compositionRel(rel, set);
    }
    
    public static TupleSet composition(NodeSet set, TupleSet rel) {
        return compositionRel(set, rel);
    }

    public static TupleSet composition(TupleSet rel, EdgeSet edgeSet) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel.size() > 0 && edgeSet.size() > 0) {
            int col_a = rel.columns() - 1;
            int col_b = 0;
            
            rel.trySort(col_a);
            edgeSet.trySort(col_b);
            result.data = OperationRel.composition(rel.data, col_a, true,
                                                   edgeSet.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet composition(EdgeSet edgeSet, TupleSet rel) {
        TupleSet result;
        
        result = new TupleSet();
        if(edgeSet.size() > 0 && rel.size() > 0) {
            int col_a = 1;
            int col_b = 0;

            edgeSet.trySort(col_a);
            rel.trySort(col_b);
            result.data = OperationRel.composition(edgeSet.data, col_a, true,
                                                   rel.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet composition(TupleSet rel1, TupleSet rel2) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel1.size() > 0 && rel2.size() > 0) {
            int col_a = rel1.columns() - 1;
            int col_b = 0;
            
            rel1.trySort(col_a);
            rel2.trySort(col_b);
            result.data = OperationRel.composition(rel1.data, col_a, true,
                                                   rel2.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(EdgeSet edgeSet1,
                                          EdgeSet edgeSet2) {
        TupleSet result;
        result = new TupleSet();
        
        if(edgeSet1.data == edgeSet2.data) {
            TupleList shadow;
            shadow = edgeSet1.shadow();
            edgeSet1.trySort(0);
            result.data = OperationRel.compositionRel(shadow, 1, true,
                                                      edgeSet1.data, 0, true);
        } else {
            edgeSet1.trySort(1);
            edgeSet2.trySort(0);
            result.data = OperationRel.compositionRel(edgeSet1.data, 1, true,
                                                      edgeSet2.data, 0, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(TupleSet rel, NodeSet set) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel.size() > 0 && set.size() > 0) {
            int col_a = rel.columns() - 1;
            int col_b = 0;
            
            rel.trySort(col_a);
            set.trySort(col_b);
            result.data = OperationRel.compositionRel(rel.data, col_a, true,
                                                      set.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(NodeSet set, TupleSet rel) {
        TupleSet result;
        
        result = new TupleSet();
        if(set.size() > 0 && rel.size() > 0) {
            set.trySort(0);
            rel.trySort(0);
            result.data = OperationRel.compositionRel(set.data, 0, true,
                                                      rel.data, 0, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(TupleSet rel, EdgeSet edgeSet) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel.size() > 0 && edgeSet.size() > 0) {
            int col_a = rel.columns() - 1;
            int col_b = 0;
            
            rel.trySort(col_a);
            edgeSet.trySort(col_b);
            result.data = OperationRel.compositionRel(rel.data, col_a, true,
                                                      edgeSet.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(EdgeSet edgeSet, TupleSet rel) {
        TupleSet result;
        
        result = new TupleSet();
        if(edgeSet.size() > 0 && rel.size() > 0) {
            int col_a = 1;
            int col_b = 0;
            
            edgeSet.trySort(col_a);
            rel.trySort(col_b);
            result.data = OperationRel.compositionRel(edgeSet.data, col_a, true,
                                                      rel.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(TupleSet rel1, TupleSet rel2) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel1.size() > 0 && rel2.size() > 0) {
            int col_a = rel1.columns() - 1;
            int col_b = 0;
            
            rel1.trySort(col_a);
            rel2.trySort(col_b);
            result.data = OperationRel.compositionRel(rel1.data, col_a, true,
                                                      rel2.data, col_b, true);
        }
        
        return result;
    }
    
    public static TupleSet compositionRel(TupleSet rel, int col, NodeSet set) {
        TupleSet result;
        
        result = new TupleSet();
        if(rel.size() > 0 && set.size() > 0) {
            int col_a = col;
            int col_b = 0;
            
            rel.trySort(col_a);
            set.trySort(col_b);
            result.data = OperationRel.compositionRel(rel.data, col_a, true,
                                                      set.data, col_b, true);
        }
        
        return result;
    }
    
    public static EdgeSet unclosure(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.unclosure(edgeSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static EdgeSet transitiveClosure(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.closure(edgeSet.data, false);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static EdgeSet reflectiveClosure(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.closure(edgeSet.data, true);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static EdgeSet symmetricClousre(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.symmetricClosure(edgeSet.data);
        result.setHasDuplicates(false);
        result.sortLevel = 2;
        
        return result;
    }
    
    public static EdgeSet inverse(EdgeSet edgeSet) {
        EdgeSet result;
        
        result = new EdgeSet();
        result.data = Operation.inverse(edgeSet.data);
        result.setHasDuplicates(edgeSet.hasDuplicates());
        
        switch(edgeSet.sortLevel) {
        case 0:
            result.sortLevel = 1;
            break;
        case 1:
            result.sortLevel = 0;
            break;
        case 2:
            result.sortLevel = 1;
            break;
        }
        
        return result;
    }
}
