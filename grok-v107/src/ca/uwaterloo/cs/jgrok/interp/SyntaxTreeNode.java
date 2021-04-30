package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public abstract class SyntaxTreeNode {
    protected Location location;
    protected boolean evaluated;
    
    public SyntaxTreeNode() {
        location = null;
        evaluated = false;
    }
    
    public String strLocation() {
        if(location == null) return "";
        else return location.toString();
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location l) {
        this.location = l;
    }
    
    public void accept(SyntaxTreeNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public abstract void propagate(Env env, Object userObj)
            throws EvaluationException;
    
    public abstract Value evaluate(Env env)
        throws EvaluationException;
}

