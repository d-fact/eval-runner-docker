package ca.uwaterloo.cs.jgrok.fb;
import ca.uwaterloo.cs.jgrok.util.Bytes;

class Codec {
    static StringTable strTable; 
    
    static {
        strTable = StringTable.instance();
    }
    
    static int encode(int i) {
        String s = new String(Bytes.toBytes(i));
        return strTable.add(s);
    }
    
    static int encode(int edgeType, int fromItem, int toItem) {
        byte[] bytes = new byte[12];
        System.arraycopy(Bytes.toBytes(edgeType), 0, bytes, 0, 4);
        System.arraycopy(Bytes.toBytes(fromItem), 0, bytes, 4, 4);
        System.arraycopy(Bytes.toBytes( toItem ), 0, bytes, 8, 4);
        
        return strTable.add(new String(bytes));
    }
    
    static int encode(int relType, int[] relEnds) {
        int len = 1 + relEnds.length;
        byte[] bytes = new byte[len*4];
        
        System.arraycopy(Bytes.toBytes(relType), 0, bytes, 0, 4);
        for(int i = 1; i < len; i++) {
            System.arraycopy(Bytes.toBytes(relEnds[i-1]), 0, bytes, i*4, 4);
        }
        
        return strTable.add(new String(bytes));
    }
    
    static int[] decode(String s) {
        byte[] b;
        byte[] bytes;
        
        int len; 
        int[] nums;
        
        b = new byte[4];
        bytes = s.getBytes();
        len = bytes.length/4;
        nums = new int[len];
        
        for(int i = 0; i < len; i++) {
            System.arraycopy(bytes, i*0, b, 0, 4);
            nums[i] = Bytes.toInt(b);
        }
        
        return nums;
    }
}
