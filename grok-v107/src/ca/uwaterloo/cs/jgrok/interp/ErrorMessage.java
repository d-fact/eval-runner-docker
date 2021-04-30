package ca.uwaterloo.cs.jgrok.interp;

public class ErrorMessage {
    
    public static String errIndexOutOfBounds(int index, int bounds) {
        return "index " + index + " out of bounds 0.." + (bounds-1);
    }
    
    public static String errUnresolvable(String name) {
        return name + " unresolvable";
    }

    public static String errNotInterpretable(String expr) {
        return expr + " not interpretable";
    }

    public static String errIllegalExpression() {
        return "illegal expression";
    }
    
    public static String errIllegalExpression(Class<?> type, Class<?> expected) {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append("illegal expression: ");
        buf.append(Type.findName(type));
        buf.append(" encountered, ");
        buf.append(Type.findName(expected));
        buf.append(" expected");
        
        return buf.toString();
    }
    
    public static String errExpect(Class<?> wrong, Class<?> right) {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append("illegal expression: ");
        buf.append(Type.findName(wrong));
        buf.append(" encountered, ");
        buf.append(Type.findName(right));
        buf.append(" expected");
        
        return buf.toString();
    }
    
    public static String errUnsupportedOperation(int op, Class<?> t) {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append("operation not supported: ");
        buf.append(Operator.key(op));
        buf.append(' ');
        buf.append(Type.findName(t));
        
        return buf.toString();
    }
    
    public static String errUnsupportedOperation(Class<?> t, int op) {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append("operation not supported: ");
        buf.append(Type.findName(t));
        buf.append(' ');
        buf.append(Operator.key(op));
        
        return buf.toString();
    }

    public static String errUnsupportedOperation(int op, Class<?> t1, Class<?> t2) {
        StringBuffer buf;
        
        buf = new StringBuffer();
        buf.append("operation not supported: ");
        buf.append(Type.findName(t1));
        buf.append(' ');
        buf.append(Operator.key(op));
        buf.append(' ');
        buf.append(Type.findName(t2));
        
        return buf.toString();
    }
}
