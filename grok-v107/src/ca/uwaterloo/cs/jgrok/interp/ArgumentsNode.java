package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class ArgumentsNode extends ExpressionNode {
    ExpressionNode[] args;
    
    public ArgumentsNode(ExpressionNode[] args) {
        this.args = args;
        if(args == null)
            this.args = new ExpressionNode[0];
    }
    
    public ExpressionNode[] getArguments() {
        return args;
    }

    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        for(int i = 0; i < args.length; i++) {
            args[i].propagate(env, userObj);
        }
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value[] argValues;
        
        argValues = new Value[args.length];
        for(int i = 0; i < args.length; i++) {
            argValues[i] = args[i].evaluate(env);
        }
        
        if(args.length == 1)
            return argValues[0];
        else
            return new Value(argValues);
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append('(');
        for(int i = 0; i < args.length; i++) {
            buffer.append(args[i]);
            buffer.append(", ");
        }
        if(args.length == 0) {
            buffer.append(')');
        } else {
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.setCharAt(buffer.length()-1, ')');
        }
        
        return buffer.toString();
    }
}
