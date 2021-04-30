package ca.uwaterloo.cs.jgrok.test;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

public class Regression {
    private String name;
    private String inputDirName;
    private String outputDirName;
    private ArrayList<RegressionTest> testList;
    private boolean enabled;
    
    public static final String STATE_ON = "ON"; 
    public static final String STATE_OFF = "OFF";
    
    public Regression() {
        testList = new ArrayList<RegressionTest>(100);
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getInput() {
        return this.inputDirName;
    }
    
    public void setInput(String dirName) {
        this.inputDirName = dirName;
    }

    public String getOutput() {
        return this.outputDirName;
    }
    
    public void setOutput(String dirName) {
        this.outputDirName = dirName;
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
    
    public static String getRegressHome() {
        String dir = System.getProperty("JGrokRegress");
        if(dir == null) dir = ".";
        return dir;
    }

    public static String getRegressResultHome() {
        String dir = System.getProperty("JGrokRegressResult");
        if(dir == null) dir = ".";
        return dir;
    }
    
    public void clear() {
        testList.clear();
    }
    
    public int size() {
        return testList.size();
    }
    
    public void add(RegressionTest test) {
        testList.add(test);
    }
    
    public void addAll(Collection<? extends RegressionTest> tests) {
        testList.addAll(tests);
    }
    
    public RegressionTest remove(int index) {
        return testList.remove(index);
    }
    
    public boolean remove(RegressionTest test) {
        return testList.remove(test);
    }
    
    public Iterator<RegressionTest> iterator() {
        return testList.iterator();
    }
    
    public void toArray(RegressionTest[] testArray) {
        testList.toArray(testArray);
    }
}
