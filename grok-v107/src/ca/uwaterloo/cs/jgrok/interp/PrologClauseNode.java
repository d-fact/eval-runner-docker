package ca.uwaterloo.cs.jgrok.interp;

import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

public class PrologClauseNode extends StatementNode {
    PrologExpressionNode expNode;
    StatementNode bodyNode;
    
    public PrologClauseNode(PrologExpressionNode expNode, StatementNode bodyNode) {
        this.expNode = expNode;
        this.bodyNode = bodyNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Header header = expNode.getHeader();
        
        if(bodyNode instanceof BlockStatementNode) {
            StatementNode s;
            ArrayList<StatementNode> nodes = ((BlockStatementNode)bodyNode).stmtNodes;
            ArrayList<Relation> myRels = new ArrayList<Relation>();
            
            for(int i = 0; i < nodes.size(); i++) {
                s = nodes.get(i);
                if(s instanceof PrologExpressionNode) {
                    Value val = s.evaluate(env);
                    myRels.add((Relation)val.objectValue());
                } else {
                    //???
                }
            }
            
            //??? selection  
            
            /////////////////////////////////////////////////////////////
            
            compose(myRels);
            
            Relation rel = myRels.get(0);

            try {
                rel = Relation.project(rel, header);
            } catch(UnknownColumnException e) {
                throw new EvaluationException(expNode, e.getMessage());
            }
            
            Scope scp = env.peepScope();
            String name = expNode.varNode.toString();
            Variable var;
            
            try {
                var = scp.lookup(name);
            } catch(LookupException e) {
                var = new Variable(scp, name);
                scp.addVariable(var);
            }
            
            //??? post-processing.
            
            var.setValue(new Value(rel.getBody()));
            
            return Value.EVAL;
        }
        
        return bodyNode.evaluate(env);
    }
    
    private void compose(ArrayList<Relation> myRels) {
        boolean outLoop = true;
        
        while(outLoop) {
            outLoop = false;
            
            for(int i = 0; i < myRels.size(); i++) {
                Relation source = myRels.get(i);
                Header srcHeader = source.getHeader();
                
                for(int j = i+1; j < myRels.size(); j++) {
                    Relation target = myRels.get(j);
                    Header trgHeader = target.getHeader();
                    
                    Header h = srcHeader.intersect(trgHeader);
                    if(h.size() > 0) {
                        Relation rel = Relation.compose(source, target, h);
                        myRels.remove(j);
                        myRels.remove(i);
                        myRels.add(rel);
                        outLoop = true;
                        break;
                    }
                }
                
                if(outLoop) break;
            }
        }
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(expNode);
        buffer.append(" = ");
        buffer.append(bodyNode);
        
        return buffer.toString();
    }
}
