package ca.uwaterloo.cs.jgrok.test;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.Interp;
import ca.uwaterloo.cs.jgrok.util.Timing;

public class RegressionTest {

    private String name = null;
    private boolean enabled = true;
    private boolean success = true;
    private Regression regress = null;
    private Timing timing = new Timing();
    
    private String scriptFile = null;
    private String inputFile = null;
    private String logFile = null;
    private ArrayList<String> argList;
    
    public RegressionTest(Regression regress) {
        this.regress = regress;
        this.argList = new ArrayList<String>(5);
        this.regress.add(this);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getScriptFile() {
        return this.scriptFile;
    }
    
    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }
    
    public String getInputFile() {
        return this.inputFile;
    }
    
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getLogFile() {
        return this.logFile;
    }
    
    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(boolean b) {
        this.enabled = b;
    }
    
    public void setState(String state) {
        if(state.equalsIgnoreCase(Regression.STATE_ON)) {
            setEnabled(true);
        } else if(state.equalsIgnoreCase(Regression.STATE_OFF)) {
            setEnabled(false);
        } else {
            throw new IllegalArgumentException("state");
        }
    }
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public void setSuccess(boolean b) {
        this.success = b;
    }
    
    public int countArguments() {
        return argList.size();
    }
    
    public void clearArguments() {
        argList.clear();
    }
    
    public void addArgument(String value) {
        argList.add(value);
    }
    
    public String[] getArguments() {
        String[] result = new String[argList.size()];
        argList.toArray(result);
        return result;
    }
    
    protected String getResult() {
        StringBuffer buf = new StringBuffer();
        
        if(isEnabled()) {
            if(isSuccess())
                buf.append("S");
            else
                buf.append("F");
        } else {
            buf.append("_");
        }

        buf.append(' ');
        buf.append("Test=");
        buf.append(getName());
        
        buf.append(' ');
        buf.append("Time=");
        buf.append(timing.getTime());
        buf.append("sec");
        
        return buf.toString();
    }
    
    public String execute(Env env) {
        if(!isEnabled()) return getResult();
        timing.start();
        
        // Fill execution arguments {scriptFile, inputFile, ...}
        int argCount = 1;
        if(inputFile != null) argCount++;
        argCount = argCount + countArguments();

        String[] args = null;
        args = new String[argCount];
        args[0] = Regression.getRegressHome()
                + File.separator + regress.getInput()
                + File.separator + getScriptFile();
        
        if(inputFile != null) {
            args[1] = Regression.getRegressHome()
                    + File.separator + regress.getInput()
                    + File.separator + inputFile;
            System.arraycopy(getArguments(), 0, args, 2, countArguments());
        } else {
            System.arraycopy(getArguments(), 0, args, 1, countArguments());
        }
        
        String logFileName;
        logFileName = Regression.getRegressHome();
        logFileName = logFileName + File.separator + regress.getInput();
        logFileName = logFileName + File.separator + getLogFile();
        
        File outFile;
        String outFileName;
        outFileName = Regression.getRegressResultHome();
        outFileName = outFileName + File.separator + regress.getOutput();
        outFileName = outFileName + File.separator + getLogFile();
        outFile = new File(outFileName);
        
        // Redirect print stream
        PrintStream oldEnvOut = env.out;
        PrintStream newEnvOut = env.out;
        try {
            File outPath = outFile.getParentFile();
            if(!outPath.exists()) outPath.mkdirs();
            if(!outFile.exists()) outFile.createNewFile();
            
            newEnvOut = new PrintStream(outFile);
            env.out = newEnvOut;
        } catch(Exception e) {
            env.out = oldEnvOut;
            e.printStackTrace(env.err);
        }
        
        // Execute test
        File file = new File(args[0]);
        try {
            Interp interp = Interp.reinit(file);
            interp.fileEvaluate(env, args);
        } catch(Exception e) {
            e.printStackTrace(env.err);
        } finally {
            env.out = oldEnvOut;
        }
        
        File difFile;
        String difFileName;
        difFileName = Regression.getRegressResultHome();
        difFileName = difFileName + File.separator + regress.getOutput();
        difFileName = difFileName + File.separator + outFile.getName();
        difFileName = difFileName.replace(".log", ".dif");
        difFile = new File(difFileName);
        
        File sucFile;
        String sucFileName;
        sucFileName = difFileName.replace(".dif", ".suc");
        sucFile = new File(sucFileName);
        
        try {
            File difPath = difFile.getParentFile();
            if(!difPath.exists()) difPath.mkdirs();
            if(!difFile.exists()) difFile.createNewFile();
            PrintStream difOut = new PrintStream(difFile);

            // Examine results
            Regress.Diff diff = new Regress.Diff(logFileName, outFileName);
            boolean noDiff = diff.execute(difOut);
            difOut.close();
            
            if (noDiff) {
                this.setSuccess(true);
                if(sucFile.exists()) {
                    difFile.delete();
                } else {
                    difFile.renameTo(sucFile);
                }
            } else {
                if(sucFile.exists()) {
                    sucFile.delete();
                }
                this.setSuccess(false);
            }
        } catch(Exception e) {
            e.printStackTrace(env.err);
        } finally {
            env.out = oldEnvOut;
        }
        
        timing.stop();
        return getResult();
    }
    
}
