package ca.uwaterloo.cs.jgrok.lib;

import java.util.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * TupleSet form(TupleSet, description, ...)
 *
 * <pre>
 * Examples:
 *
 *     form(relation, "(&0 &1)")
 *     form(relation, "(use &0 &2)", "&1")
 * </pre>
 */
public class Form extends Function {
    
    public Form() {
        name = "form";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        if (vals.length > 0) {
            TupleSet input = vals[0].tupleSetValue();
            if (vals.length == 1) {
                return new Value(input.newSet());
            }
            return makeValue(vals);
        }
        return(illegalUsage());
    }
    
    @SuppressWarnings(value={"unchecked"})
    private Value makeValue(Value[] vals) throws InvocationException 
    {
        TupleSet input;
        TupleSet result;
        
        int colCount = vals.length - 1;
        String[] cols = new String[colCount];
        for(int i = 1; i < vals.length; i++) {
            Object o = vals[i].objectValue();
            if(o instanceof String)
                cols[i-1] = (String)o;
            else
                throw new InvocationException("argument " +(i+1)+ " is not " + Type.findName(String.class));
        }

        input = (TupleSet)vals[0].objectValue();
        switch(colCount) {
        case 1:
            result = new NodeSet();
            break;
        case 2:
            result = new EdgeSet();
            break;
        default:
            result = new TupleSet();
        }
        
        String[] colVals = new String[colCount];
        ArrayList<Object>[] colLists = new ArrayList[colCount];
        // Get list for each column;
        for(int i = 0; i < colCount; i++) {
            colLists[i] = parse(cols[i]);
        }
        
        ArrayList<Object> colList;
        StringBuffer colBuf;
        colBuf = new StringBuffer();
        
        TupleList data = input.getTupleList();
        for(int i = 0; i < data.size(); i++) {
            Tuple tuple = data.get(i);
            
            for(int j = 0; j < colCount; j++) {
                colBuf.delete(0, colBuf.length());
                colList = colLists[j];
                
                for(int k = 0; k < colList.size(); k++) {
                    Object o = colList.get(k);
                    if(o instanceof String)
                        colBuf.append(o);
                    else {
                        int v = ((Integer)o).intValue();
                        if(v < tuple.size())
                            colBuf.append(IDManager.get(tuple.get(v)));
                        else
                            throw new InvocationException("index " + v + " out of bounds " + tuple.size());
                    }
                }
                
                colVals[j] = colBuf.toString();
            }
            
            result.add(TupleFactory.create(colVals));
        }
        
        return new Value(result);
    }
    
    private ArrayList<Object> parse(String exp)
        throws InvocationException {
        String rest = exp;
        StringBuffer buf = new StringBuffer();
        ArrayList<Object> list = new ArrayList<Object>(5);
        
        int pos, len;
        pos = rest.indexOf("&");
        len = rest.length();
        
        while(pos >= 0) {
            list.add(rest.substring(0, pos));
            pos++;
            rest = rest.substring(pos, len);
            len = rest.length();
            
            buf.delete(0, buf.length());
            for(int i = 0; i < len; i++) {
                char c = rest.charAt(i);
                if(isDigit(c)) buf.append(c);
                else break;
            }
            
            if(buf.length() > 0) {
                try {
                    Integer I = new Integer(buf.toString());
                    list.add(I);
                } catch(NumberFormatException e) {
                    throw new InvocationException(buf + " is not int");
                }
            }
            
            rest = rest.substring(buf.length(), len);
            len = rest.length();
            pos = rest.indexOf("&");
        }
        
        if(rest.length() > 0) list.add(rest);
        
        return list;
    }
    
    private boolean isDigit(char c) {
        if('0' <= c && c <= '9') return true;
        else return false;
    }
    
	public String usage()
    {
		return "TupleSet form(TupleSet Relation [, String description]*)";
	}
    
}
