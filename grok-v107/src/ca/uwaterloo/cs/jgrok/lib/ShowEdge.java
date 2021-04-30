package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * Shows edges along the contain hierarchy.
 * 
 * <pre>
 * + Show edges inside the specified node:
 *     void showedge(string node, EdgeSet edgeSet)
 *     void showedge(string node, EdgeSet edgeSet, int level)
 *
 * + Show edges from the source node to the target node:
 *     void showedge(string srcNode, string trgNode, EdgeSet edgeSet)
 *     void showedge(string srcNode, string trgNode, EdgeSet edgeSet, int level)
 * </pre>
 */
public class ShowEdge extends Function {
    private Env env;
    private StringBuffer indent = new StringBuffer();
    
    public static final int METHOD_UNKNOWN = 0;
    public static final int METHOD_ROOT_NODE = 1;
    public static final int METHOD_ROOT_NODE_LEVEL = 2;
    public static final int METHOD_SOURCE_DESTINATION = 3;
    public static final int METHOD_SOURCE_DESTINATION_LEVEL = 4;
    
    public ShowEdge() 
    {
        name = "showedge";
    }
    
    public Value invoke(Env env, Value[] vals)  throws InvocationException 
    {
        int		whichMethod = METHOD_UNKNOWN;
        String	srcNode     = null;
        String	trgNode     = null;
        EdgeSet	edgeSet     = null;
        int		level       = 0;
        
        EdgeSet contain;
        Tree2	tree2;
        
        this.env = env;

		if (vals.length > 0) {
			srcNode     = vals[0].toString();
	        switch (vals.length) {
		    case 2:
				whichMethod = METHOD_ROOT_NODE;
				edgeSet     = (EdgeSet)vals[1].objectValue();
				break;
			case 3:
				if (vals[1].isPrimitive()) {
					whichMethod = METHOD_SOURCE_DESTINATION;
					trgNode     = vals[1].toString();
					edgeSet     = (EdgeSet) vals[2].objectValue();
				} else {
					whichMethod = METHOD_ROOT_NODE_LEVEL;
					edgeSet     = (EdgeSet) vals[1].objectValue();
					level       = vals[2].intValue();
					if(level < 0) {
						throw new InvocationException("illegal showedge level: " + level);
				}	}	
				break;	
			case 4:
				whichMethod = METHOD_SOURCE_DESTINATION_LEVEL;
				trgNode     = vals[1].toString();
				edgeSet     = (EdgeSet)vals[2].objectValue();
				level       = vals[3].intValue();
				break;
			}
			if (level < 0) {
				throw new InvocationException("illegal showedge level: " + level);
		}	}
		
		if (whichMethod == METHOD_UNKNOWN) {
			return illegalUsage();
		}
			
        try {
            contain = findContain();
            tree2 = new Tree2(contain);
        } catch(InvocationException e) {
            env.out.println("trivial showedge, not implemented");
            return Value.VOID;
        }
        
        switch(whichMethod) {
        case METHOD_ROOT_NODE:
            {
                int srcId = IDManager.getID(srcNode);
                Tree edgeTree = tree2.getEdgeTree(srcId, edgeSet);
                EdgeSet multiplicity = new EdgeSet();
                calculateMultiplicity(srcId, edgeTree, multiplicity, true);
                printNode(srcId, edgeTree, edgeSet.getName(), multiplicity);
            }
            break;
        case METHOD_ROOT_NODE_LEVEL:
            {
                int srcId = IDManager.getID(srcNode);
                Tree edgeTree = tree2.getEdgeTree(srcId, edgeSet);
                EdgeSet multiplicity = new EdgeSet();
                calculateMultiplicity(srcId, edgeTree, multiplicity, true);
                printNode(srcId, edgeTree, edgeSet.getName(), multiplicity, 0, level);
            }
            break;
        case METHOD_SOURCE_DESTINATION:
            {
                int srcID = IDManager.getID(srcNode);
                int trgID = IDManager.getID(trgNode);
                Tree edgeTree = tree2.getEdgeTree(srcID, trgID, edgeSet);
				int edgeRoot = edgeTree.getRoots()[0];
                EdgeSet multiplicity = new EdgeSet();
                calculateMultiplicity(edgeRoot, edgeTree, multiplicity, true);
                printEdge(edgeRoot, edgeTree, true, edgeSet.getName(), multiplicity);
            }
            break;
        case METHOD_SOURCE_DESTINATION_LEVEL:
            {
                int srcID = IDManager.getID(srcNode);
                int trgID = IDManager.getID(trgNode);
                Tree edgeTree = tree2.getEdgeTree(srcID, trgID, edgeSet);
                int edgeRoot = edgeTree.getRoots()[0];
                EdgeSet multiplicity = new EdgeSet();
                calculateMultiplicity(edgeRoot, edgeTree, multiplicity, true);
                printEdge(edgeRoot, edgeTree, true, edgeSet.getName(), multiplicity, 0, level);
            }
            break;
        }
        
        return Value.VOID;
    }
    
    private EdgeSet findContain() throws InvocationException 
    {
        try {
            Variable var;
            var = env.peepScope().lookup("contain");
            if(var.getType() != EdgeSet.class) {
                throw new InvocationException("contain is not relation");
            }
            return (EdgeSet)var.getValue().objectValue();
        } catch(Exception e) {
            throw new InvocationException("contain not found");
        }
    }
    
    private int calculateMultiplicity(int node,
                                      Tree tree,
                                      EdgeSet eSet,
                                      boolean isRoot) {
        int multiplicity = 0;
        int[] children = tree.getChildren(node);
        if(children.length == 0) {
            if(isRoot)
                multiplicity = 0;
            else
                multiplicity = 1;
        } else {
            for(int i = 0; i < children.length; i++) {
                multiplicity += calculateMultiplicity(children[i], tree, eSet, false);
            }
        }
        
        eSet.add(node, IDManager.getID(multiplicity + ""));
        return multiplicity;
    }
    
    private void printNode(int node,
                           Tree tree,
                           String relName,
                           EdgeSet multiplicity) {
        String num = ShowDB.getAtt(node, multiplicity);
        env.out.println(indent.toString()
                        + num + " : " + IDManager.get(node));
        
        int[] children;        
        children = tree.getChildren(node);
        
        if(children.length > 0) {
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printEdge(children[i], tree, false, relName, multiplicity);
            }
            decrIndent();
        }
    }
    
    private void printNode(int node,
                           Tree tree,
                           String relName,
                           EdgeSet multiplicity,
                           int curLevel, int endLevel) {
        String num = ShowDB.getAtt(node, multiplicity);
        env.out.println(indent.toString()
                        + num + " : " + IDManager.get(node));
        if(curLevel == endLevel) return;
        
        int[] children;        
        children = tree.getChildren(node);
        
        if(children.length > 0) {
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printEdge(children[i], tree, false, relName,
                          multiplicity, curLevel+1, endLevel);
            }
            decrIndent();
        }
    }
    
    private void printEdge(int edge,
                           Tree tree,
                           boolean isRoot,
                           String relName,
                           EdgeSet multiplicity) {
        String s;
        if(isRoot) s = "=>";
        else s = IDManager.get(edge);
        
        String num = ShowDB.getAtt(edge, multiplicity);
        int[] children = tree.getChildren(edge);
                
        if(children.length == 0) {
            // Get rid of '(' and ')'
            s = s.substring(1, s.length()-1);
            
            StringBuffer buffer;
            buffer = new StringBuffer();
            buffer.append(indent.toString());
            buffer.append(' ');
            buffer.append("-> (");
            buffer.append(relName);
            buffer.append(' ');
            buffer.append(s);
            buffer.append(')');
            
            env.out.println(buffer.toString());
        } else {
            if(isRoot) 
                env.out.println(indent.toString()
                                + num + " : " + s);
            else {
                int[] parse;
                parse = IDManager.parse(edge);
                if(parse.length == 1)
                    env.out.println(indent.toString()
                                    + num + " : " + s);
                else
                    env.out.println(indent.toString()
                                    + num + " : => " + s);
            }
            
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printEdge(children[i], tree, false, relName, multiplicity);
            }
            decrIndent();
        }
    }
    
    private void printEdge(int edge,
                           Tree tree,
                           boolean isRoot,
                           String relName,
                           EdgeSet multiplicity,
                           int curLevel, int endLevel) {
        String s;
        if(isRoot) s = "=>";
        else s = IDManager.get(edge);
        
        String num = ShowDB.getAtt(edge, multiplicity);
        int[] children = tree.getChildren(edge);
        
        if(children.length == 0) {
            // Get rid of '(' and ')'
            s = s.substring(1, s.length()-1);
            
            StringBuffer buffer;
            buffer = new StringBuffer();
            buffer.append(indent.toString());
            buffer.append(' ');
            buffer.append("-> (");
            buffer.append(relName);
            buffer.append(' ');
            buffer.append(s);
            buffer.append(')');
            
            env.out.println(buffer.toString());
        } else {
            if(isRoot) 
                env.out.println(indent.toString()
                                + num + " : " + s);
            else {
                int[] parse;
                parse = IDManager.parse(edge);
                if(parse.length == 1)
                    env.out.println(indent.toString()
                                    + num + " : " + s);
                else
                    env.out.println(indent.toString()
                                    + num + " : => " + s);
            }
            
            if(curLevel == endLevel) return;
            
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printEdge(children[i], tree, false, relName,
                          multiplicity, curLevel+1, endLevel);
            }
            decrIndent();
        }
    }
    
    private void incrIndent() {
        indent.append("|  ");
    }
    
    private void decrIndent() {
        int len = indent.length();
        if(len > 0) indent.delete(len-3, len);
    }
    
    public String usage()
    {
		return "void " + name + "(string srcNode [, string trgNode], EdgeSet edgeSet [, int level])";
	}
}
