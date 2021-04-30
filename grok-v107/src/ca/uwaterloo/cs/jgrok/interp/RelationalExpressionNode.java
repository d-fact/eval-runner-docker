package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class RelationalExpressionNode extends ExpressionNode {
    int op;
    ExpressionNode left;
    ExpressionNode right;
    
    public RelationalExpressionNode(int op,
                             ExpressionNode left,
                             ExpressionNode right) {
        this.op = op;
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
        
        // "=="  "!="  "<"  "<="  ">"  ">="  "=~"  "!~"  "in"
        TypeOperation TOP;
        Operation operation;
        
        TOP = TypeOperation.analyze(op, valL.getType(), valR.getType());
        if(TOP != null) {
            try {
                operation = TOP.getOperation();
                return operation.eval(op, valL, valR);
            } catch(Exception e) {
                throw new EvaluationException(this, e.getMessage());
            }
        } else {
            String err;
            err = ErrorMessage.errUnsupportedOperation(op, valL.getType(), valR.getType());
            throw new EvaluationException(this, err);
        }
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(left);
        buffer.append(" ");
        buffer.append(Operator.key(op));
        buffer.append(" ");
        buffer.append(right);
        
        return buffer.toString();
    }
}
