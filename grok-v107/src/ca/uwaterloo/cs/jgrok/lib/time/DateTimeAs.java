package ca.uwaterloo.cs.jgrok.lib.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     string datetimeas(long, [pattern]);
 * </pre>
 */
public class DateTimeAs extends Function {
    
    static Date				m_date        = new Date();
    static String			m_lastPattern = null;
    static SimpleDateFormat	m_lastFormat  = null;
  
    public DateTimeAs() 
    {
        name = "dateTimeAs";
	}
       
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		try {
            String				pattern = null;
            SimpleDateFormat	dateFormat;
            long				secs;
                
            switch(vals.length) {
           	case 1:
				secs = vals[0].longValue();
				if (m_lastFormat == null) {
					dateFormat = m_lastFormat;
					if (dateFormat == null) {
						m_lastFormat = dateFormat = new SimpleDateFormat();
					}
					break;
				}
				dateFormat = new SimpleDateFormat();
				m_lastPattern = null;
				m_lastFormat  = dateFormat;
				break;
			case 2:
	            secs = vals[0].longValue();
				pattern = vals[1].toString();
				if (m_lastPattern != null && pattern.equals(m_lastPattern)) {
					dateFormat = m_lastFormat;
					break;
				}
				dateFormat = new SimpleDateFormat(pattern);
				m_lastPattern = pattern;
				m_lastFormat  = dateFormat;
				break;
			default:
				return (illegalUsage());
			}
			secs *= 1000;
          	m_date.setTime(secs);
            return new Value(dateFormat.format(m_date));
        } catch(Exception e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    public String usage()
    {
		return "String " + name + "(long, [pattern])";
	}
}
