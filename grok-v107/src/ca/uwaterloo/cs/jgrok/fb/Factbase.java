package ca.uwaterloo.cs.jgrok.fb;

import java.io.*;
import java.util.*;

public class Factbase {
    private String name;
    private String type;
    private Hashtable<String,TupleSet> table;
    
    public Factbase() {
        this(null);
    }
    
    public Factbase(String name) {
        this.name = name;
        table = new Hashtable<String,TupleSet>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean hasSet(String name) {
        if(table.containsKey(name))
            return true;
        return false;
    }
    
    public Enumeration<TupleSet> allSets() {
        return table.elements();
    }
    
    public void remove(String name) {
        table.remove(name);
    }
    
    public TupleSet getSet(String name) {
        return (TupleSet)table.get(name);
    }
    
    public void addSet(TupleSet tSet) {
        if(tSet != null) {
            table.put(tSet.getName(), tSet);
        }
    }
    
    public NodeSet getNodeSet(String name) {
        return (NodeSet)table.get(name);
    }
    
    public EdgeSet getEdgeSet(String name) {
        return (EdgeSet)table.get(name);
    }
    
    public Enumeration<NodeSet> allNodeSets() {
        Vector<NodeSet> list = new Vector<NodeSet>();
        Enumeration<TupleSet> enm = table.elements();
        while(enm.hasMoreElements()) {
            TupleSet set = enm.nextElement();
            if(set instanceof NodeSet) list.add((NodeSet)set);
        }
        return list.elements();
    }
    
    public Enumeration<EdgeSet> allEdgeSets() {
        Vector<EdgeSet> list = new Vector<EdgeSet>();
        Enumeration<TupleSet> enm = table.elements();
        while(enm.hasMoreElements()) {
            TupleSet set = enm.nextElement();
            if(set instanceof EdgeSet) list.add((EdgeSet)set);
        }
        return list.elements();
    }
    
    ////////////////////////////////////////////
    
    public void print(OutputStream out) throws IOException {
        TupleSet set;
        Enumeration<TupleSet> enm;
        
        enm = allSets();
        while(enm.hasMoreElements()) {
            set = enm.nextElement();
            set.print(out);
        }
    }
}
