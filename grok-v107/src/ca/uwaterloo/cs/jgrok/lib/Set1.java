package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     NodeSet set()
 * </pre>
 */
public class Set1 extends Function {
    
    public Set1() 
    {
		name = "set";
    }
        
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length == 0) {
			return new Value(new NodeSet());
		}
		return illegalUsage();
    }
    
	public String usage()
	{
		return "NodeSet " + name + "()";
	}
}
