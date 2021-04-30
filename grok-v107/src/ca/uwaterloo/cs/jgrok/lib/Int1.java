package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *     int   int(int/float/string)
 *     float float(int/float/string)
 * </pre>
 */
public class Int1 extends Function {
    
    public Int1() {
        name = "int";
    }
        
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			return new Value(vals[0].intValue());
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "int " + name + "(short|int|long|float|double|string val)";
	}
}
