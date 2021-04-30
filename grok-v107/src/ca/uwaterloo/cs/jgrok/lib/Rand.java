package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     tset rand(tset)
 *     tset rand(tset, int)
 * </pre>
 */
public class Rand extends Function {
    
    public Rand() {
        name = "rand";
    }
    
    public Value invoke(Env env, Value[] vals)  throws InvocationException 
    {
        int count = 10;
        
        switch (vals.length) {
        case 2:
			count = vals[1].intValue();
		case 1:
            TupleSet	input  = vals[0].tupleSetValue();
			TupleList	tList  = input.getTupleList();
			TupleSet	output = input.newSet();
            int			size = tList.size();
            
			if(count < 1) {
				return new Value(output);
			}
			if(count >= size) {
				return new Value(input.clone());
			}
            int step;
			int index;
			boolean b = false;
        
			step = size / count;
			if(step == 1) {
				b = true;
				count = size - count;
				step = size / count;
			}
        
			for(int i = 0; i < count; i++) {
				index = i * step + (int)(Math.random() * step);
				output.add((Tuple)tList.get(index).clone());
			}
        
			if(b) output = AlgebraOperation.difference(input, output);
			return new Value(output);
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet set [, int count])";
	}
}
