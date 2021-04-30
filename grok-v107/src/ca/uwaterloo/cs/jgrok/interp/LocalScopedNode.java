package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class LocalScopedNode extends ScopedNode {

    public LocalScopedNode(Scope parent) {
        super(parent);
    }
    
    @Override
    public Variable lookup(String name) throws LookupException {
        // Step 1: look up in the local table.
        Variable var = (Variable)varTbl.get(name);
        // Step 2: if not found, throw exception.
        if(var == null) throw new LookupException(name);
        
        return var;
    }

    @Override
    public Value evaluate(Env env) throws EvaluationException {
        return Value.EVAL;
    }

    @Override
    public void propagate(Env env, Object userObj) throws EvaluationException {
        // do nothing
    }

}
