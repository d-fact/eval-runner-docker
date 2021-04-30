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
 *     NodeSet list()
 *     NodeSet flist()
 *     NodeSet relnames()
 *
 * </pre> 
 */
public class Relnames extends Function {
    
    public Relnames() {
        name = "relnames";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			NodeSet set = new NodeSet();
			Scope scp = env.peepScope();
			Enumeration<Variable> enm = scp.allVariables();
			ArrayList<String> list = new ArrayList<String>();
	        
			Class<?> type;
			Variable var;
			while(enm.hasMoreElements()) {
				var = enm.nextElement();
				type = var.getType();
				if(type == EdgeSet.class ||
				   type == TupleSet.class ) {
					list.add(var.getName());
				}
			}
	        
			// Sort names
			String[] names = new String[list.size()];
			list.toArray(names);
			Arrays.sort(names);
	        
			// Save names to set
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
