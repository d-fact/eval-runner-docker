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
 * </pre>
 */
public class Read extends Reader {
    
    public Read() {
        name = "read";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			String			fileName = vals[0].toString();
			FactbaseReader	fbReader;
	         
			if(fileName.endsWith(".ta")) {
				fbReader = new TAFileReader();
			} else if(fileName.endsWith(".rsf")) {
				fbReader = new RSFFileReader();
			} else {
				throw new InvocationException("illegal file: " + fileName);
			}
			return load(env, fbReader, fileName);
		}
		return illegalUsage();
    }
}
