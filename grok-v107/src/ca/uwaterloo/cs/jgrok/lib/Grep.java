package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.Node;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.interp.Value;

import java.util.regex.PatternSyntaxException;

/**
 * <pre>
 *   set grep(set, regex)
 * </pre>
 */
public class Grep extends Function {
    
    public Grep() {
        name = "grep";
    }
    
    public Value invoke(Env env, Value[] vals)  throws InvocationException 
    {
		switch (vals.length) {
		case 2:
			NodeSet result = new NodeSet();
			NodeSet set = vals[0].nodeSetValue();
			String regex = vals[1].stringValue();
	        
			int len = regex.length();
			if(!((regex.charAt(0) == '^') ||
				 (len > 1 && regex.substring(0, 2).equals(".*")))) {
				regex = ".*" + regex;
			}
	        
			len = regex.length();
			if(!((regex.charAt(len-1) == '$') ||
				 (len > 1 && regex.substring(len-2, len).equals(".*")))) {
				regex = regex + ".*";
			}
	        
			String s;
			Node node;
			Node[] nodes = set.getAllNodes();
	        
			for(int i = 0; i < nodes.length; i++) {
				node = nodes[i];
				s = node.get();
				try {
					if(s.matches(regex)) {
						result.add(node.getID());
					} else if(s.indexOf(regex) > 0) {
						result.add(node.getID());
					}
				} catch(PatternSyntaxException e) {
					throw new InvocationException(e.getMessage());
				}
			}
	        
			return new Value(result);
		}
		return illegalUsage();
    }
    
    public String usage()
    {
		return "NodeSet " + name + "(NodeSet nodes, String regex)";
	}
}
