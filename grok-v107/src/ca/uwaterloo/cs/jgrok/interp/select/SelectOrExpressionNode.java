package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SelectOrExpressionNode extends SelectConditionNode {
    SelectConditionNode left;
    SelectConditionNode right;
    
    public SelectOrExpressionNode(SelectConditionNode left,
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
        TupleSet leftSet = left.evaluate(env, tSet);
        TupleSet rightSet = right.evaluate(env, tSet);
        return AlgebraOperation.union(leftSet, rightSet);
    }
    
    public String toString() {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append(left.toString());
        buf.append(" || ");
        buf.append(right.toString());
        
        return buf.toString();
    }
}

