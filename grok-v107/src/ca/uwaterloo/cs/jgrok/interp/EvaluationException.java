package ca.uwaterloo.cs.jgrok.interp;

import java.io.PrintStream;

public class EvaluationException extends Exception {
    private static final long serialVersionUID = 1L;
    
    protected SyntaxTreeNode node;
    
    public EvaluationException(SyntaxTreeNode node, String msg) {
        super(msg);
        this.node = node;
    }
    
    public EvaluationException(SyntaxTreeNode node, Exception e) {
        super(e.getCause().toString());
        this.node = node;
    }
    
    @Override
    public String toString() {
        return (node.strLocation() + " : " + getMessage());
    }
    
    public void report(PrintStream out) {
        out.println(this);
    }
    
    public SyntaxTreeNode getNode() {
        return this.node;
    }
    
    public static EvaluationException createReservedWordException(VariableNode node) {
        return new EvaluationException(node, node.getName() + " is reserved word"); 
    }
}
