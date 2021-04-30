package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * void run(...)
 */
public class Run extends Function {
    
    public Run() 
    {
        name = "run";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        if(vals.length < 1) {
            env.out.println("[Warning] Run nothing");
            return Value.VOID;
        }
        
        String[] cmdArray = new String[vals.length];
        for(int i = 0; i < cmdArray.length; i++) {
            cmdArray[i] = vals[i].stringValue();
        }
        
        ExecRunnable run = new ExecRunnable(cmdArray);
        new Thread(run).start();
       
        return Value.VOID;
    }
    
    public String usage()
    {
		return "void " + name + "(String program [, String arg]*)";
	}
}

class ExecRunnable implements Runnable {
    private String[] cmdArray;

    ExecRunnable(String[] cmdArray) {
        this.cmdArray = cmdArray;
    }
    
    public void run() {
        Process p;
        
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
                System.out.println(line);
            }
            
            reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while((line = reader.readLine()) != null) {
                System.err.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
