package ca.uwaterloo.cs.jgrok.interp;

import java.util.Hashtable;
import java.util.Enumeration;

public abstract class ScopedNode extends SyntaxTreeNode implements Scope {
    
    /**
     * The parent scope.
     */
    protected Scope parent;
    
    /**
     * Maps name to variable.
     */
    protected Hashtable<String, Variable> varTbl;
    
    /**
     * Construct a new ScopedNode.
     */
    public ScopedNode(Scope parent) {
        this.parent = parent;
        varTbl = new Hashtable<String, Variable>();
    }
    
    /**
     * Clears this scope.
     */
    public void clear() {
        varTbl.clear();
    }
    
    /**
     * Gets the parent scope.
     */
    public Scope getParent() {
        return parent;
    }
    
    /**
     * Enumerates all variables in this scope.
     */
    public Enumeration<Variable> allVariables() {
        return varTbl.elements();
    }
    
    /**
     * Adds a variable into this scope.
     * @param v the variable to add.
     */
    public void addVariable(Variable v) {
        if(v != null) {
            varTbl.put(v.getName(), v);
        }
    }
    
    /**
     * Removes a variable from this scope.
     * @param v the variable to remove.
     */
    public void removeVariable(Variable v) {
        if(v != null) {
            varTbl.remove(v.getName());
        }
    }
    
    /**
     * Tests if this scope has a variable with the
     * specified <code>name</code>.
     * @param name the variable's name.
     */
    public boolean hasVariable(String name) {
        return varTbl.containsKey(name);
    }
    
    /**
     * Looks up a variable from this scope.
     * @param name the name of the variable to look up.
     * @throws LookupException if no variable was found.
     */
    public abstract Variable lookup(String name)
        throws LookupException;
}
