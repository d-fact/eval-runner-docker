package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class ExpStatementNode extends StatementNode {
    ExpressionNode expNode;
    
    public ExpStatementNode(ExpressionNode expNode) {
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val = expNode.evaluate(env);
        Object o = val.objectValue();
        
        if(o instanceof Value[]) {
            Value[] vals;
            vals = (Value[])o;
            for(int i = 0; i < vals.length; i++) {
                if(vals[i] != Value.VOID)
                    vals[i].print(env.out);
            }
        } else {
            if(val != Value.VOID)
                val.print(env.out);
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        return expNode.toString() + ";";
    }
}
