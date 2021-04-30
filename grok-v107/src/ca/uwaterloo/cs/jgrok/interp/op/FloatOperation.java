package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class FloatOperation extends Operation {
    
    public FloatOperation() {}
    
    public Value eval(int op, Value v) throws OperationException 
    {
        if(op == Operator.MINUS) {
   			Class<?> type = v.getType();
   			
   			if (type != float.class ) {
	            return new Value(0 - v.doubleValue());
	        }
	       	return new Value(0 - v.floatValue());
		}
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, double.class));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        double l = left.doubleValue();
        double r = right.doubleValue();
        
        switch(op) {
        case Operator.EQ:
            return new Value(l == r);
        case Operator.NE:
            return new Value(l != r);
        case Operator.GT:
            return new Value(l > r);
        case Operator.GE:
            return new Value(l >= r);
        case Operator.LT:
            return new Value(l < r);
        case Operator.LE:
            return new Value(l <= r);
        case Operator.PLUS:
            return new Value(l + r);
        case Operator.MINUS:
            return new Value(l - r);
        case Operator.MULTIPLY:
            return new Value(l * r);
        case Operator.DIVIDE:
            return new Value(l / r);
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, double.class, double.class));
    }
}
