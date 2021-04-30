package ca.uwaterloo.cs.jgrok.lib;

import java.util.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *      void show(set)
 *      void show(string)
 *
 * </pre>
 */
public class Show extends Function {
    
    public Show() 
    {
        name = "show";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			Value	value0 = vals[0];
			Scope	scp    = env.peepScope();
			String sid;
	        
			if(!vals[0].isNodeSet()) {
				sid = value0.stringValue();
				print(env, IDManager.getID(sid), scp);
			} else {
				NodeSet	nodeset = value0.nodeSetValue();
				Node[]  nodes   = nodeset.getAllNodes();
				int		i;
	            
				// Print all but the last
				for(i = 0; i < nodes.length-1; i++) {
					sid = nodes[i].get();
					print(env, IDManager.getID(sid), scp);
					env.out.println();
				}
	            
				// Print the last one
				if(nodes.length > 0) {
					sid = nodes[i].get();
					print(env, IDManager.getID(sid), scp);
				}
			}
	        
			return Value.VOID;
		}
		return illegalUsage();
    }
    
    static void print(Env env, int node, Scope scp) {
        ArrayList<EdgeSet> attrSets = new ArrayList<EdgeSet>();
        ArrayList<EdgeSet> edgeSets = new ArrayList<EdgeSet>();
        StringBuffer buffer = new StringBuffer();
        
        EdgeSet eSet;
        EdgeSet contain = null;
        EdgeSet instance = null;
        EdgeSet nameAttr = null;
        
        String varName;
        Variable var;
        Enumeration<Variable> enm = scp.allVariables();
        while(enm.hasMoreElements()) {
            var = enm.nextElement();
            if(var.getType() == EdgeSet.class) {
                varName = var.getName();
                if(varName.equals("@name")) {
                    nameAttr = (EdgeSet)var.getValue().objectValue();
                } else if(varName.charAt(0) == '@') {
                    attrSets.add((EdgeSet)var.getValue().objectValue());
                } else if(varName.equals("$INSTANCE")) {
                    instance = (EdgeSet)var.getValue().objectValue();
                } else if(varName.equals("contain")) {
                    contain = (EdgeSet)var.getValue().objectValue();
                } else {
                    edgeSets.add((EdgeSet)var.getValue().objectValue());
                }
            }
        }
        
        String name;
        String attVal;
        
        // Print the head.
        name = ShowDB.getAtt(node, nameAttr);
        if(name == null) {
            buffer.append(IDManager.get(node));
            buffer.append(" : ");
            attVal = ShowDB.getAtt(node, instance);
            if(attVal != null) {
                buffer.append(attVal);
                buffer.append(' ');
            }
        } else {
            buffer.append(name);
            buffer.append(" : ");
            attVal = ShowDB.getAtt(node, instance);
            if(attVal != null) {
                buffer.append(attVal);
                buffer.append(' ');
            }
            buffer.append("@ ");
            buffer.append(IDManager.get(node));
            buffer.append(' ');
        }
        
        buffer.append("{ ");
        int sofar = buffer.length();
        
        for(int i = 0; i < attrSets.size(); i++) {
            eSet = (EdgeSet)attrSets.get(i);
            attVal = ShowDB.getAtt(node, eSet);
            if(attVal != null) {
                buffer.append(eSet.getName().substring(1));
                buffer.append('=');
                buffer.append(attVal);
                buffer.append(' ');
            }
        }
        if(sofar != buffer.length())
            buffer.append('}');
        else
            buffer.delete(sofar-3, sofar);
        
        env.out.println(buffer.toString());
        
        NodeSet singleton = NodeSet.singleton(node);
        // Print the parent
        if(contain != null) {
            NodeSet data = AlgebraOperation.project(contain, singleton);
            Node[] nodes = data.getAllNodes();
            for(int i = 0; i < nodes.length; i++) {
                env.out.println("   ( contain <- "
                                + compose(nodes[i].get(), nameAttr)
                                + " )");
            }
        }
        
        // Print the edges
        ArrayList<String> info;
        for(int i = 0; i < edgeSets.size(); i++) {
            eSet = (EdgeSet)edgeSets.get(i);
            info = ShowDB.getRels(node, eSet, nameAttr);
            for(int j = 0; j < info.size(); j++) {
                env.out.println("   " + info.get(j));
            }
        }
        
        // print the children
        if(contain != null) {
            NodeSet data = AlgebraOperation.project(singleton, contain);
            Node[] nodes = data.getAllNodes();
            for(int i = 0; i < nodes.length; i++) {
                env.out.println("|  " + compose(nodes[i].get(), nameAttr));
            }
        }
    }
    
    private static String compose(String sid, EdgeSet nameAttr) {
        String name = ShowDB.getAtt(IDManager.getID(sid), nameAttr);
        if(name != null) return name + " @ " + sid;
        return sid;
    }
    
    public String usage()
    {
		return "void " + name + "(String/Nodeset set)";
	}
}
