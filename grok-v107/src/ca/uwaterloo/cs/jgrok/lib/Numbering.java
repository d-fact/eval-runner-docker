package ca.uwaterloo.cs.jgrok.lib;

import java.util.HashMap;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * EdgeSet numbering(NodeSet items)
 * EdgeSet numbering(TupleSet tuples, int column)
 *
 * <pre>
 * Examples:
 *
 *     >> s = {"a", "b", "c"};
 *     >> s
 *     b
 *     a
 *     c
 *     >> numbering(s)
 *     0 b
 *     1 a
 *     2 c
 *     >> numbering(s, &0)
 *     0 b
 *     1 a
 *     2 c
 *
 * </pre>
 */
public class Numbering extends Function {
    
    public Numbering() {
        name = "numbering";
     }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length < 1 || vals.length > 2) {
			return illegalUsage();
		}
		
        int number;
        String skey;
        TupleSet input;
        EdgeSet result;
        HashMap<String,Integer> hash;
        
        number = 0;
        result = new EdgeSet();
        input = (TupleSet)vals[0].objectValue();
        hash = new HashMap<String,Integer>(101);
        
        TupleList data = input.getTupleList();
        if(vals.length == 1) {
            for(int i = 0; i < data.size(); i++) {
                Tuple tuple = data.get(i);
                skey = IDManager.get(tuple.getDom());
                if(!hash.containsKey(skey)) {
                    hash.put(skey, new Integer(number));
                    result.add(IDManager.getID(number+""), tuple.getDom());
                    number++;
                }
            }
        } else {
            if(input.size() > 0) {
                int col = vals[1].intValue();
                for(int i = 0; i < data.size(); i++) {
                    Tuple tuple = data.get(i);
                    if(col < tuple.size()) {
                        skey = IDManager.get(tuple.get(col));
                        if(!hash.containsKey(skey)) {
                            hash.put(skey, new Integer(number));
                            result.add(IDManager.getID(number+""), tuple.get(col));
                            number++;
                        }
                    } else {
                        throw new InvocationException("index " + col + " out of bounds " + tuple.size());
                    }
                }
                
            }
        }
        
        return new Value(result);
    }
    
    public String usage()
    {
		return "EdgeSet numbering(NodeSet items)/(TupleSet tuples, int column)";
	}
}
