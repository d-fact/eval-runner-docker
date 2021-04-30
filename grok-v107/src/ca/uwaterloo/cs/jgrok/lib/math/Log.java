package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     double log(int, int);
 *     double log(int, float);
 * </pre>
 */
public class Log extends Function {
    
    public Log() 
    {
        name = "log";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 2) {
			return illegalUsage();
		}
        
        try {
            double base  = vals[0].doubleValue();
            double value = vals[1].doubleValue();
            return new Value(Math.log(value)/Math.log(base));
        } catch(Exception e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    public String usage()
    {
		return "double " + name + "(int base, int/long/float/double/string val)";
	}
}
