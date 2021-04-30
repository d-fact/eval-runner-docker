package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class WhileStatementNode extends StatementNode {
    ExpressionNode expNode;
    StatementNode bodyNode;
    
    /**
     * Constructs WhileStatementNode.
     * @param expNode the expression.
     * @param bodyNode the body statement.
     */
    public WhileStatementNode(ExpressionNode expNode,
                              StatementNode bodyNode) {
        super();
        this.expNode = expNode;
        this.bodyNode = bodyNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value expVal;
        Value stmtVal;
        
        env.pushScope(new LocalScopedNode(env.peepScope()));
        
        try {
            while(true) {
                expVal = expNode.evaluate(env);
                if(expVal.getType() != boolean.class)
                    throw new EvaluationException(expNode, "not boolean");
                if(expVal.booleanValue() == false) break;
                
                stmtVal = bodyNode.evaluate(env);
                if(stmtVal != Value.EVAL) return stmtVal;
            }
        } finally {
            env.popScope();
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("while");
        buffer.append('(');
        buffer.append(expNode);
        buffer.append(')');        
        buffer.append(' ');
        buffer.append(bodyNode);
        
        return buffer.toString();
    }
}
