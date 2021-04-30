package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class IntOperation extends Operation {
    
    public IntOperation() {}
    
    public Value eval(int op, Value v)  throws OperationException {
        if(op == Operator.MINUS) {
			Class<?> type = v.getType();
			
			if (type != int.class) {
				return new Value(0 - v.longValue());
			}
			return new Value(0 - v.intValue());
		}	
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, long.class));
    }
    
    public Value eval(int op, Value left, Value right) throws OperationException {
        long l = left.longValue();
        long r = right.longValue();
        
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
        case Operator.MOD:
            return new Value(l % r);
        case Operator.PLUS:
            return new Value(l + r);
        case Operator.MINUS:
            return new Value(l - r);
        case Operator.MULTIPLY:
            return new Value(l * r);
        case Operator.DIVIDE:
            return new Value(l / r);
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, long.class, long.class));
    }
}
