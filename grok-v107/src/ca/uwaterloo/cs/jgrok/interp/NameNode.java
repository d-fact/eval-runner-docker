package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class NameNode extends SyntaxTreeNode {
    private String name;
    
    public NameNode(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {}
    
    public Value evaluate(Env env) throws EvaluationException {
        return Value.EVAL;
    }

    public String toString() {
        return getName();
    }
}
