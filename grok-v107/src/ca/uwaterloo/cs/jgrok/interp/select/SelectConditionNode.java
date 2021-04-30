package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public abstract class SelectConditionNode extends ExpressionNode {
    
    public SelectConditionNode() {}
    
    public Value evaluate(Env env) throws EvaluationException {
        throw new EvaluationException(this, "Illegal evaluation");
    }
    
    public abstract TupleSet evaluate(Env env, TupleSet tSet)
        throws EvaluationException;
}
