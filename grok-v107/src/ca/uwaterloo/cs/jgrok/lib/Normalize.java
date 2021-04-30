package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * TupleSet normalize(TupleSet)
 */
public class Normalize extends Function {
    
    public Normalize() {
        name = "normalize";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			TupleSet input = vals[0].tupleSetValue();
	        
			if(input.hasName())
				input = (TupleSet)input.clone();
	        
			TupleList data = input.getTupleList();
			TupleSet result = input.newSet();
	        
			for(int i = 0; i < data.size(); i++) {
				Tuple tuple = data.get(i);
				for(int j = 0; j < tuple.size(); j++) {
					String s = IDManager.get(tuple.get(j));
					tuple.set(j, IDManager.getID(s));
				}
				result.add(tuple);
			}
            return new Value(result);
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet set)";
	}
}
