package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *     void    delset(NodeSet)
 *     EdgeSet delset(EdgeSet, NodeSet)
 * </pre>
 */
public class DeleteSet extends Function {
    
    public DeleteSet() {
        name = "delset";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        NodeSet set, del;
        EdgeSet data, result;
        
        switch (vals.length) {
        case 1:
            set = (NodeSet)vals[0].objectValue();
            del = CompositeID.getEnclosingIDs(set);
            del = AlgebraOperation.union(del, set);
            
            Scope scp;
            Variable var;
            Enumeration<Variable> enm;
            
            scp = env.peepScope();
            enm = scp.allVariables();
            while(enm.hasMoreElements()) {
                var = enm.nextElement();
                if(var.getType() == EdgeSet.class) {
                    EdgeSet eSet = (EdgeSet)var.getValue().objectValue();
                    eSet = UtilityOperation.delset(eSet, del);
                    eSet.setName(var.getName());
                    var.setValue(new Value(eSet));
                }
            }
            return Value.VOID;
        case 2:
            data = (EdgeSet)vals[0].objectValue();
            set  = (NodeSet)vals[1].objectValue();
            del = CompositeID.getEnclosingIDs(set);
            del = AlgebraOperation.union(del, set);
            
            result = UtilityOperation.delset(data, del);
            return new Value(result);
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "([EdgeSet edgeset,] NodeSet del)";
	}
}
