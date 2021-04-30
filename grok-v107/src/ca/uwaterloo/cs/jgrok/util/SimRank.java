package ca.uwaterloo.cs.jgrok.util;

import java.util.HashMap;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.fb.*;

public class SimRank {
    int simCol = 0;
    EntryPool ePool;
    TupleList graph;
    
    protected double epsilon = 0.001;
    
    /**
     * Decay factor (default = 0.8).
     */
    protected double decayFactor = 0.8;
    
    /**
     * Construct a SimRank to compute similarity based on out-neighbors. 
     * @param aGraph the graph.
     */
    public SimRank(EdgeSet aGraph) {
        ePool = new EntryPool();
        graph = new TupleList(aGraph.size());
        RadixSorter.sort(aGraph.getTupleList(), 0, graph);
        initEntryPool(aGraph);
    }
    
    protected void initEntryPool(EdgeSet aGraph) {
        Entry e;
        NodeSet nodes = AlgebraOperation.entityOf(aGraph);
        EdgeSet edges = AlgebraOperation.crossProduct(nodes, nodes);
        TupleList pairs = edges.getTupleList();
        
        for(int i = 0; i < pairs.size(); i++) {
            e = ePool.getEntry(pairs.get(i));
            if(e == null) {
                e = ePool.putEntry(pairs.get(i));
                processEntry(e);
            }
        }
    }
    
    protected void processEntry(Entry e) {
        int length = graph.size();
        int index1 = BinarySearch.search(graph, e.elem1, 0);
        int index2 = BinarySearch.search(graph, e.elem2, 0);
        if(index1 < 0 || index2 < 0) return;

        int i, j;
        Tuple t1, t2;
        int count1 = 0;
        int count2 = 0;
        
        for(i = index1; i < length; i++) {
            t1 = graph.get(i);
            if(t1.getDom() == e.elem1) count1++;
            else break;
        }
        
        for(j = index2; j < length; j++) {
            t2 = graph.get(j);
            if(t2.getDom() == e.elem2) count2++;
            else break;
        }
        
        int length1 = index1 + count1;
        int length2 = index2 + count2;
        Entry tmp;
        
        for(i = index1; i < length1; i++) {
            t1 = graph.get(i);
            for(j = index2; j < length2; j++) {
                t2 = graph.get(j);
                tmp = ePool.getEntry(t1.getRng(), t2.getRng());
                if(tmp == null) {
                    tmp = ePool.putEntry(t1.getRng(), t2.getRng());
                    processEntry(tmp);
                }
            }
        }
    }
    
    public double getDecayFactor() {
        return decayFactor;
    }
    
    public void setDecayFactor(double d) {
        decayFactor = d;
    }
    
    public double querySim(Tuple t) {
        Entry e = ePool.getEntry(t);
        if(e != null) return e.sims[simCol];
        return 0;
    }
    
    public TupleSet compute(int iterationCount) {
        int readCol;
        int writeCol=0;
        ArrayList<Entry> list;
        
        list = ePool.entryList;
        int count = list.size();

        Entry e;
        for(int i = 0; i < iterationCount; i++) {
            readCol = i % 2;
            writeCol = (readCol+1) % 2; 
            for(int j = 0; j < count; j++) {
                e = list.get(j);
                if(e.sims[readCol] < 1)
                    computeEntry(e, readCol, writeCol);
                else
                    e.sims[writeCol] = 1.0;
            }
        }
        simCol = writeCol;
        
        ///////////////////////////////////////////////////////////
        
        int[] ids = new int[3];
        TupleSet tSet = new TupleSet(count);
        
        for(int i = 0; i < count; i++) {
            e = (Entry)list.get(i);
            
            ids[0] = e.elem1;
            ids[1] = e.elem2;
            ids[2] = stringID(e.sims[simCol]);
            
            tSet.add(TupleFactory.create(ids, true));
        }
        
        return tSet;
    }
    
    private int stringID(double d) {
        if(d < epsilon) d = 0;
        String s = (Math.round(d/epsilon) * epsilon) + "";
        if(s.length() > 5) s = s.substring(0, 5);
        return IDManager.getID(s);
    }
    
    protected void computeEntry(Entry e, int readCol, int writeCol) {
        int length = graph.size();
        int index1 = BinarySearch.search(graph, e.elem1, 0);
        int index2 = BinarySearch.search(graph, e.elem2, 0);
        if(index1 < 0 || index2 < 0) return;
        
        int i, j;
        Tuple t1, t2;
        int count1 = 0;
        int count2 = 0;
        
        for(i = index1; i < length; i++) {
            t1 = graph.get(i);
            if(t1.getDom() == e.elem1) count1++;
            else break;
        }
        
        for(j = index2; j < length; j++) {
            t2 = graph.get(j);
            if(t2.getDom() == e.elem2) count2++;
            else break;
        }
        
        Entry tmp;
        double sum = 0;
        int length1 = index1 + count1;
        int length2 = index2 + count2;
        
        for(i = index1; i < length1; i++) {
            t1 = graph.get(i);
            for(j = index2; j < length2; j++) {
                t2 = graph.get(j);
                tmp = ePool.getEntry(t1.getRng(), t2.getRng());
                sum += tmp.sims[readCol];
            }
        }
        
        double sim = sum * decayFactor / (count1 * count2);
        if(sim > e.sims[readCol]) e.sims[writeCol] = sim;
        else e.sims[writeCol] = e.sims[readCol];
    }
    
    static class EntryPool {
        int s_index = 0;
        ArrayList<Entry> entryList;
        HashMap<String,Entry> allStrings;
        
        EntryPool() {
            entryList = new ArrayList<Entry>(1997);
            allStrings = new HashMap<String,Entry>(1997, 0.75f);
        }
        
        Entry putEntry(Tuple t) {
            return putEntry(t.getDom(), t.getRng());
        }
        
        Entry getEntry(Tuple t) {
            return getEntry(t.getDom(), t.getRng());
        }
        
        Entry putEntry(int elem1, int elem2) {
            Entry entry;
            String name;
            
            if(elem1 > elem2) {
                int tmp = elem1;
                elem1 = elem2;
                elem2 = tmp;
            }
            
            name = elem1 + ":" + elem2;
            entry = (Entry)allStrings.get(name);
            if(entry != null) return entry;
            
            entry = new Entry(elem1, elem2);
            entryList.add(s_index, entry);
            allStrings.put(name, entry);
            s_index++;
            
            return entry;
        }
        
        Entry getEntry(int elem1, int elem2) {
            String name;
            
            if(elem1 > elem2) {
                int tmp = elem1;
                elem1 = elem2;
                elem2 = tmp;
            } 
            
            name = elem1 + ":" + elem2;
            return (Entry)allStrings.get(name);
        }
    }
    
    static class Entry {
        int elem1;
        int elem2;
        boolean outNeighbour = true;  // [Default]
        double[] sims = new double[2];
        
        Entry(Tuple t) {
            this(t.getDom(), t.getRng());
        }
        
        Entry(int elem1, int elem2) {
            this.elem1 = elem1;
            this.elem2 = elem2;
            if(elem1 == elem2) {
                sims[0] = 1;
                sims[1] = 1;
            } else {
                sims[0] = 0;
                sims[1] = 0;
            }
        }
    }
}
