package ca.uwaterloo.cs.jgrok.fb;

import java.util.ArrayList;

public class PathClosure {
    TupleList tertiary;
    TupleComparator tcmp;
    
    public PathClosure(EdgeSet eSet) {
        int[] cols = new int[2];
        cols[0] = 0;
        cols[1] = 2;
        tcmp = new TupleCmpSimple(cols);
        
        if(eSet == null) {
            tertiary = new TupleList(5);
        } else {
            tertiary = Operation.pathClosure(eSet.data);
        }
        
        // Sort in columns 0 and 2.
        TupleList tmp;
        tmp = new TupleList(tertiary.size());
        RadixSorter.sort(tertiary, 2, tmp);
        tertiary = new TupleList(tertiary.size());
        RadixSorter.sort(tmp, 0, tertiary);
    }
    
    public Path[] getPaths() {
        ArrayList<Path> all = new ArrayList<Path>(tertiary.size());
        
        for(int i = 0; i < tertiary.size(); i++) {
            Tuple t = tertiary.get(i);
            all.addAll(getPaths(t.get(0), t.get(2)));
        }
        
        Path[] paths = new Path[all.size()];
        all.toArray(paths);
        return paths;
    }
    
    public Path[] getPaths(String head, String tail) {
        Path[] paths;
        ArrayList<Path> found;
        
        found = getPaths(IDManager.getID(head), IDManager.getID(tail));
        paths = new Path[found.size()];
        found.toArray(paths);
        return paths;
    }
    
    private ArrayList<Path> getPaths(int head, int tail) {
        int[] dat = new int[3];
        dat[0] = head;
        dat[1] = 0;
        dat[2] = tail;
        
        ArrayList<Path> list = new ArrayList<Path>();
        ArrayList<Path> done = new ArrayList<Path>();
        
        Tuple compound = new TupleImpl(dat);
        int ind = search(compound);
        
        if(ind >= 0) {
            Tuple edge;
            
            for(int i = ind; i < tertiary.size(); i++) {
                edge = tertiary.get(i);
                if(tcmp.compare(compound, edge) == 0) {
                    if(isPrimitive(edge)) {
                        done.add(Path.link(edge.getDom(), edge.getRng()));
                    } else {
                        list.add(Path.link(edge.getDom(), edge.get(1)));
                    }
                } else break;
            }
            
            search(list, done, dat[2]);
            return done;
        } else {
            return new ArrayList<Path>(0);
        }
    }
    
    private void search(ArrayList<Path> undone, ArrayList<Path> done, int tail) {
        int[] dat = new int[3];
        dat[1] = 0;
        dat[2] = tail;
        
        Path p;
        Tuple edge;
        Tuple compound;
        ArrayList<Path> list = new ArrayList<Path>();
        
        for(int i = 0; i < undone.size(); i++) {
            p = undone.get(i);
            dat[0] = p.tail();
            compound = new TupleImpl(dat);
            
            int ind = search(compound);
            if(ind >= 0) {
                for(; ind < tertiary.size(); ind++) {
                    edge = tertiary.get(ind);
                    if(tcmp.compare(compound, edge) == 0) {
                        if(isPrimitive(edge)) {
                            done.add(Path.link(p, tail));
                        } else {
                            if(!p.contains(edge.get(1))) {
                                list.add(Path.link(p, edge.get(1)));                                
                            }
                        }
                    } else break;
                }
            }
        }
        
        if(list.size() > 0) {
            search(list, done, tail);
        }
    }
    
    private boolean isPrimitive(Tuple t) {
        if(t.get(1) == 0) return true;
        else return false; 
    }
    
    private int search(Tuple t) {
        return BinarySearch.search(tertiary, t, tcmp);
    }
}
