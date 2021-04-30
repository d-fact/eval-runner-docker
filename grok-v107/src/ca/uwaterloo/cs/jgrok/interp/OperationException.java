package ca.uwaterloo.cs.jgrok.interp;

public class OperationException extends Exception {
    private static final long serialVersionUID = 1L;

    public OperationException(String msg) {
        super(msg);
    }
}
