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
 *     int maxi(set)
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
public class Maxi extends Function {
    
    public Maxi() 
    {
        this.name = "maxi";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 1) {
			return illegalUsage();
		}
       
        long	max    = Long.MIN_VALUE;
        boolean	seen   = false;
        NodeSet set    = vals[0].nodeSetValue();
        Node[]	data   = set.getAllNodes();
        Node	node;
        int		i;
        long	val;
        
        for (i = 0; i < data.length; i++) {
			try {
				node = data[i];
                val = Long.parseLong(node.get());
                if (val >= max){
					max  = val;
					seen = true;
                }
            } catch(NumberFormatException e) {}
        }
        
        if (seen) {
            return new Value(max);
        }
        return Value.VOID;
    }
   
    public String usage()
    {	
		return "long " + name + "(NodeSet set)";
	} 
}
