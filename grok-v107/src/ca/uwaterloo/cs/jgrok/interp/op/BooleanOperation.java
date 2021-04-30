package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class BooleanOperation extends Operation {
    
    public BooleanOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        if(op == Operator.NOT || op == Operator.TILDE)
            return new Value(! v.booleanValue());
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, boolean.class));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        boolean l = left.booleanValue();
        boolean r = right.booleanValue();
        
        switch(op) {
        case Operator.EQ:
            return new Value(l == r);
        case Operator.NE:
            return new Value(l != r);
        case Operator.OR:
            return new Value(l || r);
        case Operator.AND:
            return new Value(l && r);
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, boolean.class, boolean.class));
    }
}
