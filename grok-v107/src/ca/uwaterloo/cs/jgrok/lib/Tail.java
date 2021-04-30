package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     tset tail(tset)
 *     tset tail(tset, int)
 * </pre>
 */
public class Tail extends Function {
    
    public Tail() {
        name = "tail";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        int count = 10;
        int start;
        TupleSet input;
        TupleSet output;
        TupleList tList;
        int	i;
        
        switch (vals.length) {
        case 2:
			count = vals[1].intValue();
		case 1:
			input = vals[0].tupleSetValue();
			tList = input.getTupleList();
                     
			start = tList.size() - count;
			start = (start > 0) ? start : 0;
        
			output = input.newSet();        
			for(i = start; i < tList.size(); i++) {
				output.add((Tuple)tList.get(i).clone());
			}
            return new Value(output);
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet set [, int count])";
	}
}
