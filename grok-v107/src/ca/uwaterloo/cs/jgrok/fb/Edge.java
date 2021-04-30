package ca.uwaterloo.cs.jgrok.fb;

public class Edge {
    private int frID;
    private int toID;
    
    public Edge(String from, String to) {
        this.frID = IDManager.getID(from);
        this.toID = IDManager.getID(to);
    }
    
    protected Edge(int from, int to) {
        this.frID = from;
        this.toID = to;
    }
    
    public int getFromID() {
        return frID;
    }
    
    public String getFrom() {
        return IDManager.get(frID);
    }
    
    public int getToID() {
        return toID;
    }
    
    public String getTo() {
        return IDManager.get(toID);
    }
}
