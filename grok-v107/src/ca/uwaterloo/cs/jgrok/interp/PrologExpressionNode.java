package ca.uwaterloo.cs.jgrok.interp;

import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

public class PrologExpressionNode extends StatementNode {
    VariableNode varNode;
    ArrayList<VariableNode> argVarNodes;
    
    public PrologExpressionNode(VariableNode varNode, ArrayList<VariableNode> argVarNodes) {
        this.varNode = varNode;
        this.argVarNodes = argVarNodes;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val;
        Relation rel;
        
        val = varNode.evaluate(env);
        if(val.objectValue() instanceof TupleSet) {
            rel = new Relation(getHeader(), (TupleSet)val.objectValue(), true);
            return new Value(rel);
        } else {
            throw new EvaluationException(varNode, "unknown prolog error //???");
        }
    }
    
    Header getHeader() {
        int count = argVarNodes.size();
        Column[] cols = new Column[count];
        
        for(int i = 0; i < count; i++) {
            cols[i] = new Column(argVarNodes.get(i).toString());
        }
        return new Header(cols);
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(varNode);
        buffer.append('[');
        for(int i = 0; i < argVarNodes.size(); i++) {
            if(i > 0) {
                buffer.append(',');
                buffer.append(' ');
            }
            buffer.append(argVarNodes.get(i));
        }
        buffer.append(']');
        
        return buffer.toString();
    }
}
