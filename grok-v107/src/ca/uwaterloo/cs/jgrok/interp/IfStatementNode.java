package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class IfStatementNode extends StatementNode {
    ExpressionNode expNode;
    StatementNode thenNode;
    StatementNode elseNode;
    
    public IfStatementNode(ExpressionNode expNode,
                           StatementNode thenNode) {
        this(expNode, thenNode, null);
    }
    
    public IfStatementNode(ExpressionNode expNode,
                           StatementNode thenNode,
                           StatementNode elseNode) {
        this.expNode = expNode;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val = expNode.evaluate(env);
        if(val.getType() != boolean.class)
            throw new EvaluationException(expNode, "not evaluated to boolean");
        
        if(val.booleanValue()) {
            return thenNode.evaluate(env);
        } else {
            if(elseNode != null)
                return elseNode.evaluate(env);
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("if ");
        buffer.append(expNode);
        buffer.append(' ');
        buffer.append(thenNode);
        
        if(elseNode != null) {
            buffer.append("\n");
            buffer.append("else");
            buffer.append(' ');
            buffer.append(elseNode);
        }
        
        return buffer.toString();
    }
}
