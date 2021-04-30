package ca.uwaterloo.cs.jgrok.fb;

import java.util.HashMap;

public class Header {
    Column[] cols;
    
    public Header(Column[] cols) {
        this.cols = cols;
    }
    
    public int size() {
        return cols.length;
    }
    
    public Column get(int i) {
        return cols[i];
    }
    
    public boolean contains(Column col) {
        return locate(col) > -1;
    }
    
    public int locate(Column col) {
        if(col == null) return -1;
        for(int i = 0; i < cols.length; i++) {
            if(cols[i].equals(col)) return i;
        }
        return -1;
    }
    
    public Header union(Header h) {
        HashMap<String,Column> map = new HashMap<String,Column>(3);
        
        int i = 0;
        String key;
        Column col;
        for(i = 0; i < cols.length; i++) {
            col = cols[i];
            key = col.getName();
            if(!map.containsKey(key)) map.put(key, col);
        }
        
        for(i = 0; i < h.cols.length; i++) {
            col = h.cols[i];
            key = col.getName();
            if(!map.containsKey(key)) map.put(key, col);
        }
        
        Column[] result = new Column[map.size()];
        map.values().toArray(result);
        return new Header(result);
    }
    
    public Header intersect(Header h) {
        HashMap<String,Column> map1 = new HashMap<String,Column>(5);
        HashMap<String,Column> map2 = new HashMap<String,Column>(5);
        
        int i = 0;
        String key;
        Column col;
        for(i = 0; i < cols.length; i++) {
            col = cols[i];
            key = col.getName();
            if(!map1.containsKey(key)) map1.put(key, col);
        }
        
        for(i = 0; i < h.cols.length; i++) {
            col = h.cols[i];
            key = col.getName();
            if(!map2.containsKey(key)) map2.put(key, col);
        }
        
        String[] keys = new String[map1.size()];
        map1.keySet().toArray(keys);
        for(i = 0; i < keys.length; i++) {
            key = keys[i];
            if(!map2.containsKey(key)) map1.remove(key);
        }
        
        Column[] result = new Column[map1.size()];
        map1.values().toArray(result);
        return new Header(result);
    }

    public Header difference(Header h) {
        HashMap<String,Column> map1 = new HashMap<String,Column>(3);
        HashMap<String,Column> map2 = new HashMap<String,Column>(3);
        
        int i = 0;
        String key;
        Column col;
        for(i = 0; i < cols.length; i++) {
            col = cols[i];
            key = col.getName();
            if(!map1.containsKey(key)) map1.put(key, col);
        }
        
        for(i = 0; i < h.cols.length; i++) {
            col = h.cols[i];
            key = col.getName();
            if(!map2.containsKey(key)) map2.put(key, col);
        }
        
        String[] keys = new String[map1.size()];
        map1.keySet().toArray(keys);
        for(i = 0; i < keys.length; i++) {
            key = keys[i];
            if(map2.containsKey(key)) map1.remove(key);
        }
        
        Column[] result = new Column[map1.size()];
        map1.values().toArray(result);
        return new Header(result);
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for(int i = 0; i < cols.length; i++) {
            if(i > 0) buf.append(' ');
            buf.append(cols[i].getName());
        }
        buf.append(')');
        return buf.toString();
    }
}
