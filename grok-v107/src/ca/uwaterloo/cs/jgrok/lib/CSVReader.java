package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;

import java.io.FileNotFoundException;

/**
 * void getcsv(String fileName)
 */
public class CSVReader extends Function {
    
    public CSVReader() {
        name = "getcsv";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			String fileName;
			FactbaseReader fbReader;
	        
			fileName = vals[0].stringValue();
			fbReader = new CSVFileReader();
	        
			Factbase fb;
			try {
				fb = fbReader.read(fileName);
			} catch (FileNotFoundException e) {
				throw new InvocationException(fileName + " not found");
			}
	        
			Scope scp;
			Variable var;
			TupleSet set;
	        
			set = fb.getSet("CSVDATA");
			scp = env.peepScope();
	        
			try {
				var = scp.lookup(set.getName());
				var.setValue(new Value(set));
			} catch(LookupException e) {
				var = new Variable(scp, set.getName(), new Value(set));
				scp.addVariable(var);
			}
	            
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "(String file)";
	}
}
