package ca.uwaterloo.cs.jgrok.env;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.InputStream;
import java.io.PrintStream;

public class Tracing {
    public InputStream in  = System.in;
    public PrintStream out = System.out;
    public PrintStream err = System.err;
    
    private boolean enabled;
    private Hashtable<String, TracingEvent> evtMap;
    
    /**
     * Private constructor for Tracing.
     */
    public Tracing() {
        enabled = false;
        evtMap = new Hashtable<String, TracingEvent>();
    }
    
    public void clearEvents() 
    {
        evtMap.clear();
    }
    
    public Enumeration<TracingEvent> elements()
    {
		return evtMap.elements();
	}
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean b) {
        this.enabled = b;
    }
    
    public void setState(String state) {
        if (state == null) {
            setEnabled(false);
        } else if (state.equalsIgnoreCase(TracingEvent.STATE_OFF)) {
            setEnabled(false);
        } else if (state.equalsIgnoreCase(TracingEvent.STATE_ON)) {
            setEnabled(true);
        } else {
            throw new IllegalArgumentException("state");
        }
    }
    
    public String getState() {
        return (this.enabled ? TracingEvent.STATE_ON : TracingEvent.STATE_OFF);
    }

    public TracingEvent get(String eventName) {
        if (eventName == null) {
            return null;
        } 
        String key = eventName.toLowerCase();
        return evtMap.get(key);
    }
    
    public boolean add(TracingEvent event) {
        boolean ret = false;
        
        if(event != null) {
            String key = event.getName().toLowerCase();
            if(!evtMap.containsKey(key)) {
                evtMap.put(key, event);
                ret = true;
            }
        }
        
        return ret;
    }
    
    public boolean remove(TracingEvent event) {
        boolean ret = false;
        
        if(event != null) {
            String key = event.getName().toLowerCase();
            if(evtMap.containsKey(key)) {
                evtMap.remove(key);
                ret = true;
            }
        }
        
        return ret;
    }
    
    public boolean traceOn(String eventName) {
        if(isEnabled()) {
            if (eventName == null) {
                return false;
            } 
            String key = eventName.toLowerCase();
            return evtMap.containsKey(key) && evtMap.get(key).isEnabled();
        }
        return false;
     }

    public boolean traceOn(TracingEvent event) {
        if(isEnabled()) {
            String key = event.getName().toLowerCase();
            return evtMap.containsKey(key) && evtMap.get(key).isEnabled();
        } else {
            return false;
        }
    }
    
    /**
     * Prints a message to standard Tracing output.
     * 
     * @param eventName
     * @param message
     */
    public void printMessage(String eventName, String message) {
        if(traceOn(eventName)) {
            out.print(message);
        }
    }
    
    /**
     * Prints an error Message to standard Tracing error output.
     *  
     * @param eventName
     * @param message
     */
    public void printError(String eventName, String message) {
        if(traceOn(eventName)) {
            err.print(message);
        }
    }
    
    /**
     * Prints a message to the user specified print stream.
     * 
     * @param ps User specified print stream
     * @param eventName
     * @param message
     */
    public void printMessage(PrintStream ps, String eventName, String message) {
        if(traceOn(eventName)) {
            ps.print(message);
        }
    }
}
