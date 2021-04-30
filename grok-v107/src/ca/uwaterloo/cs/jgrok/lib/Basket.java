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
 
public class Basket extends Function {
    
    public Basket() {
        name = "basket";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			Value value0 = vals[0];
			
			if (value0.isNodeSet()) {
	            return new Value(SpecialOperation.basket(value0.nodeSetValue()));
	        }
	        if (value0.isEdgeSet()) {
	            return new Value(SpecialOperation.basket(value0.edgeSetValue()));
	        }
            return new Value(SpecialOperation.basket(value0.stringValue()));
        }
        return illegalUsage();
	}
   
	public String usage()
	{
		return "set " + name + "(string|itemset|edgeset items)";
	}	
}
