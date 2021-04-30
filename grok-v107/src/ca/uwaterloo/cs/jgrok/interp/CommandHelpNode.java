package ca.uwaterloo.cs.jgrok.interp;

import java.util.HashSet;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.lib.Function;

public class CommandHelpNode extends CommandNode {
    VariableNode varNode;
    
    public CommandHelpNode(VariableNode varNode) {
        super("help");
        this.varNode = varNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        if(varNode == null) {
            env.out.println("help [$|@] identifier");
            return Value.EVAL;
        }
        
        String name = varNode.toString();
        Exception e = null;
        
        try {
            Variable var;
            var = env.lookup(name);
            env.out.println("Variable: " + var.getType().getName() + " " + var.getName());
        } catch(LookupException e1) {
            e = e1;
        }
        
        try {
            Function function = env.lookupFunction(name);
           	String usage = function.usage();
			if(usage != null) {
				env.out.println(usage);
	        }
        } catch(LookupException e2) {
            // Throw exception by e1 not by e2.
            if(e != null)
                throw new EvaluationException(this, e.getMessage());
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("help");
        if(varNode != null) {
            buffer.append(' ');
            buffer.append(varNode);
        }
        buffer.append(';');
        
        return buffer.toString();
    }
}
