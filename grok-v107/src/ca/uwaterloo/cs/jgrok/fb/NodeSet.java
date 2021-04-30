package ca.uwaterloo.cs.jgrok.fb;

import java.io.*;

public class NodeSet extends TupleSet {
    /**
     * <pre>
     *    sortLevel (defined in TupleSet)
     *
     *    Level -1 : not sorted
     *    Level  0 : data is sorted
     *
     *    +-----------+-----------+
     *    | sortLevel |    data   |
     *    +-----------+-----------+
     *    |    -1     |     X     |
     *    |     0     |   sorted  |
     *    +-----------+-----------+
     * </pre>
     */
    
    /**
     * NodeSet.
     */
    public NodeSet() {
        data = TupleListFactory.newNodeList();
    }
    
    /**
     * NodeSet.
     * @param name the name.
     */
    public NodeSet(String name) {
        super(name);
        data = TupleListFactory.newNodeList();
    }
    
    /**
     * NodeSet.
     * @param capacity the capacity.
     */
    public NodeSet(int capacity) {
        data = TupleListFactory.newNodeList(capacity);
    }
    
    /**
     * Create a singleton set.
     */
    public static NodeSet singleton(int nodeID) {
        NodeSet set = new NodeSet(1);
        
        set.data.add(new Tuple4Node(nodeID));
        set.unsetFlag(FLAG_DUPLICATE);
        set.sortLevel = 0;
        
        return set;
    }
    
    /**
     * Create a singleton set.
     */
    public static NodeSet singleton(String node) {
        NodeSet set = new NodeSet(1);
        
        set.add(node);
        set.sortLevel = 0;
        set.unsetFlag(FLAG_DUPLICATE);
        
        return set;
    }
    
    /**
     * Adds a node efficiently, whose ID is known.
     * @param nodeID the node's ID
     */
    public void add(int nodeID) {
        data.add(new Tuple4Node(nodeID));
        sortLevel = -1;
        flags = FLAG_NONE;
    }
    
    /**
     * Adds a node to this set.
     * @param node the node to add.
     */
    public void add(String node) {
        data.add(new Tuple4Node(IDManager.getID(node)));
        sortLevel = -1;
        flags = FLAG_NONE;
    }
    
    /**
     * Creates an empty NodeSet.
     * @return a NodeSet.
     */
    public TupleSet newSet() {
        return new NodeSet();
    }
    
    /**
     * Tests if this NodeSet contains <code>node</code>.
     * @param node a node to test.
     * @return <code>true</code> is <code>node</code> belongs to this set.
     * Otherwise, <code>false</code>.
     */
    public boolean contain(String node) {
        trySort(0);
        if(BinarySearch.search(data, IDManager.getID(node), 0) > -1) return true;
        return false;
    }
    
    /**
     * Picks a node randomly from this NodeSet.
     * @return a node.
     */
    public String pick() {
        int random = (int)(Math.random() * data.size());
        int nodeID = data.get(random).getDom();
        return IDManager.get(nodeID);
    }
    
    /**
     * Picks a set of nodes randomly from this NodeSet.
     * @param num the number of nodes to pick.
     * @return a set of nodes.
     */
    public NodeSet pick(int num) {
        int size = data.size();
        if(num < 1) return new NodeSet();
        if(num >= size) return (NodeSet)this.clone();
        
        int step;
        int index;
        int random;
        NodeSet result;
        result = new NodeSet();
        boolean b = false;
        
        step = size / num;
        if(step == 1) {
            b = true;
            num = size - num;
            step = size / num;
        }
        
        for(int i = 0; i < num; i++) {
            random = (int)(Math.random() * step);
            index = i * step + random;
            result.add(data.get(index).getDom());
        }
        
        if(b) result = AlgebraOperation.difference(this, result);
        return result;
    }
    
    public Node[] getAllNodes() {
        int size;
        Node[] all;
        
        size = data.size();
        all = new Node[size];
        for(int i = 0; i < size; i++) {
            all[i] = new Node(((Tuple)data.get(i)).getDom()); 
        }
        return all;
    }
    
    public void print(OutputStream out) {
        Tuple t;
        PrintWriter writer;
        writer = new PrintWriter(out, true);
        
        int size = data.size();
        for(int i = 0; i < size; i++) {
            t = (Tuple)data.get(i);
            writer.println(IDManager.get(t.getDom()));
        }
        writer.flush();
    }
 
    public void prefixCat(String s) {
        int count;
        int nodeID;
        Tuple t;
        
        count = data.size();
        for(int i = 0; i < count; i++) {
            t = (Tuple)data.get(i);
            nodeID = t.getDom();
            nodeID = IDManager.getID(s + IDManager.get(nodeID));
            t.setDom(nodeID);
        }
        
        // Reset sortLevel.
        sortLevel = -1;
    }
    
    public void suffixCat(String s) {
        int count;
        int nodeID;
        Tuple t;
        
        count = data.size();
        for(int i = 0; i < count; i++) {
            t = (Tuple)data.get(i);
            nodeID = t.getDom();
            nodeID = IDManager.getID(IDManager.get(nodeID) + s);
            t.setDom(nodeID);
        }
        
        // Reset sortLevel.
        sortLevel = -1;
    }
    
    /**
     * Sorts this NodeSet into ascending order.
     */
    protected void sort() {
        trySort(0);
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
        trySort(0);
    }
    
    /**
     * Sorts this NodeSet onto a sorting level.
     * @param level the sort level, and its value must be 0.
     * Note: other levels not equal to 0 are processed as 0.
     */
    protected void trySort(int level) {
        int col = 0;
        TupleList t_l;
        
        switch(level) {
            
        case 0:
            if(sortLevel < 0) {
                t_l = TupleListFactory.newNodeList(data.size());
                RadixSorter.sort(data, col, t_l);
                data.setList(t_l.getList());
                sortLevel = 0;
            }
            break;
            
        default :
            trySort(0);
        }
    }
}
