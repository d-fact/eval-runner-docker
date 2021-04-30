package ca.uwaterloo.cs.jgrok.interp;

import java.util.ArrayList;
import java.io.PrintStream;

public class PrintVisitor implements SyntaxTreeNodeVisitor {
    private PrintStream out;
    
    private ArrayList<StringBuffer> lines;
    
    public PrintVisitor(PrintStream out) {
        this.out = out;
        lines = new ArrayList<StringBuffer>();
    }
    
    @Override
    public void visit(SyntaxTreeNode nd) {
        if(nd instanceof ForStatementNode) {
            process((ForStatementNode)nd);
        } else if(nd instanceof StatementNode) {
            process((StatementNode)nd);
        } else {
            StringBuffer buf = getBuffer(nd.getLocation().getLine());
            buf.append(nd.toString());
        }
        
        for(int i = nd.getLocation().getLine(); i < lines.size(); i++) {
            out.println(getLine(i));
        }
    }
    
    public String getLine(int lineNum) {
        getBuffer(lineNum);
        return lines.get(lineNum).toString();
    }
    
    public void setLine(int lineNum, String line) {
        getBuffer(lineNum);
        lines.set(lineNum, new StringBuffer(line));
    }
    
    private StringBuffer getBuffer(int lineNum) {
        for(int i = lines.size(); i <= lineNum; i++) {
            lines.add(new StringBuffer());
        }
        return lines.get(lineNum);
    }
    
    private void fillIndent(StringBuffer buffer, SyntaxTreeNode nd) {
        for(int i = buffer.length(); i < nd.getLocation().getColumn()-1; i++) {
            buffer.append(' ');
        }
    }
    
    private void process(StatementNode nd) {
        StringBuffer buf = getBuffer(nd.getLocation().getLine());
        buf.append(nd);
    }
    
    private void process(ForStatementNode nd) {
        StringBuffer buf = getBuffer(nd.getLocation().getLine());
        
        fillIndent(buf, nd);
        
        buf.append("for");
        buf.append(' ');
        buf.append(nd.varNode);
        buf.append(' ');
        buf.append("in");
        buf.append(' ');
        buf.append(nd.expNode);
        
        if(nd.bodyNode instanceof BlockStatementNode) {
            process(nd, (BlockStatementNode)nd.bodyNode);
        } else {
            process(nd.bodyNode);
        }
    }
    
    private void process(StatementNode parent, BlockStatementNode nd) {
        StringBuffer buf;
        
        buf = getBuffer(nd.getLocation().getLine());
        fillIndent(buf, nd);
        buf.append('{');
        
        for(int i = 0; i < nd.stmtNodes.size(); i++) {
            process(nd.stmtNodes.get(i));
        }
        
        buf = getBuffer(lines.size());
        buf.append('}');
    }
}
