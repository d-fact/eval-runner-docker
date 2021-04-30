package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class FunctionNameNode extends SyntaxTreeNode implements EvalName {
    VariableNode nameExp;
    
    public FunctionNameNode(VariableNode nameExp) {
        this.nameExp = nameExp;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        nameExp.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        return new Value(evalName(env));
    }
    
    public String evalName(Env env) throws EvaluationException {
        return nameExp.evalName(env);
    }

    public String toString() {
        return nameExp.toString();
    }
}
