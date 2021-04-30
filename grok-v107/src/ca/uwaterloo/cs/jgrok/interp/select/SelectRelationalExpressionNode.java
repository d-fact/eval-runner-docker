package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

import java.util.regex.PatternSyntaxException;

public class SelectRelationalExpressionNode extends SelectContextNode {
    int op;
    ExpressionNode left;
    ExpressionNode right;
    
    public SelectRelationalExpressionNode(int op,
                                          ExpressionNode left,
                                          ExpressionNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }
    
    public String toString() {
        StringBuffer buf;
        buf = new StringBuffer();
        
        buf.append(left.toString());
        buf.append(' ');
        buf.append(Operator.key(op));
        buf.append(' ');
        buf.append(right.toString());
        
        return buf.toString(); 
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        left.propagate(env, userObj);
        right.propagate(env, userObj);
    }
    
    public TupleSet evaluate(Env env, TupleSet tSet)
        throws EvaluationException {
        if(tSet.size() == 0) return tSet;
        
        try {
            left.propagate(env, this);
            right.propagate(env, this);
            
            if(op == Operator.IN) {
                return evalSetMembership(env, tSet);
            } else {
                return evalRelationalMath(env, tSet);
            }
        } finally {
            setTuple(null);
            left.propagate(env, null);
            right.propagate(env, null);
        }
    }
    
    private TupleSet evalSetMembership(Env env, TupleSet tSet)
            throws EvaluationException {
        NodeSet set;
        TupleSet result;
        TupleList tList;
        Value vLeft, vRight;
        
        vRight = right.evaluate(env);
        Object o = vRight.objectValue();
        
        if(o instanceof NodeSet) {
            set = (NodeSet)o;
        } else {
            throw new EvaluationException(right, "not a set");
        }
        
        result = tSet.newSet();
        tList = tSet.getTupleList();
        
        int count = tList.size();
        for (int i = 0; i < count; i++) {
            setTuple(tList.get(i));
            vLeft = left.evaluate(env);
            
            if(vLeft == Value.VOID) continue;
            
            if(set.contain(vLeft.toString()))
                result.add(getTuple());
            
        }
        
        return result;
    }
    
    private TupleSet evalRelationalMath(Env env, TupleSet tSet)
        throws EvaluationException {
        TupleSet result;
        TupleList tList;
        Value val, vLeft, vRight;
        Operation operation = null;
        
        result = tSet.newSet();
        tList = tSet.getTupleList();
        
        int count = tList.size();
        for (int i = 0; i < count; i++) {
            setTuple(tList.get(i));
            vLeft = left.evaluate(env);
            vRight = right.evaluate(env);
            
            if(vLeft == Value.VOID) continue;
            if(vRight == Value.VOID) continue;
            
            try {
                val = ValueMath.eval(op, vLeft, vRight);
                if(val.booleanValue()) result.add(getTuple());
            } catch(PatternSyntaxException e) {
                throw new EvaluationException(this, e.getMessage());
            } catch(EvaluationException e) {
                try {
                    if(operation == null)
                        operation = getOperation(vLeft, vRight);
                    val = operation.eval(op, vLeft, vRight);
                    if(val.booleanValue()) result.add(getTuple());
                } catch(Exception ex) {
                    throw new EvaluationException(this, ex.getMessage());
                }
            }
        }
        
        return result;
    }
    
    private Operation getOperation(Value vLeft, Value vRight)
        throws EvaluationException {
        TypeOperation TOP;
        TOP = TypeOperation.analyze(op, vLeft.getType(), vRight.getType());
        
        if(TOP != null) {
            try {
                return TOP.getOperation();
            } catch(Exception e) {
                throw new EvaluationException(this, e.getMessage());
            }
        } else {
            String err;
            err = ErrorMessage.errUnsupportedOperation(op, vLeft.getType(), vRight.getType());
            throw new EvaluationException(this, err);
        }
    }
}
