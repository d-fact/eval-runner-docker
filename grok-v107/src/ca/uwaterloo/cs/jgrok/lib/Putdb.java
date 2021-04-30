package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;

/**
 * Write/append facts to file.
 *
 * <pre>
 * Functions:
 *
 *    void putdb(dataFileName)
 *    void putdb(dataFileName, setToSave)
 *
 * </pre>
 */
 
 public class Putdb extends Writer {
    
    public Putdb() 
    {
        name = "putdb";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        FactbaseWriter writer = new RSFFileWriter();
        
        return put(env, vals, writer);
    }
}
