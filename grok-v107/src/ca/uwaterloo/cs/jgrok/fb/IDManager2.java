package ca.uwaterloo.cs.jgrok.fb;

import java.util.StringTokenizer;
import ca.uwaterloo.cs.jgrok.util.Bytes;

public class IDManager2 {

    private static StringTable strTable;
    private static String tag = "[!]";
    private static StringBuffer buffer;
    private static StringBuffer dataBuffer;
    
    static {
        buffer = new StringBuffer(tag);
        dataBuffer = new StringBuffer();
        strTable = StringTable.instance();
    }
    
    public static int getID(Object o) {
        return getID(o.toString());
    }
 
    public static int getID(String s) {
        return strTable.add(s);
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
    
    public static String get(int ID) {
        String s;
        String composite;
        
        s = strTable.get(ID);
        if(s == null) return s;
        
        if(!s.startsWith(tag)) {
            return s;
        } else {
            composite = parse(s.substring(2));
            if(composite == null) return s;
            return composite;
        }
    }
    
    public static int getID(int n1, int n2) {
        buffer.delete(2, buffer.length());
        buffer.append(Bytes.encode(n1));
        buffer.append(Bytes.encode(n2));
        return strTable.add(buffer.toString());
    }
    
    public static int getID(int n1, int n2, int n3) {
        buffer.delete(2, buffer.length());
        buffer.append(Bytes.encode(n1));
        buffer.append(Bytes.encode(n2));
        buffer.append(Bytes.encode(n3));
        return strTable.add(buffer.toString());
    }
    
    private static String parse(String s) {
        byte[] a = s.getBytes();
        byte[] b = new byte[4];
        
        if(a.length%4 == 0) {
            dataBuffer.delete(0, dataBuffer.length());
            
            dataBuffer.append('(');
            for(int i = 0; i < a.length; ) {
                b[0] = a[i];
                i++;
                b[1] = a[i];
                i++;
                b[2] = a[i];
                i++;
                b[3] = a[i];
                i++;
                
                dataBuffer.append(get(Bytes.toInt(b)));
                dataBuffer.append(' ');
            }
            
            dataBuffer.delete(dataBuffer.length()-2, dataBuffer.length());
            dataBuffer.append(')');
            return dataBuffer.toString();
        } else {
            return null;
        }
    }
    
}
