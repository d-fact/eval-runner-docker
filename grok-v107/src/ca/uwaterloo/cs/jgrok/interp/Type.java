package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;
import ca.uwaterloo.cs.jgrok.fb.TupleSet;

public class Type {
    /**
     * Find a type.
     */
    public static Class<?> findType(String name) {
        if(name == null)
            return null;
        
        if(name.equals("void"))
            return void.class;
        if (name.equals("long"))
			return long.class;
        if(name.equals("int"))
            return int.class;
        if (name.equals("double"))
			return double.class;
        if(name.equals("float"))
            return float.class;
        if(name.equals("string"))
            return String.class;
        if(name.equals("String"))
            return String.class;
        if(name.equals("boolean"))
            return boolean.class;
        if(name.equals("NodeSet"))
            return NodeSet.class;
        if(name.equals("EdgeSet"))
            return EdgeSet.class;
        if(name.equals("TupleSet"))
            return TupleSet.class;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String findName(Class<?> type) {
        if (type == null) {
            return "...";
        }
        
        String name = type.getName();
        if(isPrimitive(type)) {
            return name;
        } 
        if(name.equals("java.lang.String")) {
			return "String";
		}
        if(name.endsWith(".NodeSet" )) {
			return "NodeSet";
		}
        if(name.endsWith(".EdgeSet" )) {
			return "EdgeSet";
		}
        if(name.endsWith(".TupleSet")) {
			return "TupleSet";
		}
        int index = name.indexOf(' ');
        name = name.substring(index+1);
        return name;
    }
    
    ///////////////////////////////////////////////////////////
    
    public static final int MATCH = 0;
    public static final int NOT_MATCH = -10000;
    
    /**
     * Computes the distance between two classes: t1, t2.
     * If t1 is sub-type of t2, the distance is equal to or
     * greater than 0. Otherwise, the distance is less than 0.
     */
    public static int distanceTo(Class<?> t1, Class<?> t2) {
        if(isIdentical(t1, t2)) return MATCH;
        else return computeDistance(t1, t2);
    }
    
    public static int distanceTo(Class<?>[] argTypes,
                                 Class<?>[] paramTypes) {
        int dist = 0;
        int numArgs = argTypes.length;
        int numParams = paramTypes.length;
        
        if(numArgs != numParams) return NOT_MATCH;
        if(numArgs == 0) return MATCH;
        
        for(int i = 0; i < numArgs; i++) {
            dist += distanceTo(argTypes[i], paramTypes[i]);
            if(dist < 0) break;
        }
        
        return dist;
    }
    
    private static int computeDistance(Class<?> t1, Class<?> t2) {
        int dist = 0;
        Class<?> supType;
        supType = t1.getSuperclass();
        
        if(supType != null) {
            if(t2 == supType) dist += 1;
            else {
                if(supType == Object.class)
                    dist = NOT_MATCH;
                else
                    dist = 1 + distanceTo(supType, t2);
            }
        }
        
        if(dist > 0) return dist;
        
        // Reset
        dist = 0;
        
        Class<?> infType;
        Class<?>[] interfaces = t1.getInterfaces();
        if(interfaces != null) {
            for(int i = 0; i < interfaces.length; i++) {
                dist = 0;
                infType = interfaces[i];
                if(t2 == infType) {
                    dist += 1;
                    break;
                } else {
                    dist = 1 + distanceTo(infType, t2);
                    if(dist > 0) break;
                    else dist = NOT_MATCH;
                }
            }
        } else {
            dist = NOT_MATCH;
        }
        
        if(dist > 0) return dist;
        else return NOT_MATCH;
    }
    
    public static boolean isPrimitive(Class<?> t) {
        if(t == int.class ||
           t == float.class ||
           t == long.class ||
           t == double.class ||
           t == boolean.class ||
           t == void.class ) return true;
        return false;
    }
    
    public static boolean isIdentical(Class<?> t1, Class<?> t2) {
        if(t1 == t2) return true;
        else return false;
    }
    
    public static boolean isSubtypeOf(Class<?> t1, Class<?> t2) {
        return (distanceTo(t1, t2) >= 0);
    }
    
    public static boolean isRelatedTo(Class<?> t1, Class<?> t2) {
        return ((distanceTo(t1, t2) >= 0) ||
                (distanceTo(t2, t1) >= 0) );
    }
}
