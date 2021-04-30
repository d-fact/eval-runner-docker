package ca.uwaterloo.cs.jgrok.fb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class Relation {
    protected Header header;
    protected TupleSet body;
    protected boolean reduce = true;
    
    public Relation(Header header, TupleSet inputData) {
        this.header = header;
        this.init(inputData);
    }
    
    public Relation(Header header, TupleSet inputData, boolean reduce) {
        this.header = header;
        this.reduce = reduce;
        this.init(inputData);
    }
    
    private void init(TupleSet inputData) {
        int[] indexes = new int[header.size()];

        int i = 0;
        int j = 0;
        int dims = 0;
        for(i = 0; i < header.size(); i++) {
            if(header.get(i).getName().equals("_"))
                indexes[i] = -1;
            else {
                dims++;
                indexes[i] = i;
            }
        }
        
        int[] indexes2 = new int[dims];
        Column[] cols = new Column[dims];
        
        i = 0;
        j = 0;
        for(i = 0 ; i < indexes2.length; i++) {
            for(; j < indexes.length; j++) {
                if(indexes[j] > -1) break;
            }
            
            cols[i] = header.get(j);
            indexes2[i] = indexes[j];
            j++;
        }
        
        // Reset Header
        header = new Header(cols);
        
        ///////////////////////////////////////////////////////////////////
        // SELECT
        
        TupleList tList;
        tList = inputData.getTupleList();
        tList = tList.getTupleList(indexes2);
        
        for(i = 0; i < dims; i++) {
            for(j = i+1; j < dims; j++) {
                if(header.get(i).equals(header.get(j))) {
                    TupleSelector selector = new TupleSelectorSimple(i, j);
                    tList = tList.select(selector);
                }
            }
        }
        
        ///////////////////////////////////////////////////////////////////
        // REDUCE (Each Column Must have Unique Name)
        
        if(reduce) {
            HashMap<Integer,Column> map = new HashMap<Integer,Column>(3);
            Column col;        
            String key;
            
            for(i = 0; i < header.size(); i++) {
                col = header.get(i);
                key = col.getName();
                if(!map.containsKey(key)) map.put(new Integer(i), col);
            }
            
            if(map.size() < dims) {
                i = 0;
                dims = map.size();
                int[] choose = new int[dims];
                Iterator<Integer> itr = map.keySet().iterator();
                
                while(itr.hasNext()) {
                    choose[i] = itr.next().intValue();
                    i++;
                }
                
                Arrays.sort(choose);
                cols = new Column[choose.length];
                for(i = 0; i < choose.length; i++) {
                    cols[i] = (Column)map.get(new Integer(choose[i]));
                }
                // Reset Header
                header = new Header(cols);
                
                TupleList tmp = new TupleList(tList.size());
                for(i = 0; i < tList.size(); i++) {
                    tmp.add(TupleFactory.create(tList.get(i).get(choose), false));
                }
                tList = tmp;
            }
        }
        
        ///////////////////////////////////////////////////////////////////
        if(dims == 1)
            body = new NodeSet();
        else if(dims == 2)
            body = new EdgeSet();
        else
            body = new TupleSet();
        
        body.data = tList;
        body.removeDuplicates();
    }
    
    public Header getHeader() {
        return header;
    }
    
    public TupleSet getBody() {
        return body;
    }
    
    public int[] getHeaderIndexes(Header h) {
        int[] indexes = new int[h.size()];
        
        for(int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
        }
        
        for(int i = 0; i < indexes.length; i++) {
            for(int j = 0; j < header.size(); j++) {
                if(h.get(i).equals(header.get(j)))
                    indexes[i] = j; 
            }
        }
        return indexes;
    }
    
    public static Relation project(Relation R, Header h)
        throws UnknownColumnException {
        int[] indexes = R.getHeaderIndexes(h);
        for(int i = 0; i < indexes.length; i++) {
            if(indexes[i] < 0) throw new UnknownColumnException(h.get(i));
        }
        
        TupleList tList = R.getBody().getTupleList().getTupleList(indexes);
        TupleSet newBody = new TupleSet();
        newBody.data = tList;
        
        return new Relation(h, newBody);
    }
    
    public static Relation compose(Relation R1, Relation R2, Header h) {
        int[] indexes1, indexes2;
        indexes1 = R1.getHeaderIndexes(h);
        indexes2 = R2.getHeaderIndexes(h);
        
        TupleList tList = OperationRel.compositionRel(R1.getBody().getTupleList(), indexes1, false,
                                                      R2.getBody().getTupleList(), indexes2, false);
        
        int dims = R1.getHeader().size() + R2.getHeader().size() - h.size();
        Column[] cols = new Column[dims];
        
        int i = 0;
        for(; i < R1.getHeader().size(); i++) {
            cols[i] = R1.getHeader().get(i);
        }
        
        int j = 0;
        for(; i < cols.length; i++) {
            for(; j < R2.getHeader().size(); j++) {
                if(!h.contains(R2.getHeader().get(j))) break;
            }
            cols[i] = R2.getHeader().get(j);
            j++;
         }
        
        Header newHeader = new Header(cols);
        TupleSet newBody = new TupleSet();
        newBody.data = tList;
        return new Relation(newHeader, newBody);
    }
}
