package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SelectTupleNode extends SelectConditionNode {
    private int index;
    
    public SelectTupleNode(int index) {
        this.index = index;
    }
    
    public void propagate(Env env, Object userObj) throws EvaluationException {}
    
    public TupleSet evaluate(Env env, TupleSet tSet) throws EvaluationException {
        throw new EvaluationException(this, "Illegal evaluation");
    }
    
    public Tuple evaluate(TupleSet tSet) throws EvaluationException {
        try {
            return tSet.get(index);
        } catch(IndexOutOfBoundsException e) {
            throw new EvaluationException(this, e.getMessage());
        }
    }
}
