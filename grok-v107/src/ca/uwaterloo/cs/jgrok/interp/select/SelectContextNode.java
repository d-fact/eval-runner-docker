package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public abstract class SelectContextNode
    extends SelectConditionNode implements SelectContext {
    
    private Tuple catchedTuple;
    
    public SelectContextNode() {}
    
    public Tuple getTuple() {
        return catchedTuple;
    }
    
    public void setTuple(Tuple t) {
        catchedTuple = t;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        throw new EvaluationException(this, "Illegal evaluation");
    }
    
    public abstract TupleSet evaluate(Env env, TupleSet tSet)
        throws EvaluationException;
}
