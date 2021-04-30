package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.env.Env;

/**
 * EdgeSet paths(EdgeSet edgeSet)
 * EdgeSet paths(string node, EdgeSet edgeSet)
 * EdgeSet paths(string sourceNode, string targetNode, EdgeSet edgeSet)
 */
public class Paths extends Function {
    
    public Paths() {
        name = "paths";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException {
        EdgeSet eSet;
        TupleSet pSet;
        PathClosure pClosure;
        Path[] paths;
        
        switch (vals.length) {
        case 3:
			String source = vals[0].stringValue();
            String target = vals[1].stringValue();
            eSet = vals[2].edgeSetValue();
            
            eSet = UtilityOperation.reach(NodeSet.singleton(source),
                                          NodeSet.singleton(target),
                                          eSet);
            
            pClosure = new PathClosure(eSet);
            paths = pClosure.getPaths(source, target);
            break;
        case 2:
            paths = new Path[0];
            break;
        case 1:
            eSet = vals[0].edgeSetValue();
            
            pClosure = new PathClosure(eSet);
            paths = pClosure.getPaths();
            break;
		default:
			return illegalUsage();
		}
        
        pSet = new TupleSet(paths.length);
        for(int i = 0; i < paths.length; i++) {
            pSet.add(paths[i].getTuple());
        }
        
        return new Value(pSet);
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "([[String sourceNode,] String targetNode,] EdgeSet edgeSet)";
	}
}
