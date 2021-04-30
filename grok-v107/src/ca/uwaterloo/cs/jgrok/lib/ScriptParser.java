package ca.uwaterloo.cs.jgrok.lib;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * void parse(String scriptFile, String alias)
 */
public class ScriptParser extends Function {
    
    public ScriptParser() 
    {
        name = "parse";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		switch (vals.length) {
		case 2:
			String filePath  = vals[0].stringValue();
			String aliasName = vals[1].stringValue();
	        File   file;
			Interp interp;
			ScriptUnitNode unit;
			ScriptUnitNode master;
			master = env.traceScriptUnit();
	        
			try {
				try {
					file = new File(filePath);
					interp = Interp.reinit(file);
				} catch(FileNotFoundException e) {
					if(filePath.charAt(0) == '~') {
						file = new File(System.getProperty("user.home"), filePath.substring(1));
					} else {
						file = new File(master.getFileParent(), filePath);
					}
					interp = Interp.reinit(file);
				}
	            
				unit = interp.parse();
				unit.setAliasName(aliasName);
				master.addSourceUnit(unit);
			} catch(FileNotFoundException e) {
				throw new InvocationException("no such a file: " + filePath);
			}
	        
			return Value.VOID;
		}
		return illegalUsage();
	}
    
    public String usage()
    {
		return  "void " + name + "(String scriptFile, String alias)";
	}
}
