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
 *     double avg(NodeSet)
 *     double avg(TupleSet, col)
 *
 * Examples:
 *     A { age = 10 }
 *     B { age = 10 }
 *     C { age = 30 }
 *     D { age = 32 }
 *     E { age = 32 }
 *
 *     >> avg(rng @age)
 *     24.0
 *     >> avg(@age, 1)
 *     22.8
 *
 * </pre>
 */
public class Avg extends Function {
    
    public Avg() 
    {
        name = "avg";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        double		result = 0;
        TupleSet	input;
        TupleList	data;
        Tuple		tuple;
        int			i, size;
        
        switch (vals.length) {
        case 1:
			input  = vals[0].tupleSetValue();
			size   = input.size();
     
			if(size > 0) {
				data = input.getTupleList();
            
           
                for(i = 0; i < data.size(); i++) {
                    tuple = data.get(i);
                    try {
                        result += Double.parseDouble(IDManager.get(tuple.getDom()));
                    } catch(NumberFormatException e) {}
            }   }
            break;
       case 2:
            int col = vals[1].intValue();
        	input   = vals[0].tupleSetValue();
	       	size    = input.size();
        
            if(size > 0) {
				data = input.getTupleList();
                for(i = 0; i < data.size(); i++) {
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
               
        return new Value(result / size);
    }
    
    public String usage()
    {
		return "double avg(NodeSet nodeset)/(TupleSet tupleset, int col)";
	}
}
