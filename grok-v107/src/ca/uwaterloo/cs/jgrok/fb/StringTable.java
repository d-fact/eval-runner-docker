package ca.uwaterloo.cs.jgrok.fb;

import java.util.*;

class StringTable {
    private static int s_index = 0;
    private static StringTable instance;
    private ArrayList<StringCell> cellList;
    private HashMap<String,StringCell> allStrings;
    
    static {
        // Prime Number:
        // 97 101 997 1997 2003 4999 9967
        // 9973 10007 49999 100003 500009
        instance = new StringTable(100003);
    }
    
    static StringTable instance() {
        return instance;
    }
    
    private StringTable(int initialCapacity) {
        cellList = new ArrayList<StringCell>(initialCapacity);
        allStrings = new HashMap<String,StringCell>(initialCapacity, 0.75f);
        
        // Add null at index 0
        add(null);
    }
    
    int size() {
        return cellList.size();
    }
    
    int add(String data) {
        StringCell cell;
        cell = allStrings.get(data);
        if(cell != null) return cell.index;
        
        cell = new StringCell(s_index, data);
        cellList.add(s_index, cell);
        allStrings.put(data, cell);
        s_index++;
        
        return cell.index;
    }
    
    int addComposite(String data) {
        StringCell cell;
        cell = allStrings.get(data);
        if(cell != null) return cell.index;
        
        cell = new StringCell(s_index, data);
        cell.setComposite(true);
        
        cellList.add(s_index, cell);
        allStrings.put(data, cell);
        s_index++;
        
        return cell.index;
    }
    
    String get(int index) {
        StringCell cell;
        cell = cellList.get(index);
        return cell.data;
    }
    
    int getReplace(int index) {
        StringCell cell;
        cell = cellList.get(index);
        return cell.replace;
    }
    
    void setReplace(int index, int replace) {
        StringCell cell;
        cell = cellList.get(index);
        cell.replace = replace;
    }
    
    void initReplace(TupleList pairIDs) {
        Tuple t;
        int count;
        
        count = pairIDs.size();
        for(int i = 0; i < count; i++) {
            t = pairIDs.get(i);
            setReplace(t.getDom(), t.getRng());
        }
    }
    
    void closeReplace() {
        for(int i = 0; i < s_index; i++) {
            setReplace(i, i);
        }
    }
    
    boolean isComposite(int index) {
        StringCell cell;
        cell = cellList.get(index);
        return cell.isComposite();
    }
    
    TupleList getID() {
        TupleList t_l = new TupleList(s_index);
        
        // ignore null at index 0
        for(int i = 1; i < s_index; i++) {
            t_l.add(new Tuple4Edge(i, i));
        }
        return t_l;
    }
    
    TupleList getENT() {
        TupleList t_l = new TupleList(s_index);
        
        // ignore null at index 0
        for(int i = 1; i < s_index; i++) {
            t_l.add(new Tuple4Node(i));
        }
        return t_l;
    }
    
    TupleList getAllComposites() {
        TupleList t_l = new TupleList();
        for(int i = 0; i < s_index; i++) {
            if(isComposite(i)) t_l.add(new Tuple4Node(i));
        }
        return t_l;
    }
    
    private static class StringCell {
        int flag = 0;
        int index = 0;
        int replace = 0;
        String data = null;
        static final int FLAG_COMPOSITE = 1;
        
        StringCell(int index, String data) {
            this.data = data;
            this.index = index;
            this.replace = index;
        }
        
        boolean isComposite() {
            return (FLAG_COMPOSITE == (flag & FLAG_COMPOSITE));
        }
        
        void setComposite(boolean b) {
            flag = flag | FLAG_COMPOSITE;
        }
    }
}
