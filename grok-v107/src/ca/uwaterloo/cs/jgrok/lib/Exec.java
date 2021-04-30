package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * void exec(...)
 */
public class Exec extends Function {
    
    public Exec() {
        name = "exec";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        Process p;

        if(vals.length < 1) {
            env.out.println("[Warning] Execute nothing");
            return Value.VOID;
        }
        
        String[] cmdArray = new String[vals.length];
        for(int i = 0; i < cmdArray.length; i++) {
            cmdArray[i] = vals[i].objectValue().toString();
        }
 
        try {
            if(cmdArray.length == 1) {
                p = Runtime.getRuntime().exec(cmdArray[0]);
            } else {
                p = Runtime.getRuntime().exec(cmdArray);
            }
            
            String line;
            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((line = reader.readLine()) != null) {
                env.out.println(line);
            }
            
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = reader.readLine()) != null) {
                env.err.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace(env.err);
        }
        return Value.VOID;
	}
	
	public String usage()
	{
		return "void " + name + "(String program [, String arg]*)";
	}
}

