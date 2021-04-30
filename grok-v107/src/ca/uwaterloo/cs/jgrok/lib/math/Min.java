package ca.uwaterloo.cs.jgrok.lib.math;

import java.util.Arrays;
import java.util.HashMap;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 * Functions:
 *
 *     set min(set)
 *     set min(set, int)
 *
 * </pre>
 */
public class Min extends Function {
    
     public Min() 
    {
        name = "min";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
            return multiset(vals);
        case 1:
			double	min      = Double.MAX_VALUE;
			NodeSet set      = vals[0].nodeSetValue();
			Node[]	data     = set.getAllNodes();
			NodeSet result   = null;
			String	s_min    = null;
			Node	node;
			String	s;
			double	val;
			int		i;
	        
			for(i = 0; i < data.length; i++) {
				try {
					node = data[i];
					s    = node.get();
					val  = Double.parseDouble(s);
					if (val <= min){
						s_min = s;
						min   = val;
					}
				} catch(NumberFormatException e) {}
			}
	        
			if (s_min == null) {
				result = new NodeSet();
			} else {
				result = NodeSet.singleton(s_min);
			}
			return new Value(result);
		}
		return illegalUsage();
	}
    
    private Value multiset(Value[] vals) 
    {
        NodeSet set    = vals[0].nodeSetValue();
        int		count  = vals[1].intValue();
        NodeSet result = new NodeSet();
        
        if(count > 0) {
            Node[] data = set.getAllNodes(); 
            HashMap<Double,String> map = new HashMap<Double,String>(101, 0.75f); 
            Double f;
            String s;
            for(int i = 0; i < data.length; i++) {
                try {
                    s = data[i].get();
                    f = new Double(s);
                    map.put(f, s);
                } catch(NumberFormatException e) {}
            }
            
            Object[] keys;
            keys = map.keySet().toArray();
            Arrays.sort(keys);
            
            for(int i = 0; i < keys.length && count > 0; i++, count--) {
                result.add((String)map.get(keys[i]));
            }
        }
        return new Value(result);
    }

    public String usage()
    {	
		return "NodeSet " + name + "(NodeSet set [, int count])";
	} 
}
