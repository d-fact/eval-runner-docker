package ca.uwaterloo.cs.jgrok.interp;

import java.util.*;
import java.io.File;
import java.io.IOException;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.env.Indent;
import ca.uwaterloo.cs.jgrok.util.Timing;

public class ScriptUnitNode extends ScopedNode {
    private int currStmtNum = 0;
    private File scriptFile = null;
    private String aliasName= null;
    
    /**
     * Settings for echo, timing and pause.
     */
    private Option unitOption;
    
    /**
     * Hash table for the source script units.
     */
    private Hashtable<String, Object> sourceUnits;
    
    /**
     * List of all the statements to evaluate.
     */
    private ArrayList<StatementNode> stmtNodeList;
    
    /**
     * The echo indentation buffer.
     */
    private StringBuffer echoIndent;
    
    /**
     * Constructor.
     */
    public ScriptUnitNode() {
        super(null);
        unitOption = new Option();
        sourceUnits = new Hashtable<String, Object>(5);
        stmtNodeList = new ArrayList<StatementNode>(50);
        echoIndent = new StringBuffer();
    }
    
    /**
     * Gets the script file.
     */
    public File getFile() {
        return scriptFile;
    }
    
    /**
     * Sets the script file.
     */
    public void setFile(File file) {
        if(file != null) {
            try {
                this.scriptFile = file.getCanonicalFile();
            } catch(IOException e) {
                e.printStackTrace();
                this.scriptFile = null;
            }
        } else {
            this.scriptFile = null;
        }
    }
    
    /**
     * Gets this script's alias name.
     */
    public String getAliasName() {
        return aliasName;
    }
    
    /**
     * Sets this script's alias name.
     */
    public void setAliasName(String alias) {
        this.aliasName = alias;
    }
    
    /**
     * Gets this script's file name.
     * <pre>
     * For example:
     *     Script  "/data/analysis/grep.ql"
     *     Return  "grep.ql"
     * </pre>
     */
    public String getFileName() {
        if(scriptFile != null)
            return scriptFile.getName();
        else
            return null;
    }
    
    /**
     * Gets this script's full-path name.
     * <pre>
     * For example:
     *     Script  "/data/analysis/grep.ql"
     *     Return  "/data/analysis/grep.ql"
     * </pre>
     */
    public String getFullName() {
        return getFilePath();
    }
    
    /**
     * Gets this script's full-path name.
     * <pre>
     * For example:
     *     Script  "/data/analysis/grep.ql"
     *     Return  "/data/analysis/grep.ql"
     * </pre>
     */
    public String getFilePath() {
        if(scriptFile != null)
            return scriptFile.getPath();
        else
            return null;
    }
    
    /**
     * Gets this script's parent directory.
     * <pre>
     * For example:
     *     Script  "/data/analysis/grep.ql"
     *     Return  "/data/analysis"
     * </pre>
     */
    public String getFileParent() {
        if(scriptFile != null)
            return scriptFile.getParent();
        else
            return System.getProperty("user.dir");
    }
    
    /**
     * Gets this script's setting option. 
     */
    public Option getOption() {
        return unitOption;
    }
    
    /**
     * Sets this script's setting option. 
     */
    public void setOption(Option opt) {
        if(opt != null) unitOption = opt;
    }
    
    /**
     * Tests if echo setting is on.
     */
    public boolean isEchoOn() {
        return unitOption.isEchoOn();
    }
    
    /**
     * Tests if timing setting is on.
     */
    public boolean isTimeOn() {
        return unitOption.isTimeOn();
    }
    
    /**
     * Tests if debug setting pause is on.
     */
    public boolean isPauseOn() {
        return unitOption.isPauseOn();
    }
    
    public void addStatement(StatementNode stmtNode) {
        if(stmtNode != null) stmtNodeList.add(stmtNode);
    }
    
    public ScriptUnitNode[] getSourceUnits() {
        Object o;
        ArrayList<ScriptUnitNode> aList;
        Enumeration<Object> enm;
        
        aList = new ArrayList<ScriptUnitNode>(5);
        enm = sourceUnits.elements();
        while(enm.hasMoreElements()) {
            o = enm.nextElement();
            if(o instanceof ScriptUnitNode)
                aList.add((ScriptUnitNode)o);
            else
                aList.addAll(((UnitContainer)o).list);
        }
        
        ScriptUnitNode[] a;
        a = new ScriptUnitNode[aList.size()];
        aList.toArray(a);
        return a;
    }
    
    public ScriptUnitNode[] getSourceUnits(String alias) {
        if(alias == null) return new ScriptUnitNode[0];
        
        ScriptUnitNode[] a;
        Object o = sourceUnits.get(alias);
        if(o == null) {
            a = new ScriptUnitNode[0];
        } else if(o instanceof ScriptUnitNode) {
            a = new ScriptUnitNode[1];
            a[0] = (ScriptUnitNode)o ;
        } else {
            UnitContainer uCon;
            uCon = (UnitContainer)o;
            a = new ScriptUnitNode[uCon.list.size()];
            uCon.list.toArray(a);
        }
        
        return a;
    }
    
    public void addSourceUnit(ScriptUnitNode unit) {
        if(unit == null) return;
        
        String alias;
        alias = unit.getAliasName();
        
        if(!sourceUnits.containsKey(alias)) {
            sourceUnits.put(alias, unit);
        } else {
            Object o = sourceUnits.get(alias);
            if(o instanceof ScriptUnitNode) {
                ScriptUnitNode u;
                u = (ScriptUnitNode)o;
                if(u.getFilePath().equals(unit.getFilePath())) {
                    Variable var;
                    Enumeration<Variable> enm;
                    enm = u.allVariables();
                    while(enm.hasMoreElements()) {
                        var = enm.nextElement();
                        var.setValue(null);
                    }
                    sourceUnits.put(alias, unit);
                } else {
                    UnitContainer uCon;
                    uCon = new UnitContainer(u);
                    uCon.add(unit);
                    sourceUnits.put(alias, uCon);
                }
            } else {
                int i = 0;
                ArrayList<ScriptUnitNode> list;
                ScriptUnitNode u;
                
                list = ((UnitContainer)o).list;
                for(; i < list.size(); i++) {
                    u = list.get(i);
                    if(u.getFilePath().equals(unit.getFilePath())) {
                        Variable var;
                        Enumeration<Variable> enm;
                        enm = u.allVariables();
                        while(enm.hasMoreElements()) {
                            var = enm.nextElement();
                            var.setValue(null);
                        }
                        list.set(i, unit);
                        break;
                    }
                }
                
                if(i == list.size()) list.add(unit);
            }
        }
    }
    
    /**
     * Looks up a variable from this scope.
     * @param name the name of the variable to look up.
     * @return the variable found.
     * @throws LookupException if no variable was found.
     */
    public Variable lookup(String name) throws LookupException {
        // Step 1: look up in the local table.
        Variable var = (Variable)varTbl.get(name);
        // Step 2: if not found, throw exception.
        if(var == null) throw new LookupException(name);
        
        return var;
    } 
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {}
    
    public Value evaluate(Env env) throws EvaluationException {
        int size;
        Timing timing;
        Value stmtVal;
        StatementNode stmt;
        
        timing = new Timing();
        size = stmtNodeList.size();
        for(int i = 0; i < size; i++) {
            stmt = stmtNodeList.get(i);
                        
            if(isEchoOn()) {
                env.out.println(Indent.addIndent(stmt.toString(), Env.promptText));
            }
            
            if(isTimeOn()) {
                timing.start();
            }
            
            // Do evaluation.
            stmtVal = stmt.evaluate(env);
            
            if(isTimeOn()) {
                timing.stop();
                env.out.print("time:\t");
                env.out.println(stmt.shortFormLocation()
                                + " " + timing.getTime());
            }
            
            if(stmtVal != Value.EVAL) return stmtVal;
        }
        return Value.EVAL;
    }
    
    public Value debugEvaluate(Env env, int qdbCode)
        throws EvaluationException {
        int size;
        Timing timing;
        Value stmtVal;
        StatementNode stmt;
        
        timing = new Timing();
        size = stmtNodeList.size();
        if(currStmtNum >= size) {
            env.out.println("[qdb]: no more scripts left");
            return Value.EVAL;
        }
        
        if(qdbCode == QdbCode.l) {
            stmt = stmtNodeList.get(currStmtNum);
            env.out.println(stmt.shortFormLocation());
            env.out.println(stmt.toString());
            return Value.EVAL;
        }
        
        if(qdbCode == QdbCode.c) {
            getOption().setPauseOn(false);
        }
        
        boolean pause = false;        
        for(; currStmtNum < size; currStmtNum++) {
            if(pause) break;
            
            stmt = stmtNodeList.get(currStmtNum);
            
            if(isEchoOn()) {
                env.out.println(Indent.addIndent(stmt.toString(), Env.promptText));
            }
            
            if(isTimeOn()) {
                timing.start();
            }
            
            // Do evaluation.
            stmtVal = stmt.evaluate(env);
            
            if(isTimeOn()) {
                timing.stop();
                env.out.print("time:\t");
                env.out.println(stmt.shortFormLocation()
                                + " " + timing.getTime());
            }
            
            switch(qdbCode) {
            case QdbCode.c:
                if(isPauseOn()) pause = true;
                break;
            case QdbCode.n:
                pause = true;
                break;
            default:
                pause = false;
            }
            
            if(stmtVal != Value.EVAL) {
                currStmtNum = size;
                return stmtVal;
            }
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        for(int i = 0; i < stmtNodeList.size(); i++) {
            buffer.append(stmtNodeList.get(i));
            buffer.append("\n\n");
        }
        
        return buffer.toString();
    }
    
    public void incrEchoIndent() {
        echoIndent.append("\t");
    }
    
    public void decrEchoIndent() {
        int len = echoIndent.length();
        if(len > 0) echoIndent.delete(len-1, len);
    }
    
    public String getEchoIndent() {
        return echoIndent.toString();
    }
    
    private class UnitContainer {
        ArrayList<ScriptUnitNode> list;
        
        UnitContainer(ScriptUnitNode unit) {
            list = new ArrayList<ScriptUnitNode>(5);
            list.add(unit);
        }
        
        void add(ScriptUnitNode unit) {
            list.add(unit);
        }
    }
}
