package ca.uwaterloo.cs.jgrok.util;

public class Timing {
    private long bgnTime;
    private long endTime;
    
    public Timing() {
        bgnTime = System.currentTimeMillis();
        endTime = bgnTime;
    }
    
    public long start() {
        bgnTime = System.currentTimeMillis();
        endTime = bgnTime;
        return bgnTime;
    }

    public long stop() {
        endTime = System.currentTimeMillis();
        return endTime;
    } 
    
    public double getTime() {
        return ((double)(endTime - bgnTime))/1000.0;
    }
    
    public double getElapsedTime() {
        long curTime = System.currentTimeMillis();
        return ((double)(curTime - bgnTime))/1000.0;
    }
}
