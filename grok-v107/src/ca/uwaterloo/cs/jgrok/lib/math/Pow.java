package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     double pow(int, int);
 *     double pow(int, double);
 *
 *     double pow(double, double);
 *     double pow(double, double);
 * </pre>
 */
public class Pow extends Function {
    
    public Pow() 
    {
        name = "pow";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2: 
   	        try {
				double base  = vals[0].doubleValue();
				double power = vals[1].doubleValue();

				return new Value(Math.pow(base, power));
			} catch(Exception e) {
				throw new InvocationException(e.getMessage());
		}	}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "double " + name + "(double base, double power)";
	}
}
