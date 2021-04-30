package ca.uwaterloo.cs.jgrok.fb;

public interface Tuple extends Comparable<Tuple>, Cloneable {
    
    /**
     * Counts the elements.
     */
    public int size();
    
    /**
     * Gets the domain of this Tuple.
     */
    public int getDom();
    
    /**
     * Sets the domain of this Tuple.
     */
    public void setDom(int value);
    
    /**
     * Gets the range of this Tuple.
     */
    public int getRng();
    
    /**
     * Sets the range of this Tuple.
     */
    public void setRng(int value);
    
    /**
     * Gets the element at <code>col</code>.
     */
    public int get(int col);
    
    /**
     * Assigns a <code>value</code> to the element at <code>col</code>.
     */
    public void set(int col, int value);
    
    /**
     * Gets the elements by way of an array of <code>cols</code>.
     */
    public int[] get(int[] cols);
    
    /**
     * Gets the inverse of this Tuple.
     */
    public Tuple getInverse();
    
    /**
     * Returns an array containing all of the elements.
     * @return a clone of the internal array in this Tuple.
     */
    public int[] toArray();
    
    /**
     * Clones this Tuple.
     */
    public Object clone();
    
    /**
     * Compares itself to another Tuple.
     */
    public int compareTo(Tuple o) throws ClassCastException;
    
    /**
     * Gets the elements by filtering out columns.
     * <pre>
     *   Tuple   x y z u v w
     *   Filter  0 1 0 1 1 0 (BitSet left to right)
     *   Result  x z w
     * </pre>
     * In the above example, 1 means that the element will be filtered out.
     */
    //    public int[] get(BitSet filter);
}
