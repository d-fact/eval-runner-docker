package ca.uwaterloo.cs.jgrok.fb;

import java.util.StringTokenizer;

public final class IDManager {
    private static StringTable strTable;
    private static StringBuffer dataBuffer;
    
    static {
        dataBuffer = new StringBuffer();
        strTable = StringTable.instance();
    }
    
    public static int getID(Object o) {
        if(o == null)
            return getID((String)null);
        else
            return getID(o.toString());
    }
    
    public static int getID(String s) {
        return strTable.add(s);
    }
    
    public static String get(int ID) {
        String s;
        s = strTable.get(ID);
        if(strTable.isComposite(ID)) return parse(s);
        else return s;
    }
    
    public static boolean isComposite(int ID) {
        return strTable.isComposite(ID);
    }
    
    public static int[] parse(int ID) {
        int[] result;
        
        if(strTable.isComposite(ID)) {
            String s = strTable.get(ID);
            StringTokenizer st = new StringTokenizer(s, ":");
            result = new int[st.countTokens()];
            
            int i = 0;
            while(st.hasMoreTokens()) {
                try {
                    result[i] = Integer.parseInt(st.nextToken());
                    i++;
                } catch(NumberFormatException e) {
                    result = new int[1];
                    result[0] = ID;
                    break;
                }
            }
        } else {
            result = new int[1];
            result[0] = ID;
        }
        
        return result;
    }
    
    public static int getID(int[] ids) {
        int i = 0;
        StringBuffer buf;
        buf = new StringBuffer();
        for(; i < ids.length; i++) {
            buf.append(ids[i]);
            buf.append(":");
        }
        
        if(i > 1) buf.delete(buf.length() - 1, buf.length());
        return strTable.addComposite(buf.toString());
    }
    
    public static int getID(int n1, int n2) {
        return strTable.addComposite(n1 + ":" + n2);
    }
    
    public static int getID(int n1, int n2, int n3) {
        return strTable.addComposite(n1 + ":" + n2 + ":" + n3);
    }
    
    public static int getReplaceID(int ID) {
        if(strTable.isComposite(ID)) {
            int[] ids = parse(ID);
            for(int i = 0; i < ids.length; i++) {
                ids[i] = getReplaceID(ids[i]);
            }
            return getID(ids);
        } else {
            return strTable.getReplace(ID);
        }
    }
    
    public static EdgeSet getID() {
        EdgeSet all = new EdgeSet();
        all.data = strTable.getID();
        all.sortLevel = 2;
        return all;
    }
    
    public static NodeSet getENT() {
        NodeSet all = new NodeSet();
        all.data = strTable.getENT();
        all.sortLevel = 0;
        return all;
    }
    
    public static NodeSet getAllCompositeIDs() {
        NodeSet comIDs = new NodeSet();
        comIDs.data = strTable.getAllComposites();
        return comIDs;
    }
    
    private static String parse(String s) {
        int num;
        StringTokenizer st;
        
        dataBuffer.delete(0, dataBuffer.length());
        dataBuffer.append('(');
        
        st = new StringTokenizer(s, ":");
        while(st.hasMoreTokens()) {
            try {
                num = Integer.parseInt(st.nextToken());
                dataBuffer.append(get(num));
                dataBuffer.append(' ');
            } catch(NumberFormatException e) {
                return null;
            }
        }
        
        dataBuffer.delete(dataBuffer.length()-1, dataBuffer.length());
        dataBuffer.append(')');
        
        return dataBuffer.toString();
    }

}
