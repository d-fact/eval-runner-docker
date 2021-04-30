package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class ReturnStatementNode extends StatementNode {
    ExpressionNode expNode;
    
    public ReturnStatementNode() {}
    
    public ReturnStatementNode(ExpressionNode expNode) {
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        if(expNode == null)
            return Value.VOID;
        else
            return expNode.evaluate(env);
    }
    
    public String toString() {
        if(expNode == null)
            return "return;";
        else
            return "return " + expNode + ";";
    }
}
