package ca.uwaterloo.cs.jgrok.lib;

public class InvocationException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public InvocationException(String msg) {
        super(msg);
    }
    
    public static InvocationException createUnknownFunction(String functionName) {
        return new InvocationException("unknown function: " + functionName);
    }
}
