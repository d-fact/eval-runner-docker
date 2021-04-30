package ca.uwaterloo.cs.jgrok.fb;

import java.util.HashSet;

/**
 * Patition a binary relation into a collection
 * of subgraphs.
 *
 */
public class Partition {
    HashSet<Integer> hash;
    TupleList tuplist;
    
    public Partition() {
        hash = new HashSet<Integer>(1997, 0.75f);
    }
    
    public int countPartitions(EdgeSet rel) {
        int numGraphs;
        
        if(rel == null) return 0;
        
        hash.clear();
        tuplist = null;
        
        // Set up the data base
        setup(rel);
        
        // Compute all subgraphs
        numGraphs = compute();
        
        return numGraphs;
    }
    
    public EdgeSet[] getPartitions(EdgeSet rel) {
        int numGraphs;
        EdgeSet[] result;
        TupleList[] tlists;
        
        if(rel == null)
            return new EdgeSet[0];
        
        hash.clear();
        tuplist = null;
        
        // Set up the data base
        setup(rel);
        
        // Compute all subgraphs
        numGraphs = compute();
        
        // Collect all subgraphs
        result = new EdgeSet[numGraphs];
        tlists = new TupleList[numGraphs];
        for(int i = 0; i < numGraphs; i++) {
            result[i] = new EdgeSet();
            tlists[i] = result[i].data;
        }
        collect(tlists);
        
        return result;
    }
    
    private void setup(EdgeSet rel) {
        int size;
        int[] data;
        Tuple t;
        TupleList t_l;
        
        ////////////////////////////////
        // src  trg  graph  direction //
        ////////////////////////////////
        
        t_l = rel.data;
        size = t_l.size();
        data = new int[4];
        data[2] = -1;
        
        tuplist = new TupleList(size * 2);
        
        data[3] = 1;  // Forward direction        
        for(int i = 0; i < size; i++) {
            t = t_l.get(i);
            data[0] = t.getDom();
            data[1] = t.getRng();
            tuplist.add(new TupleImpl(data));
        }
        
        data[3] = 0;  // Inverse direction        
        for(int i = 0; i < size; i++) {
            t = t_l.get(i);
            data[0] = t.getRng();
            data[1] = t.getDom();
            tuplist.add(new TupleImpl(data));
        }
        
        TupleList tmp = new TupleList(size * 2);
        RadixSorter.sort(tuplist, 0, tmp);
        tuplist = tmp;
    }
    
    private int compute() {
        Tuple t;
        int numGraphs = 0;
        int size = tuplist.size();
        
        for(int i = 0; i < size; i++) {
            t = tuplist.get(i);
            if(t.get(2) < 0) {
                    reachGraph(t.get(0), numGraphs);
                numGraphs++;
            }
        }
        
        return numGraphs;
    }
    
    private void collect(TupleList[] graphs) {
        Tuple t;
        int size = tuplist.size();
        for(int i = 0; i < size; i++) {
            t = tuplist.get(i);
            if(t.get(3) == 1)
                graphs[t.get(2)].add(new Tuple4Edge(t.get(0), t.get(1)));
        }
    }
    
    /**
     * Find a graph in the tuplist.
     */
    private void reachGraph(int dom, int graph) {
        Tuple t;
        Integer I;
        int ind, size;
        
        I = new Integer(dom);
        if(hash.contains(I)) return;
        else hash.add(I);
        
        ind = BinarySearch.search(tuplist, dom, 0);
        if(ind < 0) return;
        
        size = tuplist.size();        
        for(int i = ind; i < size; i++) {
            t = tuplist.get(i);
            if(t.get(0) == dom) {
                t.set(2, graph);
                reachGraph(t.get(1), graph);
            } else break;
        }
    }
}
