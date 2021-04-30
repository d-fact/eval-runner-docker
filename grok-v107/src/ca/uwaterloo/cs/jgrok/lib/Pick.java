package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 * Functions:
 *     string pick(set)
 *     set pick(set, int)
 * </pre>
 */
public class Pick extends Function {
    
    public Pick() {
        name = "pick";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        NodeSet set;
        
        switch (vals.length) {
        case 1:
			set = (NodeSet)vals[0].objectValue();
			return new Value(set.pick());
		case 2:
			set = (NodeSet)vals[0].objectValue();
            return new Value(set.pick(vals[1].intValue()));
        }
		return illegalUsage();
    }
    
    public String usage()
    {
		return "Set " + name + "(Set set [, int cnt])";
	}
}
