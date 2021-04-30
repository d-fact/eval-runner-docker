package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SelectExpressionNode extends ExpressionNode implements SelectContext {
    ExpressionNode exp;
    SelectConditionNode cond;
    private Tuple catchedTuple;
    
    public SelectExpressionNode(ExpressionNode exp,
                                SelectConditionNode cond) {
        this.exp = exp;
        this.cond = cond;
    }
    
    public String toString() {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append(exp.toString());
        buf.append('[');
        buf.append(cond.toString());
        buf.append(']');
        
        return buf.toString();
    }
    
    public Tuple getTuple() {
        return catchedTuple;
    }
    
    public void setTuple(Tuple t) {
        catchedTuple = t;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        exp.propagate(env, userObj);
        cond.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value expVal;
        TupleSet tSet;
        TupleSet result;
        
        expVal = exp.evaluate(env);
        if(expVal.objectValue() instanceof TupleSet) {
            tSet = (TupleSet)expVal.objectValue();
            if(cond instanceof SelectTupleNode) {
                return new Value(((SelectTupleNode)cond).evaluate(tSet));
            } else {
                result = cond.evaluate(env, tSet);
                if(tSet.hasName()) result = (TupleSet)result.clone();
                return new Value(result);
            }
        } else {
            String msg;
            msg = ErrorMessage.errIllegalExpression(expVal.getType(), TupleSet.class);
            throw new EvaluationException(exp, msg);
        }
    }
}
