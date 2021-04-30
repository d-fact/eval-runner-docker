package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Value;
import ca.uwaterloo.cs.jgrok.io.FactbaseWriter;
import ca.uwaterloo.cs.jgrok.io.TAFileWriter;


/**
 * Write/append facts to file.
 *
 * <pre>
 * Functions:
 *
 *    void putta(dataFileName)
 *    void putta(dataFileName, setToSave)
 *
 * </pre>
 */
 
 public class Putta extends Writer {
    
    public Putta() 
    {
        name = "putta";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        FactbaseWriter writer = new TAFileWriter();
        
        return put(env, vals, writer);
    }
}
 
