package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.util.*;

/**
 * SimRank for bipartite graph.
 * <pre>
 *     TupleSet simBi(EdgeSet biGraph)
 *     TupleSet simBi(EdgeSet biGraph, int iterationCount)
 *
 *     TupleSet simBi(EdgeSet biGraph, EdgeSet featureGraph)
 *     TupleSet simBi(EdgeSet biGraph, EdgeSet featureGraph, int iterationCount)
 * </pre>
 */
public class SimRankBi extends Function {
    
    public SimRankBi() 
    {
        name = "simBi";
    }
    
    public Value invoke(Env env, Value[] vals)  throws InvocationException 
    {
        SimInitialization simInit            = null;
        int				  iterationCount     = 10;
        boolean			  iterationCountSeen = false;

		switch (vals.length) {
		case 3:
            iterationCount = vals[2].intValue();
            iterationCountSeen = true;
        case 2:    
			if (!iterationCountSeen && vals[1].isNumeric()) {
				iterationCount = vals[1].intValue();
				iterationCountSeen = true;
			} else {
				simInit = new SimInitialization(vals[1].edgeSetValue(), null);
			}
		case 1:
            EdgeSet graph = vals[0].edgeSetValue();
			SimRankBipartite simRank = new SimRankBipartite(graph);
	        if (simInit != null) {
				simInit.initialize(simRank);
			}
            return new Value(simRank.compute(iterationCount));
        }
        return illegalUsage();
    }
    
    public String usage()
	{
		return "TupleSet " + name + "(EdgeSet biGraph [, EdgeSet featureGraph] [, int iterationCount])";
	}
}
