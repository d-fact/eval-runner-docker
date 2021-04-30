package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet closure(EdgeSet edgeSet);
 *     EdgeSet unclosure(EdgeSet edgeSet);
 * </pre>
 */
public class Closure extends Function {
    
    public Closure() {
        name = "closure";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			EdgeSet eset = vals[0].edgeSetValue();
			return new Value(AlgebraOperation.transitiveClosure(eset));
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(EdgeSet set)";
	}
}
