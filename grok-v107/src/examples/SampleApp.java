package examples;

import ca.uwaterloo.cs.jgrok.*;
import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SampleApp {
    private Env env;
    private LineInterp interp;

    public SampleApp() {
        env = new Env();
        interp = new LineInterp();
        ScriptUnitNode unit = new ScriptUnitNode();
        env.setMainUnit(unit);
        env.pushScope(unit);
    }

    public void addSomething() {
        Scope scp = env.peepScope();
        
        Variable x = new Variable(scp, "x", new Value(5));
        Variable y = new Variable(scp, "y", new Value(7));
        scp.addVariable(x);
        scp.addVariable(y);
        
        EdgeSet eSet = new EdgeSet("call");
        eSet.add("x", "y");
        eSet.add("y", "z");
        Variable call = new Variable(scp, "call", new Value(eSet));
        scp.addVariable(call);
    }

    public void doSomething() {
        interp.evaluate(env, "x+y");
        interp.evaluate(env, "p=call*");
        interp.evaluate(env, "p");
    }

    public static void main(String[] args) {
        SampleApp p = new SampleApp();
        p.addSomething();
        p.doSomething();
    }
}
