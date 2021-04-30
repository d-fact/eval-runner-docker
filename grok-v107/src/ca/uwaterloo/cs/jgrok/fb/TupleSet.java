package ca.uwaterloo.cs.jgrok.fb;

import java.io.*;

public class TupleSet implements Cloneable {
    
    public final static int FLAG_NONE = 0x0;
    public final static int FLAG_DUPLICATE = 0x4;

    /**
     * Its flags.
     */
    protected int flags = 0;
    
    /**
     * Its name.
     */
    protected String name = "?";
    
    /**
     * The sort level of tuples.
     *
     * <pre>
     *    +-----------+---------------------+
     *    | sortLevel |        data         |
     *    +-----------+---------------------+
     *    |    -1     |     not sorted      |
     *    |     0     |  sorted in Column 0 |
     *    |     1     |  sorted in Column 1 |
     *    |     2     |  sorted in Column 2 |
     *    |     3     |  sorted in Column 3 |
     *    |    ...    |  ...                |
     *    +-----------+---------------------+
     * </pre>
     */
    protected int sortLevel = -1;
    
    /**
     * The data (a list of tuples).
     */
    protected TupleList data;
    
    public TupleSet() {
        data = new TupleList(5);
    }
    
    public TupleSet(int initialCapacity) {
        data = new TupleList(initialCapacity);
    }
    
    public TupleSet(String name) {
        if(name != null)
            this.name = name;
        data = new TupleList(5);
    }
    
    public boolean hasName() {
        if(name == null || name == "?") return false;
        return true;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if(name != null)
            this.name = name;
    }
    
    public void setFlag(int flag) {        
        if(flag == FLAG_NONE)
            flags = flag;
        else
            flags = flags | flag;
    }
    
    public void unsetFlag(int flag) {
        flags = flags & (~flag);
    }
    
    public boolean hasDuplicates() {
        if(flags == FLAG_NONE)
            setFlag(FLAG_DUPLICATE);
        return (flags & FLAG_DUPLICATE) != 0;
    }
    
    public void setHasDuplicates(boolean b) {
        if(b) setFlag(FLAG_DUPLICATE);
        else unsetFlag(FLAG_DUPLICATE);
    }
    
    public int size() {
        return data.size();
    }
    
    public int columns() {
        if(size() > 0) {
            Tuple t = (Tuple)data.getList().get(0);
            return t.size();
        } else if(this instanceof NodeSet) {
            return 1;
        } else if(this instanceof EdgeSet) {
            return 2;
        }
        
        return 0;
    }
    
    public void add(Tuple t) {
        if(t != null) {
            data.add(t);
            flags = FLAG_NONE;
            sortLevel = -1;
        }
    }
    
    public Tuple get(int index) {
        return data.get(index);
    }
    
    public Tuple[] getAllTuples() {
        int count = size();
        Tuple[] result = new Tuple[count];
        
        for(int i = 0; i < count; i++) {
            result[i] = data.get(i);
        }
        return result;
    }
    
	public boolean inDom(String value)
	{
		int		count = size();
		int		i, index;
		Tuple	tuple;

		index = IDManager.getID(value);
		for (i = 0; i < count; ++i) {
			tuple = data.get(i);
			if (tuple.getDom() == index) {
				return true;
		}	}
		return false;
	}

    public TupleList getTupleList() {
        return data;
    }
    
    /**
     * Needs to be overwritten.
     */
    public TupleSet newSet() {
        TupleSet inst;
        inst = new TupleSet();
        return inst;
    }
    
    /**
     * Makes a clone of this TupleSet.
     */
    public Object clone() {
        TupleSet tupSet;
        
        tupSet = newSet();
        tupSet.name = this.name;
        tupSet.flags = this.flags;
        tupSet.data = (TupleList)data.clone();
        
        return tupSet;
    }
    
    /**
     * Sort tuples into ascending order.
     */
    protected void sort() {
        data.sort();
        if(size() > 1) {
            if(data.get(0).size() == data.get(size()-1).size()) {
                sortLevel = 0;
            } else {
                sortLevel = -1;   //??? We should set it to 'sortLevelAll'.
            }
        } else {
            sortLevel = 0;
        }
    }
    
    /**
     * Sort tuples into ascending order in the first column (Dom).
     */
    protected void sortDom() {
        trySort(0);
    }
    
    /**
     * Sort tuples into ascending order in the last column (Rng).
     */
    protected void sortRng() {
        TupleList t_l = new TupleList(data.size());
        RadixSorter.sortRng(data, t_l);
        sortLevel = -1;
        data = t_l;
    }
    
    /**
     * Sort tuples onto a sorting level.
     */
    protected void trySort(int level) {
        if(sortLevel != level) {
            sortLevel = level;
            TupleList t_l = new TupleList(data.size());
            RadixSorter.sort(data, level, t_l);
            data = t_l;
        }
    }
    
    public void removeDuplicates() {
        if(hasDuplicates()) {
            sort();
            data.removeDuplicates();
            unsetFlag(FLAG_DUPLICATE);
        }
    }
    
    /**
     * Prints all element data to <code>out</code>.
     * @param out the output stream.
     */
    public void print(OutputStream out) {
        Tuple t;
        PrintWriter writer;
        StringBuffer buffer;
        
        buffer = new StringBuffer();
        writer = new PrintWriter(out, true);
        
        int size = data.size();
        for(int i = 0; i < size; i++) {
            t = (Tuple)data.get(i);
            buffer.delete(0, buffer.length());
            
            int k;
            for(k = 0; k < t.size()-1; k++) {
                buffer.append(IDManager.get(t.get(k)));
                buffer.append(' ');
            }
            if (k > 0) {
                buffer.append(IDManager.get(t.get(k)));
            }
            
            if(buffer.length() > 0) {
                writer.println(buffer);
            }
        }
        
        writer.flush();
    }
    
    /**
     * Prints all element data to <code>out</code>
     * in the TA form.
     * @param out the output stream.
     */
    public void printTA(String name1, PrintWriter writer) 
    {
        Tuple	t;
        int		i, k;
        int		size = data.size();
        String	field;
        
        for(i = 0; i < size; i++) {
            t = (Tuple)data.get(i);
            writer.print(name1);
                      
            for(k = 0; k < t.size(); k++) {
                writer.print(' ');
                field = IDManager.get(t.get(k));
                if (field.charAt(0) == '$') {
					if (!field.equals("$ENTITY")) {
						if (field.equals("$RELATION")) {
							field = "($RELATION)";
						} else {
							field = field.substring(1);
							if (field.charAt(0) == '_') {
								field = field.substring(1);
								if (field.charAt(0) == '$') {
									field = field.substring(1);
								}
								field = "(" + field + ")";
				}	}	}	}
						
                writer.print(field);
            }
            writer.println();
        }
    }
    
    public void printRSF(String name1, PrintWriter writer) 
    {
        Tuple	t;
        int		i, k;
        int		size = data.size();
        String	field;
        
        for(i = 0; i < size; i++) {
            t = (Tuple)data.get(i);
            writer.print(name1);
                      
            for(k = 0; k < t.size(); k++) {
                writer.print(' ');
                field = IDManager.get(t.get(k));
                writer.print(field);
            }
            writer.println();
        }
    }
    
    /**
     * Appends all element data to the end of a file in RSF format.
     * @param fileName the output file name.
     */
    public void appendDB(String fileName) throws IOException {
        Tuple t;
        StringBuffer buffer;
        RandomAccessFile raf;
        
        buffer = new StringBuffer(name);
        raf = new RandomAccessFile(new File(fileName), "rw");
        raf.seek(raf.length());
        
        int i, k;
        int size = data.size();
        int nameLength = name.length();
        
        for(i = 0; i < size; i++) {
            t = (Tuple)data.get(i);
            buffer.delete(nameLength, buffer.length());	// Move back to the name
            
            for(k = 0; k < t.size(); k++) {
                buffer.append(' ');
                buffer.append(IDManager.get(t.get(k)));
            }
            
            raf.writeBytes(buffer + "\n");
        }
        
        raf.close();
    }
    
    /**
     * Add a tuple.
     * @param elems the elements.
     */
    public void add(String[] elems) {
        int[] ids = new int[elems.length];
        for(int i = 0; i < elems.length; i++) {
            ids[i] = IDManager.getID(elems[i]);
        }
        
        data.add(new TupleImpl(ids, false));
        sortLevel = -1;
        flags = FLAG_NONE;
    }
}
