package ca.uwaterloo.cs.jgrok.lib.math;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     double stdev(NodeSet)
 *     double stdev(TupleSet, col)
 * </pre>
 */
public class Stdev extends Function {
    
    public Stdev() {
        name = "stdev";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        double   sum  = 0;
        double	 sum2 = 0;
        int		 size;
        
        double   mean;
        double	 mean2;
        double   result = 0;
        TupleSet input;
        TupleList data;
        Tuple	  tuple;
	    int		 i;
	    double	 d;
        
        switch (vals.length) {
        case 1:
	        input = vals[0].tupleSetValue();
	        size  = input.size();
			if (size > 0) {
				data = input.getTupleList();
                for(i = 0; i < data.size(); i++) {
                    tuple = data.get(i);
                    try {
                        d     = Double.parseDouble(IDManager.get(tuple.getDom()));
                        sum  += d;
                        sum2 += d*d;
                    } catch(NumberFormatException e) {}
                }
			}
			break;
        case 2:
			int col = vals[1].intValue();
			
	        input = vals[0].tupleSetValue();
   	        size  = input.size();

			if (size > 0) {
				data = input.getTupleList();
                for(i = 0; i < data.size(); i++) {
                    tuple = data.get(i);
                    if(col < tuple.size()) {
                        try {
                            d = Double.parseDouble(IDManager.get(tuple.get(col)));
                            sum  += d;
                            sum2 += d*d;
                            
                        } catch(NumberFormatException e) {}
                    } 
                }
            }
            break;
        default:
        	return illegalUsage();
		}

		if (size > 0) {
			mean   = sum  / size;
			mean2  = sum2 / size;
			result = Math.sqrt(mean2 - mean*mean);
		}
        
        return new Value(result);
    }
    
    public String usage()
    {	
		return "double " + name + "(NodeSet set [, int column])";
	} 
}
