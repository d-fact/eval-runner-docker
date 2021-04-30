package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class ConditionalExpressionNode extends ExpressionNode {
    ExpressionNode test;
    
    public ConditionalExpressionNode(ExpressionNode exp) {
        this.test = exp;
    }
    
    public void propagate(Env env, Object userObj) throws EvaluationException {
        test.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        return test.evaluate(env);
    }
    
    public String toString() {
        return test.toString();
    }
}
