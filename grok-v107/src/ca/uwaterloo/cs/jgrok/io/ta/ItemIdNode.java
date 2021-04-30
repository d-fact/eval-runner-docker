package ca.uwaterloo.cs.jgrok.io.ta;

import ca.uwaterloo.cs.jgrok.fb.IDManager;

class ItemIdNode {
    static String item;
    static String source;
    static String target;
    
    public void dump(int indent)
    {
		int					i, size;
		for (i = 0; i < indent; ++i) {
			System.out.print(' ');
		}
		System.out.println("ItemIdNode " + toString());
	}
		
    ItemIdNode(String t) {
        item   = t;
        source = null;
        target = null;
    }
    
    ItemIdNode(String rel, String src, String trg) {
		item   = rel;
        source = src;
        target = trg;
    }
    
    int itemID() {
        if(source == null) {
            // Fact entity
            // Entity class in scheme
            // Relation class in scheme
            return IDManager.getID(item);
        } else {
            // edge relation
            int rel = IDManager.getID(item);
            int dom = IDManager.getID(source);
            int rng = IDManager.getID(target);
            int sid = IDManager.getID(rel, dom, rng);
            return sid;
        }
    }
    
    public String toString()
    {
		if (source != null) {
			return "(" + item + " " + source + " " + target + ")";
		} else {
			return item;
	}	}
}
