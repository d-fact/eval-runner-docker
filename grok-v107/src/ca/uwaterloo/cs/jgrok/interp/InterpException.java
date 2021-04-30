package ca.uwaterloo.cs.jgrok.interp;

public class InterpException extends ParseException {
    private static final long serialVersionUID = 1L;
    
    Location l;
    
    public InterpException(Location l, String msg) {
        super(msg);
        this.l = l;
    }
    
    public Location getLocation() {
        return l;
    }
    
    public String getMessage() {
        return l + " : " + super.getMessage();
    }
}
