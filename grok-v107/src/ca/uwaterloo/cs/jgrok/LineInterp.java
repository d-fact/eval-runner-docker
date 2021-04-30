package ca.uwaterloo.cs.jgrok;

import java.io.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.util.*;

public class LineInterp {
    private Timing timing;
    private History history;
    private boolean keepHistory = true;

    private StringBuffer buffer;
    
    public LineInterp() {
        Interp.instance();
        history = new History();
        buffer = new StringBuffer();
    }
    
    public LineInterp(boolean keepHistory) {
        Interp.instance();
        history = new History();
        buffer = new StringBuffer();
        this.keepHistory = keepHistory;
    }
    
    public History getHistory() {
        return history;
    }
    
    public void evaluate(Env env, String line) {
        SyntaxTreeNode node;
        ScriptUnitNode unit;
        
        if(env == null) return;
        unit = env.getMainUnit();
        
        try {
            line = line.trim();
            
            if(line.startsWith("%") ||
               line.startsWith("//") ) {
                line = "";
            }
            
            if(line.endsWith("\\")) {
                while(line.endsWith("\\")) {
                    line = line.substring(0, line.length()-1);
                    line = line.trim();
                }
                
                if(line.length() > 0) {
                    buffer.append(" ");
                    buffer.append(line);
                }
                return;
            } else {
                if(line.length() > 0) {
                    buffer.append(" ");
                    buffer.append(line);
                }
            }
            
            // No need to parse/evaluate.
            if(buffer.length() == 0) return;
            
            // Record the command in the history.
            if(keepHistory) history.add(buffer.toString());
            
            // Start to parse/evaluate it.
            ByteArrayInputStream byteStream;
            byteStream = new ByteArrayInputStream(buffer.toString().getBytes());
            Interp.ReInit(byteStream);
            
            try {
                node = Interp.Statement();
            } catch(ParseException e) {
                byteStream.close();
                byteStream = new ByteArrayInputStream(buffer.toString().getBytes());
                Interp.ReInit(byteStream);
                node = Interp.Expression();
            }
            
            if(unit.isTimeOn()) {
                if(timing == null) timing = new Timing();
                timing.start();
            }
            
            // Do evaluation.
            Value value = node.evaluate(env);
            
            if(node instanceof ExpressionNode) value.print(env.out);
            
            if(unit.isTimeOn()) {
                if(timing == null) timing = new Timing();
                else {
                    timing.stop();
                    env.out.println("time:");
                    env.out.println("\t" + timing.getTime());
                }
            }
            
            if(unit.isEchoOn()) {
                env.out.println(Env.promptText + node.toString());
            }
            
            byteStream.close();
            byteStream = null;
            buffer.delete(0, buffer.length());
            
        } catch(IOException e) {
            env.out.println("Exception: " + e.getMessage());
        } catch(TokenMgrError e) {
            env.out.println("Exception: " + e.getMessage());
        } catch(ParseException e) {
            env.out.println("Exception: unable to parse " + buffer);
        } catch(EvaluationException e) {
            env.out.println("Exception: " + e.getMessage());
        } finally {
            buffer.delete(0, buffer.length());            
        }
    }
}    
