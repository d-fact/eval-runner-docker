package ca.uwaterloo.cs.jgrok.lib;

import java.util.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

/**
 * Function <b>sort</b>
 * 
 * Sort set in ascending order.
 * <pre>
 *   TupleSet sort(TupleSet set) 
 *   TupleSet sort(TupleSet set, int colIndex)
 *   TupleSet sort(TupleSet set, boolean ascending)
 *   TupleSet sort(TupleSet set, int colIndex, boolean ascending)
 * </pre>
 * 
 * Sort set in descending order.
 * <pre>
 *   TupleSet sortd(TupleSet set)
 *   TupleSet sortd(TupleSet set, int colIndex)
 * </pre>
 * 
 * @author JingweiWu
 */
public class Sort extends Function {
         
    public Sort() 
    {
		name = "sort";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		boolean		ascending = true;
		int			col       = 0;
		boolean		colSeen   = false;
		TupleSet	tset;	
    
		switch (vals.length) {
		case 3:
			ascending = vals[2].booleanValue();
			col       = vals[1].intValue();
			colSeen   = true;
		case 2:
			if (!colSeen) {
				if (vals[1].isBoolean()) {
					ascending = vals[1].booleanValue();
				} else {
					col = vals[1].intValue();
					colSeen = true;
			}	}
		case 1:
			TupleSet tSet = vals[0].tupleSetValue();
		    Tuple[]  data;
			TupleSet result;
        
	        tSet = (TupleSet)tSet.clone();
			tSet.removeDuplicates();
        
			result = tSet.newSet();
            data   = tSet.getAllTuples();
        
			if(tSet.size() > 0) { 
				if (!colSeen) {
					sort(data, result, 0, ascending);
				} else {
					if(data[0].size() <= col) {
						throw new InvocationException("index " + col + " out of bounds " + data[0].size());
					}
					sort(data, result, col, ascending);
				}
			}
            result.setHasDuplicates(false);
			return new Value(result);
		}
		return illegalUsage();
    }
     
    private void sort(Tuple[] data, TupleSet result, int col, boolean upward) {        
        try {
            Arrays.sort(data, new TupleNumCmp(col));
        } catch(NumberFormatException e) {
            Arrays.sort(data, new TupleLexCmp(col));
        }
        
        int count = data.length;
        if(upward)
            for(int i = 0; i < count; i++) {
                result.add(data[i]);
            }
        else
            for(int i = 1; i <= count; i++) {
                result.add(data[count-i]);
            }
    }
    
    class TupleNumCmp implements Comparator<Tuple> {
        int col;
        
        TupleNumCmp(int col) {
            this.col = col;
        }
        
        public int compare(Tuple t1, Tuple t2) throws NumberFormatException {
            double d1 = Double.parseDouble(IDManager.get(t1.get(col)));
            double d2 = Double.parseDouble(IDManager.get(t2.get(col)));
            
            if (d1 < d2) return -1;
            if (d1 == d2) return 0;
            return 1;
        }
        
        public boolean equals(Object o) {
            return false;
        }
    }
    
    class TupleLexCmp implements Comparator<Tuple> {
        int col;
        
        TupleLexCmp(int col) {
            this.col = col;
        }
        
        public int compare(Tuple t1, Tuple t2) {
            String s1 = IDManager.get(t1.get(col));
            String s2 = IDManager.get(t2.get(col));
            return s1.compareTo(s2);
        }
        
        public boolean equals(Object o) {
            return false;
        }
    }
    
    public String usage()
    {
		return "TupleSet " + name + "(TupleSet set [, int colIndex] [, boolean ascending])";
	}
}
