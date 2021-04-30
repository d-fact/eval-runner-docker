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
public class Concept extends Function {
    
    public Concept() {
        name = "concept";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			EdgeSet hasFeature = vals[0].edgeSetValue();
			ConceptEngine engine = new ConceptEngine();
			engine.compute(hasFeature);
	            
			return new Value(engine.getConcepts());
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "(EdgeSet hasFeature)";
	}
}
