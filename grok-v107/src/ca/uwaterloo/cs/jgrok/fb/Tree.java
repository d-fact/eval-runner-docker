package ca.uwaterloo.cs.jgrok.fb;

import java.util.HashMap;
import java.util.ArrayList;

public class Tree {
    private int[] theRoots;
    private TupleList tlist;
    private MiniTable table;
    
    public Tree(EdgeSet eSet) {
        eSet.trySort(0);
        tlist = eSet.data;
        table = new MiniTable();
        this.init();
    }
    
    private void init() {
        int dom;
        int rng;
        int size;
        Tuple tup;
        Entry entry;
        
        size = tlist.size();
        for(int i = 0; i < size; i++) {
            tup = tlist.get(i);
            dom = tup.getDom();
            rng = tup.getRng();
            
            entry = table.putNode(dom);
            if(entry.outIndex == -1) entry.outIndex = i;
            
            entry = table.putNode(rng);
            entry.parent = dom;
        }
    }
    
    public int[] getRoots() {
        if(theRoots != null)
            return (int[])theRoots.clone();
        
        ArrayList<Entry> entryList = table.entryList;
        ArrayList<Entry> rootEntries = new ArrayList<Entry>(2);
        int count = entryList.size();
        
        Entry entry;
        for(int i = 0; i < count; i++) {
            entry = (Entry)entryList.get(i);
            if(entry.parent != -1) continue;
            rootEntries.add(entry);
        }
        
        count = rootEntries.size();
        theRoots = new int[count];
        for(int i = 0; i < count; i++) {
            entry = (Entry)rootEntries.get(i);
            theRoots[i] = entry.node;
        }
        
        return (int[])theRoots.clone();
    }
    
    public int getParent(int child) {
        Entry entry;
        
        entry = table.getNode(child);
        if(entry == null) return -1;
        else return entry.parent;
    }
    
    public int[] getChildren(int parent) {
        int dom = parent;
        Entry entry = table.getNode(dom);
        if(entry == null) return new int[0];

        if(entry.outIndex == -1) {
            return new int[0];
        } else {
            Tuple tup;
            int size = tlist.size();
            ArrayList<Tuple> childList = new ArrayList<Tuple>(5);
            for(int i = entry.outIndex; i < size; i++) {
                tup = tlist.get(i);
                if(tup.getDom() != dom) break;
                childList.add(tup);
            }
            
            int count = childList.size();
            int[] children = new int[count];
            for(int i = 0; i < count; i++) {
                tup = (Tuple)childList.get(i);
                children[i] = tup.getRng();
            }
            return children;
        }
    }
    
    public EdgeSet getLevelRelation() {
        EdgeSet l = new EdgeSet();
        ArrayList<Entry> list = table.entryList;
        
        setLevels();
        
        int size = list.size();
        for(int i = 0; i < size; i++) {
            Entry e = list.get(i);
            l.add(e.node, IDManager.getID(e.level+""));
        }
        return l;
    }
    
    void setRoot(int root) {
        theRoots = new int[1];
        theRoots[0] = root;
    }
    
    void setLevels() {
        int[] roots = getRoots();
        if(roots.length == 1) setLevel(roots[0], 0);
    }
    
    void setLevel(int node, int level) {
        Entry entry;
        entry = table.getNode(node);
        entry.level = level;
        
        int[] children = getChildren(node);
        for(int i = 0; i < children.length; i++) {
            setLevel(children[i], level + 1);
        }
    }
    
    Entry getEntry(int node) {
        return table.getNode(node);
    }
    
    static class MiniTable {
        int s_index = 0;
        ArrayList<Entry> entryList;
        HashMap<String,Entry> allStrings;
        
        MiniTable() {
            entryList = new ArrayList<Entry>(1997);
            allStrings = new HashMap<String,Entry>(1997, 0.75f);
               }
        
        Entry putNode(int node) {
            Entry entry;
            String name;
            
            name = node + "";
            entry = (Entry)allStrings.get(name);
            if(entry != null) return entry;
            
            entry = new Entry(node);
            entryList.add(s_index, entry);
            allStrings.put(name, entry);
            s_index++;
            
            return entry;
        }
        
        Entry getNode(int node) {
            String name;
            name = node + "";
            return (Entry)allStrings.get(name);
        }
    }
    
    static class Entry {
        int node;
        int level = -1;
        int parent = -1;
        int outIndex = -1;
        
        Entry(int node) {
            this.node = node;
        }
    }
}
