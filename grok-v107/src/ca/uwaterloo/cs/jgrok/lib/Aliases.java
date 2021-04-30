package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * void aliases()
 */
public class Aliases extends Function {
    
    public Aliases() {
        name = "aliases";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			ScriptUnitNode unit = env.traceScriptUnit();
			ScriptUnitNode[] sourceUnits = unit.getSourceUnits();
	        
			for(int i = 0; i < sourceUnits.length; i++) {
				env.out.println(sourceUnits[i].getAliasName() + " - " + sourceUnits[i].getFullName()) ;
			}
	        return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "()";
	}
}
