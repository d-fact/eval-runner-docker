package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * showpath(string source, string target, EdgeSet edgeSet)
 */
public class ShowPath extends Function {
    
    public ShowPath() 
    {
        name = "showpath";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 3:
			String source = vals[0].stringValue();
			String target = vals[1].stringValue();
			EdgeSet eSet  = vals[2].edgeSetValue();
	        
			eSet = UtilityOperation.reach(NodeSet.singleton(source),
										  NodeSet.singleton(target),
										  eSet);
	        
			PathClosure pc = new PathClosure(eSet);
			Path[] paths = pc.getPaths(source, target);
	        
			for(int i = 0; i < paths.length; i++) {
				env.out.println(paths[i]);
			}
	        
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "(string source, string target, EdgeSet edgeSet)";
	}
}
