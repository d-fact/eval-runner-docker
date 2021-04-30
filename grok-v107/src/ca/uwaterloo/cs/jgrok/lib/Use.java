package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;


/**
 * <pre>
 *	   void    use() 
 *     boolean use(string);
 *     boolean use(string, string);
 *	   boolean use(string, string, string);
 * </pre>
 */
public class Use extends Function {
    
    public Use() 
    {
        name = "use";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		String		library = null;
		String		name    = null;
		String		java    = null;
		boolean		ret     = false;
		
		switch (vals.length) {
		case 3:
			library = vals[2].stringValue();
		case 2:
			name    = vals[1].stringValue();
		case 1:
			Class<?>					foundClass;
			Class<? extends Function>	validClass;
			Function					function;
			
			java = vals[0].stringValue();
			try {
				foundClass = Class.forName(java);
			} catch (LinkageError e) {
				env.out.println("use: can't link " + java + " " + e.getMessage());
				break;
			} catch (ClassNotFoundException e) {
				env.out.println("use: can't find " + java + " " + e.getMessage());
				break;
			}
							
			try {
				validClass = foundClass.asSubclass(Function.class);
                function   = validClass.newInstance();
				if (name != null) {
					function.setName(name);
				}
				FunctionLibManager.register(library, function);
            } catch(Exception e) {
   				env.out.println("use: class " + java + " is not a subclass of Function " + e.getMessage());
   				break;
   			}
   			ret = true;
			break;
		case 0:
			EdgeSet edgeSet = new EdgeSet();
			
			FunctionLibManager.getUse(edgeSet);
			return new Value(edgeSet);
		}
		return new Value(ret);
    }
    
    public String usage()
    {
		return "boolean " + name + "(String javaclass [, String name [,String library]])";
	}
}
