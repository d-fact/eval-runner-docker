package ca.uwaterloo.cs.jgrok.io.ta;

import java.util.*;

/**
 * The nested attribute is referred to as:
 * <pre>
 *     MainKey(_SubKey)*
 * </pre>
 */
class AttributeSettingNode {
	// The name of this attribute
    String attributeId;
    // The value of this attribute
    String value;
   
    public void dump(int indent)
    {
		int					i, size;
		for (i = 0; i < indent; ++i) {
			System.out.print(' ');
		}
		System.out.print("AttributeSettingNode " + attributeId);
		if (value != null) {
			System.out.print("=" + value);
		}
		System.out.println("");
	}
    
    AttributeSettingNode(String attrId) {
        attributeId = attrId;
        value       = null;
    }
    
    void setValue(String value1)
    {
		value = value1;
	}

    AttributeSettingNode(String attrId, String value1) {
        this.attributeId = attrId;
        this.value       = value1;
    }
    
    HashMap<String, String> allAttributes() {
        HashMap<String, String> map;
        map = new HashMap<String, String>();
        
        map.put(attributeId, value);
        return map;
    }
}
