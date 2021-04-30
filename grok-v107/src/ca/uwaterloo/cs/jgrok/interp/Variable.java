package ca.uwaterloo.cs.jgrok.interp;

public class Variable {
    private String name;
    private Value value;
    protected Scope parent;
    
    public Variable(Scope parent, String name) {
        this(parent, name, null);
    }
    
    public Variable(Scope parent, String name, Value val) {
        this.name = name;
        this.value = val;
        this.parent = parent;
    }
    
    public Scope getScope() {
        return this.parent;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Class<?> getType() {
        if(value == null) return null;
        else return value.getType();
    }
    
    public Value getValue() {
        return value;
    }
    
    public void setValue(Value val) {
        this.value = val;
    }
}
