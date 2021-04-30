package ca.uwaterloo.cs.jgrok.env;

public class TracingEvent implements Comparable<TracingEvent> {
    private String name;
    private boolean enabled = false;
    
    public static final String STATE_ON = "on";
    public static final String STATE_OFF = "off";
    
    public static final String TRACING_FUNCTIONLIB = "FunctionLib";
    
    public TracingEvent(String name) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        
        this.name = name;
    }
    
    public TracingEvent(String name, boolean state) {
        this(name);
        this.setEnabled(state);
    }
    
    public TracingEvent(String name, String state) {
        this(name);
        this.setState(state);
    }
    
    public int compareTo(TracingEvent event) {
        if (event == null) {
            throw new NullPointerException("event");
        }
        
        return name.compareToIgnoreCase(event.name);
    }

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        if(name == null)
            throw new NullPointerException("name");
        
        this.name = name;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }
    
    public void setState(String state) {
        if (state == null) {
            this.enabled = false;
        } else if (state.equalsIgnoreCase(STATE_OFF)) {
            this.enabled = false;
        } else if (state.equalsIgnoreCase(STATE_ON)) {
            this.enabled = true;
        } else {
            throw new IllegalArgumentException("state");
        }
    }
    
    public String getState() {
        return (this.enabled ? STATE_ON : STATE_OFF);
    }
    
    public void on() {
        setEnabled(true);
    }
    
    public void off() {
        setEnabled(false);
    }
    
    public String toString() {
        return "Tracing " + this.name + "=" + getState();
    }
}
