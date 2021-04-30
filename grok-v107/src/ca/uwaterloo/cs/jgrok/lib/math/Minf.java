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
 *     double minf(set)
 *
 * </pre>
 */
public class Minf extends Function {
    
    public Minf() 
    {
        name = "minf";        
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 1) {
			return illegalUsage();
		}
		
		boolean seen     = false;
        double	min      = Double.MAX_VALUE;
        NodeSet set      = vals[0].nodeSetValue();
        Node[]  data     = set.getAllNodes();
        Node	node;
        double	val;
        int		i;
        
        for (i = 0; i < data.length; ++i) {
            try {
				node = data[i];
                val = Double.parseDouble(node.get());
                if( val <= min ){
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
		return "double " + name + "(NodeSet set)";
	}
}
