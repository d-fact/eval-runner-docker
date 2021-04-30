package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * Function sqrt
 * <pre>
 *     double sqrt(int);
 *     double sqrt(float);
 * </pre>
 * 
 * @author JingweiWu
 */
public class Sqrt extends Function {

    public Sqrt() 
    {
        name = "sqrt";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException {
        
        switch (vals.length) {
        case 1:
			try {
				double param = vals[0].doubleValue();
				return new Value(Math.sqrt(param));
			} catch(Exception e) {
				throw new InvocationException(e.getMessage());
			}
		default:
			return illegalUsage();
		}
    }
    
    public String usage()
    {	
		return "double " + name + "(double val)";
	} 
}
