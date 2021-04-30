package ca.uwaterloo.cs.jgrok.interp;

import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.NodeSet;

/**
 * SetConstructorNode is used to create a set.
 */
public class SetConstructorNode extends ExpressionNode {
    ArrayList<ExpressionNode> items;
    
    public SetConstructorNode() {
        items = new ArrayList<ExpressionNode>();
    }
    
    public void addItem(ExpressionNode nd) {
        items.add(nd);
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        ExpressionNode nd;
        for(int i = 0; i < items.size(); i++) {
            nd = items.get(i);
            nd.propagate(env, userObj);
        }
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val;
        NodeSet set;
        ExpressionNode e;
        
        set = new NodeSet();
        for(int i = 0; i < items.size(); i++) {
            e = items.get(i);
            val = e.evaluate(env);
            set.add(val.toString());
        }
        
        return new Value(set);
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append('{');
        for(int i = 0; i < items.size(); i++) {
            buffer.append(items.get(i));
            buffer.append(", ");
        }
        if(items.size() == 0) {
            buffer.append('}');
        } else {
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.setCharAt(buffer.length()-1, '}');
        }
        return buffer.toString();
    }
}
