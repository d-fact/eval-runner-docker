package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

public class SuffixExpressionNode extends ExpressionNode {
    int op = -100;
    ExpressionNode expNode;
    
    public SuffixExpressionNode(ExpressionNode expNode) {
        this.expNode = expNode;
    }
    
    public SuffixExpressionNode(ExpressionNode expNode, int op) {
        this.op = op;
        this.expNode = expNode;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        expNode.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val = expNode.evaluate(env);
        if(val.getType() != EdgeSet.class) {
            throw new EvaluationException(this, 
                                          "EdgeSet expected for op: "
                                          + Operator.key(op));
        }
        EdgeSet set = (EdgeSet)val.objectValue();
        
        switch(op) {
        case Operator.MULTIPLY:
            return new Value(AlgebraOperation.reflectiveClosure(set));
        case Operator.PLUS:
            return new Value(AlgebraOperation.transitiveClosure(set));
        case -100:
            return val;
        }
        
        throw new EvaluationException(this,
                                      "illegal expression: "
                                      + val.getType() + " "
                                      + Operator.key(op));
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(expNode);
        buffer.append(" ");
        buffer.append(Operator.key(op));
        
        return buffer.toString();
    }
}
