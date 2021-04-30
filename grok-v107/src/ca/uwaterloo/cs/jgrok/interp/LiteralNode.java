package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class LiteralNode extends ExpressionNode {
    Value value;
    
    public LiteralNode(int i) {
        value = new Value(i);
    }
    
    public LiteralNode(long l) {
		value = new Value(l);
	}

    public LiteralNode(float f) {
        value = new Value(f);
    }
    
    public LiteralNode(double d) {
		value = new Value(d);
	}
    
    public LiteralNode(String s) {
        value = new Value(s);
    }
    
    public LiteralNode(boolean b) {
        value = new Value(b);
    }

    public void propagate(Env env, Object userObj)
        throws EvaluationException {}
    
    public Value evaluate(Env env) throws EvaluationException {
        return value;
    }
    
    public String toString() {
        if(value.getType() == String.class)
            return "\""+value+"\"";
        else
            return value.toString();
    }
}
