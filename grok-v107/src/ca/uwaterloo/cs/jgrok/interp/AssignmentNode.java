package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

public class AssignmentNode extends StatementNode {
    VariableNode[] lefts;
    ExpressionNode expNode;
    
    public AssignmentNode(VariableNode left, 
                          ExpressionNode expNode) {
        this.lefts = new VariableNode[1];
        this.lefts[0] = left;
        this.expNode = expNode;
    }
    
    public AssignmentNode(VariableNode[] lefts, 
                          ExpressionNode expNode) {
        this.lefts = lefts;
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Value val;
        Variable var;
        Variable[] vars;
        VariableNode left;
        String evalName;
        
        vars = new Variable[lefts.length];
        for(int i = 0; i < lefts.length; i++) {
            left = lefts[i];
            try {
                evalName = left.evalName(env);
            } catch(EvaluationException e) {
                evalName = ((VariableNode)left).toString();
            }
            
            // left cannot be reserved word
            if(Env.isReservedWord(evalName)) {
                throw EvaluationException.createReservedWordException(lefts[i]);
            }
            
            try {
                var = env.lookup(evalName);
            } catch(LookupException e) {
                Scope scp = env.peepScope();
                var = new Variable(scp, evalName);
                scp.addVariable(var);
            }
            vars[i] = var;
        }
        
        try {
            val = expNode.evaluate(env);
        } catch(EvaluationException e) {
            for(int j = 0; j < vars.length; j++) {
                if(vars[j].getValue() == null) {
                    vars[j].getScope().removeVariable(vars[j]);
                }
            }
            throw e;
        }
        
        Object o = val.objectValue();
        
        if(! (o instanceof Value[])) {
            if(lefts.length == 1) {
                if(o instanceof TupleSet) {
                    TupleSet aSet;
                    aSet = (TupleSet)o;
                    
                    if(aSet.hasName()) {
                        val = new Value(aSet.clone());
                        aSet = (TupleSet)val.objectValue();
                    }
                    
                    aSet.setName(vars[0].getName());
                    aSet.removeDuplicates();
                }
            } else {
                for(int j = 1; j < vars.length; j++) {
                    if(vars[j].getValue() == null) {
                        vars[j].getScope().removeVariable(vars[j]);
                    }
                }
                
                if(vars.length > 1)
                    throw new EvaluationException(lefts[1],
                                                  vars[1].getName()
                                                  + " not assigned a value");
            }
            vars[0].setValue(val);
        } else {
            Value[] vals;
            vals = (Value[])o;
            
            if(lefts.length == 1) {
                if(vals.length >= 1) {
                    val = vals[0];
                    o = val.objectValue();
                    if(o instanceof TupleSet) {
                        TupleSet aSet;
                        aSet = (TupleSet)o;
                        
                        if(aSet.hasName()) {
                            val = new Value(aSet.clone());
                            aSet = (TupleSet)val.objectValue();
                        }
                        
                        aSet.setName(vars[0].getName());
                        aSet.removeDuplicates();
                    }
                    vars[0].setValue(val);
                } else {
                    throw new EvaluationException(lefts[0],
                                                  vars[0].getName()
                                                  + " not assigned a value");
                }
            } else {
                int i = 0;
                Object oi;
                try {
                    for(; i < lefts.length; i++) {
                        val = vals[i];
                        oi = val.objectValue();
                        if(oi instanceof TupleSet) {
                            TupleSet aSet;
                            aSet = (TupleSet)oi;
                            
                            if(aSet.hasName()) {
                                val = new Value(aSet.clone());
                                aSet = (TupleSet)val.objectValue();
                            }
                            
                            aSet.setName(vars[i].getName());
                            aSet.removeDuplicates();
                        }
                        vars[i].setValue(val);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    for(int j = 0; j < vars.length; j++) {
                        if(vars[j].getValue() == null)
                            vars[j].getScope().removeVariable(vars[j]);
                    }
                    throw new EvaluationException(lefts[i],
                                                  vars[i].getName()
                                                  + " not assigned a value");
                }
            }
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        if(lefts.length == 1) {
            buffer.append(lefts[0]);
        } else {
            buffer.append('(');
            for(int i = 0; i < lefts.length; i++) {
                buffer.append(lefts[i]);
                buffer.append(", ");
            }
            buffer.delete(buffer.length() - 2, buffer.length());
            buffer.append(')');
        }
        
        buffer.append(" = ");
        buffer.append(expNode);
        buffer.append(';');
        
        return buffer.toString();
    }    
}
