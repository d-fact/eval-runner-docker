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
 *     set max(set)
 *     set max(set, int)
 *
 *     long   maxi(set)
 *     double maxf(set)
 *
 * Examples:
 *     A { age = 10 }
 *     B { age = 10 }
 *     C { age = 30 }
 *     D { age = 32 }
 *     E { age = 32 }
 *
 *     >> max(rng @age)
 *     32
 *     >> max(rng @age, 2)
 *     32
 *     30
 *     >> maxi(rng @age) / 4
 *     8
 *     >> maxf(rng @age) / 4
 *     8.0
 *     >> @age . max(rng @age}
 *     D
 *     E
 *
 * </pre>
 */
public class Max extends Function {
    
    public Max() 
    {
        name = "max";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
            return multiset(vals);
		case 1:
			double	max      = Double.MIN_VALUE;
			NodeSet set      = vals[0].nodeSetValue();
			Node[]	data     = set.getAllNodes();
			NodeSet result   = null;
			String	s_max    = null;
			Node	node;
			String	s;
			double	val;
			int		i;
	        
			for(i = 0; i < data.length; i++) {
				try {
					node = data[i];
					s    = node.get();
					val  = Double.parseDouble(s);
					if (val >= max){
						s_max = s;
						max   = val;
					}
				} catch(NumberFormatException e) {}
			}
	        
			if (s_max == null) {
				result = new NodeSet();
			} else {
				result = NodeSet.singleton(s_max);
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
            HashMap<Double,String> map = new HashMap<Double,String>(101, 0.75f); ;
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
            
            for(int i = keys.length; --i >= 0 && count > 0; count--) {
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
