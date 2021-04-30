package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *      EdgeSet reach(string  startNode,  EdgeSet edges)
 *      EdgeSet reach(NodeSet startNodes, EdgeSet edges)
 *      EdgeSet reach(NodeSet startNodes, NodeSet endNodes, EdgeSet edges)
 * </pre>
 */
public class Reach extends Function {
    
    public Reach() {
        name = "reach";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        Object o;
        EdgeSet eSet;
        EdgeSet result;
        
        switch (vals.length) {
        case 3:
			NodeSet srcSet  = vals[0].nodeSetValue();
            NodeSet sinkSet = vals[1].nodeSetValue();
            
            eSet =  vals[2].edgeSetValue();
            result = UtilityOperation.reach(srcSet, sinkSet, eSet);
            break;
        case 2:
             NodeSet set;

			if (vals[0].isNodeSet()) {
                set = vals[0].nodeSetValue();
            } else {
                set = NodeSet.singleton(vals[0].stringValue());
            }
            eSet   = vals[1].edgeSetValue();
            result = UtilityOperation.reach(set, eSet);
			break;
		default:
			return illegalUsage();
		}
        
        return new Value(result);
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(String startNode, Edgeset edges)/(NodeSet startNodes [, NodeSet endNodes], EdgeSet edges)"; 
	}
}
