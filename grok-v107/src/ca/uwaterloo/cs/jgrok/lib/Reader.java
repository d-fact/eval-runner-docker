package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;
import java.io.FileNotFoundException;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;
import ca.uwaterloo.cs.jgrok.io.ta.TAFileReader;

/**
 * <pre>
 *    void read (string dataFile)
 *    void getta(string dataFile)
 *    void getdb(string dataFile)
 * </pre>
 */
public abstract class Reader extends Function {
    
    public Reader() {
    }
    
    protected Value load(Env env, FactbaseReader fbReader, String fileName) throws InvocationException 
    {
        Factbase fb;
        
        try {
            fb = fbReader.read(fileName);
        } catch (FileNotFoundException e) {
            throw new InvocationException(fileName + " not found");
        }
        
        Scope scp;
        Variable var;
        TupleSet set;
        Enumeration<TupleSet> enm;
        
        EdgeSet eSet1;
        EdgeSet eSet2;
        EdgeSet result;
        
        enm = fb.allSets();
        scp = env.peepScope();
        while(enm.hasMoreElements()) {
            set = enm.nextElement();
            try {
                var = scp.lookup(set.getName());
                if(set instanceof EdgeSet &&
                   var.getType() == EdgeSet.class) {
                    eSet1 = (EdgeSet)var.getValue().objectValue();
                    eSet2 = (EdgeSet)set;
                    result= AlgebraOperation.union(eSet1, eSet2);
                    var.setValue(new Value(result));
                    result.setName(var.getName());
                } else {
                    var.setValue(new Value(set));
                }
            } catch(LookupException e) {
                var = new Variable(scp, set.getName(), new Value(set));
                scp.addVariable(var);
            }
        }
            
        return Value.VOID;
    }
    
    public String usage()
    {
		return "void " + name + "(String dataFile)";
	}
}
