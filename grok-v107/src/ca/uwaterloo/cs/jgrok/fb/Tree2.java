package ca.uwaterloo.cs.jgrok.fb;

/**
 * Tree2 defines a compound edge tree.
 */
public class Tree2 {
    EdgeSet contain;
    EdgeSet edgeContain;
    private Tree nodeTree;
    
    /**
     * Tree2.
     * @param contain must be a tree.
     */
    public Tree2(EdgeSet contain) {
        this.contain = contain;
    }
    
    private void init(EdgeSet myContain) {
        edgeContain = new EdgeSet();
        nodeTree = new Tree(myContain);
        nodeTree.setLevels();
    }
    
    /**
     * Calculate compound edges.
     * @param top the expected root of the edge tree.
     * @param fake whether top is a node or a faked edge.
     * @param edges the edges for creating upperlevel compound edges.
     * @param compound whether <code>edges</code> is compound or not.
     */
    private void setupCompoundEdges(int top,
                                    boolean fake,
                                    EdgeSet edges,
                                    boolean compound) {
        Tuple t;
        EdgeSet newEdges;
        Tree.Entry topEntry;
        Tree.Entry domParentEntry;
        Tree.Entry rngParentEntry;
        
        newEdges = new EdgeSet();
        newEdges.setName(edges.getName());
        topEntry = nodeTree.getEntry(top);
        int edgeType = IDManager.getID(edges.getName());
        
        int childEdge;
        int parentEdge;
        int dom, domParent, domAncestor;
        int rng, rngParent, rngAncestor;

        TupleList tlist = edges.data;
        int count = tlist.size();
        for(int i = 0; i < count; i++) {
            t = tlist.get(i);
            dom = t.getDom();
            rng = t.getRng();
            
            domParent = nodeTree.getParent(dom);
            rngParent = nodeTree.getParent(rng);
            
            if(domParent < 0 || rngParent < 0) continue;
            
            domParentEntry = nodeTree.getEntry(domParent);
            rngParentEntry = nodeTree.getEntry(rngParent);
            
            if(compound) {
                childEdge = IDManager.getID(edgeType, dom, rng);
            } else {
                childEdge = IDManager.getID(dom, rng);
            }
            
            if(domParentEntry.level > topEntry.level &&
               rngParentEntry.level > topEntry.level ) {
                
                if(domParent != rngParent) {
                    boolean levelup = false;
                    
                    if(domParentEntry.level > rngParentEntry.level) {
                        int j = domParentEntry.level- rngParentEntry.level;
                        domAncestor = domParent;
                        while(j > 0) {
                            domAncestor = nodeTree.getParent(domAncestor);
                            j--;
                        }
                        
                        if(domAncestor == rngParent) {
                            j = domParentEntry.level- rngParentEntry.level;
                            domAncestor = domParent;
                            
                            int currentEdge = childEdge;
                            while(j > 0) {
                                parentEdge = IDManager.getID(edgeType, domAncestor, rng);
                                edgeContain.add(parentEdge, currentEdge);
                                currentEdge = parentEdge;
                                
                                domAncestor = nodeTree.getParent(domAncestor);
                                j--;
                            }
                            edgeContain.add(domAncestor, currentEdge);
                            
                            // Add node contain path up to the top
                            // when top is not a fake.
                            if(!fake && domAncestor != top) {
                                int node;
                                int nodeParent;
                                node = domAncestor;
                                while(node != top) {
                                    nodeParent = nodeTree.getParent(node);
                                    edgeContain.add(nodeParent, node);
                                    node = nodeParent;
                                }
                            }
                            levelup = true;
                        }
                        
                    } else if(rngParentEntry.level > domParentEntry.level) {
                        int j = rngParentEntry.level- domParentEntry.level;
                        rngAncestor = rngParent;
                        while(j > 0) {
                            rngAncestor = nodeTree.getParent(rngAncestor);
                            j--;
                        }
                        
                        if(rngAncestor == domParent) {
                            j = rngParentEntry.level- domParentEntry.level;
                            rngAncestor = rngParent;
                            
                            int currentEdge = childEdge;
                            while(j > 0) {
                                parentEdge = IDManager.getID(edgeType, dom, rngAncestor);
                                edgeContain.add(parentEdge, currentEdge);
                                currentEdge = parentEdge;
                                
                                rngAncestor = nodeTree.getParent(rngAncestor);
                                j--;
                            }
                            edgeContain.add(rngAncestor, currentEdge);
                            
                            // Add node contain path up to the top
                            // when top is not a fake.
                            if(!fake && rngAncestor != top) {
                                int node;
                                int nodeParent;
                                node = rngAncestor;
                                while(node != top) {
                                    nodeParent = nodeTree.getParent(node);
                                    edgeContain.add(nodeParent, node);
                                    node = nodeParent;
                                }
                            }
                            levelup = true;
                        }
                    }
                    
                    if(!levelup) {
                        newEdges.add(domParent, rngParent);
                        parentEdge = IDManager.getID(edgeType, domParent, rngParent);
                        edgeContain.add(parentEdge, childEdge);
                    }
                } else {
                    edgeContain.add(domParent, childEdge);
                    
                    // Add node contain path up to the top
                    // when top is not a fake.
                    if(!fake && domParent != top) {
                        int node;
                        int nodeParent;
                        node = domParent;
                        while(node != top) {
                            nodeParent = nodeTree.getParent(node);
                            edgeContain.add(nodeParent, node);
                            node = nodeParent;
                        }
                    }
                }
            } else {
                if(domParentEntry.level > topEntry.level) {
                    if(rngParent == top) {
                        newEdges.add(domParent, rng);
                        parentEdge = IDManager.getID(edgeType, domParent, rng);
                        edgeContain.add(parentEdge, childEdge);
                    }
                } else if(rngParentEntry.level > topEntry.level) {
                    if(domParent == top) {
                        newEdges.add(dom, rngParent);
                        parentEdge = IDManager.getID(edgeType, dom, rngParent);
                        edgeContain.add(parentEdge, childEdge);
                    }
                } else {
                    if(domParent == top && rngParent == top) {
                        edgeContain.add(top, childEdge);
                    }
                }
            }
        }
        
        if(newEdges.size() > 0) {
            setupCompoundEdges(top, fake, newEdges, true);
        }
    }
    
    public Tree getEdgeTree(int top, EdgeSet primitiveEdges) {
        EdgeSet Do = AlgebraOperation.reflectiveClosure(contain);
        NodeSet useSet = AlgebraOperation.project(NodeSet.singleton(top), Do);
        NodeSet entSet = AlgebraOperation.entityOf(contain);
        NodeSet delSet = AlgebraOperation.difference(entSet, useSet);
        EdgeSet usefulEdges;
        
        usefulEdges = UtilityOperation.delset(primitiveEdges, delSet);
        usefulEdges.setName(primitiveEdges.getName());
        
        EdgeSet myContain = UtilityOperation.delset(contain, delSet);
        this.init(myContain);
        
        // Calculate compound edges. The top is not a fake.
        setupCompoundEdges(top, false, usefulEdges, false);
        edgeContain.removeDuplicates();
        
        Tree tree;
        tree = new Tree(edgeContain);
        tree.setRoot(top);
        return tree;
    }
    
    public Tree getEdgeTree(int src, int trg, EdgeSet primitiveEdges) {
        NodeSet roots;
        EdgeSet subtree;
        EdgeSet usefulEdges;
        
        roots = new NodeSet();
        roots.add(src); roots.add(trg);
        subtree = UtilityOperation.subtree(roots, contain);
        
        NodeSet entSet = AlgebraOperation.entityOf(contain);
        NodeSet useSet = AlgebraOperation.entityOf(subtree);
        NodeSet delSet = AlgebraOperation.difference(entSet, useSet);
        
        usefulEdges = UtilityOperation.delset(primitiveEdges, delSet);
        usefulEdges.setName(primitiveEdges.getName());
        
        // Use (null null null) as the top.
        EdgeSet myContain = subtree;
        int top = IDManager.getID(0, 0, 0);
        myContain.add(top, src);
        myContain.add(top, trg);
        
        // Initiate node tree.
        this.init(myContain);
        
        // Calculate compound edges. The top is a fake.
        setupCompoundEdges(top, true, usefulEdges, false);
        edgeContain.removeDuplicates();
        
        Tree tree;
        tree = new Tree(edgeContain);
        tree.setRoot(top);
        return tree;
    }
    
    public Tree getEdgeTree(EdgeSet primitiveEdges) {
        NodeSet dom = AlgebraOperation.domainOf(contain);
        NodeSet rng = AlgebraOperation.rangeOf (contain);
        NodeSet ent = AlgebraOperation.entityOf(contain);
        NodeSet source = AlgebraOperation.difference(dom, rng);
        
        if(source.size() == 0) {
            return new Tree(new EdgeSet());
        }
        
        EdgeSet usefulEdges;
        usefulEdges = AlgebraOperation.composition(ent, primitiveEdges);
        usefulEdges = AlgebraOperation.composition(usefulEdges, ent);
        usefulEdges.setName(primitiveEdges.getName());
        
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
        
        // Initiate node tree.
        this.init(myContain);
        
        // Calculate compound edges.
        setupCompoundEdges(top, fake, usefulEdges, false);
        edgeContain.removeDuplicates();
        
        Tree tree;
        tree = new Tree(edgeContain);
        tree.setRoot(top);
        return tree;
    }
}
