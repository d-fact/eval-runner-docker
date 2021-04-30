package ca.uwaterloo.cs.jgrok.interp;

import java.io.PrintStream;
import ca.uwaterloo.cs.jgrok.env.Clazz;
import ca.uwaterloo.cs.jgrok.fb.TupleSet;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;

public class Value {
    Object val;
    Class<?> primitiveType;
    
    public static final Value VOID = new Value();
    public static final Value EVAL = new Value();
    
    static {
        VOID.primitiveType = void.class;
        EVAL.primitiveType = void.class;
    }
    
    protected Value() {
        val = new Object();
        primitiveType = null;
    }
    
    public Value(int i) {
        val = new Integer(i);
        primitiveType = int.class;
    }
    
    public Value(long l) {
        val = new Long(l);
        primitiveType = long.class;
    }
    
    public Value(short s) {
        val = new Short(s);
        primitiveType = short.class;
    }
    
    public Value(float f) {
        val = new Float(f);
        primitiveType = float.class;
    }

    public Value(double d) {
        val = new Double(d);
        primitiveType = double.class;
    }
    
    public Value(boolean b) {
        val = new Boolean(b);
        primitiveType = boolean.class;
    }
    
    public Value(Object o) {
        val = o;
        primitiveType = null;
    }
    
    public Value(Object o, Class<?> clazz) {
        val = o;
        if(Clazz.isJavaPrimitive(clazz)) {
            primitiveType = clazz;
        } else {
            primitiveType = null;
        }
    }
    
    public Class<?> getType() {
        if(primitiveType != null) {
            return primitiveType;
        } 
        return val.getClass();
    }
    
    public String getTypeName()
    {
		return Type.findName(getType());
	}
	
    /**
     * Tests Primitives and String.
     */
    
    public boolean isBoolean()
    {
		return (primitiveType == boolean.class);
	}
         
    public boolean isNumeric()
    {
		return (primitiveType != null);
	}
     
    public boolean isString()
    {
		return (val.getClass() == String.class);
	}
	
    public boolean isPrimitive() 
    {
        return (isNumeric() || isString());
    }
    
    public boolean isNodeSet()
    {
		return (val instanceof NodeSet);
	}
   
    public boolean isEdgeSet()
    {
		return (val instanceof EdgeSet);
	}
	
	public String toString() {
        if(val == null) {
			return "null";
		}
        return val.toString();
    }
    
    public void cantConvert(String type) throws NumberFormatException
    {
	    throw new NumberFormatException("Can't convert " + getTypeName() + "[" + toString() +"] to " + type); 
    }	

    public boolean booleanValue() throws NumberFormatException 
    {
		if (primitiveType == boolean.class) {
			return ((Boolean)val).booleanValue();
		}
	    cantConvert("boolean"); 
	    return false;
    }
    
    public short shortValue() throws NumberFormatException
    {
        if(primitiveType == short.class) {
	        return ((Short)val).shortValue();
	    }
		if (primitiveType == int.class){
			int ret = ((Integer)val).intValue();
			
			if (ret >= Short.MIN_VALUE && ret <= Short.MAX_VALUE) {
				return (short) ret;
        }	}
        if (primitiveType == long.class){
			long ret = 	((Long)val).longValue();
			
			if (ret >= Short.MIN_VALUE && ret <= Short.MAX_VALUE) {
				return (short) ret;
        }	}
		if (primitiveType == float.class) {
			float ret = ((Float)val).floatValue();
			
			if (ret >= Short.MIN_VALUE && ret <= Short.MAX_VALUE) {
				return (short) ret;
        }	}
        if (primitiveType == double.class) {
			double ret = ((Double)val).doubleValue();
			if (ret >= Short.MIN_VALUE && ret <= Short.MAX_VALUE) {
				return (short) ret;
        }	}
 		if (primitiveType == String.class) {
	        return Short.parseShort(toString());
	    }
		cantConvert("short");
		return 0;
	}
    
     public int intValue() throws NumberFormatException 
     {
		if (primitiveType == int.class){
			return ((Integer)val).intValue();
		}
        if (primitiveType == long.class){
			long ret = 	((Long)val).longValue();
			
			if (ret >= Integer.MIN_VALUE && ret <= Integer.MAX_VALUE) {
				return (int) ret;
        }	}
        if(primitiveType == short.class) {
	        return ((Short)val).shortValue();
	    }
		if (primitiveType == float.class) {
			float ret = ((Float)val).floatValue();
			
			if (ret >= Integer.MIN_VALUE && ret <= Integer.MAX_VALUE) {
				return (int) ret;
        }	}
        if (primitiveType == double.class) {
			double ret = ((Double)val).doubleValue();
	
			if (ret >= Integer.MIN_VALUE && ret <= Integer.MAX_VALUE) {
				return (int) ret;
        }	}
 		if (primitiveType == String.class) {
	        return Integer.parseInt(toString());
	    }
		cantConvert("int");
		return 0;
    }
    
    public long longValue() throws NumberFormatException 
    {
        if (primitiveType == long.class){
			return	((Long)val).longValue();
		}
		if (primitiveType == int.class){
			return ((Integer)val).intValue();
		}
        if(primitiveType == short.class) {
	        return ((Short)val).shortValue();
	    }
		if (primitiveType == float.class) {
			float ret = ((Float)val).floatValue();
			
			if (ret >= Long.MIN_VALUE && ret <= Long.MAX_VALUE) {
				return (long) ret;
        }	}
        if (primitiveType == double.class) {
			double ret = ((Double)val).doubleValue();
			if (ret >= Long.MIN_VALUE && ret <= Long.MAX_VALUE) {
				return (long) ret;
        }	}
		if (primitiveType == String.class) {
	        return Long.parseLong(toString());
	    }
		cantConvert("long");  
		return 0l;  
    }
 
    
    public float floatValue() throws NumberFormatException
    {
		if (primitiveType == float.class) {
            return ((Float)val).floatValue();
        }
        if (primitiveType == double.class) {
			double ret = ((Double)val).doubleValue();
			if (ret >= Float.MIN_VALUE && ret <= Float.MAX_VALUE) {
				return (float) ret;
        }	}
        if (primitiveType == int.class) {
            return ((Integer)val).intValue();
        } 
        if(primitiveType == long.class) {
			long ret = ((Long)val).longValue();
			if (ret >= Float.MIN_VALUE && ret <= Float.MAX_VALUE) {
				return ret;
        }	}
        if(primitiveType == short.class) {
            return ((Short)val).shortValue();
        }
		if (primitiveType == String.class) {
	        return Float.parseFloat(toString());
	    }
	    cantConvert("float"); 
	    return 0.0f;
	}
    
    public double doubleValue() throws NumberFormatException
    {
		if (primitiveType == double.class) {
			return ((Double)val).doubleValue();
		}
        if (primitiveType == float.class) {
            return ((Float)val).floatValue();
        }
		if(primitiveType == int.class) {
            return ((Integer)val).intValue();
        }
        if(primitiveType == long.class) {
            return ((Long)val).longValue();
        }
        if(primitiveType == short.class) {
            return ((Short)val).shortValue();
        }
		if (primitiveType == String.class) {
	        return Double.parseDouble(toString());
	    }
	    cantConvert("double"); 
	    return 0.0;
    }
    
    public String stringValue()
    {
		return val.toString();
	}
	
    public Object objectValue() 
    {
        return val;
    }
    
    public TupleSet tupleSetValue()
    {
		if (val instanceof TupleSet || val instanceof NodeSet || val instanceof EdgeSet) {
			return (TupleSet) val;
		}
		cantConvert("TupleSet");
		return null;
	}
	
	public NodeSet nodeSetValue()
    {
		if (val instanceof NodeSet) {
			return (NodeSet) val;
		}
		cantConvert("NodeSet");
		return null;
	}
  
	public EdgeSet edgeSetValue()
    {
		if (val instanceof EdgeSet) {
			return (EdgeSet) val;
		}
		cantConvert("EdgeSet");
		return null;
	}
    
    public void set(int i) {
        val = new Integer(i);
        primitiveType = int.class;
    }

    public void set(long l) {
        val = new Long(l);
        primitiveType = long.class;
    }
    
    public void set(short s) {
        val = new Short(s);
        primitiveType = short.class;
    }
    
    public void set(float f) {
        val = new Float(f);
        primitiveType = float.class;
    }

    public void set(double d) {
        val = new Double(d);
        primitiveType = double.class;
    }
    
    public void set(boolean b) {
        val = new Boolean(b);
        primitiveType = boolean.class;
    }
    
    public void set(Object o) {
        val = o;
        primitiveType = null;
    }
    
    public void print(PrintStream ps) {
        if(isPrimitive()) {
            if(primitiveType != void.class) {
                ps.println(this);
            }
        } else if(val instanceof Value[]) {
            Value[] vals = (Value[])val;
            for(int i = 0; i < vals.length; i++) {
                vals[i].print(ps);
            }
        } else {
            if(Type.isSubtypeOf(getType(), TupleSet.class)) {
                ((TupleSet)val).print(ps);
            } else {
                ps.println(val);
            }
        }
    }
}
