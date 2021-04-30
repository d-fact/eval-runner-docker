package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *     EdgeSet level()
 *     EdgeSet level(NodeSet nodes)
 *     EdgeSet level(NodeSet nodes, EdgeSet contain)
 * </pre>
 */
public class Level extends Function {
    
    public Level() 
    {
        name = "level";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        NodeSet nodes = null;
        EdgeSet contain = null;
        
        switch(vals.length) {
		case 2: // level(set, contain)
            contain = vals[1].edgeSetValue();
        case 1: // level(set)
            nodes   = vals[0].nodeSetValue();
            break;
        default:
			return illegalUsage();
        }
        
        if(contain == null) {
            try {
                Variable var;
                var = env.lookup("contain");
                if(var.getType() != EdgeSet.class) {
                    throw new InvocationException("contain is not relation");
                }
                contain = (EdgeSet)var.getValue().objectValue();
            } catch(Exception e) {
                throw new InvocationException("contain not found");
            }
        }
        
        Tree tree = new Tree(contain);
        int[] roots = tree.getRoots();
        
        if(roots.length > 1) {
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i < roots.length; i++) {
                buffer.append("\n\t"+IDManager.get(roots[i]));
            }
            throw new InvocationException("multiple roots found:"
                                          + buffer.toString());
        } else if(roots.length == 1) {
            EdgeSet l = tree.getLevelRelation();
            if(nodes == null) {
                return new Value(l);
            } else {
                return new Value(AlgebraOperation.composition(nodes, l));
            }
        } else {
            throw new InvocationException("root not found");
        }
    }
    
    public String usage()
    {
		return "EdgeSet " + name + "([NodeSet nodes [, EdgeSet contain]])";
	}
}