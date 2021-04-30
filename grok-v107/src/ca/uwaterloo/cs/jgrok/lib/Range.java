package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *     NodeSet range(int num1, int num2)
 *     NodeSet range(int num1, int num2, int step)
 * </pre>
 */
public class Range extends Function {
    
    public Range() {
        name = "range";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        int incr = 1;
        
        switch (vals.length) {
        case 3:
			incr = vals[2].intValue();
            if (incr < 1) {
                throw new InvocationException("range step less than 1");
            }
        case 2:
	        int a = vals[0].intValue();
		    int b = vals[1].intValue();

            NodeSet set = new NodeSet();
        
            if(a < b) {
				for(int i = a; i <= b; i = i+incr) {
					set.add(i+"");  //??? UGLY CODE INDEED!
				}
			} else {
				for(int i = a; i >= b; i= i-incr) {
					set.add(i+"");  //??? UGLY CODE INDEED!
				}
			}
            return new Value(set);
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "NodeSet " + name + "(int start, int end [, int step])"; 
	}
}
