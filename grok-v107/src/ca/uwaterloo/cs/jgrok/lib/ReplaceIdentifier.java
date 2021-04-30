package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *     void replaceid(eset)
 *     mset replaceid(mset, eset)
 *
 * </pre>
 */
public class ReplaceIdentifier extends Function {
    
    public ReplaceIdentifier() 
    {
        name = "replaceid";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        EdgeSet pairIDs;
        TupleSet m;
        
        switch (vals.length) {
        case 1:
            Scope scp = env.peepScope();
            Enumeration<Variable> enm = scp.allVariables();
            
            pairIDs = vals[0].edgeSetValue();
            ReplaceID.init(pairIDs);
            
            Object o;
            Variable var;
            while(enm.hasMoreElements()) {
                var = enm.nextElement();
                o = var.getValue().objectValue();
                if(o instanceof TupleSet) {
                    m = (TupleSet)o;
                    ReplaceID.process(m);
                    m.removeDuplicates();
                }
            }
            
            ReplaceID.close();
            
            return Value.VOID;
        case 2:
            m = vals[0].tupleSetValue();
            m = (TupleSet)m.clone();
            
            pairIDs = vals[1].edgeSetValue();
            ReplaceID.init(pairIDs);
            
            ReplaceID.process(m);
            m.removeDuplicates();
            
            ReplaceID.close();
            
            return new Value(m);
        }
        return illegalUsage();
    }
    
    public String usage()
    {
		return "void " + name + "(EdgeSet tuples)/TupleSet " + name + "(TupleSet tuples, EdgeSet edges)";
	}
}
