package examples;

import ca.uwaterloo.cs.jgrok.*;
import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.ScriptSource;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

public class RunScript {
    private Env env;
    
    @SuppressWarnings("unused")
    private LineInterp interp;
    
    public RunScript() {
        env = new Env();
        interp = new LineInterp();
        ScriptUnitNode unit = new ScriptUnitNode();
        env.setMainUnit(unit);
        env.pushScope(unit);
    }

    public void run(String script, String argument) {
        Value result;
        Value[] vals;
        
        vals = new Value[2]; 
        vals[0] = new Value(script);
        vals[1] = new Value(argument);
        
        ScriptSource s = new ScriptSource();
        try {
            result = s.invoke(env, vals);
            result.print(env.out);
        } catch(InvocationException e) {
            env.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        RunScript p = new RunScript();
        p.run(args[0], args[1]);
    }
}
