package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class ConditionalAndExpressionNode extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    
    public ConditionalAndExpressionNode(ExpressionNode left,
                                 ExpressionNode right) {
        this.left = left;
        this.right = right;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        left.propagate(env, userObj);
        right.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value valL = left.evaluate(env);
        Value valR = right.evaluate(env);
        
        Class<?> type = valL.getType();
        if(type != boolean.class) {
            throw new EvaluationException(left, "boolean expression expected");
        }
        
        type = valR.getType();
        if(type != boolean.class) {
            throw new EvaluationException(right, "boolean expression expected");
        }
        
        return new Value(valL.booleanValue() && valR.booleanValue());
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(left);
        buffer.append(" && ");
        buffer.append(right);
        
        return buffer.toString();
    }
}
