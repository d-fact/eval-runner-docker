package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet eset()
 * </pre>
 */
public class Eset extends Function {
    
    public Eset() {
        name = "eset";
    }
        
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			return new Value(new EdgeSet());
		}
		return illegalUsage();
    }
    
	public String usage()
	{
		return "EdgeSet " + name + "()";
	}
}
