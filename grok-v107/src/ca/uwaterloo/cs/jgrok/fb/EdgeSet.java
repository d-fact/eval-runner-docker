package ca.uwaterloo.cs.jgrok.fb;

import java.io.*;

public class EdgeSet extends TupleSet {
    /**
     * <pre>
     *    Level -1: data not sorted
     *    Level  0: data sorted in DOM
     *    Level  1: data sorted in RNG
     *    Level  2: data sorted in DOM.RNG
     *
     *    +-----------+-------------------+
     *    | sortLevel |       data        |
     *    +-----------+-------------------+
     *    |    -1     |        X          |
     *    |     0     | sorted in DOM     |
     *    |     1     | sorted in RNG     |
     *    |     2     | sorted in DOM.RNG |
     *    +-----------+-------------------+
     * </pre>
     */
    
    /**
     * EdgeSet.
     */
    public EdgeSet() {
        data = TupleListFactory.newEdgeList();
    }
    
    /**
     * EdgeSet.
     */
    public EdgeSet(String name) {
        super(name);
        data = TupleListFactory.newEdgeList();
    }
    
    /**
     * EdgeSet.
     */
    public EdgeSet(int capacity) {
        data = TupleListFactory.newEdgeList(capacity);
    }
    
    /**
     * Adds an edge to this set.
     * @param fr the bgn of the edge.
     * @param to the end of the edge.
     */
    public void add(String fr, String to) {
        int dom, rng;
        
        dom = IDManager.getID(fr);
        rng = IDManager.getID(to);
        data.add(new Tuple4Edge(dom, rng));
        sortLevel = -1;
        flags = FLAG_NONE;
    }
    
    /**
     * Adds an edge to this set efficiently.
     * @param fr the source of the edge.
     * @param to the destination of the edge.
     */
    public void add(int fr, int to) {
        data.add(new Tuple4Edge(fr, to));
        sortLevel = -1;
        flags = FLAG_NONE;
    }
    
    /**
     * Gets a node's attribute's ID.
     * @return -1 if the node has no attribute.
     */
    public int getAttributeID(int nodeID) {
        int ind;
        
        trySort(0);
        ind = BinarySearch.search(data, nodeID, 0);
        if(ind < 0) return -1;
        else return data.get(ind).getRng();
    }
    
    public String getAttribute(int nodeID) {
        int ind;
        
        trySort(0);
        ind = BinarySearch.search(data, nodeID, 0);
        if(ind < 0) return null;
        else return IDManager.get(data.get(ind).getRng());
    }
    
    public Edge[] getAllEdges() {
        int count = data.size();
        Edge[] edges = new Edge[count];
        
        Tuple tup;
        for(int i = 0; i < count; i++) {
            tup = data.get(i);
            edges[i] = new Edge(tup.getDom(), tup.getRng());
        }
        return edges;
    }
    
    public TupleSet newSet() {
        return new EdgeSet();
    }
    
    public void printRSF(PrintWriter writer) 
    {
        super.printRSF(getName(), writer);
    }
    
    /**
     * Sorts this EdgeSet into ascending order.
     */
    protected void sort() {
        this.trySort(2);
    }
    
    /**
     * Sort tuples into ascending order in the first column (Dom).
     */
    protected void sortDom() {
        trySort(0);
    }
    
    /**
     * Sort tuples into ascending order in the last column (Rng).
     */
    protected void sortRng() {
        trySort(1);
    }
    
    /*  +-----------------+-------------+-----------------+
     *  | sortLevel (old) | level (arg) | sortLevel (new) |
     *  +-----------------+-------------+-----------------+
     *  |     -1, 0       |      0      |        0        |
     *  |      1, 2       |      0      |        2        |
     *  +-----------------+-------------+-----------------+
     *  |   -1, 0, 1, 2   |      1      |        1        |
     *  +-----------------+-------------+-----------------+
     *  |   -1, 0, 1, 2   |      2      |        2        |
     *  +-----------------+-------------+-----------------+
     */
    
    /**
     * Sorts this EdgeSet onto a sorting level.
     * @param level the sort level, and its value must be one
     * of the following values: 0, 1, 2. Note: the level less 
     * than 0 is processed as 0, and the level greater than 2
     * is processed as 2.
     */
    protected void trySort(int level) {
        int domCol = 0;
        int rngCol = 1;
        TupleList t_l;
        
        switch(level) {
            
        case 0:
            if(sortLevel < 0) {
                t_l = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(data, domCol, t_l);
                data.setList(t_l.getList());
                sortLevel = 0;
            } else if(sortLevel == 1) {
                t_l = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(data, domCol, t_l);
                data.setList(t_l.getList());
                sortLevel = 2;
            }
            break;
            
        case 1:
            if(sortLevel != 1) {
                t_l = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(data, rngCol, t_l);
                data.setList(t_l.getList());
                sortLevel = 1;
            }
            break;
            
        case 2:
            if(sortLevel == 1) {
                t_l = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(data, domCol, t_l);
                data.setList(t_l.getList());
                sortLevel = 2;
            } else if(sortLevel < 1) {
                TupleList shadow;
                shadow = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(data, rngCol, shadow);
                
                t_l = TupleListFactory.newEdgeList(data.size());
                RadixSorter.sort(shadow, domCol, t_l);
                data.setList(t_l.getList());
                sortLevel = 2;
            }
            break;
            
        default :
            if(level < 0) trySort(0);
            if(level > 2) trySort(2);
            break;
        }
    }
    
    /**
     * Gets a tuple list which is sorted in RNG.
     */
    protected TupleList shadow() {
        TupleList shadow;
        
        if(sortLevel < 0) {
            trySort(1);
            shadow = new TupleList(size());
            shadow.getList().addAll(data.getList());
        } else if(sortLevel == 1) {
            shadow = new TupleList(size());
            shadow.getList().addAll(data.getList());
        } else {
            shadow = new TupleList(size());
            RadixSorter.sort(data, 1, shadow);
        }
        
        return shadow;
    }
}
