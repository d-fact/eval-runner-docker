package ca.uwaterloo.cs.jgrok.lib;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *     NodeSet flist()
 *
 * </pre> 
 */
public class Flist extends Function {
    
    public Flist() {
        name = "flist";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			String[] names;
			FunctionLib fLib;
			NodeSet set = new NodeSet();
	        
			fLib = FunctionLibManager.getRootLib();
			names = fLib.getFunctionNames();
			Arrays.sort(names);
	        
			for(int i = 0; i < names.length; i++) {
				set.add(names[i]);
			}
	        
			return new Value(set);
		}
		return illegalUsage();
    }

    public String usage()
    {
		return "NodeSet " + name + "()";
	}    
}
