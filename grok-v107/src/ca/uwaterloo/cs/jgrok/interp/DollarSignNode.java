package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class DollarSignNode extends VariableNode implements EvalName {
    
    public DollarSignNode(String name) {
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
            
            return var.getValue();
        } catch(LookupException e) {
            throw new EvaluationException(this, e.getMessage());
        }
    }
    
    public String evalName(Env env) throws EvaluationException {
        Value val;
        Variable var;
        
        try {
            var = env.lookup(toString());
            
            if(var.getValue() == null)
                throw new EvaluationException(this, ErrorMessage.errUnresolvable(toString()));
            
            return var.getName();
        } catch(LookupException e1) {
            try {
                var = env.lookup(name);
                val = var.getValue();
                
                if(val == null)
                    throw new EvaluationException(this, ErrorMessage.errUnresolvable(name));
                
                if(val.getType() != String.class) {
                    throw new EvaluationException(this, "String expected after $");
                }
                return val.toString();
            } catch(LookupException e2) {
                // throw e1 not e2
                throw new EvaluationException(this, e1.getMessage());
            }
        }
    }
    
    public String toString() {
        return ("$" + name);
    }
}
