package ca.uwaterloo.cs.jgrok.fb;

class Tuple4Node implements Tuple {
    int elem;
    
    Tuple4Node(int elem) {
        this.elem = elem;
    }
    
    public int size() {
        return 1;
    }
    
    public void set(int value) {
        elem = value;
    }
    
    /**
     * Gets the domain of this Tuple.
     */
    public int getDom() {
        return elem;
    }
    
    /**
     * Sets the domain of this Tuple.
     */
    public void setDom(int value) {
        elem = value;
    }
    
    /**
     * Gets the range of this Tuple.
     */
    public int getRng() {
        return elem;
    }
    
    /**
     * Sets the range of this Tuple.
     */
    public void setRng(int value) {
        elem = value;
    }
    
    /**
     * Gets the element at <code>col</code>.
     */
    public int get(int col) {
        if(col != 0) {
            throw new ArrayIndexOutOfBoundsException("Illegal argument: " + col);
        }
        return elem;
    }
    
    /**
     * Assigns a <code>value</code> to the element at <code>col</code>.
     */
    public void set(int col, int value) {
        if(col != 0) {
            throw new ArrayIndexOutOfBoundsException("Illegal argument: " + col);
        }
        elem = value;
    }
    
    /**
     * Gets the elements by way of an array of <code>cols</code>.
     */
    public int[] get(int[] cols) {
        int length = cols.length;
        int[] a = new int[length];
        
        for(int i = 0; i < length; i++) {
            if(cols[i] == 0) a[i] = elem;
            else throw new ArrayIndexOutOfBoundsException("index out of bounds 1: " + cols[i]);
        }
        return a;
    }
    
    /**
     * Gets the inverse of this Tuple.
     */
    public Tuple getInverse() {
        return new Tuple4Node(elem);
    }
    
    /**
     * Returns an array containing all of the elements.
     * @return a clone of the internal array in this Tuple.
     */
    public int[] toArray() {
        int[] a = new int[1];
        a[0] = elem;
        return a;
    }
    
    /**
     * Clones this Tuple.
     */
    public Object clone() {
        return new Tuple4Node(elem);
    }
    
    /**
     * Compares itself to the other Tuple.
     */
    public int compareTo(Tuple t) throws ClassCastException {
        if(t.size() == 1) {
            if(elem > t.get(0)) return 1;
            else if(elem < t.get(0)) return -1;
            else return 0;
        } else if(t.size() > 1){
            return -1;
        } else {
            return 1;
        }
    }

}
