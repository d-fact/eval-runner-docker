package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * ENT returns all entities in the Factbase.
 */
public class ENT extends Function {
    
    public ENT() 
    {
        name = "ENT";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			return new Value(IDManager.getENT());
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "NodeSet " + name + "()";
	} 
}
