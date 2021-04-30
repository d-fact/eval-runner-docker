package ca.uwaterloo.cs.jgrok.lib;

import java.util.*;
import java.io.PrintStream;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;

/* This provides the mappings to all the functions in a single namespace */

public class FunctionLib {
    private String name = null;
    private Hashtable<String,Function> funEntries = new Hashtable<String,Function>();
    
    public FunctionLib(String name) 
    {
        this.name = name;
    }
         
    public String getName() {
        return this.name;
    }
    
    public void register(Function f) 
	{
		funEntries.put(f.getName(), f);
    }
    
    public void register(String functionName, String functionClass)
    {
    }
    
    public String[] getFunctionNames() {
        Set<String> keySet = funEntries.keySet();
        String[] names = new String[keySet.size()];
        keySet.toArray(names);
        return names;
    }
            
    public Function find(String funcName) 
    {
        return(funEntries.get(funcName));
    }
    
	public void getUse(EdgeSet edgeSet)
    {
		int						size      = funEntries.size();
		Function[]			    functions = new Function[size];
		Enumeration<Function>	en        = funEntries.elements();
		int						i, j;
		String					name1;
		Function				function, function1;
		Class<?>				type;
		
		for (i = 0; en.hasMoreElements(); ++i) {
			function = en.nextElement();
			name1    = function.getName();
			for (j = i; j > 0; --j) {
				function1 = functions[j-1];
				if (name1.compareToIgnoreCase(function1.getName()) >= 0) {
					break;
				}
				functions[j] = function1;
			}
			functions[j] = function; 
		}

		for (i = 0; i < size; ++i) {
			function = functions[i];
			type     = function.getClass();
			edgeSet.add(name + "." + function.getName(), type.getName());
	}	}
}
