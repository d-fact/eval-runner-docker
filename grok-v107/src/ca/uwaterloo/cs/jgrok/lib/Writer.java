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
 *
 *    void putdb(dataFileName)
 *    void putdb(dataFileName, setToSave)
 *
 *    void putta(dataFileName)
 *    void putta(dataFileName, setToSave)
 *
 *    void appendta(dataFileName, setToSave)
 *    void appenddb(dataFileName, setToSave)
 *
 * </pre>
 */
 
 public abstract class Writer extends Function {
    
    public Writer() {
    }
       
    protected Value put(Env env, Value[] vals, FactbaseWriter writer) throws InvocationException 
    {
        Variable var;
        Class<?> type;
        String fileName;
        Factbase fb;
        
        switch (vals.length) {
        case 1:
			fileName = vals[0].stringValue();
			fb = new Factbase();
			if(vals.length == 1) {
				ScriptUnitNode unit;
				Enumeration<Variable> enm;
	            
				unit = env.traceScriptUnit();
				enm = unit.allVariables();
	            
				while(enm.hasMoreElements()) {
					var = enm.nextElement();
					type = var.getType();
					if(type == EdgeSet.class)
						fb.addSet((EdgeSet)var.getValue().objectValue());
					else if(type == TupleSet.class)
						fb.addSet((TupleSet)var.getValue().objectValue());
			}   }
			break;
        case 2:
 			fileName     = vals[0].stringValue();
            NodeSet set  = vals[1].nodeSetValue();
            Node[] nodes= set.getAllNodes(); 
   			fb = new Factbase();
            
            for(int i = 0; i < nodes.length; i++) {
                try {
                    var = env.lookup(nodes[i].get());
                    type = var.getType();
                    if(type == EdgeSet.class)
                        fb.addSet((EdgeSet)var.getValue().objectValue());
                    else if(type == TupleSet.class)
                        fb.addSet((TupleSet)var.getValue().objectValue());
                } catch(LookupException e) {
                    continue;
                }
            }
            break;
        default:
			return illegalUsage();
        }
        
        try {
            writer.write(fileName, fb);
        } catch(IOException e) {
            throw new InvocationException(e.getMessage());
        }
        
        return Value.VOID;
    }

	public String usage()
	{	
		return "void " + name + "(String dataFileName [, NodeSet setToSave])";
	}
}
