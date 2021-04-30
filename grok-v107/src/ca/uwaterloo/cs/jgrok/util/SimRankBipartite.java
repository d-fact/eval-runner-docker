package ca.uwaterloo.cs.jgrok.util;

import ca.uwaterloo.cs.jgrok.fb.*;

public class SimRankBipartite extends SimRank {
    TupleList shadow;
    
    /**
     * Construct a SimRank for a bipartite graph.
     * @param aGraph the bipartite graph.
     */
    public SimRankBipartite(EdgeSet aGraph) {
        super(aGraph);
    }
    
    public boolean inBipartiteDomain(Tuple t) {
        return inBipartiteDomain(t.getDom(), t.getRng());
    }
    
    public boolean inBipartiteRange(Tuple t) {
        return inBipartiteRange(t.getDom(), t.getRng());
    }
    
    public boolean inBipartiteDomain(int ID1, int ID2) {
        int domCol = 0;
        int index1 = BinarySearch.search(graph, ID1, domCol);
        int index2 = BinarySearch.search(graph, ID2, domCol);
        if(index1 < 0 || index2 < 0) return false;
        return true;
    }
    
    public boolean inBipartiteRange(int ID1, int ID2) {
        int rngCol = 1;
        int index1 = BinarySearch.search(shadow, ID1, rngCol);
        int index2 = BinarySearch.search(shadow, ID2, rngCol);
        if(index1 < 0 || index2 < 0) return false;
        return true;
    }
    
    protected void initEntryPool(EdgeSet aGraph) {
        Entry e;
        Tuple t;
        NodeSet nodes;
        EdgeSet edges;
        TupleList tList;
        
        shadow = new TupleList(aGraph.size());
        RadixSorter.sort(aGraph.getTupleList(), 1, shadow);
        
        nodes = AlgebraOperation.domainOf(aGraph);
        edges = AlgebraOperation.crossProduct(nodes, nodes);
        tList = edges.getTupleList();
        for(int i = 0; i < tList.size(); i++) {
            t = tList.get(i);
            e = ePool.getEntry(t);
            if(e == null) {
                e = ePool.putEntry(t);
                e.outNeighbour = true;
                processEntry(e);
            }
        }
        
        nodes = AlgebraOperation.rangeOf(aGraph);
        edges = AlgebraOperation.crossProduct(nodes, nodes);
        tList = edges.getTupleList();
        for(int i = 0; i < tList.size(); i++) {
            t = tList.get(i);
            e = ePool.getEntry(t);
            if(e == null) {
                e = ePool.putEntry(t);
                e.outNeighbour = false;
                processEntry(e);
            }
        }
    }
    
    protected void processEntry(Entry e) {
        int valueCol;
        int searchCol;
        TupleList tList;
        
        if(e.outNeighbour) {
            tList = graph;
            searchCol = 0;
            valueCol = 1;
        } else {
            tList = shadow;
            searchCol = 1;
            valueCol = 0;
        }
        
        int length = tList.size();
        int index1 = BinarySearch.search(tList, e.elem1, searchCol);
        int index2 = BinarySearch.search(tList, e.elem2, searchCol);
        if(index1 < 0 || index2 < 0) return;
        
        int i, j;
        Tuple t1, t2;
        int count1 = 0;
        int count2 = 0;
        
        for(i = index1; i < length; i++) {
            t1 = tList.get(i);
            if(t1.get(searchCol) == e.elem1) count1++;
            else break;
        }
        
        for(j = index2; j < length; j++) {
            t2 = tList.get(j);
            if(t2.get(searchCol) == e.elem2) count2++;
            else break;
        }
        
        int length1 = index1 + count1;
        int length2 = index2 + count2;
        Entry tmp;
        
        for(i = index1; i < length1; i++) {
            t1 = tList.get(i);
            for(j = index2; j < length2; j++) {
                t2 = tList.get(j);
                tmp = ePool.getEntry(t1.get(valueCol), t2.get(valueCol));
                if(tmp == null) {
                    tmp = ePool.putEntry(t1.get(valueCol), t2.get(valueCol));
                    if(e.outNeighbour) tmp.outNeighbour = false;
                    else tmp.outNeighbour = true;
                    processEntry(tmp);
                }
            }
        }
    }
    
    protected void computeEntry(Entry e, int readCol, int writeCol) {
        int valueCol;
        int searchCol;
        TupleList tList;
        
        if(e.outNeighbour) {
            tList = graph;
            searchCol = 0;
            valueCol = 1;
        } else {
            tList = shadow;
            searchCol = 1;
            valueCol = 0;
        }
        
        int length = tList.size();
        int index1 = BinarySearch.search(tList, e.elem1, searchCol);
        int index2 = BinarySearch.search(tList, e.elem2, searchCol);
        if(index1 < 0 || index2 < 0) return;
        
        int i, j;
        Tuple t1, t2;
        int count1 = 0;
        int count2 = 0;
        
        for(i = index1; i < length; i++) {
            t1 = tList.get(i);
            if(t1.get(searchCol) == e.elem1) count1++;
            else break;
        }
        
        for(j = index2; j < length; j++) {
            t2 = tList.get(j);
            if(t2.get(searchCol) == e.elem2) count2++;
            else break;
        }
        
        Entry tmp;
        double sum = 0;
        int length1 = index1 + count1;
        int length2 = index2 + count2;
        
        for(i = index1; i < length1; i++) {
            t1 = tList.get(i);
            for(j = index2; j < length2; j++) {
                t2 = tList.get(j);
                tmp = ePool.getEntry(t1.get(valueCol), t2.get(valueCol));
                sum += tmp.sims[readCol];
            }
        }
        
        double sim = sum * decayFactor / (count1 * count2);
        if(sim > e.sims[readCol]) e.sims[writeCol] = sim;
        else e.sims[writeCol] = e.sims[readCol];
    }
}
