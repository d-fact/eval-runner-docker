package ca.uwaterloo.cs.jgrok.interp;

public abstract class Operation {
    public abstract Value eval(int op, Value v)
        throws OperationException;
    
    public abstract Value eval(int op, Value left, Value right)
        throws OperationException;
}
