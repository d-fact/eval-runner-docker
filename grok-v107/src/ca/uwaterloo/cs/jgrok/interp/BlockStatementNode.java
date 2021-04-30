package ca.uwaterloo.cs.jgrok.interp;

import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.env.Indent;

public class BlockStatementNode extends StatementNode {
    ArrayList<StatementNode> stmtNodes;
    
    public BlockStatementNode() {
        stmtNodes = new ArrayList<StatementNode>(5); 
    }
    
    public void add(StatementNode stmt) {
        stmtNodes.add(stmt);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value stmtVal;
        for(int i = 0; i < stmtNodes.size(); i++) {
            stmtVal = stmtNodes.get(i).evaluate(env);
            if(stmtVal != Value.EVAL) return stmtVal;
        }
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append('{');
        buffer.append("\n");
        
        for(int i = 0; i < stmtNodes.size(); i++) {
            buffer.append(Indent.addIndent(stmtNodes.get(i).toString()));
            if(stmtNodes.get(i) instanceof PrologExpressionNode) {
                buffer.append(';');
            }
            buffer.append("\n");
        }
        
        buffer.append('}');
        
        return buffer.toString();
    }
}
