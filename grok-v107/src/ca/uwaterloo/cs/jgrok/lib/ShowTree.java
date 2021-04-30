package ca.uwaterloo.cs.jgrok.lib;

import java.util.ArrayList;
import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * Shows a tree.
 * 
 * <pre>
 * Functions:
 * + choose the default relation "contain"
 *     void showtree()
 *     void showtree(int level)
 *     
 * + choose binary relation "relation" as the contain relation 
 *     void showtree(EdgeSet relation)
 *     void showtree(EdgeSet relation, int level)
 *
 * + choose the default relation "contain" and show tree with a specified root.
 *     void showtree(String rootNode) 
 *     void showtree(String rootNode, int level)
 *     
 * + choose binary relation "relation" and show tree with a specified root.
 *     void showtree(EdgeSet relation, String rootNode) 
 *     void showtree(EdgeSet relation, String rootNode, int level)
 * </pre>
 */
 
public class ShowTree extends Function {
    private Env env;
    private StringBuffer indent = new StringBuffer();

    public static final int METHOD_UNKNOWN = 0;
    public static final int METHOD_DEFAULT_CONTAIN = 1;
    public static final int METHOD_DEFAULT_CONTAIN_LEVEL = 2;
    
    public static final int METHOD_CUSTOM_CONTAIN = 3;
    public static final int METHOD_CUSTOM_CONTAIN_LEVEL = 4;
    
    public static final int METHOD_DEFAULT_CONTAIN_CUSTOM_ROOT = 5;
    public static final int METHOD_DEFAULT_CONTAIN_CUSTOM_ROOT_LEVEL = 6;
    
    public static final int METHOD_CUSTOM_CONTAIN_CUSTOM_ROOT = 7;
    public static final int METHOD_CUSTOM_CONTAIN_CUSTOM_ROOT_LEVEL = 8;
    
    public ShowTree() 
    {
        name = "showtree";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		int		whichMethod = METHOD_UNKNOWN;
        int		level       = -1;
        EdgeSet contain     = null;
        String	singleton   = null;
        boolean	seenLevel   = false;
        
        this.env = env;
        
        switch (vals.length) {
        case 0:
            // showtree()
			whichMethod = METHOD_DEFAULT_CONTAIN;
            break;
        case 1:
        {
			Value	value0 = vals[0];
			
			if (value0.isNumeric()) {
		        // showtree(level)
				whichMethod = METHOD_DEFAULT_CONTAIN_LEVEL;
				level       = value0.intValue();
				seenLevel   = true;
				break;
			}
			if (!value0.isEdgeSet()) {
		        // showtree(rootNode)
				whichMethod = METHOD_DEFAULT_CONTAIN_CUSTOM_ROOT;
				singleton   = value0.toString();
				break;
			}
			whichMethod = METHOD_CUSTOM_CONTAIN;
	        // showtree(relation)
            contain = value0.edgeSetValue();
            break;
		}
		case 2: 
		{
			Value	value0 = vals[0];
			Value	value1 = vals[1]; 
			
			if (!value0.isEdgeSet()) {
				// showtree(rootNode, level)
				whichMethod = METHOD_DEFAULT_CONTAIN_CUSTOM_ROOT_LEVEL;
				
	            singleton = vals[0].toString();
				level     = value1.intValue();
				seenLevel = true;
	            break;
			}
			contain = value0.edgeSetValue();
			if (value1.isNumeric()) {
				// showtree(relation, level)
				whichMethod = METHOD_CUSTOM_CONTAIN_LEVEL;	
				level       = value1.intValue();
				seenLevel   = true;
				break;
			}
			 // showtree(relation, rootNode)
			whichMethod = METHOD_CUSTOM_CONTAIN_CUSTOM_ROOT;
            singleton   = value1.toString();
            break;	
        }		
		case 3:
	        // showtree(relation, rootNode, level)
			whichMethod = METHOD_CUSTOM_CONTAIN_CUSTOM_ROOT_LEVEL;
            contain   = (EdgeSet)vals[0].objectValue();
            singleton = vals[1].toString();
            level     = vals[2].intValue();
            seenLevel = true;
            break;
		default:
			return illegalUsage();
		}
		
		if (seenLevel) {
			if(level < 0) {
                throw new InvocationException("illegal showtree level: " + level);
        }   }
        
		if (contain == null) {
			if (singleton == null) {
	            contain     = findContain();
			} else {
				try {
					contain = findContain();
				} catch(Exception e) {
					Show.print(env, IDManager.getID(singleton), env.peepScope());
					return Value.VOID;
		}	}	}
		
        int[] roots;
        Tree tree = new Tree(contain);
        if(singleton == null) {
            roots = tree.getRoots();
        } else {
            roots = new int[1];
            roots[0] = IDManager.getID(singleton);
        }
        
        if(roots.length > 1) {
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i < roots.length; i++) {
                buffer.append("\n\t"+IDManager.get(roots[i]));
            }
            throw new InvocationException("multiple roots found by showtree:"
                                          + buffer.toString());
        } else if(roots.length == 1) {
            ShowDB showDB = buildShowDB(contain.getName());
            if(level < 0) {
                printNode(roots[0], tree, showDB);
            } else {
                printNode(roots[0], tree, showDB, 0, level);
            }
            return Value.VOID;
        } else {
            throw new InvocationException("no root found by showtree");
        }
    }
    
    private EdgeSet findContain() throws InvocationException {
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
    
    private ShowDB buildShowDB(String relName) {
        ShowDB showDB = new ShowDB();
        Scope scp = env.peepScope();
        Enumeration<Variable> enm = scp.allVariables();
        
        Variable var;
        String varName;
        Class<?> varType;
        
        while(enm.hasMoreElements()) {
            var = enm.nextElement();
            varType = var.getType();
            if(varType == EdgeSet.class) {
                varName = var.getName();
                if(varName.equals(relName)) continue;
                else {
                    EdgeSet eSet;
                    eSet = (EdgeSet)var.getValue().objectValue();
                    if(varName.charAt(0) == '@' ||
                       varName.equals("$INSTANCE"))
                        showDB.addAtts(eSet);
                    else
                        showDB.addRels(eSet);
                }
            }
        }
        
        showDB.setup();
        return showDB;
    }
    
    private void printNode(int node,
                           Tree tree,
                           ShowDB showDB) {
        // Print the node.
        showNode(node, tree, showDB);
        
        // Print the children.
        int[] children = tree.getChildren(node);
        if(children.length > 0) {
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printNode(children[i], tree, showDB);
            }
            decrIndent();
        }
    }
    
    private void printNode(int node,
                           Tree tree,
                           ShowDB showDB,
                           int curLevel,
                           int endLevel) {
        // Print the node.
        showNode(node, tree, showDB);        
        if(curLevel == endLevel) return;
        
        // Print the children.
        int[] children = tree.getChildren(node);
        if(children.length > 0) {
            incrIndent();
            for(int i = 0; i < children.length; i++) {
                printNode(children[i], tree, showDB, curLevel+1, endLevel);
            }
            decrIndent();
        }
    }
    
    private void showNode(int node,
                          Tree tree,
                          ShowDB showDB) {
        String[] atts = showDB.getAtts(node);
        ArrayList<String> rels = showDB.getRels(node);
        StringBuffer title = new StringBuffer(); 
        
        // Print title
        if(atts[0] == null) {
            title.append(IDManager.get(node));
            title.append(" : ");
            if(atts[1] != null) {
                title.append(atts[1]);
                title.append(' ');
            }
        } else {
            title.append(atts[0]);
            title.append(" : ");
            if(atts[1] != null) {
                title.append(atts[1]);
                title.append(" @ ");
            }
            title.append(IDManager.get(node));
            title.append(' ');
        }
        
        if(atts[2] != null) title.append(atts[2]);
        env.out.println(indent.toString() + title);        
        
        // Print relations
        for(int i = 0; i < rels.size(); i++) {
            env.out.println(indent.toString() + "   " + rels.get(i));
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
		return "void " + name + "([EdgeSet relation] [,String rootNode] [, int level])";
	}
}
