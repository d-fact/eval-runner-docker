package ca.uwaterloo.cs.jgrok.interp;

public class NotQualifiedNameException extends Exception {
    private static final long serialVersionUID = 1L;
    private String aName = null;

    public NotQualifiedNameException() {
        super("not a qualified name");
    }
    
    public NotQualifiedNameException(String aName) {
        super("not a qualified name");
        this.aName = aName;
    }
    
    public String getQualifiedName() {
        return aName;
    }
    
    public void setQualifiedName(String aName) {
        this.aName = aName;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if(aName != null) {
            buf.append('"');
            buf.append(aName);
            buf.append('"');
            buf.append(' ');
        }
        buf.append("not a qualified name");
        
        return buf.toString();
    }
}
