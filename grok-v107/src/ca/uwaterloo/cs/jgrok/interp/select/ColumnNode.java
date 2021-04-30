package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class ColumnNode extends ExpressionNode {
    int col;
    String numSign = null;
    boolean positive = true;
    SelectContext context;
    
    public ColumnNode() {
        numSign = "#";
    }
    
    public ColumnNode(int col) {
        this.col = col;
    }
    
    public int getColumn() {
        return col;
    }
    
    public void setPositive(boolean b) {
        positive = b;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        if(userObj == null) {
            context = null;
            return;
        }
        
        if(userObj instanceof SelectContext) {
            context = (SelectContext)userObj;
        }
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Tuple t;
        
        if(numSign == null) {
            // If not used in the context of a SELECT statement,
            // it is evaluated to an integer.  Otherwise, it is
            // evaluated to a string.

            if(positive) {
                if(context == null)
                    return new Value(col);
                
                if((t = context.getTuple()) == null)
                    throw new EvaluationException(this, ErrorMessage.errNotInterpretable(toString()));
                
                if(col < t.size()) {
                    String s = IDManager.get(t.get(col));
                    if(s == null) return Value.VOID;
                    return new Value(s);
                }
                
                throw new EvaluationException(this, ErrorMessage.errIndexOutOfBounds(col, t.size()));

            } else {
                if(context == null)
                    return new Value(0-col);
                
                if((t = context.getTuple()) == null)
                    throw new EvaluationException(this, ErrorMessage.errNotInterpretable(toString()));
                
                if(col < t.size()) {
                    String s = IDManager.get(t.get(t.size()-1-col));
                    if(s == null) return Value.VOID;
                    return new Value(s);
                }
                
                throw new EvaluationException(this, ErrorMessage.errIndexOutOfBounds(t.size()-1-col, t.size()));
            }
            
        } else {
            // If not used in the context of a SELECT statement,
            // it is evaluated to string "#". Otherwise, it is
            // evaluated to an integer.
            
            if(context == null)
                return new Value(numSign);
            
            if((t = context.getTuple()) == null)
                return new Value(0);
            
            return new Value(t.size());
        }
    }
    
    public String toString() {
        if(numSign == null) {
            if(positive) return "&" + col;
            else return "&-" + col;
        } else return "&#";
    }
}
