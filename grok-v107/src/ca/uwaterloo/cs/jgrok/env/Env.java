package ca.uwaterloo.cs.jgrok.env;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.interp.LookupException;
import ca.uwaterloo.cs.jgrok.interp.Scope;
import ca.uwaterloo.cs.jgrok.interp.ScriptUnitNode;
import ca.uwaterloo.cs.jgrok.interp.Variable;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.FunctionLib;
import ca.uwaterloo.cs.jgrok.lib.FunctionLibManager;

public class Env {
    public InputStream in  = System.in;
    public PrintStream out = System.out;
    public PrintStream err = System.err;
    
    // Prompt text for JGrok interpreter.
    public static String promptText = ">> ";
    
    /**
     * A stack of scopes.
     */
    private ArrayList<Scope> scopes;
    
    /**
     * The main script unit.
     */
    private ScriptUnitNode mainUnit;
    
    /**
     * The associated tracing utility. 
     */
    private Tracing trcUtility;
    
    /**
     * Creates a new JGrok environment.
     */
    public Env() {
        scopes      = new ArrayList<Scope>(10);
        trcUtility  = new Tracing();
    }
    
    /**
     * Gets the tracing utility.
     */
    public Tracing getTracing() {
        return trcUtility;
    }
    
    /**
     * Gets the main script unit.
     */
    public ScriptUnitNode getMainUnit() {
        return mainUnit;
    }
    
    /**
     * Sets the main script unit.
     */
    public void setMainUnit(ScriptUnitNode unit) {
        mainUnit = unit;
    }
    
    /**
     * Removes all scopes on the stack.
     */
    public void removeAllScopes() {
        scopes.clear();
    }

    /**
     * Pops the scope on top of the stack.
     */
    public Scope popScope() {
        return (Scope)scopes.remove(scopes.size()-1);
    }
    
    /**
     * Pushes a scope onto the stack.
     */
    public void pushScope(Scope scp) {
        scopes.add(scp);
    }
    
    /**
     * Peeps the scope on top of the stack.
     */
    public Scope peepScope() {
        return (Scope)scopes.get(scopes.size()-1);
    }
    
    /**
     * Traces the script unit on top of the stack.
     */
    public ScriptUnitNode traceScriptUnit() {
        Scope scp = peepScope();
        if(scp instanceof ScriptUnitNode) {
            return (ScriptUnitNode)scp;
        } else {
            while(scp != null) {
                scp = scp.getParent();
                if(scp instanceof ScriptUnitNode)
                    return (ScriptUnitNode)scp;
            }
            return null;
        }
    }
    
    /**
     * Looks up a variable.
     * @throws LookupException if no variable can be found.
     */
    public Variable lookup(String name) throws LookupException {
        Variable var = null;
        LookupException lex = null;
        
        for (int i = scopes.size(); --i >= 0; ) {
            Scope scp = (Scope)scopes.get(i);
            
            try {
                var = scp.lookup(name);
                return var;
            } catch(LookupException e) {
                if(lex == null) lex = e;
            }
        }
        
        if (lex != null) {
			throw lex;
		}
        return var;
    }

    /**
     * Looks up a Function in the root FunctionLib.
     * @param functionName the name of Function to look up.
     * @param paramTypes the parameter types of the Function.
     */
    public Function lookupFunction(String functionName) throws LookupException 
    {
        Function aFunction = FunctionLibManager.getRootLib().find(functionName);
        if(aFunction != null) {
			return aFunction;
		}
        throw new LookupException("function " + functionName);
    }
    
    /**
     * Looks up a Function in the root FunctionLib.
     * @param functionName the name of Function to look up.
     * @param paramTypes the parameter types of the Function.
     */
    public Function lookupFunction(String libName, String functionName) throws LookupException 
    {
        FunctionLib lib = FunctionLibManager.findLib(libName);
        if(lib != null) {
            Function aFunction = lib.find(functionName);
            if(aFunction != null) {
				return aFunction;
        }	}
        throw new LookupException("function " + libName + "." + functionName);
    }
    
    /**
     * Tests if a given name is JGrok reserved word.
     * A reserved word cannot be assignment left in JGrok.
     */
    public static boolean isReservedWord(String name) {
        if(name.equals("class") ||
           name.equals("JGrok")) {
            return true;
        }
        
        if(name.equals("int")   ||
           name.equals("long")  ||
           name.equals("byte")  ||
           name.equals("short") ||
           name.equals("float") ||
           name.equals("double")||
           name.equals("boolean") ) {
            return true;
        }
        
        return false;
    }
}
