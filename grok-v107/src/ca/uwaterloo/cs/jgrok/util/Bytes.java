package ca.uwaterloo.cs.jgrok.util;

public class Bytes {
    
    public static String encode(int n) {
        return new String(toBytes(n));
    }
    
    public static String encode(long n) {
        return new String(toBytes(n));
    }
    
    public static int decodeInt(String s) {
        return toInt(s.getBytes());
    }
    
    public static long decodeLong(String s) {
        return toLong(s.getBytes());
    }
    
    public static int toInt(byte[] b) {
        return
            (((int) b[3]) & 0xFF) +
            ((((int) b[2]) & 0xFF) << 8) +
            ((((int) b[1]) & 0xFF) << 16) +
            ((((int) b[0]) & 0xFF) << 24);
    }
    
    public static long toLong(byte[] b) {
        return 
            (((long) b[7]) & 0xFF) +
            ((((long) b[6]) & 0xFF) << 8) +
            ((((long) b[5]) & 0xFF) << 16) +
            ((((long) b[4]) & 0xFF) << 24) +
            ((((long) b[3]) & 0xFF) << 32) +
            ((((long) b[2]) & 0xFF) << 40) +
            ((((long) b[1]) & 0xFF) << 48) +
            ((((long) b[0]) & 0xFF) << 56);
    }
    
    public static byte[] toBytes(int n) {
        byte[] b = new byte[4];
        
        b[3] = (byte) n;
        n >>>= 8;
        b[2] = (byte) n;
        n >>>= 8;
        b[1] = (byte) n;
        n >>>= 8;
        b[0] = (byte) n;
        
        return b;
    }
    
    public static byte[] toBytes(long n) {
        byte[] b = new byte[4];
        
        b[7] = (byte) (n);
        n >>>= 8;
        b[6] = (byte) (n);
        n >>>= 8;
        b[5] = (byte) (n);
        n >>>= 8;
        b[4] = (byte) (n);
        n >>>= 8;
        b[3] = (byte) (n);
        n >>>= 8;
        b[2] = (byte) (n);
        n >>>= 8;
        b[1] = (byte) (n);
        n >>>= 8;
        b[0] = (byte) (n);
        
        return b;
    }
}
