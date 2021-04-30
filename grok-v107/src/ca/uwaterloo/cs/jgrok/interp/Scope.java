package ca.uwaterloo.cs.jgrok.interp;

import java.util.Enumeration;

public interface Scope {
    public void clear();
    public Scope getParent();
    public Enumeration<Variable> allVariables();
    public void addVariable(Variable v);
    public void removeVariable(Variable v);
    public boolean hasVariable(String name);
    public Variable lookup(String name) throws LookupException;
}
