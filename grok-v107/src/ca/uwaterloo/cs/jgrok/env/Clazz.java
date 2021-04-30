package ca.uwaterloo.cs.jgrok.env;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import ca.uwaterloo.cs.jgrok.interp.Type;
import ca.uwaterloo.cs.jgrok.interp.Value;

public final class Clazz {

    public static boolean isJavaPrimitive(Class<?> clazz) {
        if(clazz == int.class ||
           clazz == char.class ||
           clazz == byte.class ||
           clazz == short.class ||
           clazz == long.class ||
           clazz == float.class ||
           clazz == double.class ||
           clazz == boolean.class) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isJGrokPrimitive(Class<?> clazz) {
        if(clazz == String.class) {
            return true;
        } else {
            return isJavaPrimitive(clazz);
        }
    }
    
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> clazz = null;
        
        if (className != null) {
            try {
                clazz = Class.forName(className);
            } catch(ClassNotFoundException e) {
                if(className.equals("void"))
                    clazz = void.class;
                else if(className.equals("int"))
                    clazz = int.class;
                else if(className.equals("float"))
                    clazz = float.class;
                else if(className.equals("string"))
                    clazz = String.class;
                else if(className.equals("String"))
                    clazz = String.class;
                else if(className.equals("boolean"))
                    clazz = boolean.class;
                else if(className.equals("double"))
                    clazz = double.class;
                else if(className.equals("short"))
                    clazz = short.class;
                else if(className.equals("long"))
                    clazz = long.class;
                else if(className.equals("byte"))
                    clazz = byte.class;
                else if(className.equals("..."))
                    clazz = null;
                else
                    throw new ClassNotFoundException(className);
            }
        }
        
        return clazz;
    }
    
    public static Object forValue(String type, String value) throws ClassNotFoundException, InstantiationException  {
        try {
            return Clazz.forValue(Clazz.forName(type), value);
        } catch(ClassNotFoundException e) {
            throw e;
        }
    }
    
    /**
     * Construct an object based on a String value. For example:
     * <pre>
     *   Clazz.forValue(null, "ABC")       return "ABC"
     *   Clazz.forValue(void.class, "ABC") return "ABC"
     *   Clazz.forValue(T.class, "ABC")    return new T("ABC")
     *   Clazz.forValue(int.class, "100")  return new Integer.parseInt("100")
     * </pre>
     * 
     * @param clazz
     * @param value
     * @return Constructed object based on the specified value.
     */
    public static Object forValue(Class<?> clazz, String value) throws InstantiationException {
        Object obj = null;
        
        if(clazz != null) {
            if(clazz.equals(void.class)) {
                obj = value;
            } else if(clazz.equals(int.class)) {
                obj = Integer.parseInt(value);
            } else if(clazz.equals(float.class)) {
                obj = Float.parseFloat(value);
            } else if(clazz.equals(boolean.class)) {
                obj = Boolean.parseBoolean(value);
            } else if(clazz.equals(double.class)) {
                obj = Double.parseDouble(value);
            } else if(clazz.equals(short.class)) {
                obj = Short.parseShort(value);
            } else if(clazz.equals(long.class)) {
                obj = Long.parseLong(value);
            } else if(clazz.equals(byte.class)) {
                obj = Byte.parseByte(value);
            } else if(clazz.equals(String.class)) {
                obj = value;
            } else {
                try {
                    obj = clazz.getConstructor(String.class).newInstance(value);
                } catch(Exception e) {
                    throw new InstantiationException(e.getMessage());
                }
            }
        } else {
            obj = value;
        }
        
        return obj;
    }

    /**
     * Finds a public method for a given class.
     * 
     * @param clazz the given class
     * @param methodName the method name
     * @param argTypes the argument types
     * @return the found public method
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        int distNew;
        int distOld = 1000;
        Method specific = null;
        Class<?>[] paramTypes = null;
        Method[] methods = clazz.getMethods();
        
        for(Method method : methods) {
            // Must have the same name
            if(!method.getName().equals(methodName)) continue;
            
            // Must be public
            if(!Modifier.isPublic(method.getModifiers())) continue;
            
            // Must have the same number of parameter types
            paramTypes = method.getParameterTypes();
            if(paramTypes.length != argTypes.length) continue;
            
            distNew = Type.distanceTo(argTypes, paramTypes);
            if(distNew >= 0) {
                if(specific == null) {
                    specific = method;
                    distOld = distNew;
                } else {
                    if(distNew < distOld) {
                        specific = method;
                        distOld = distNew;
                    }
                }
            }
            
            // Exact match
            if(distNew == 0) break;
        }
        
        return specific;
    }
    
    /**
     * Finds a public static method for a given class.
     * 
     * @param clazz the given class
     * @param methodName the method name
     * @param argTypes the argument types
     * @return the found public method
     */
    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        int distNew;
        int distOld = 1000;
        Method specific = null;
        Class<?>[] paramTypes = null;
        Method[] methods = clazz.getMethods();
        
        for(Method method : methods) {
            // Must have the same name
            if(!method.getName().equals(methodName)) continue;
            
            // Must be public and static
            int mod = method.getModifiers();
            if(!Modifier.isPublic(mod)) continue;
            if(!Modifier.isStatic(mod)) continue;
            
            // Must have the same number of parameter types
            paramTypes = method.getParameterTypes();
            if(paramTypes.length != argTypes.length) continue;
            
            // Calculate distance
            distNew = Type.distanceTo(argTypes, paramTypes);
            if(distNew >= 0) {
                if(specific == null) {
                    specific = method;
                    distOld = distNew;
                } else {
                    if(distNew < distOld) {
                        specific = method;
                        distOld = distNew;
                    }
                }
            }
            
            // Exact match
            if(distNew == 0) break;
        }
        
        return specific;
    }
    
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>[] argTypes) {
        int distNew;
        int distOld = 1000;
        Constructor<?> specific = null;
        Class<?>[] paramTypes = null;
        Constructor<?>[] constructors = clazz.getConstructors();
        
        for(Constructor<?> c : constructors) {
            // Must be public
            if(!Modifier.isPublic(c.getModifiers())) continue;
            
            // Must have the same number of parameter types
            paramTypes = c.getParameterTypes();
            if(paramTypes.length != argTypes.length) continue;
            
            distNew = Type.distanceTo(argTypes, paramTypes);
            if(distNew >= 0) {
                if(specific == null) {
                    specific = c;
                    distOld = distNew;
                } else {
                    if(distNew < distOld) {
                        specific = c;
                        distOld = distNew;
                    }
                }
            }
            
            // Exact match
            if(distNew == 0) break;
        }
        
        return specific;
    }
    
    public static Object newInstance(String className)
        throws ClassNotFoundException, InstantiationException {
        if(className == null) return null;
        
        try {
             Class<?> defClass;
             defClass = Class.forName(className);
             return defClass.newInstance();
         } catch(ClassNotFoundException e1) {
             throw e1;
         } catch(Exception e2) {
             throw new InstantiationException(e2.getMessage());
         }
    }
    
    public static Object newInstance(Class<?> clazz)
        throws InstantiationException {
        try {
            return clazz.newInstance();
        } catch(Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }
    
    public static Object newInstance(Class<?> clazz, Object[] args)
        throws NoSuchMethodException, InstantiationException {
        try {
            Class<?>[] paramTypes = null;
            if(args != null) {
                paramTypes = new Class[args.length];
                for(int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }
            }
            
            Constructor<?> c = clazz.getConstructor(paramTypes);
            return c.newInstance(args);
        } catch(NoSuchMethodException e1) {
            throw e1;
        } catch(Exception e2) {
            throw new InstantiationException(e2.getMessage());
        }
    }
    

    public static Object newInstance(Class<?> clazz, Value[] args)
        throws NoSuchMethodException, InstantiationException {
        try {
            Object[] objs = null;
            Class<?>[] paramTypes = null;
            if(args != null) {
                objs = new Object[args.length];
                paramTypes = new Class[args.length];
                for(int i = 0; i < args.length; i++) {
                    objs[i] = args[i].objectValue();
                    paramTypes[i] = args[i].getType();
                }
            } else {
                objs = new Object[0];
            }
             
            Constructor<?> c = clazz.getConstructor(paramTypes);
            
            return c.newInstance(objs);
         }  catch(NoSuchMethodException e1) {
             throw e1;
         } catch(Exception e2) {
            throw new InstantiationException(e2.getMessage());
         }
    }
}
