package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * EdgeSet localof(EdgeSet)
 */
public class LocalOf extends Function {
    
    public LocalOf() {
        name = "localof";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			EdgeSet eSet = vals[0].edgeSetValue();
			return new Value(UtilityOperation.localof(eSet));
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(EdgeSet set)";
	}
}
