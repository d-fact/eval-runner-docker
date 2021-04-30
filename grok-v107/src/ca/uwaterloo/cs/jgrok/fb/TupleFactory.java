package ca.uwaterloo.cs.jgrok.fb;

public class TupleFactory {
    
    public static Tuple create(int id) {
        return new Tuple4Node(id);
    }
    
    public static Tuple create(int dom, int rng) {
        return new Tuple4Edge(dom, rng);
    }

    public static Tuple create(int[] ids) {
        if(ids == null || ids.length == 0) return null;
        
        switch(ids.length) {
        case 1:
            return new Tuple4Node(ids[0]);
        case 2:
            return new Tuple4Edge(ids[0], ids[1]);
        default:
            return new TupleImpl(ids);
        }
    }
    
    public static Tuple create(int[] ids, boolean clone) {
        if(ids == null || ids.length == 0) return null;
        
        switch(ids.length) {
        case 1:
            return new Tuple4Node(ids[0]);
        case 2:
            return new Tuple4Edge(ids[0], ids[1]);
        default:
            return new TupleImpl(ids, clone);
        }
    }
    
    public static Tuple create(String val) {
        return new Tuple4Node(IDManager.getID(val));
    }
    
    public static Tuple create(String src, String trg) {
        return new Tuple4Edge(IDManager.getID(src), IDManager.getID(trg));
    }
    
    public static Tuple create(String[] vals) {
        if(vals == null || vals.length == 0) return null;
        
        switch(vals.length) {
        case 1:
            return new Tuple4Node(IDManager.getID(vals[0]));
        case 2:
            return new Tuple4Edge(IDManager.getID(vals[0]), IDManager.getID(vals[1]));
        default:
            int[] ids = new int[vals.length];
            for(int i = 0; i < vals.length; i++) {
                ids[i] = IDManager.getID(vals[i]);
            }
            return new TupleImpl(ids, false);
        }
    }
}
