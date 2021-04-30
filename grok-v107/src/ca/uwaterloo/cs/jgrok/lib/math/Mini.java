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
 *     int mini(set)
 * </pre>
 */
public class Mini extends Function {
    
    public Mini() 
    {
        this.name = "mini";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        
        if (vals.length != 1) {
	  		return illegalUsage();
		}
		
        long	min  = Long.MAX_VALUE;
	    boolean	seen = false;

		NodeSet set    = vals[0].nodeSetValue();
		Node[]	data   = set.getAllNodes();
		Node	node;
		int		i;
		long	val;
    
		for (i = 0; i < data.length; i++) {
			try {
				node = data[i];
				val  = Long.parseLong(node.get());
				if (val <= min){
					min  = val;
					seen = true;
				}
			} catch(NumberFormatException e) {}
		}
        if (seen) {
		    return new Value(min);
		}
		return Value.VOID;
	}
   
    public String usage()
    {	
		return "long " + name + "(NodeSet set)";
	} 
}
