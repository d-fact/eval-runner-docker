package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;
import java.io.FileNotFoundException;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;
import ca.uwaterloo.cs.jgrok.io.ta.TAFileReader;

/**
 * <pre>
 *    void read (string dataFile)
 *    void getta(string dataFile)
 *    void getdb(string dataFile)
 * </pre>
 */
public class Getdb extends Reader {
    
    public Getdb() {
        name = "getdb";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			FactbaseReader	fbReader = new RSFFileReader();
			String			fileName = vals[0].stringValue();
	        
			return load(env, fbReader, fileName);
		}
		return illegalUsage();
    }
}
