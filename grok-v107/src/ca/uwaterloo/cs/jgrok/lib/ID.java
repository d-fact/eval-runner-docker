package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * ID returns ID relation for all entities in the Factbase.
 */
public class ID extends Function {
    
    public ID() {
        name = "ID";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			return new Value(IDManager.getID());
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "()";
	}
}
