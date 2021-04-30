package examples;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Value;
import ca.uwaterloo.cs.jgrok.lib.*;

public class ExampleFunction extends Function {

    public ExampleFunction() {
        name = "tt";         // function name/alias 
        type = void.class;   // function return type.
        paramTypes = null;   // any number of params.
    }
    
    public Value invoke(Env env, Value[] vals)
        throws InvocationException {
        
        for(int i = 0; i < vals.length; i++) {
            env.out.println(vals[i].getType().getName());
        }
        
        return Value.VOID;
    }
}
