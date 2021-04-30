package ca.uwaterloo.cs.jgrok.util;

import java.util.HashMap;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.fb.*;

public class SimInitialization {
    protected Object userData;
    protected TupleList feature;
    
    public SimInitialization(EdgeSet hasFeature, Object userData) {
        feature = new TupleList(hasFeature.size());
        RadixSorter.sort(hasFeature.getTupleList(), 0, feature);
    }
    
    public void initialize(SimRank simRank) {
        ArrayList<SimRank.Entry> list = simRank.ePool.entryList;
        int simCol = simRank.simCol;
        int count = list.size();
        SimRank.Entry e;
        
        for(int i = 0; i < count; i++) {
            e = list.get(i);
            if(e.sims[simCol] < 1) {
                e.sims[0] = getInitialSim(e.elem1, e.elem2);
                e.sims[1] = e.sims[0];
            }
        }
    }
    
    protected double getInitialSim(int ID1, int ID2) {
        if(ID1 == ID2) return 1;
        
        int length = feature.size();
        int index1 = BinarySearch.search(feature, ID1, 0);
        int index2 = BinarySearch.search(feature, ID2, 0);
        if(index1 < 0 || index2 < 0) return 0;
        
        int i;
        Tuple t;
        Integer v;
        HashMap<Integer,Integer> map = new HashMap<Integer,Integer>(17);
        
        for(i = index1; i < length; i++) {
            t = feature.get(i);
            if(ID1 == t.getDom()) {
                v = new Integer(t.getRng());
                if(!map.containsKey(v)) map.put(v, v);
            } else break;
        }
        
        int common = 0;
        int sum = i - index1;
        
        for(i = index2; i < length; i++) {
            t = feature.get(i);
            if(ID2 == t.getDom()) {
                v = new Integer(t.getRng());
                if(map.containsKey(v)) common++;
                else sum++;
            }
        }
        
        return ((double)common)/sum;
    }
}
