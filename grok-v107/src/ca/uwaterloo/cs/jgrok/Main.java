package ca.uwaterloo.cs.jgrok;

import java.io.*;

import ca.uwaterloo.cs.jgrok.util.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.env.Env;

public class Main {
    private Env env;
    
    public Main() {
        env = new Env();
    }
    
    public void shell() {
        env.out.println(Version.authorsAndCopyright());
        Shell sh = new Shell();
        sh.shellEvaluate();
    }
    
    public void debug(String[] args) {
        env.out.println(Version.authorsAndCopyright());
        Shell sh = new Shell();
        sh.debugEvaluate(args);
        System.exit(0);
    }
    
    public void exeFile(String[] args) {
        Interp interp;
        Timing timing;
        
        timing = new Timing();
        timing.start();
        
        File file = new File(args[0]);
        try {
            interp = new Interp(file);
            interp.fileEvaluate(env, args);
        } catch(FileNotFoundException ex) {
            System.err.println("Error: File \"" + file + "\" not found!");
        }
        
        timing.stop();
        
        try {
            String showTiming = System.getProperty("timing");
            if(showTiming != null && showTiming.equalsIgnoreCase("true"))
            {
                System.out.println("Total time = " + timing.getTime());
            }
        } catch(Exception e) {}
        
        System.exit(0);
    }
    
    public static void main(String[] args) {
        Main program;
        program = new Main();
        
        if(args.length == 0) {
            program.shell();
        } else if(args[0].equals("-debug")) {
            if(args.length == 1) {
                program.shell();
            } else {
                String[] debugArgs;
                debugArgs = new String[args.length-1];
                System.arraycopy(args, 1, debugArgs, 0, args.length-1);
                program.debug(debugArgs);
            }
        } else {
            program.exeFile(args);
        }
    }
}
