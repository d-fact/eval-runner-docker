package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.TupleSet;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * string name(TupleSet)
 */
public class Name extends Function {
    
    public Name() {
        name = "name";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			return new Value(vals[0].tupleSetValue().getName());
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "String " + name + "(TupleSet set)";
	}
}
