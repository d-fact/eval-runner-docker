package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Type;
import ca.uwaterloo.cs.jgrok.interp.Value;

public abstract class Function {
    protected String	name;	// Name of this function
    
    protected Function() {
        name = null;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
  
    /**
     * Gets function usage.
     */
    
    public abstract Value invoke(Env env, Value[] vals) throws InvocationException;
        
	abstract public String usage();
    
    public Value illegalUsage() throws InvocationException
    {
		throw new InvocationException("Illegal Usage:\n" + usage());
	} 
	    
	public String toString() {
        return usage();
    }
}
