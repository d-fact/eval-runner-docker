package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.ConceptEngine;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.fb.SpecialOperation;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     EdgeSet concept(EdgeSet)
 *     EdgeSet lattice(NodeSet)
 * </pre>
 */
public class Lattice extends Function {
    
    public Lattice() {
        name = "lattice";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			NodeSet set = vals[0].nodeSetValue();
			return new Value(SpecialOperation.lattice(set));
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(EdgeSet conceptSet)";
	}
}
