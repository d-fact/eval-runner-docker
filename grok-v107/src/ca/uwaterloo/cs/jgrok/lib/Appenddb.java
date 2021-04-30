package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;
import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;

/**
 * Write/append facts to file.
 *
 * <pre>
 * Functions:
 *    void appenddb(dataFileName, setToSave)
 *
 * </pre>
 */
 
 public class Appenddb extends Function {
    private Env env;
    
    public Appenddb() {
        name = "appenddb";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
        
			String		fileName = vals[0].stringValue();
			NodeSet		set      = vals[1].nodeSetValue();
			Node[]		nodes    = set.getAllNodes();
			Variable	var;
	     
			this.env = env;
	          
			for(int i = 0; i < nodes.length; i++) {
				try {
					var = env.lookup(nodes[i].get());
					if(var.getValue().objectValue() instanceof TupleSet) {
						TupleSet tSet;
						tSet = (TupleSet)var.getValue().objectValue();
						tSet.appendDB(fileName);
					}
				} catch(LookupException e) {
					continue;
				} catch(IOException e2) {
					throw new InvocationException(e2.getMessage());
				}
			}
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "(String dataFileName, NodeSet setToAppend)";
	}
}
