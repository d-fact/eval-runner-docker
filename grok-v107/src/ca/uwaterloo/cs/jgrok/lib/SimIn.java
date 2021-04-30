package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.util.SimRank;

/**
 * TupleSet simin(EdgeSet graph)
 * TupleSet simin(EdgeSet graph, int iterationCount)
 */
public class SimIn extends Function {
    
    public SimIn() 
    {
        name = "simIn";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        int iterationCount = 10;
		EdgeSet graph;
        SimRank simRank;
        
        switch (vals.length) {
        case 2:
			iterationCount = vals[1].intValue();
		case 1:
            graph = vals[0].edgeSetValue();
			graph = AlgebraOperation.inverse(graph);
			simRank = new SimRank(graph);
        
			return new Value(simRank.compute(iterationCount));
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(EdgeSet graph [, int iterationCount])";
	}
}
