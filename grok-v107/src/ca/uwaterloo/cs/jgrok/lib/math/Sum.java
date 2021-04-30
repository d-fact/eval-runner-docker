package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 * Functions:
 *
 *     double sum(NodeSet)
 *     double sum(TupleSet, col)
 *
 * Examples:
 *     A { age = 10 }
 *     B { age = 10 }
 *     C { age = 30 }
 *     D { age = 32 }
 *     E { age = 32 }
 *
 *     >> sum(rng @age)
 *     72.0
 *     >> sum(@age, 1)
 *     114.0
 *
 * </pre>
 */
public class Sum extends Function {
    
    public Sum() {
        name = "sum";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        double		result = 0;
        TupleSet	input;
        TupleList	data;
        Tuple		tuple;
        int			i;
        
        switch (vals.length) {
        case 1:
			input  = vals[0].tupleSetValue();
	        
			if(input.size() > 0) {
				data = input.getTupleList();
	            for(i = 0; i < data.size(); i++) {
					tuple = data.get(i);
					try {
						result += Double.parseDouble(IDManager.get(tuple.getDom()));
					} catch(NumberFormatException e) {}
			}	}
			break;
		case 2:    
			input = vals[0].tupleSetValue();
	        
			if(input.size() > 0) {
	            int col = vals[1].intValue();

				data = input.getTupleList();
				for (i = 0; i < data.size(); i++) {
					tuple = data.get(i);
					if(col < tuple.size()) {
						try {
							result += Double.parseDouble(IDManager.get(tuple.get(col)));
						} catch(NumberFormatException e) {}
					} else {
						throw new InvocationException("index " + col + " out of bounds " + tuple.size());
					}
				}
			}
			break;
		default:
			return illegalUsage();
		}
	    return new Value(result);
    }
    
    public String usage()
    {	
		return "double " + name + "(NodeSet set [, int column])";
	} 
}
