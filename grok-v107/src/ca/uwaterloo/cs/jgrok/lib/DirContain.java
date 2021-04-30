package ca.uwaterloo.cs.jgrok.lib;

import java.util.StringTokenizer;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.Node;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * EdgeSet dircontain(NodeSet)
 * 
 * <pre>
 *  >> files = {"a/b/x.o", "a/c/x.o", "a/c/y.o"}
 *  >> dircontain(files)
 *  a a/b
 *  a a/c
 *  a/b a/b/x.o
 *  a/c a/c/x.o
 *  a/c a/c/y.o
 *  >>
 * </pre>
 */
public class DirContain extends Function {
    
    public DirContain() {
        name = "dircontain";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 1:
			NodeSet set = vals[0].nodeSetValue();
			Node[] nodes = set.getAllNodes();
	        
			String s;
			String child;
			String parent;
			StringTokenizer st;
	        
			EdgeSet contain = new EdgeSet();
			for(int i = 0; i < nodes.length; i++) {
				s = nodes[i].get();
				if(s.length() == 0) continue;
	            
				st = new StringTokenizer(s, "/");
				try {
					if(s.charAt(0) != '/') parent = st.nextToken();
					else parent = "/" + st.nextToken();
					child = parent + "/" + st.nextToken();
					contain.add(parent, child);
	                
					while(st.hasMoreTokens()) {
						parent = child;
						child = parent + "/" + st.nextToken();
						contain.add(parent, child);
					}
				} catch(Exception e) {}
			}
	        
			contain.removeDuplicates();
			return new Value(contain);
		}
		return illegalUsage();
	}
    
    public String usage()
    {
		return "EdgeSet " + name + "(NodeSet files)";
	}
}
