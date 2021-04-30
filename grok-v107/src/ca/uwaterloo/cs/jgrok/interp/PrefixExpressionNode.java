package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class PrefixExpressionNode extends ExpressionNode {
    int op;
    ExpressionNode expNode;
    
    public PrefixExpressionNode(int op, ExpressionNode expNode) {
        this.op = op;
        this.expNode = expNode;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        expNode.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val = expNode.evaluate(env);
        
        // "#" | "!" | "~" | "-" | "id" | "inv" | "dom" | "rng" | "ent"
        
        TypeOperation TOP;
        Operation operation;
        
        TOP = TypeOperation.analyze(op, val.getType());
        if(TOP != null) {
            try {
                operation = TOP.getOperation();
                return operation.eval(op, val);
            } catch(Exception e) {
                throw new EvaluationException(this, e.getMessage());
            }
        } else {
            String err;
            err = ErrorMessage.errUnsupportedOperation(op, val.getType());
            throw new EvaluationException(this, err);
        }
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(Operator.key(op));
        buffer.append(" ");
        buffer.append(expNode);
        
        return buffer.toString();
    }
}
