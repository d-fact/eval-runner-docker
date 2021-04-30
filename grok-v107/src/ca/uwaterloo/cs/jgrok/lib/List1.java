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
 *
 * </pre> 
 */
public class List1 extends Function {
    
    public List1() {
        name = "list";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 0:
			NodeSet set = new NodeSet();        
			Scope scp = env.peepScope();
			Enumeration<Variable> enm = scp.allVariables();
			ArrayList<String> list = new ArrayList<String>();
	        
			Variable var;
			while(enm.hasMoreElements()) {
				var = enm.nextElement();
				list.add(var.getName());
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
