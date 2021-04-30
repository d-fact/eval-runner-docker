package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.env.Tracing;
import ca.uwaterloo.cs.jgrok.env.TracingEvent;

import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     boolean trace();
 *     boolean trace(boolean);
 *	   boolean trace(string);
 *     boolean trace(string, boolean);
 *
 * </pre>
 */
public class Trace extends Function {
    
    public Trace() 
    {
        name = "trace";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		String		eventName = null;
		boolean		state     = false;
		boolean		stateSeen = false;
		boolean		ret       = false;
		
		switch (vals.length) {
		case 2:
					
			state = vals[1].booleanValue();
			stateSeen = true;
		case 1:
			Value	value0 = vals[0];
			
			if (!stateSeen && value0.isBoolean()) {
				state     = value0.booleanValue();
				stateSeen = true;
			} else {
				eventName = value0.stringValue();
			}
		case 0:
	        Tracing			trc = env.getTracing();
	        TracingEvent	event;

			if (stateSeen) {
				ret = state;
				if (eventName != null) {
					if (eventName.length() == 0) {
						if (!state) {
							trc.clearEvents();
						} else {
							Enumeration<TracingEvent> en = trc.elements();
		
							while (en.hasMoreElements()) {
								event = en.nextElement();
								event.setEnabled(true);
							}
						}
					} else {
						event = trc.get(eventName);
					
						if (event == null) {
							event = new TracingEvent(eventName, state);
							trc.add(event);
						} else {
							event.setEnabled(state);
					}	}
				} else {
					trc.setEnabled(state);
            }	}
            ret = trc.isEnabled();
			env.out.println("Tracing " + trc.getState());
            if (eventName != null) {
				if (eventName.length() == 0) {
					Enumeration<TracingEvent> en = trc.elements();
		
					while (en.hasMoreElements()) {
						event = en.nextElement();
						if (event.isEnabled()) {
							ret = true;
						}
						env.out.println(event);
					}
				} else {
					event = trc.get(eventName);
					if (event != null) {
						env.out.println(event);
						ret = event.isEnabled();
					} else {
						ret = false;
				}	}
				break;
			}
		}
		return new Value(ret);
    }
    
    public String usage()
    {
		return "boolean " + name + "([String event] [, boolean state])";
	}
}
