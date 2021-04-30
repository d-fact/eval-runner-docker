package ca.uwaterloo.cs.jgrok.fb;

public class UnknownColumnException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public UnknownColumnException(Column col) {
        super("unknown column: " + col.getName());
    }
}
