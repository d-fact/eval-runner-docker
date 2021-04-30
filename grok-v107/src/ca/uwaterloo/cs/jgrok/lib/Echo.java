package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * void echo(boolean)
 */
public class Echo extends Function {
    
    public Echo() {
        name = "echo";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			boolean b = vals[0].booleanValue();
        
			env.traceScriptUnit().getOption().setEchoOn(b);
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "(boolean state)";
	}
}
