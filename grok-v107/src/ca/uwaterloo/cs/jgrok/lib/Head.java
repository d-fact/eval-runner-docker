package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     tset head(tset)
 *     tset head(tset, int)
 * </pre>
 */
public class Head extends Function {
    
    public Head() {
        name = "head";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        int count = 10;
        TupleSet input;
        TupleSet output;
        TupleList tList;

		switch (vals.length) {
		case 2:
			count = vals[1].intValue();
		case 1:
			input = vals[0].tupleSetValue();
			tList = input.getTupleList();
        
            output = input.newSet();        
			count = (count < tList.size()) ? count : tList.size();
			for(int i = 0; i < count; i++) {
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
