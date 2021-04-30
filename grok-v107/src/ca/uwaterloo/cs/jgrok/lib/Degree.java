package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet degree(TupleSet, col)
 *
 *     TupleSet degree(TupleSet, col, ...)
 * </pre>
 */
public class Degree extends Function {
    
    public Degree() 
    {
		this.name = "degree";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		TupleSet result;
		TupleSet tSet;
		
        switch (vals.length) {
        case 0:
        case 1:
            result = new TupleSet();
            break;
        case 2:
	        tSet   = vals[0].tupleSetValue();
			int	column = vals[1].intValue();
		    result = UtilityOperation.degree(tSet, column);
		    break;
		default:
		    tSet   = vals[0].tupleSetValue();

            int[] cols = new int[vals.length-1];
            for(int i = 0; i < vals.length-1; i++) {
                cols[i] = vals[i+1].intValue();
            }
            result = UtilityOperation.degree(tSet, cols);
        }         
        return new Value(result);
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet relation [, int column]+ )";
	}
}
