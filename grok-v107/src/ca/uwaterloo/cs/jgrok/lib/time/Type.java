package ca.uwaterloo.cs.jgrok.lib.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

import ca.uwaterloo.cs.jgrok.fb.NodeSet;

/**
 * <pre>
 *     string datetimeas(long, [pattern]);
 * </pre>
 */
public class Type extends Function {
    
    public Type() 
    {
        name = "type";
	}
       
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		NodeSet set    = new NodeSet();
		int		length = vals.length;
		int		i;
		Value	value;
		
		for (i = 0; i < length; ++i) {
			value = vals[i];
			set.add(value.getTypeName());
		}
		return new Value(set);
	}
	    
    public String usage()
    {
		return "NodeSet " + name + "([parameter]*)";
	}
}
