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
 *     NodeSet setnames()
 *
 * </pre> 
 */
public class Setnames extends Function {
    
    public Setnames() 
    {
        name = "setnames";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 0) {
			return illegalUsage();
		}

        NodeSet set = new NodeSet();
        Scope scp = env.peepScope();
        Enumeration<Variable> enm = scp.allVariables();
        ArrayList<String> list = new ArrayList<String>();
        
        Class<?> type;
        Variable var;
        while(enm.hasMoreElements()) {
            var = enm.nextElement();
            type = var.getType();
            if(type == NodeSet.class) {
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
    
    public String usage()
    {
		return "NodeSet " + name + "()";
	}
}
