package ca.uwaterloo.cs.jgrok.fb;

import java.util.ArrayList;

/**
 * The ConceptEngine computes all possible concepts given
 * a contex of binary relations (object -> feature).
 *
 * <p>
 * This class, for example, can be used as follows:
 * <pre>
 *      EdgeSet result;
 *      EdgeSet hasFeature;
 *      ConceptEngine engine;
 *      
 *      engine = new ConceptEngine();
 *      engine.compute(hasFeature);
 *      result = engine.getConcepts();
 *      result.printTo(System.out);
 *      ...
 * </pre>
 *
 * Given a context as follows:
 * <pre>
 *      hasFeature cat fourlegged
 *      hasFeature cat haircovered
 *      hasFeature chimp haircovered
 *      hasFeature chimp intelligent
 *      hasFeature chimp thumbed
 *      hasFeature dog fourlegged
 *      hasFeature dog haircovered
 *      hasFeature dolphin intelligent
 *      hasFeature dolphin marine
 *      hasFeature human intelligent
 *      hasFeature human thumbed
 *      hasFeature whale intelligent
 *      hasFeature whale marine
 * </pre>
 *
 * The {@link #getConcepts() getConcepts()} returns the following:
 * <pre>
 *      (cat dog) (fourlegged haircovered)
 *      (chimp) (haircovered intelligent thumbed)
 *      (dolphin whale) (intelligent marine)
 *      (chimp human) (intelligent thumbed)
 *      (cat chimp dog) (haircovered)
 *      (chimp dolphin human whale) (intelligent)
 * </pre>
 */
public class ConceptEngine {
    TupleList data;
    TupleList shadow;
    
    EdgeSet hasFeature;
    EdgeSet curFeature;
    EdgeSet allConcepts;
    
    public ConceptEngine() {
        data = new TupleList();
        shadow = new TupleList();
        hasFeature = new EdgeSet();
    }
    
    public void compute(EdgeSet hasFeature) {
        if(this.hasFeature != hasFeature) {
            this.hasFeature = hasFeature;
            
            EdgeSet c1 = new EdgeSet(100);
            EdgeSet c2 = new EdgeSet(100);
            
            initialize1();
            computeConcepts(c1);
            
            initialize2();
            computeConcepts(c2);
            c2 = AlgebraOperation.inverse(c2);
            
            allConcepts = AlgebraOperation.union(c1, c2);
        }
    }
    
    public EdgeSet getConcepts() {
        return allConcepts;
    }
    
    private void initialize1() {
        if(hasFeature == null || hasFeature.size() == 0) {
            data = new TupleList();
            shadow = new TupleList();
            curFeature = new EdgeSet();
            allConcepts = new EdgeSet();
            return;
        }
        
        curFeature = hasFeature;
        allConcepts = new EdgeSet();
        
        int count = curFeature.size();
        TupleList tList = curFeature.getTupleList();
        data = new TupleList(count);
        
        int[] elems = new int[3];
        for(int i = 0; i < count; i++) {
            Tuple t = tList.get(i);
            elems[0] = t.getDom();
            elems[1] = t.getRng();
            elems[2] = 0;
            
            data.add(TupleFactory.create(elems));
        }
        
        // Shadow is sorted in Column 1.
        shadow = new TupleList(count);
        RadixSorter.sort(data, 1, shadow);
        
        // Data is sorted in Column 0.
        data = new TupleList(count);
        RadixSorter.sort(shadow, 0, data);
    }
    
    private void initialize2() {
        int tmpValue;
        int count = data.size();
        
        Tuple t;
        for(int i = 0; i < count; i++) {
            t = data.get(i);
            tmpValue =t.get(0);
            t.set(0, t.get(1));
            t.set(1, tmpValue);
        }
        
        // Shadow is sorted in Column 1.
        shadow = data;
        
        // Data is sorted in Column 0.
        data = new TupleList(count);
        RadixSorter.sort(shadow, 0, data);
        
        curFeature = AlgebraOperation.inverse(curFeature);
    }
    
    private void computeConcepts(EdgeSet concepts) {
        EdgeSet tmpConcepts;
        
        /****************************************/
        /** inv basket(inv basket(curFeature)) **/
        /****************************************/
        tmpConcepts = SpecialOperation.basket (curFeature);
        tmpConcepts = AlgebraOperation.inverse(tmpConcepts);
        tmpConcepts = SpecialOperation.basket (tmpConcepts);
        tmpConcepts = AlgebraOperation.inverse(tmpConcepts);
        
        TupleList tList;
        int count = tmpConcepts.size();
        tList = tmpConcepts.getTupleList();
        for(int i = 0; i < count; i++) {
            Tuple t = tList.get(i);
            int extent = t.getDom();
            int intent = t.getRng();
            
            int pos;
            int[] objects = IDManager.parse(extent);
            int[] features = IDManager.parse(intent);
            
            for(int j = 0; j < features.length; j++) {
                pos = BinarySearch.search(shadow, features[j], 1);
                while(pos < shadow.size()) {
                    if(shadow.get(pos).get(1) == features[j]) {
                        shadow.get(pos).set(2, 1);
                        pos++;
                    } else break;
                }
            }
            
            int[] cObjs = collectObjects(features.length);
            if(objects.length == cObjs.length) {
                // t is a conept
                concepts.add(t);
            } else {
                // t is not a concept
                
                // Note cObjs is in ascending order
                // since data is in ascending order.
                extent = IDManager.getID(cObjs);
                concepts.add(extent, intent);
            }
        }
    }
    
    private int[] collectObjects(int outDeg) {
        Tuple t;
        int deg, obj, next, count;
        ArrayList<Integer> aList = new ArrayList<Integer>(5);
        
        count = data.size();
        if(count > 0) {
            t = data.get(0);
            deg = 0;
            obj = t.getDom();
            if(t.get(2) == 1) {
                deg++;
                t.set(2, 0);
            }
            
            for(int i = 1; i < count; i++) {
                t = data.get(i);
                next = t.getDom();
                if(obj == next) {
                    if(t.get(2) == 1) {
                        deg++;
                        t.set(2, 0);
                    }
                    continue;
                }
                
                if(deg == outDeg)
                    aList.add(new Integer(obj));
                
                deg = 0;
                obj = next;
                if(t.get(2) == 1) {
                    deg++;
                    t.set(2, 0);
                }
            }
            
            if(deg == outDeg)
                aList.add(new Integer(obj));
        }
        
        int[] objs = new int[aList.size()];
        for(int i = 0; i < aList.size(); i++) {
            objs[i] = ((Integer)aList.get(i)).intValue();
        }
        
        return objs;
    }
}
