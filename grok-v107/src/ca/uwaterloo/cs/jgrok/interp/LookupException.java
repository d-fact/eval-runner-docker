package ca.uwaterloo.cs.jgrok.interp;

public class LookupException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String symbolName;
    
    public LookupException(String symbolName) {
        super(symbolName + " unresolvable");
        this.symbolName = symbolName;
    }
    
    public String getSymbolName() {
        return symbolName;
    }
}
