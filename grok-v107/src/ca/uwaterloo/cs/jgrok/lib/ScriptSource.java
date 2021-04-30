package ca.uwaterloo.cs.jgrok.lib;

import java.io.File;
import java.io.FileNotFoundException;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * <pre>
 * Functions:
 *
 *     source(scriptFile)
 *     source(scriptFile, argument, ...)
 *
 * </pre>
 */
public class ScriptSource extends Function {
    private Env env;
    
    public ScriptSource() 
    {
        name = "source";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
        if(vals.length == 0) {
			return illegalUsage();
		}
        
        String scriptFile = vals[0].stringValue();
        
        this.env = env;
        
        Value[] args = new Value[vals.length - 1];
        for(int i = 0; i < args.length; i++) {
            args[i] = vals[i+1];
        }
        
        ScriptUnitNode unit = find(scriptFile);
        if (unit == null) {
			unit = parse(scriptFile);
		}
        return evaluate(unit, args);
    }
    
    private ScriptUnitNode find(String aliasName)
        throws InvocationException {
        ScriptUnitNode master;
        ScriptUnitNode[] slaves;
        
        master = env.traceScriptUnit();
        slaves = master.getSourceUnits(aliasName);
        
        if(slaves.length == 0)
            return null;
        else if(slaves.length == 1)
            return slaves[0];
        else {
            StringBuffer buffer;
            buffer = new StringBuffer();
            buffer.append("ambiguous aliases: ");
            for(int i = 0; i < slaves.length; i++) {
                buffer.append("\n");
                buffer.append("\t");
                buffer.append(slaves[i].getAliasName());
                buffer.append("\t");
                buffer.append(slaves[i].getFullName());
            }
            throw new InvocationException(buffer.toString());
        }
    }
    
    private ScriptUnitNode parse(String filePath)
        throws InvocationException {
        File file;
        String fileName;
        String aliasName;
        
        file = new File(filePath);
        fileName = file.getName();
        
        int dotInd = fileName.indexOf('.');
        if(dotInd > 0) aliasName = fileName.substring(0, dotInd);
        else aliasName = fileName;
        
        Interp interp;
        ScriptUnitNode slave;
        ScriptUnitNode master;
        master = env.traceScriptUnit();
        
        try {
            try {
                interp = Interp.reinit(file);
            } catch(FileNotFoundException e) {
                if(filePath.charAt(0) == '~') {
                    file = new File(System.getProperty("user.home"), filePath.substring(1));
                } else {
                    file = new File(master.getFileParent(), filePath);
                }
                
                interp = Interp.reinit(file);
            }
            
            slave = interp.parse();
            if(slave != null) {
                slave.setAliasName(aliasName);
                master.addSourceUnit(slave);
                return slave;
            } else {
                throw new InvocationException("parse error encountered in " + filePath);
            }
        } catch(FileNotFoundException ex) {
            throw new InvocationException("no such a file: " + filePath);
        }
    }
    
    private Value evaluate(ScriptUnitNode unit, Value[] args)
        throws InvocationException {
        Value val;
        Variable var;
        
        // Add $# (number of script arguments).
        var = new Variable(unit, "$#", new Value(args.length));
        unit.addVariable(var);
        
        // Add $0 which is the script file.
        var = new Variable(unit, "$0", new Value(unit.getFileName()));
        unit.addVariable(var);
        
        // Add all the real arguments for $0.
        for(int i = 0; i < args.length; i++) {
            var = new Variable(unit, "$"+(i+1), args[i]);
            unit.addVariable(var);
        }
        
        try {
            env.pushScope(unit);
            // The source script will be evaluated
            // in the normal but not debugging mode.
            val = unit.evaluate(env);
        } catch(EvaluationException e) {
            throw new InvocationException(e.getMessage());
        } finally {
            // Clear this script for reuse next time, and
            // free memory occupied by temporary variables.
            unit.clear();
            env.popScope();
        }
        
        return val;
    }
    
    public String usage()
    {
		return "void " + name + "(scriptFile [, argument]*)";
	}
}