package ca.uwaterloo.cs.jgrok.interp;

import java.util.Iterator;
import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

public class ForStatementNode extends StatementNode {
    VariableNode varNode;
    ExpressionNode expNode;
    StatementNode bodyNode;
    
    /**
     * Constructs ForStatementNode.
     * @param varNode the variable.
     * @param expNode the expression.
     * Its evaluated type must be <code>set</code>.
     * @param bodyNode the body statement.
     */
    public ForStatementNode(VariableNode varNode,
                            ExpressionNode expNode,
                            StatementNode bodyNode) {
        super();
        this.varNode = varNode;
        this.expNode = expNode;
        this.bodyNode = bodyNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Variable var;
        Value stmtVal;
        
        // Evaluate expression first!
        Value expVal = expNode.evaluate(env);
        Class<?> expType = expVal.getType();
        
        env.pushScope(new LocalScopedNode(env.peepScope()));
        
        try {
            // We do not use recursive lookup. Instead, we only
            // try to find the variable from the current scope.
            var = env.peepScope().lookup(varNode.getName());
        } catch(LookupException e) {
            Scope scp = env.peepScope();
            var = new Variable(scp, varNode.getName());
            scp.addVariable(var);
        }
        
        try {
            if(expType == NodeSet.class) {
                NodeSet set = (NodeSet)expVal.objectValue();
                Node[] nodes = set.getAllNodes();
                
                for(int i = 0; i < nodes.length; i++) {
                    var.setValue(new Value(nodes[i].get()));
                    stmtVal = bodyNode.evaluate(env);
                    if(stmtVal != Value.EVAL) return stmtVal;
                }
            } else if(expVal.objectValue() instanceof Object[]) {
                Object[] objs = (Object[])expVal.objectValue();
                
                for(int i = 0; i < objs.length; i++) {
                    var.setValue(new Value(objs[i]));
                    stmtVal = bodyNode.evaluate(env);
                    if(stmtVal != Value.EVAL) return stmtVal;
                }
            } else if(expVal.objectValue() instanceof Iterable) {
                @SuppressWarnings("unchecked")
                Iterator iter = ((Iterable)expVal.objectValue()).iterator();
                
                while(iter.hasNext()) {
                    var.setValue(new Value(iter.next()));
                    stmtVal = bodyNode.evaluate(env);
                    if(stmtVal != Value.EVAL) return stmtVal;
                }
            } else {
                throw new EvaluationException(expNode, "illegal expression (set, array, iterable required)");
            }
        } finally {
            env.popScope();
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append("for");
        buffer.append(' ');
        buffer.append(varNode);
        buffer.append(' ');
        buffer.append("in");
        buffer.append(' ');
        buffer.append(expNode);
        buffer.append(' ');
        buffer.append(bodyNode);
        
        return buffer.toString();
    }
}
