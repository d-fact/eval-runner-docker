package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class ColumnAttributeNode extends ColumnNode {
    String attName;
    EdgeSet attSet;
    SelectContext context;
    
    public ColumnAttributeNode(int col, String attName) {
        super(col);
        this.attName = attName;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        if(userObj == null) {
            attSet = null;
            context = null;
            return;
        }
        
        if(userObj instanceof SelectContext) {
            context = (SelectContext)userObj;
            try {
                String name = "@" + attName;
                Variable var = env.lookup(name);
                if(var.getType() == EdgeSet.class) {
                    attSet = (EdgeSet)var.getValue().objectValue();
                } else {
                    throw new EvaluationException(this, "attribute was not eset: " + name);
                }
            } catch(LookupException e) {
                throw new EvaluationException(this, e.getMessage());
            }
        }
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Tuple t = context.getTuple();
        if(col < t.size()) {
            String s = attSet.getAttribute(t.get(col));
            if(s == null) return Value.VOID;
            return new Value(s);
        }
        throw new EvaluationException(this, ErrorMessage.errIndexOutOfBounds(col, t.size()));
    }
    
    public String toString() {
        return ("&" + col + "." + attName);
    }
}
