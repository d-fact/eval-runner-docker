package ca.uwaterloo.cs.jgrok.fb;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * TupleList 
 */
public class TupleList implements Cloneable {
    private ArrayList<Tuple> listBody;
    
    public TupleList() {
        // initialCapacity is 10.
        listBody = new ArrayList<Tuple>();
    }
    
    public TupleList(int initialCapacity) {
        listBody = new ArrayList<Tuple>(initialCapacity);
    }
    
    public Tuple get(int ind) {
        return (Tuple)listBody.get(ind);
    }
    
    public void add(Tuple t) {
        listBody.add(t);
    }
    
    public void addAll(TupleList list) {
        listBody.addAll(list.getList());
    }
    
    public int size() {
        return listBody.size();
    }
    
    public void clear() {
        listBody = new ArrayList<Tuple>();
    }
    
    public Object clone() {
        TupleList l;
        
        int size = size();
        l = new TupleList(size);
        
        // We use deep clone
        ArrayList<Tuple> list = l.listBody;
        for(int i = 0; i < size; i++) {
            list.add((Tuple)listBody.get(i).clone());
        }
        
        return l;
    }
    
    public Object[] toArray() {
        return listBody.toArray();
    }
    
    public boolean isEmpty() {
        return listBody.isEmpty();
    }
    
    public Iterator<Tuple> iterator() {
        return listBody.iterator();
    }
        
    void reinit(TupleList list) {
        listBody = new ArrayList<Tuple>(list.size());
        listBody.addAll(list.getList());
    }
    
    ArrayList<Tuple> getList() {
        return listBody;
    }
    
    void setList(ArrayList<Tuple> l) {
        listBody = l;
    }
    
    TupleList getTupleList(int col) throws IndexOutOfBoundsException {
        Tuple t;
        TupleList t_l;
        
        int elem;
        int size = size();
        t_l = new TupleList(size);
        
        for(int i = 0; i < size; i++) {
            elem = ((Tuple)listBody.get(i)).get(col);
            t = new Tuple4Node(elem);
            t_l.add(t);
        }
        
        return t_l;
    }
    
    TupleList getTupleList(int[] cols) throws IndexOutOfBoundsException {
        Tuple t;
        TupleList t_l;
        
        if(cols.length == 1) {
            t_l = getTupleList(cols[0]);
        } else {
            int[] elems;
            int size = size();
            t_l = new TupleList(size);
            
            // Return a n-ary relation in TupleList
            for(int i = 0; i < size; i++) {
                elems = ((Tuple)listBody.get(i)).get(cols);
                t = new TupleImpl(elems, false);
                t_l.add(t);
            }
        }
        
        return t_l;
    }
    
    public TupleList select(TupleSelector selector) {
        Tuple t;
        TupleList t_l;
        
        t_l = new TupleList();
        if(selector == null) return t_l;
        
        int size = size();
        for(int i = 0; i < size; i++) {
            t = (Tuple)listBody.get(i);
            if(selector.select(t)) t_l.add(t);
        }
        
        return t_l;
    }
    
    /**
     * Sorts this list into natural ordering.
     */
    void sort() {
        int count = listBody.size();
        TupleList t_l = new TupleList(count);
        
        if(count > 0) {
            RadixSorter.sort(this, t_l);
            listBody = t_l.listBody;
        }
    }
    
    /**
     * Sort this tuple list in the specified column.
     */
    void sort(int col) {
        int count = listBody.size();
        TupleList t_l = new TupleList(count);
        
        if(count > 0) {
            RadixSorter.sort(this, col, t_l);
            listBody = t_l.listBody;
        }
    }
    
    /**
     * Removes all duplicate tuples from this tuple list.
     * This method requires that all the tuples in this list
     * have been sorted into the natural tuple ascending order.
     * Otherwise, this method may not work properly.
     * @return a list of removed tuples.
     * @see Tuple#compareTo
     */
    TupleList removeDuplicates() {
        int count = listBody.size();
        TupleList dupList = new TupleList();
        
        if(count > 0) {
            ArrayList<Tuple> al;
            al = new ArrayList<Tuple>(size());
            
            Tuple prev, next;
            prev = (Tuple)listBody.get(0);
            al.add(prev);
            
            for(int i = 1; i < count; i++) {
                next = (Tuple)listBody.get(i);
                if(prev.compareTo(next) != 0) {
                    al.add(next);
                } else {
                    dupList.add(next);
                }
                prev = next;
            }
            listBody = al;
        }
        
        return dupList;
    }
    
    /**
     * No precondition.
     * @return a list of removed tuples.
     */
    TupleList sort_removeDuplicates() {
        sort();
        return removeDuplicates();
    }
}
