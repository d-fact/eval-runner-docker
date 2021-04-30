package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public abstract class StatementNode extends SyntaxTreeNode {
    public StatementNode() {}
    
    public Location shortFormLocation() {
        return getLocation().shortForm();
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {}
}
