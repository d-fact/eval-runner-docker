package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet indegree(EdgeSet)
 *     EdgeSet indegree(NodeSet, EdgeSet)
 *
 *     EdgeSet indegree(TupleSet, col)
 * </pre>
 */
public class InDegree extends Function {
    
    public InDegree() {
        name = "indegree";
   }
     
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        NodeSet nSet;
        EdgeSet eSet;
        EdgeSet result;
        
        switch (vals.length) {
        case 1:
            eSet = (EdgeSet)vals[0].objectValue();
            nSet = AlgebraOperation.rangeOf(eSet);
            break;

        case 2:
			Value value0 = vals[0];
			Value value1 = vals[1];
			
			if (value1.isPrimitive()) {
		        TupleSet tSet   = (TupleSet)value0.objectValue();
				int		 column = value1.intValue();
				
				result = UtilityOperation.indegree(tSet, column);
		        return new Value(result);
			}
            eSet = (EdgeSet)value1.objectValue();
            nSet = (NodeSet)value0.objectValue();
            break;
        default:
			return(illegalUsage());
        }
        result = AlgebraOperation.composition(eSet, nSet);
        result = AlgebraOperation.inverse(result);
        result = UtilityOperation.outdegree(result);
        
        return new Value(result);
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "([NodeSet nodes,] EdgeSet edges)/(TupleSet relation, int column)"; 
	}
}	

