package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *     float entropy(EdgeSet)
 *
 * Examples:
 *     A { probability = 0.1 }
 *     B { probability = 0.2 }
 *     C { probability = 0.4 }
 *     D { probability = 0.3 }
 *
 *     >> entropy(@probability)
 *     1.8464394
 *
 * </pre>
 */
public class Entropy extends Function {
    
    public Entropy() {
        name = "entropy";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			double p;
			double result = 0;
			EdgeSet eSet = vals[0].edgeSetValue();
	        
			if(eSet.size() == 0) {
				result = 0;
			} else {
				Tuple t;
				TupleList tList = eSet.getTupleList();
				for(int i = 0; i < tList.size(); i++) {
					t = tList.get(i);
					try {
						p = Double.parseDouble(IDManager.get(t.getRng()));
						if(0 < p && p <= 1.0)
							result = result - p * Math.log(p)/Math.log(2);
	                    
						if(p < 0 || p > 1.0)
							throw new InvocationException("illegal probability " + p);
					} catch(Exception e) {
						throw new InvocationException(e.getMessage());
					}
				}
			}
			return new Value(result);
		}
		return illegalUsage();
	}
    
    public String usage()
    {
		return "double " + name + "(EdgeSet tuples)";
	}
}
