package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class AttrSignNode extends VariableNode implements EvalName {    
    
    public AttrSignNode(String name) {
        super(name);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Variable var;
        
        try {
            var = env.lookup(evalName(env));
            
            if(var.getValue() == null)
                throw new EvaluationException(this, ErrorMessage.errUnresolvable(evalName(env)));
            
        } catch(LookupException e) {
            throw new EvaluationException(this, e.getMessage());
        }
        
        return var.getValue();
    }
    
    public String evalName(Env env) throws EvaluationException {
        return ("@" + name);
    }
    
    public String toString() {
        return ("@" + name);
    }
}
