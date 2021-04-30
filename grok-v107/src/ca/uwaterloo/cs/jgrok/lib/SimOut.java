package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.util.SimRank;

/**
 * TupleSet sim/simout(EdgeSet graph)
 * TupleSet sim/simout(EdgeSet graph, int iterationCount)
 */
public class SimOut extends Function {
    
    public SimOut(String name1) 
    {
        name = name1;
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        int iterationCount = 10;
       
        switch (vals.length) {
			case 2:
 				iterationCount = vals[1].intValue();
 			case 1:
 			{
				EdgeSet graph   = (EdgeSet)vals[0].objectValue();
				SimRank simRank = new SimRank(graph);
	        
				return new Value(simRank.compute(iterationCount));
		}	}
        return illegalUsage();
    }

    public String usage()
    {
		return "TupleSet " + name + "(EdgeSet graph [, int iterationCount])";
	}
}
