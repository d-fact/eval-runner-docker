package ca.uwaterloo.cs.jgrok.fb;

/**
 * Tree2 defines a tree for edges.
 */
public class EdgeTree {
    private Tree nodeTree;
    private EdgeSet contain;
    
    /**
     * EdgeTree.
     * @param contain must be a tree.
     */
    public EdgeTree(EdgeSet contain) {
        this.contain = contain;
    }
    
    private void init(EdgeSet myContain) {
        nodeTree = new Tree(myContain);
        nodeTree.setLevels();
    }
    
    private int findCommonAncestor(int dom, int rng) {
        if(dom == rng) {
            return nodeTree.getParent(dom);
        }
        
        Tree.Entry domEntry;
        Tree.Entry rngEntry;
        
        domEntry = nodeTree.getEntry(dom);
        rngEntry = nodeTree.getEntry(rng);
        
        int node1 = dom;
        int node2 = rng;        
        int domLevel = domEntry.level;
        int rngLevel = rngEntry.level;
        
        int count = 0;
        if(domLevel > rngLevel) {
            count = domLevel - rngLevel;
            for(int i = 0; i < count; i++) {
                node1 = nodeTree.getParent(node1);
            }
        } else if(domLevel < rngLevel) {
            count = rngLevel - domLevel;
            for(int i = 0; i < count; i++) {
                node2 = nodeTree.getParent(node2);
            }
        }
        
        while(node1 > -1 || node2 > -1) {
            if(node1 == node2) return node1;
            node1 = nodeTree.getParent(node1);
            node2 = nodeTree.getParent(node2);
        }
        
        return -1; // This should never happen!
    }
    
    private int computeDistance(int dom, int rng) {
        if(dom == rng) return 0;
        
        Tree.Entry domEntry;
        Tree.Entry rngEntry;
        
        domEntry = nodeTree.getEntry(dom);
        rngEntry = nodeTree.getEntry(rng);
        
        int node1 = dom;
        int node2 = rng;        
        int domLevel = domEntry.level;
        int rngLevel = rngEntry.level;
        
        int count = 0;
        if(domLevel > rngLevel) {
            count = domLevel - rngLevel;
            for(int i = 0; i < count; i++) {
                node1 = nodeTree.getParent(node1);
            }
        } else if(domLevel < rngLevel) {
            count = rngLevel - domLevel;
            for(int i = 0; i < count; i++) {
                node2 = nodeTree.getParent(node2);
            }
        }
        
        int distance = count;
        while(node1 > -1 || node2 > -1) {
            if(node1 == node2) break;
            node1 = nodeTree.getParent(node1);
            node2 = nodeTree.getParent(node2);
            distance += 2;
        }
        
        return distance;
    }
    
    public EdgeSet getEdgeTree(EdgeSet edges) {
        NodeSet dom = AlgebraOperation.domainOf(contain);
        NodeSet rng = AlgebraOperation.rangeOf (contain);
        NodeSet ent = AlgebraOperation.entityOf(contain);
        NodeSet source = AlgebraOperation.difference(dom, rng);
        
        if(source.size() == 0) return new EdgeSet();
        
        EdgeSet usefulEdges;
        usefulEdges = AlgebraOperation.composition(ent, edges);
        usefulEdges = AlgebraOperation.composition(usefulEdges, ent);
        
        int top;
        boolean fake;
        EdgeSet myContain = contain;
        
        if(source.size() > 1) {
            fake = true;
            top = IDManager.getID(0, 0, 0);
            myContain = AlgebraOperation.crossProduct(NodeSet.singleton(top), source);
            myContain = AlgebraOperation.union(contain, myContain);
        } else {
            fake = false;
            top = source.getTupleList().get(0).getDom();
        }
        
        this.init(myContain);                
        
        /////////////////////////////////////////////////////////////////////////
        
        TupleList tlist = usefulEdges.data;
        EdgeSet edgeContain = new EdgeSet();
        
        for(int i = 0; i < tlist.size(); i++) {
            Tuple t = tlist.get(i);
            int node = IDManager.getID(t.getDom(), t.getRng());
            int nodeParent = findCommonAncestor(t.getDom(), t.getRng());
            
            if(fake && nodeParent == top) continue;
            
            if(nodeParent > -1) {
                edgeContain.add(nodeParent, node);
                
                node = nodeParent;
                while(node != top) {
                    nodeParent = nodeTree.getParent(node);
                    if(nodeParent > -1) {
                        edgeContain.add(nodeParent, node);
                        node = nodeParent;
                    } else break;
                }
            }
        }
        
        edgeContain.removeDuplicates();
        return edgeContain;
    }
    
    public EdgeSet getEdgeDistance(EdgeSet edges) {
        NodeSet dom = AlgebraOperation.domainOf(contain);
        NodeSet rng = AlgebraOperation.rangeOf (contain);
        NodeSet ent = AlgebraOperation.entityOf(contain);
        NodeSet source = AlgebraOperation.difference(dom, rng);
        
        if(source.size() == 0) return new EdgeSet();
        
        EdgeSet usefulEdges;
        usefulEdges = AlgebraOperation.composition(ent, edges);
        usefulEdges = AlgebraOperation.composition(usefulEdges, ent);
        
        EdgeSet myContain = contain;
        if(source.size() > 1) {
            int top = IDManager.getID(0, 0, 0);
            myContain = AlgebraOperation.crossProduct(NodeSet.singleton(top), source);
            myContain = AlgebraOperation.union(contain, myContain);
        } 
        
        this.init(myContain);
        
        /////////////////////////////////////////////////////////////////////////
        
        TupleList tlist = usefulEdges.data;
        EdgeSet edgeDistance = new EdgeSet();
        
        for(int i = 0; i < tlist.size(); i++) {
            Tuple t = tlist.get(i);
            int edge = IDManager.getID(t.getDom(), t.getRng());
            int distance = computeDistance(t.getDom(), t.getRng());
            
            edgeDistance.add(edge, IDManager.getID(distance+""));
        }
        
        edgeDistance.removeDuplicates();
        return edgeDistance;
    }
}
