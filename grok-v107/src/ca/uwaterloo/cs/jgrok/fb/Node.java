package ca.uwaterloo.cs.jgrok.fb;

public class Node {
    private int ID;
    
    public Node(int ID) {
        this.ID = ID;
    }
    
    public Node(String SID) {
        this.ID = IDManager.getID(SID);
    }
    
    public int getID() {
        return ID;
    }
    
    public String get() {
        return IDManager.get(ID);            
    }
    
    public String toString() {
        return IDManager.get(ID);
    }
}
