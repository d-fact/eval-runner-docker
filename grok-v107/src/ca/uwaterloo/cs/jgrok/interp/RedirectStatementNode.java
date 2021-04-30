package ca.uwaterloo.cs.jgrok.interp;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.io.FileOutputAppendStream;

public class RedirectStatementNode extends StatementNode {
    StatementNode stmtNode;
    ExpressionNode expNode;
    boolean isAppend;
    
    public RedirectStatementNode(StatementNode stmtNode,
                                 ExpressionNode expNode,
                                 boolean isAppend) {
        this.stmtNode = stmtNode;
        this.expNode  = expNode;
        this.isAppend = isAppend;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val = expNode.evaluate(env);
        if(val.getType() != String.class)
            throw new EvaluationException(expNode,
                                          "string expected after >>");
        
        String fileName = val.toString();
        PrintStream oldOut = env.out;
        
        try {
            fileName= val.toString();
            if(isAppend)
                env.out = new PrintStream(new FileOutputAppendStream(fileName), true);
            else 
                env.out = new PrintStream(new FileOutputStream(fileName), true);
            stmtNode.evaluate(env);
            env.out.close();
        } catch(FileNotFoundException e) {
            throw new EvaluationException(expNode,
                                          fileName + " not found");
        } catch(IOException e) {
            throw new EvaluationException(expNode, e.getMessage());
        } finally {
            env.out = oldOut;
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(stmtNode);
        buffer.append(' ');
        buffer.append(">>");
        if(isAppend) buffer.append('>');
        buffer.append(' ');
        buffer.append(expNode);
        buffer.append(';');
        
        return buffer.toString();
    }    
}
