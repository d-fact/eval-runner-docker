package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *     int   int(int/float/string)
 *     float float(int/float/string)
 * </pre>
 */
public class Float1 extends Function {
    
    public Float1() 
    {
        name = "float";
    }
        
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			return new Value(vals[0].floatValue());
		} 
		return illegalUsage();
    }
    
    public String usage()
    {
		return "float " + name + "(short|int|long|float|double|string val)";
	}
}
