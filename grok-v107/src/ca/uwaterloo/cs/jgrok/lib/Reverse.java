package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     TupleSet reverse(TupleSet tuples)
 * </pre>
 * 
 * Note: function <b>reverse</b> should be used in combination of function <b>sort</b> and <b>sortd</b>.
 * The following must hold true.
 * <pre>
 *     reverse(sort(nodes)) == sortd(nodes)
 *     reverse(sortd(nodes)) == sort(nodes)
 * </pre>
 */
public class Reverse extends Function {
    
    public Reverse() 
    {
        name = "reverse";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    { 
		switch (vals.length) {
		case 1:
			TupleSet	input   = vals[0].tupleSetValue();
			TupleList	tList   = input.getTupleList();
			TupleSet	output  = input.newSet();
			int			count   = tList.size();
			int index;
			for(int i = 1; i <= count; i++) {
				index = count - i;
				output.add((Tuple)tList.get(index).clone());
			}
	        
			return new Value(output);
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet tuples)";
	}
}
