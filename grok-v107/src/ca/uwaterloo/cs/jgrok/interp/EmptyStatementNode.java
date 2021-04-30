package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class EmptyStatementNode extends StatementNode {
    
    public EmptyStatementNode() {}
    
    public Value evaluate(Env env) throws EvaluationException {
        return Value.EVAL;
    }
    
    public String toString() {
        return ";";
    }
}
