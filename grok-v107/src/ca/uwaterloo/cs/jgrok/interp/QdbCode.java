package ca.uwaterloo.cs.jgrok.interp;

public class QdbCode {
    // Continue to the end.
    public static final int x = 0;
    
    // Continue over the next stop.
    public static final int c = 1;
    
    // Display the next jGrok statement.
    public static final int l = 2;
    
    // Step over the next jGrok statement.
    public static final int n = 3;
    
    public static int get(String code) {
        if(code.equals("x")) return QdbCode.x;
        else if(code.equals("c")) return QdbCode.c;
        else if(code.equals("l")) return QdbCode.l;
        else if(code.equals("n")) return QdbCode.n;
        else return -1;
    }
}
