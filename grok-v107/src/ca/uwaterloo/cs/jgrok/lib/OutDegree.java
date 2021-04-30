package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet outdegree(EdgeSet)
 *     EdgeSet outdegree(NodeSet, EdgeSet)
 * 
 *     EdgeSet outdegree(TupleSet, col)

 * </pre>
 */
public class OutDegree extends Function {
    
    public OutDegree() 
    {
        name = "outindegree";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException {
        NodeSet nSet;
        EdgeSet eSet;
        EdgeSet result;
        
        switch (vals.length) {
        case 1:
             eSet = (EdgeSet)vals[0].objectValue();
             nSet = AlgebraOperation.domainOf(eSet);
             break;
        case 2:
			Value	value0 = vals[0];
			Value	value1 = vals[1];
			
			if (value1.isPrimitive()) {
		        TupleSet tSet   = (TupleSet)value0.objectValue();
				int		 column = value1.intValue();
                result = UtilityOperation.outdegree(tSet, column);
                return new Value(result);
			}			
			eSet = (EdgeSet)value1.objectValue();
            nSet = (NodeSet)value0.objectValue();
            break;
        default:
			return(illegalUsage());
        }
                  
        result = AlgebraOperation.composition(nSet, eSet);
        result = UtilityOperation.outdegree(result);
        return new Value(result);
    }
    
	public String usage()
    {
		return "EdgeSet " + name + "([NodeSet nodes,] EdgeSet edges)/(TupleSet relation, int column)"; 
	}
}
