package ca.uwaterloo.cs.jgrok.interp;

public class Option {
    protected int modifier;
    public static int time  = 0x0001;
    public static int echo  = 0x0002;
    public static int pause = 0x0004;
    
    public Option() {
        modifier = 0;
    }
    
    public Option(int modifier) {
        if(modifier < 0)
            this.modifier = 0;
        else
            this.modifier = modifier;
    }
    
    public void setTimeOn(boolean b) {
        if(b) modifier = modifier | time;
        else {
            if(isTimeOn()) modifier = modifier - time;
        }
    }
    
    public boolean isTimeOn() {
        return (modifier & time) == time;
    }
    
    public void setEchoOn(boolean b) {
        if(b) modifier = modifier | echo;
        else {
            if(isEchoOn()) modifier = modifier - echo;
        }
    }
    
    public boolean isEchoOn() {
        return (modifier & echo) == echo;
    }
    
    public void setPauseOn(boolean b) {
        if(b) modifier = modifier | pause;
        else {
            if(isPauseOn()) modifier = modifier - pause;
        }
    }
    
    public boolean isPauseOn() {
        return (modifier & pause) == pause;
    }
}
