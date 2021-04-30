package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public interface EvalName {
    
    public String evalName(Env env)
        throws EvaluationException;

}
