package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;

/**
 * Assignment Extensions: += -= *= /=
 */
public class AssignmentExtensionNode extends StatementNode {
    int op;
    VariableNode left;
    ExpressionNode expNode;
    
    public AssignmentExtensionNode(int op,
                                   VariableNode left,
                                   ExpressionNode expNode) {
        this.op = op;
        this.left = left;
        this.expNode = expNode;
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        Variable var;
        String evalName;
        Scope scp = env.peepScope();
        
        try {
            evalName = left.evalName(env);
        } catch(EvaluationException e) {
            evalName = ((VariableNode)left).toString();
        }
        
        // Only lookup the top scope.
        try {
            var = scp.lookup(evalName);
        } catch(LookupException e) {
            throw new EvaluationException(this, e.getMessage());
        }
        
        Value val;
        Value valL = var.getValue();
        Value valR = expNode.evaluate(env);

        // "+=" "-=" "*=" "/="
        if(valL.isPrimitive() && valR.isPrimitive()) {
            int opProxy = op;
            TypeOperation typeOp = null;
            
            switch(op) {
            case Operator.ASSIGN_PLUS:
                opProxy = Operator.PLUS;
                break;
            case Operator.ASSIGN_MINUS:
                opProxy = Operator.MINUS;
                break;
            case Operator.ASSIGN_MULTIPLY:
                opProxy = Operator.MULTIPLY;
                break;
            case Operator.ASSIGN_DIVIDE:
                opProxy = Operator.DIVIDE;
                break;
            }
            
            typeOp = TypeOperation.analyze(opProxy, valL.getType(), valR.getType());            
            if(typeOp != null) {
                try {
                    Operation operation = typeOp.getOperation();
                    var.setValue(operation.eval(opProxy, valL, valR));
                    // Assign variable's name to TupleSet
                    if(var.getValue().objectValue() instanceof TupleSet) {
                        ((TupleSet)var.getValue().objectValue()).setName(var.getName());
                    }
                } catch(Exception e) {
                    throw new EvaluationException(this, e.getMessage());
                }
            } else {
                String err;
                err = ErrorMessage.errUnsupportedOperation(op, valL.getType(), valR.getType());
                throw new EvaluationException(this, err);
            }
        } else {
            Object objL = valL.objectValue();
            Object objR = valR.objectValue();
            
            if(objL instanceof TupleSet && objR instanceof TupleSet && op == Operator.ASSIGN_PLUS) {
                val = new Value(AlgebraOperation.append((TupleSet)objL, (TupleSet)objR));
                var.setValue(val);
                // Assign variable's name to TupleSet
                if(var.getValue().objectValue() instanceof TupleSet) {
                    ((TupleSet)var.getValue().objectValue()).setName(var.getName());
                }
            } else {
                String err;
                err = ErrorMessage.errUnsupportedOperation(op, valL.getType(), valR.getType());
                throw new EvaluationException(this, err);
            }
        }
        
        return Value.EVAL;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(left);
        buffer.append(' ');
        buffer.append(Operator.key(op));
        buffer.append(' ');
        buffer.append(expNode);
        buffer.append(';');
        
        return buffer.toString();
    }    
}

