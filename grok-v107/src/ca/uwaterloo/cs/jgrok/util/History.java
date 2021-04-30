package ca.uwaterloo.cs.jgrok.util;

import java.util.ArrayList;

public class History {

    private ArrayList<String> list;
    
    public History() {
        list = new ArrayList<String>(100);
    }
    
    public int size() {
        return list.size();
    }
    
    public void clear() {
        list = new ArrayList<String>(100);
    }
    
    public void add(String line) {
        if(line != null) {
            if(line.trim().length() > 0) 
                list.add(line);
        }
    }
    
    public String get(int index) {
        if (0 <= index && index < list.size())
            return list.get(index);
        else
            return null;
    }
}
