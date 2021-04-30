package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * int sgcount(EdgeSet)
 */
public class GraphPartitionCount extends Function {
    
    public GraphPartitionCount() {
        name = "sgcount";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			EdgeSet rel = vals[0].edgeSetValue();
			Partition p = new Partition();
			int count = p.countPartitions(rel);
			return new Value(count);
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "int " + name + "(EdgeSet set)";
	}
}
