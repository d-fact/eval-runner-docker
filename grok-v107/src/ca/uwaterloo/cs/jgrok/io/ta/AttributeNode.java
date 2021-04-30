package ca.uwaterloo.cs.jgrok.io.ta;

import java.util.*;

import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.Factbase;
import ca.uwaterloo.cs.jgrok.fb.IDManager;

class AttributeNode {
	// The item this attribute is for
    ItemIdNode item;
    ArrayList<AttributeSettingNode> settings;
    
    public void dump(int indent)
    {
		int					i, size;
        AttributeSettingNode settingNd;

		for (i = 0; i < indent; ++i) {
			System.out.print(' ');
		}
		System.out.println("AttributeNode");
		if (item != null) {
			item.dump(indent+2);
		}
		if (settings == null) {
			size = 0;
		} else {
			size = settings.size();
		}
		for (i = 0; i < size; ++i) {
			settingNd = settings.get(i);
			settingNd.dump(indent+2);
	}	}
		 
    AttributeNode(ItemIdNode item) {
        this.item = item;
        settings = new ArrayList<AttributeSettingNode>();
    }
    
    void add(AttributeSettingNode setting) {
        settings.add(setting);
    }
    
    void putInto(Factbase factbase) {
        HashMap<String, String> allAttributes;
        AttributeSettingNode settingNd;
        
        int size;
        EdgeSet aSet;
        String value;
        String attrId;
        String attrRel;
        Iterator<String> iter;
        
        size = settings.size();
        for(int i = 0; i < size; i++) {
            settingNd = settings.get(i);
            allAttributes = settingNd.allAttributes();
            
            iter = allAttributes.keySet().iterator();
            while(iter.hasNext()) {
                attrId = iter.next();
                value = allAttributes.get(attrId);
                
                attrRel = "@" + attrId;
                aSet = factbase.getEdgeSet(attrRel);
                if(aSet == null) {
                    aSet = new EdgeSet(attrRel);
                    factbase.addSet(aSet);
                }
                
                aSet.add(item.itemID(), IDManager.getID(value));
            }
        }
    }
    
    void putIntoScheme(Factbase factbase) {
        HashMap<String, String> allAttributes;
        AttributeSettingNode settingNd;
        
        int size;
        EdgeSet aSet;
        String value;
        String attrId;
        String attrRel;
        Iterator<String> iter;
        
        size = settings.size();
        for(int i = 0; i < size; i++) {
            settingNd = settings.get(i);
            allAttributes = settingNd.allAttributes();
            
            iter = allAttributes.keySet().iterator();
            while(iter.hasNext()) {
                attrId = iter.next();
                value = allAttributes.get(attrId);
                
                attrRel = "$@" + attrId;
                aSet = factbase.getEdgeSet(attrRel);
                if(aSet == null) {
                    aSet = new EdgeSet(attrRel);
                    factbase.addSet(aSet);
                }
                
                aSet.add(item.itemID(), IDManager.getID(value));
            }
        }
    }
    
    public String toString()
    {
		return item.toString();
	}
}
