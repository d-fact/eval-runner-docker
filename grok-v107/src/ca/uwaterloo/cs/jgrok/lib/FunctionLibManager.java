package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;
import java.util.Hashtable;
import java.io.PrintStream;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;

/* This essentially puts all invocable functions into a single level namespace
 * The default one is JGrok, but there can be others.
 */

public class FunctionLibManager {
    public  static final String                      JGrokLibName = "JGrok";
    private static final FunctionLib                 rootLib      = new RootFunctionLib(JGrokLibName);
    private static final Hashtable<String,FunctionLib> libEntries   = new Hashtable<String,FunctionLib>();
    
    static {
          libEntries.put(rootLib.getName(), rootLib);
    }
          
    /**
     * Gets the root FunctionLib.
     */
    public static FunctionLib getRootLib() 
    {
        return rootLib;
    }
    
    /**
     * Finds a FunctionLib by name.
     * @return the found FunctionLib or null if no FunctionLib was found.
     */
    public static FunctionLib findLib(String libName) 
    {
		if (libName == null) {
			return rootLib;
		}
        return libEntries.get(libName);
    }
    
    /**
     * Gets a FunctionLib by name.
     * If no FunctionLib was found, a new one is created.
     */
    public static FunctionLib getLib(String libName) {
        FunctionLib lib = findLib(libName);
        if(lib == null) {
            lib = new FunctionLib(libName);
            libEntries.put(libName, lib);
        }
        return lib;
    }
    
    /**
     * Registers a function in a namespace.
     * 
     * @param libName the function library
     * @param f the function to be registered
     * @return <b>true</b> if successfully registered.
     */
    public static void register(String libName, Function f) 
    {
		FunctionLib lib;
		
		if (libName == null) {
			lib = getRootLib();
		} else {
		    lib = getLib(libName);
		}
        lib.register(f);
    }
    
    public static void getUse(EdgeSet edgeSet)
    {
		int						size      = libEntries.size();
		FunctionLib[]			libraries = new FunctionLib[size];
		Enumeration<FunctionLib>en        = libEntries.elements();
		int						i, j;
		String					name1;
		FunctionLib				library, library1;
		
		for (i = 0; en.hasMoreElements(); ++i) {
			library = en.nextElement();
			name1   = library.getName();
			for (j = i; j > 0; --j) {
				library1 = libraries[j-1];
				if (name1.compareToIgnoreCase(library1.getName()) >= 0) {
					break;
				}
				libraries[j] = library1;
			}
			libraries[j] = library; 
		}
		for (i = 0; i < size; ++i) {
			library = libraries[i];
			library.getUse(edgeSet);
	}	}
}
