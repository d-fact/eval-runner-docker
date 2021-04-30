package ca.uwaterloo.cs.jgrok.fb;

class TupleImpl implements Tuple {
    int[] elems;
    
    TupleImpl(int[] elems) {
        this.elems = (int[])elems.clone();
    }
    
    TupleImpl(int dom, int rng) {
        elems = new int[2];
        elems[0] = dom;
        elems[1] = rng;
    }
    
    TupleImpl(int[] elems, boolean clone) {
        if(clone) this.elems = (int[])elems.clone();
        else this.elems = elems;
    }
    
    TupleImpl(int[] elems1, int[] elems2) {
        int i, length;
        
        length = elems1.length + elems2.length;
        
        elems = new int[length];
        
        for(i = 0; i < elems1.length; i++) {
            elems[i] = elems1[i];
        }
        
        for(; i < length; i++) {
            elems[i] = elems2[i-elems1.length];
        }
    }
    
    TupleImpl(int[] elems, int elem) {
        int i;
        
        this.elems = new int[elems.length + 1];
        for(i = 0; i < elems.length; i++) {
            this.elems[i] = elems[i];
        }
        this.elems[i] = elem;
    }
    
    /**
     * Counts the elements.
     */
    public int size() {
        return elems.length;
    }
    
    /**
     * Gets the domain of this Tuple.
     */
    public int getDom() {
        return elems[0];
    }
    
    /**
     * Sets the domain of this Tuple.
     */
    public void setDom(int value) {
        elems[0] = value;
    }
    
    /**
     * Gets the range of this Tuple.
     */
    public int getRng() {
        return elems[elems.length-1];
    }
    
    /**
     * Sets the range of this Tuple.
     */
    public void setRng(int value) {
        elems[elems.length-1] = value;
    }
    
    /**
     * Gets the element at <code>col</code>.
     */
    public int get(int col) {
        return elems[col];
    }
    
    /**
     * Assigns a <code>value</code> to the element at <code>col</code>.
     */
    public void set(int col, int value) {
        elems[col] = value;
    }
    
    /**
     * Gets the elements by way of an array of <code>cols</code>.
     */
    public int[] get(int[] cols) {
        int length = cols.length;
        int[] a = new int[length];
        
        for(int i = 0; i < length; i++) {
            a[i] = elems[cols[i]];
        }
        return a;
    }
    
    /**
     * Gets the inverse of this Tuple.
     */
    public Tuple getInverse() {
        int length = elems.length;
        int[] a = new int[length];
        
        for(int i = 0; i < length; i++) {
            a[i] = elems[length-1-i];
        }
        
        return new TupleImpl(a, false);
    }
    
    /**
     * Returns an array containing all of the elements.
     * @return a clone of the internal array in this Tuple.
     */
    public int[] toArray() {
        return (int[])elems.clone();
    }
    
    /**
     * Clones this Tuple.
     */
    public Object clone() {
        return new TupleImpl(elems);
    }
    
    /**
     * Compares itself to the other Tuple.
     */
    public int compareTo(Tuple t) throws ClassCastException {
        if(size() == t.size()) {
            for(int i = 0; i < elems.length; i++) {
                if(elems[i] > t.get(i)) return 1;
                else if(elems[i] < t.get(i)) return -1;
            }
            return 0;
        } else if (size() < t.size()) {
            return -1;
        } else {
            return 1;
        }
    }
}
