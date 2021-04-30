package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *     NodeSet basket(string items);     // return a singleton.
 *     NodeSet basket(NodeSet itemSet);  // return a singleton.
 *     EdgeSet basket(EdgeSet edgeSet);  // return EdgeSet whose outDegree equals 1.
 *     
 *     NodeSet unbasket(string items);     // reverse basket
 *     NodeSet unbasket(NodeSet itemSet);  // reverse basket
 *     EdgeSet unbasket(EdgeSet edgeSet);  // reverse basket
 *
 * Examples:
 *     >> b1 = basket({1, 2, "ABC"});
 *     >> b2 = basket("(1 2 ABC)");
 *     >> b1
 *     (1 2 ABC)
 *     >> b2
 *     (1 2 ABC)
 *     >> b1 ^ b2
 *     (1 2 ABC)
 *     >> b1 + b2
 *     (1 2 ABC)
 *     >> s = unbasket(b1);
 *     >> s
 *     1
 *     2
 *     ABC
 *     >> r = s X s;
 *     >> basket(r);
 *     1 (1 2 ABC)
 *     2 (1 2 ABC)
 *     ABC (1 2 ABC)
 *     >> unbasket(basket(r));
 *     1 1
 *     1 2
 *     1 ABC
 *     2 1
 *     2 2
 *     2 ABC
 *     ABC 1
 *     ABC 2
 *     ABC ABC
 *     >> 
 * </pre>
 */
public class Unbasket extends Function {
    
    public Unbasket() 
    {
        name = "unbasket";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        Object o = vals[0].objectValue();
        
        if(o instanceof String) {
            return new Value(SpecialOperation.unbasket((String)o));
        } 
        if(o instanceof NodeSet) {
            return new Value(SpecialOperation.unbasket((NodeSet)o));
        }
        if (o instanceof EdgeSet) {
			return new Value(SpecialOperation.unbasket((EdgeSet)o));
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "set " + name + "(string|NodeSet|EdgeSet items)";
	}
}
