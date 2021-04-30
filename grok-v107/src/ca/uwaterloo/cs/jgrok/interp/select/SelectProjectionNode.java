package ca.uwaterloo.cs.jgrok.interp.select;

import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;

public class SelectProjectionNode extends SelectConditionNode {
    ArrayList<ColumnNode> columnList;
    
    public SelectProjectionNode(ArrayList<ColumnNode> columnList) {
        this.columnList = columnList;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        ColumnNode column;
        for(int i = 0; i < columnList.size(); i++) {
            column = columnList.get(i);
            column.propagate(env, userObj);
        }
    }
    
    public TupleSet evaluate(Env env, TupleSet tSet)
        throws EvaluationException {
        int[] indexes;
        ColumnNode column;
        
        //??? varied TupleSet
        //??? &-n not processed.
        
        indexes = new int[columnList.size()];
        for(int i = 0; i < columnList.size(); i++) {
            column = columnList.get(i); //??? BUG
            indexes[i] = column.evaluate(env).intValue();  //??? BUG
        }
        
        Tuple t;
        TupleSet result;
        
        switch(indexes.length) {
        case 1:
            result = new NodeSet(tSet.size());
            break;
        case 2:
            result = new EdgeSet(tSet.size());
            break;
        default:
            result = new TupleSet(tSet.size());
            break;
        }
        
        for(int i = 0; i < tSet.size(); i++) {
            try {
                t = tSet.get(i);
                result.add(TupleFactory.create(t.get(indexes), false));
            } catch(Exception e) {}
        }
        
        result.removeDuplicates();
        return result;
    }
    
    public String toString() {
        StringBuffer buf;
        ColumnNode column;
        
        column = (ColumnNode)columnList.get(0);
        buf = new StringBuffer();        
        buf.append(column);
        
        for(int i = 1; i < columnList.size(); i++) {
            column = (ColumnNode)columnList.get(i);
            buf.append(", ");
            buf.append(column);
        }
        return buf.toString();
    }
}

