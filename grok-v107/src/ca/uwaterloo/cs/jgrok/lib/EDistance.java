package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * Embed edges into a tree.
 *
 * <pre>
 *     EdgeSet edist(EdgeSet edges)
 *     EdgeSet edist(EdgeSet edges, EdgeSet contain)
 * </pre>
 */
public class EDistance extends Function {
    
    public EDistance() {
        name = "edist";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        EdgeSet edges;
        EdgeSet contain;
        
        switch (vals.length) {
        case 2:
			edges   = vals[0].edgeSetValue();
			contain = vals[1].edgeSetValue();
			break;
		case 1:
 			edges   = vals[0].edgeSetValue();
            try {
                Variable var;
                var = env.peepScope().lookup("contain");
                if(var.getType() != EdgeSet.class) {
                    throw new InvocationException("contain is not relation");
                }
                contain = (EdgeSet)var.getValue().objectValue();
            } catch(Exception e) {
                throw new InvocationException("contain not found");
            }
            break;
        default:
			return illegalUsage();
        }
        
        EdgeTree tree = new EdgeTree(contain);
        EdgeSet edgeDistance = tree.getEdgeDistance(edges);
        return new Value(edgeDistance);
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(EdgeSet edges [,EdgeSet contain])";
	}
}
