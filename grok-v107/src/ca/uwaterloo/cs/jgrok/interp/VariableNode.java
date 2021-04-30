package ca.uwaterloo.cs.jgrok.interp;

import java.lang.reflect.Field;
import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.lib.Function;

public class VariableNode extends ExpressionNode implements EvalName {
    String name;
    
    public VariableNode(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {}
    
    public Value evaluate(Env env) throws EvaluationException {
        Variable var;
        
        try {
            var = env.lookup(name);
            
            if(var.getValue() == null)
                throw new EvaluationException(this, ErrorMessage.errUnresolvable(name));
            
            return var.getValue();
        } catch(LookupException e) {
            try {
                Function function;
                Value[] argValues = new Value[0];
                function = env.lookupFunction(name);
                
                try {
                    return function.invoke(env, argValues);
                } catch(Exception inv) {
                    throw new EvaluationException(this, name + "(): " + inv.getMessage());
                }
            } catch(LookupException e2) {
                // Throw exception by e not by e2.
                throw new EvaluationException(this, e.getMessage());
            }
        }
    }
    
    @Override
    public String evalName(Env env) throws EvaluationException {
        return name;
    }
    
    public String toString() {
        return name;
    }

    Value evalClassField(Env env, Class<?> clazz)
        throws NoSuchFieldException, EvaluationException {
        String fName = evalName(env);
        
        if(fName.equals("class")) {
            return new Value(clazz);
        }
        
        Field f = clazz.getDeclaredField(fName);
        
        try {
            return new Value(f.get(null), f.getType());
        } catch (IllegalAccessException e) {
            throw new EvaluationException(this, e);
        }
    }
    
    Value evalInstanceField(Env env, Object obj)
        throws NoSuchFieldException, EvaluationException {
        String fName = evalName(env);
        Field f = obj.getClass().getDeclaredField(fName);
        
        try {
            return new Value(f.get(obj), f.getType());
        } catch (IllegalAccessException e) {
            throw new EvaluationException(this, e);
        }
    }
}
