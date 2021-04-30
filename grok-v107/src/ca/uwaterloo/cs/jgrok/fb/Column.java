package ca.uwaterloo.cs.jgrok.fb;

public class Column {
    protected String name;
    protected Class<?> type;
    
    public Column(String name) {
        this.name = name;
        this.type = String.class;
    }
    
    public Column(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public boolean equals(Object o) throws ClassCastException {
        Column c = (Column)o;
        
        if( name.equals(c.getName()) &&
            type.equals(c.getType()) ) {
            return true;
        }
        return false;
    }
}
