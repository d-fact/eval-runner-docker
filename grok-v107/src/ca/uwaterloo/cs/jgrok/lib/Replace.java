package ca.uwaterloo.cs.jgrok.lib;

import java.util.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 *   String   replace(String,   substitution)
 *   TupleSet replace(TupleSet, substitution)
 * </pre>
 *
 * <pre>
 * Example:
 *
 *     link = replace(link, "&0/[0-9]+/&1/")
 * </pre>
 */
public class Replace extends Function {
    private boolean flagReplaceAll = false;
    
    public Replace() 
    {
        name = "replace";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		if (vals.length != 2) {
			return illegalUsage();
		}
		
        String exp;
        TupleSet input;
        TupleSet result;
        
        flagReplaceAll = false;
        
        if(vals[0].isPrimitive()) {
            return stringReplace(vals);
        }
        
        input = vals[0].tupleSetValue();
        if(input.hasName())
            input = (TupleSet)input.clone();
        result = input.newSet();
        
        exp = vals[1].stringValue().trim();
        
        String parts[] = tokenize(exp);
        int col = parseColumn(parts[0]);
        String regxExp = parts[1];
        String replExp = parts[2];
        
        ArrayList<Object> replList = parse(replExp);
        int replSize = replList.size();
        
        Tuple tuple;
        TupleList data = input.getTupleList();
        StringBuffer buf = new StringBuffer();
        
        for(int i = 0; i < data.size(); i++) {
            tuple = data.get(i);
            
            if(col < tuple.size()) {
                buf.delete(0, buf.length());
                
                for(int j = 0; j < replSize; j++) {
                    Object o = replList.get(j);
                    if(o instanceof String)
                        buf.append(o);
                    else {
                        int v = ((Integer)o).intValue();
                        if(v < tuple.size())
                            buf.append(IDManager.get(tuple.get(v)));
                        else
                            throw new InvocationException("index " + v + " out of bounds " + tuple.size());
                    }
                }
                
                try {
                    String repl;
                    String sval;
                    
                    repl = buf.toString();
                    if(flagReplaceAll)
                        sval = IDManager.get(tuple.get(col)).replaceAll(regxExp, repl);
                    else
                        sval = IDManager.get(tuple.get(col)).replaceFirst(regxExp, repl);

                    tuple.set(col, IDManager.getID(sval));
                    result.add(tuple);
                } catch(Exception e) {
                    throw new InvocationException(e.getMessage());
                }
            }
        }
        
        return new Value(result);
    }
    
    private String[] tokenize(String exp)
        throws InvocationException {
        int len = exp.length();
        String[] result = new String[3];
        StringBuffer buf = new StringBuffer();
        
        char curr;
        char prev = '&';
        int i = 0, j = 0;
        for(; i < len && j < 3; i++) {
            curr = exp.charAt(i);
            if(curr == '/') {
                if(prev == '\\') {
                    prev = curr;
                    buf.append('/');
                } else {
                    result[j] = buf.toString();
                    buf.delete(0, buf.length());
                    j++;
                }
            } else {
                prev = curr;
                buf.append(curr);
            }
        }
        
        if(j == 2 && buf.length() > 0) {
            result[j] = buf.toString();
            j++;
        }
        
        String flag = exp.substring(i).trim();
        if(flag.length() > 0 && flag.charAt(0) == 'g') {
            flagReplaceAll = true;
        }
        
        if(j == 3) return result;
        throw new InvocationException("illegal replace expression: " + exp);
    }
    
    private int parseColumn(String colExp)
        throws InvocationException {
        String trim = colExp.trim();
        
        if(trim.length() == 0)
            throw new InvocationException(trim + "column not specified");
        
        if(trim.charAt(0) == '&') {
            trim = trim.substring(1);
            trim = trim.trim();
        }
        
        if(trim.length() == 0)
            throw new InvocationException(trim + "column not specified");
        
        int pos = 0;
        while(pos < trim.length()) {
            if(isDigit(trim.charAt(pos))) {
                pos++;
            } else {
                throw new InvocationException(trim + " is not int");
            }
        }
        
        try {
            int col = Integer.parseInt(trim);
            return col;
        } catch(NumberFormatException e) {
            throw new InvocationException(trim + " is not int");
        }
    }
    
    private ArrayList<Object> parse(String exp)
        throws InvocationException {
        String rest = exp;
        StringBuffer buf = new StringBuffer();
        ArrayList<Object> list = new ArrayList<Object>(2);
        
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
    
    private Value stringReplace(Value[] vals)
        throws InvocationException {
        String str;
        String exp;
        
        str = vals[0].toString();
        exp = vals[1].toString().trim();
        
        String parts[] = tokenize2(exp);
        String regxExp = parts[0];
        String replExp = parts[1];
        
        try {
            if(flagReplaceAll)
                return new Value(str.replaceAll(regxExp, replExp));
            else
                return new Value(str.replaceFirst(regxExp, replExp));
        } catch(Exception e) {
            throw new InvocationException(e.getMessage());
        }
    }
    
    private String[] tokenize2(String exp)
        throws InvocationException {
        int len = exp.length();
        String[] result = new String[2];
        StringBuffer buf = new StringBuffer();
        
        char curr;
        char prev = '/';
        int i = 0, j = 0;
        for(; i < len && j < 2; i++) {
            curr = exp.charAt(i);
            if(curr == '/') {
                if(i==0) continue;
                
                if(prev == '\\') {
                    prev = curr;
                    buf.append('/');
                } else {
                    result[j] = buf.toString();
                    buf.delete(0, buf.length());
                    j++;
                }
            } else {
                prev = curr;
                buf.append(curr);
            }
        }
        
        if(j == 1 && buf.length() > 0) {
            result[j] = buf.toString();
            j++;
        }
        
        String flag = exp.substring(i).trim();
        if(flag.length() > 0 && flag.charAt(0) == 'g') {
            flagReplaceAll = true;
        }
        
        if(j == 2) return result;
        throw new InvocationException("illegal replace expression: " + exp);
    }
    
    public String usage()
    {
		return "String " + name + "(String source, String regex)/TupleSet " + name + "(TupleSet set, String regex)";
	}	
}
