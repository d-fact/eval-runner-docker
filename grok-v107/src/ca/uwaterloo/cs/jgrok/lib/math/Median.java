package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;
import ca.uwaterloo.cs.jgrok.lib.Sort;

/**
 * <pre>
 * Functions:
 *
 *     NodeSet  median(NodeSet nodes)
 *     TupleSet median(TupleSet tuples, int column)
 *
 * Examples:
 *     A { age = 10 }
 *     B { age = 10 }
 *     C { age = 30 }
 *     D { age = 32 }
 *     E { age = 32 }
 *
 *     >> median(rng @age)
 *     30
 *     >> median(@age, &1)
 *     C 30
 *
 * </pre>
 */
public class Median extends Function {
    
    public Median() 
    {
        name = "Median";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        Sort s          = new Sort();
        TupleSet tSet   = s.invoke(env, vals).tupleSetValue();
        TupleSet result = tSet.newSet();
        int size        = tSet.size();
        
        if(size > 0) {
			result.getTupleList().add((Tuple)tSet.getTupleList().get(size/2).clone());
		}	
        return new Value(result);
    }
    
    public String usage()
    {
		return "NodeSet " + name + "(NodeSet nodes)/TupleSet " + name + "(TupleSet tuples, int column)";
	}
}
