package ca.uwaterloo.cs.jgrok.fb;

/**
 * Path.
 */
public class Path {    
    int[] vertices;
    
    private Path() {}
    
    public Path(int head) {
        vertices = new int[1];
        vertices[0] = head;
    }
    
    public Path(int[] vertices) {
        this.vertices = vertices;
    }
    
    public int head() {
        return vertices[0];
    }
    
    public int tail() {
        return vertices[length()];
    }
    
    public int length() {
        return vertices.length-1;
    }
    
    public int countVertices() {
        return vertices.length;
    }
    
    public Tuple getTuple() {
        return new TupleImpl(vertices, false);
    }
    
    public boolean contains(int v) {
        for(int i = 0; i < vertices.length; i++) {
            if(vertices[i] == v) return true;
        }
        return false;
    }
    
    public String toString() {
        int count = countVertices();
        StringBuffer b = new StringBuffer();
        
        if(count > 0) {
            for(int i = 0; i < count - 1; i++) {
                b.append(IDManager.get(vertices[i]));
                b.append(" -> ");
            }
            b.append(IDManager.get(vertices[count-1]));
        }
        
        return b.toString();
    }
    
    public static Path link(Path p1, Path p2) {
        Path path = new Path();
        path.vertices = new int[p1.countVertices()+p2.countVertices()];
        System.arraycopy(p1.vertices, 0,
                         path.vertices, 0,
                         p1.countVertices());
        System.arraycopy(p2.vertices, 0,
                         path.vertices,
                         p1.countVertices(),
                         p2.countVertices());
        return path;
    }
    
    public static Path link(int head, Path p) {
        Path path = new Path();
        path.vertices = new int[p.countVertices()+1];
        path.vertices[0] = head;
        System.arraycopy(p.vertices, 0,
                         path.vertices, 1,
                         p.countVertices());
        return path;
    }
    
    public static Path link(Path p, int tail) {
        Path path = new Path();
        path.vertices = new int[p.countVertices()+1];
        System.arraycopy(p.vertices, 0,
                         path.vertices, 0,
                         p.countVertices());
        path.vertices[p.countVertices()] = tail;
        return path;
    }
    
    public static Path link(int head, int tail) {
        int[] v = new int[2];
        v[0] = head;
        v[1] = tail;
        return new Path(v);
    }
}
