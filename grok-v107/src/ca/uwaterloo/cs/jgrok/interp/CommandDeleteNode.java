package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class CommandDeleteNode extends CommandNode {
    ExpressionNode expNode;
    
    public CommandDeleteNode(ExpressionNode expNode) {
        super("delete");
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Scope scp;
        String name;
        Variable var;
        ExpressionNode arg;
        ExpressionNode[] args;
        
        scp = env.peepScope();
        
        if(expNode instanceof ArgumentsNode) {
            ArgumentsNode argsNode;
            argsNode = (ArgumentsNode)expNode;
            
            args = argsNode.getArguments();
            for(int i = 0; i < args.length; i++) {
                arg = args[i];
                
                // Get the name of variable.
                if(arg instanceof EvalName) {
                    name = ((EvalName)arg).evalName(env);
                } else {
                    Value val = arg.evaluate(env);
                    if(val.getType() == String.class) {
                        name = val.toString();
                    } else {
                        throw new EvaluationException(arg, "illegal expression: " + arg);
                    }
                }
                
                // Look up the variable.
                try {
                    var = scp.lookup(name);
                } catch(LookupException e) { continue; }
                
                // Remove the variable from its scope.
                scp.removeVariable(var);
            }
        } else {
            // Get the name of variable.
            if(expNode instanceof EvalName) {
                name = ((EvalName)expNode).evalName(env);
            } else {
                Value val = expNode.evaluate(env);
                if(val.getType() == String.class) {
                    name = val.toString();
                } else {
                    throw new EvaluationException(expNode, "illegal expression: " + expNode);
                }
            }
            
            // Look up the variable.
            try {
                var = scp.lookup(name);
            } catch(LookupException e) {
                return Value.EVAL;
            }
            
            // Remove var from its scope.
            scp.removeVariable(var);
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("delete");
        if(!(expNode instanceof ArgumentsNode)) {
            buffer.append(' ');
        }
        buffer.append(expNode);
        buffer.append(';');
        
        return buffer.toString();
    }
}
