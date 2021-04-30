package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.Value;

/**
 * <pre>
 *     void writeDot(String dotFile, EdgeSet dotLink)
 *     void writeDot(String dotFile, EdgeSet dotLink, EdgeSet contain)
 * </pre>
 */
public class WriteDotty extends Function {
    private static StringBuffer indent = new StringBuffer();
    
    public WriteDotty() {
        name = "writeDot";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
            return action_2(vals);
		case 3:
            return action_3(vals);
        } 
        return illegalUsage();
	}
    
    private static Value action_2(Value[] vals) throws InvocationException 
    {
        String	dotFile = vals[0].stringValue();
        EdgeSet dotLink = vals[1].edgeSetValue();
        PrintWriter writer = getWriter(dotFile);
        
        writeHead(writer, dotFile);
        writeLinks(writer, dotLink);
        writeTail(writer);
        writer.flush();
        
        return Value.VOID;
    }
    
    private static Value action_3(Value[] vals) throws InvocationException 
    {
        String dotFile  = vals[0].stringValue();
        EdgeSet dotLink = vals[1].edgeSetValue();
        EdgeSet contain = vals[2].edgeSetValue();
        PrintWriter writer = getWriter(dotFile);
        
        Tree tree = new Tree(contain);
        int[] roots = tree.getRoots();
        if(roots.length > 1) {
            throw new InvocationException("multiple roots found by writeDot");
        } else if(roots.length == 1) {
            writeHead(writer, dotFile);
            writeLinks(writer, dotLink);
            writeClusters(writer, tree, roots[0]);
            writer.println();
            writeTail(writer);
            writer.flush();
            
            return Value.VOID;
        } else {
            throw new InvocationException("no root found by writeDot");
        }
    }

    private static PrintWriter getWriter(String dotFile)
        throws InvocationException {
        try {
            FileOutputStream fileOut;
            fileOut = new FileOutputStream(new File(dotFile));
            return new PrintWriter(fileOut, true);
        } catch(IOException e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    private static void writeHead(PrintWriter writer, String title) {
        writer.println("digraph \"" + title + "\" {");
        incrIndent();
        writer.println(indent + "size = \"8,6\";");
        writer.println();
    }
    
    private static void writeTail(PrintWriter writer) {
        decrIndent();
        writer.println("}");
    }
    
    private static void writeLinks(PrintWriter writer, EdgeSet links) {
        Edge e;
        Edge[] edges = links.getAllEdges();
        
        for(int i = 0; i < edges.length; i++) {
            e = edges[i];
            writer.println(indent + "\"" + e.getFrom()
                           + "\"->\"" + e.getTo() + "\";");
        }
        writer.println();
    }
    
    private static void writeClusters(PrintWriter writer,
                                      Tree tree, int current) {
        int[] children;
        children = tree.getChildren(current);
        if(children.length > 0) {
            writer.println(indent + "subgraph \"cluster_" + current + "\" {");
            
            incrIndent();
            writer.println(indent + "label=\""+IDManager.get(current) + "\";");
            for(int i = 0; i < children.length; i++) {
                writeClusters(writer, tree, children[i]);
            }
            decrIndent();
            writer.println(indent + "}");
        } else {
            writer.println(indent + "\"" + IDManager.get(current) + "\";");
        }
    }
    
    private static void incrIndent() {
        indent.append("    ");
    }
    
    private static void decrIndent() {
        int len = indent.length();
        if(len > 0) indent.delete(len-4, len);
    }
    
    public String usage()
    {
		return "void " + name + "(String dotFile, EdgeSet dotLink [, EdgeSet contain])";
	}
}
