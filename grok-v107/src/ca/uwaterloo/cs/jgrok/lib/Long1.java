package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *     int   int(int/float/string)
 *     float float(int/float/string)
 * </pre>
 */
public class Long1 extends Function {
    
    public Long1() {
        name = "long";
    }
        
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			return new Value(vals[0].longValue());
		} 
		return illegalUsage();
    }
    
    public String usage()
    {
		return "long " + name + "(short|int|long|float|double|string val)";
	}
}
