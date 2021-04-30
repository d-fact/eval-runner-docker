package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * void reset()
 */
public class Reset extends Function {
    
    public Reset() 
    {
        name = "reset";
    }
    
    public Value invoke(Env env, Value[] vals)  throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			Variable var;
			Enumeration<Variable> enm;
			ScriptUnitNode unit;
	        
			// Remove all variables.
			unit = env.traceScriptUnit();
			enm = unit.allVariables();
			while(enm.hasMoreElements()) {
				var = enm.nextElement();
				var.setValue(null);
				unit.removeVariable(var);
			}
	        
			// Suggest java to do garbage collection if necessary.
			System.gc();
	        
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "()";
	}
}
