package ca.uwaterloo.cs.jgrok.lib.time;

import java.util.Date;
import java.text.DateFormat;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.InvocationException;

/**
 * <pre>
 *     string datetime(long, [int], [int]);
 * </pre>
 */
public class DateTime extends Function {
    
    static Date			m_date        = new Date();
    static DateFormat[]	m_dateFormats = new DateFormat[25];
  
    public DateTime() 
    {
        name          = "dateTime";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException {
        
        if (vals.length < 1 || vals.length > 3) {
			return illegalUsage();
		}
		
        try {
            int			time  = -1;
            int			date;
            int			mode;
            DateFormat	dateFormat;
            long		millsecs;
                
            millsecs = vals[0].longValue();
            switch(vals.length) {
	        case 3:
				time   = vals[2].intValue();
				if (time < 0 || time > 3) {
					time = -1;
				}
            case 2:
 				date   = vals[1].intValue();
 				if (date < 0 || date > 3) {
 					date = -1;
 				}
 				break;
 			default:
 				date = 0;
 				time = 0;
 			}
 			
 			mode = ((date + 1) * 5) + time + 1;
 			
			dateFormat = m_dateFormats[mode];
			if (dateFormat == null) {
				if (0 <= date) {
					if (0 <= time) {
						dateFormat = DateFormat.getDateTimeInstance(date, time);
					} else {
						dateFormat = DateFormat.getDateInstance(date);
					}
				} else {
					if (0 <= time) {
						dateFormat = DateFormat.getTimeInstance(time);
					} else {
						return new Value("");
				}	}
				m_dateFormats[mode] = dateFormat;
			}
			m_date.setTime(millsecs);
            return new Value(dateFormat.format(m_date));
        } catch(Exception e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    public String usage()
    {
		return "String " + name + "(long, [int], [int])";
	}
}
