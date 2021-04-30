package ca.uwaterloo.cs.jgrok.lib;

import java.util.ArrayList;
import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * int partition(EdgeSet graph, String sgPrefix)
 */
public class GraphPartition extends Function {
    
    public GraphPartition() {
        name = "partition";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
			EdgeSet graph    = vals[0].edgeSetValue();
			String	name     = vals[1].stringValue() + "_";
	        Scope	scp      = env.peepScope();
			Enumeration<Variable> enm = scp.allVariables();
			ArrayList<Variable> list = new ArrayList<Variable>(5);
	        
			EdgeSet subgraph;
			Variable var;
			while(enm.hasMoreElements()) {
				var = enm.nextElement();
				if(var.getName().startsWith(name)) list.add(var);
			}
	        
			for(int i = 0; i < list.size(); i++) {
				var = list.get(i);
				scp.removeVariable(var);
			}
	        
			Partition p = new Partition();
			EdgeSet[] sgs = p.getPartitions(graph);
	        
			for(int i = 0; i < sgs.length; i++) {
				subgraph = sgs[i];
				subgraph.setName(name + i);
				var = new Variable(scp, subgraph.getName(), new Value(subgraph));
				scp.addVariable(var);
			}
	        
			return new Value(sgs.length);
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "int " + name + "(EdgeSet graph, String sgPrefix)";
	}
}
