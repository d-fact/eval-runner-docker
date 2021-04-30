package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     double ln(int);
 *     double ln(float);
 * </pre>
 */
public class Ln extends Function {
    
    public Ln() 
    {
        name = "ln";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 2) {
			return illegalUsage();
		}    
        try {
			double value = vals[0].doubleValue();
            return new Value((Math.log(value)));
        } catch(Exception e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    public String usage()
    {
		return "double " + name + "(int/long/float/double/string val)";
	}
}
