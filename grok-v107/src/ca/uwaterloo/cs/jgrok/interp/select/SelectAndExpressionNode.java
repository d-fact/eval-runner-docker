package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SelectAndExpressionNode extends SelectConditionNode {
    SelectConditionNode left;
    SelectConditionNode right;
    
    public SelectAndExpressionNode(SelectConditionNode left,
                                   SelectConditionNode right) {
        this.left = left;
        this.right = right;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        left.propagate(env, userObj);
        right.propagate(env, userObj);
    }
    
    public TupleSet evaluate(Env env, TupleSet tSet)
        throws EvaluationException {
        TupleSet result;
        
        result = left.evaluate(env, tSet);
        result = right.evaluate(env, result);
        
        return result;
    }
    
    public String toString() {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append(left.toString());
        buf.append(" && ");
        buf.append(right.toString());
        
        return buf.toString();
    }
}
