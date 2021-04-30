package ca.uwaterloo.cs.jgrok.fb;

import java.util.*;

public class RadixSorter {
    
    /**
     * Sort tuples into an natural ascending order.
     * <pre>
     *   - -|
     *   - -|
     *   - - -|
     *   - - -|
     *   - - - -|
     *   - - - - -|
     * </pre>
     */
    public static void sort(TupleList list, TupleList result) {
        TupleList t_l;
        TupleList[] listArray;
        TupleListMap listMap = new TupleListMap();
        
        // Regroup into separate lists based on dimension.
        regroup(list, listMap);
        
        listArray = listMap.listArray;
        for(int i = 0; i < listArray.length; i++) {
            t_l = listArray[i];
            if(t_l != null) sortAll(t_l, t_l);
        }
        
        if(list == result) result.clear();
        for(int i = 0; i < listArray.length; i++) {
            t_l = listArray[i];
            if(t_l != null) result.addAll(t_l);
        }
    }
    
    private static void regroup(TupleList list, TupleListMap listMap) {
        Tuple t;
        TupleList t_l;
        int count = list.size();
        for(int i = 0; i < count; i++) {
            t = list.get(i);
            t_l = listMap.getList(t.size());
            t_l.add(t);
        }
    }
    
    private static void sortAll(TupleList list, TupleList result) {
        int count;
        TupleList t_l;
        TupleList aList = list;
        
        count = list.size();
        if(count > 0) {
            int colCount = list.get(0).size();
            for(int i = colCount-1; i >= 0; i--) {
                t_l = new TupleList(count);
                RadixSorter.sort(aList, i, t_l);
                aList = t_l;
            }
        }
        
        if(list == result) result.setList(aList.getList());
        else result.addAll(aList);
    }
    
    /**
     * Sort tuples by its first column (dom).
     */
    public static void sortDom(TupleList list, TupleList result) {
        sort(list, 0, result);
    }
    
    /**
     * Sort tuples by its last column (rng).
     */
    public static void sortRng(TupleList list, TupleList result) {
        RadixItem item;
        Queue q = new Queue();
        Queue p[] = new Queue[10];
        
        int loop = 0;
        int max = initQueueRng(q, list);
        for(;max > 0;) { max /= 10; loop++; }
        
        int val, last;
        int size = list.size();
        for(int j = 0; j < loop; j++) {
            for(int i = 0; i < 10; i++) {
                p[i] = new Queue();
            }
            
            for(int i = 0; i < size; i++) {
                item = (RadixItem)q.dequeue();
                val = item.getValue(); 
                last = getLastDigit(val);
                item.setValue(getPrefix(val));
                p[last].enqueue(item);
            }
            
            for(int i = 0 ; i < 10; i++) {
                q.append(p[i]);
            }
        }
        
        Cell next = null;
        Cell tail = q.trailer();
        if(tail == null) return;
        
        if(list == result) result.clear();
        for(next = tail.getNext(); next != tail; next = next.getNext()) {
            result.add(next.getItem().getTuple());
        }
        result.add(tail.getItem().getTuple());
    }
    
    /**
     * Sort tuples according to a column.
     */
    public static void sort(TupleList list, int col, TupleList result) {
        RadixItem item;
        Queue q = new Queue();
        Queue p[] = new Queue[10];
        
        int loop = 0;
        int max = initQueue(q, list, col);
        for(;max > 0;) { max /= 10; loop++; }

        int val, last;
        int size = list.size();
        for(int j = 0; j < loop; j++) {
            for(int i = 0; i < 10; i++) {
                p[i] = new Queue();
            }
            
            for(int i = 0; i < size; i++) {
                item = (RadixItem)q.dequeue();
                val = item.getValue(); 
                last = getLastDigit(val);
                item.setValue(getPrefix(val));
                p[last].enqueue(item);
            }
            
            for(int i = 0 ; i < 10; i++) {
                q.append(p[i]);
            }
        }
        
        Cell next = null;
        Cell tail = q.trailer();
        if(tail == null) return;
        
        if(list == result) result.clear();
        for(next = tail.getNext(); next != tail; next = next.getNext()) {
            result.add(next.getItem().getTuple());
        }
        result.add(tail.getItem().getTuple());
    }
    
    static int getPrefix(int val) {
        return val/10;
    }
    
    static int getLastDigit(int val) {
        return val%10;
    }
    
    static int initQueue(Queue queue, TupleList list, int col)  {
        Tuple t;
        Iterator<Tuple> iter;
        int val, maxNum = 0;
        
        iter = list.iterator();
        while(iter.hasNext()) {
            t = iter.next();
            
            if(col < t.size()) val = t.get(col);
            else val = 0;
            
            if(val > maxNum) maxNum = val;
            queue.enqueue(new RadixItem(val, t));
        }
        return maxNum;
    }
    
    static int initQueueRng(Queue queue, TupleList list)  {
        Tuple t;
        Iterator<Tuple> iter;
        int maxNum = 0;
        
        iter = list.iterator();
        while(iter.hasNext()) {
            t = iter.next();
            int val = t.getRng();
            if(val > maxNum) maxNum = val;
            queue.enqueue(new RadixItem(val, t));
        }
        return maxNum;
    }
}

class RadixItem {
    int val;
    Tuple tup;
    
    RadixItem(int val, Tuple tup) {
        this.val = val;
        this.tup = tup;
    }
    
    Tuple getTuple() {
        return tup;
    }
    
    int getValue() {
        return val;
    }
    
    void setValue(int val) {
        this.val = val;
    }
}

class Queue {
    Cell tail;
    
    Queue() {
        tail = null;
    }
    
    void enqueue(RadixItem item) {
        if(item == null) return;
        
        if(tail == null) {
            tail = new Cell(item, null);
            tail.setNext(tail);
        } else {
            tail.setNext(new Cell(item, tail.getNext()));
            tail = tail.getNext();
        }
    }
    
    RadixItem dequeue() {
        if(tail == null) return null;
        Cell ptr = tail.getNext();
        RadixItem item = ptr.getItem();
        if(ptr != tail) tail.setNext(ptr.getNext());
        else tail = null;
        return item;
    }
    
    void append(Queue q) {
        Cell ptr;
        
        if(tail == null) {
            tail = q.trailer();
        } else if(!q.empty()) {
            ptr = q.trailer().getNext();
            q.trailer().setNext(tail.getNext());
            tail.setNext(ptr);
            tail = q.trailer();
        }
    }
    
    Cell trailer() {
        return tail;
    }
    
    boolean empty() {
        return tail == null;
    }
}

class Cell {
    Cell next;
    RadixItem item;
    
    Cell(RadixItem item, Cell next) {
        this.item = item;
        this.next = next;
    }
    
    void setNext(Cell next) {
        this.next = next;
    }
    
    Cell getNext() {
        return next;
    }
    
    RadixItem getItem() {
        return item;
    }
}

class TupleListMap {
    TupleList[] listArray;
    
    TupleListMap() {
        listArray = new TupleList[10];
    }
    
    TupleList getList(int key) {
        TupleList list;
        
        try {
            list = listArray[key];
            if(list == null) {
                list = new TupleList();
                listArray[key] = list;
            }
        } catch(Exception e) {
            TupleList[] newListArray;
            newListArray = new TupleList[key+5];
            for(int i = 0; i < listArray.length; i++) {
                newListArray[i] = listArray[i];
            }
            
            list = new TupleList();
            newListArray[key] = list;
            listArray = newListArray;
        }
        return list;
    }
}
