package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class CommandPrintNode extends CommandNode {
    ExpressionNode expNode;
    
    public CommandPrintNode(ExpressionNode expNode) {
        super("print");
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Object o;
        Value val;
        val = expNode.evaluate(env);
        o = val.objectValue();
        
        if(o instanceof Value[]) {
            Value[] vals;
            vals = (Value[])o;
            for(int i = 0; i < vals.length; i++) {
                vals[i].print(env.out);
            }
        } else {
            val.print(env.out);
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("print ");
        buffer.append(expNode);
        buffer.append(';');
        
        return buffer.toString();
    }
}
