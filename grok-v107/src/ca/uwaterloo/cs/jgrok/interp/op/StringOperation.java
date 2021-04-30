package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

import java.util.regex.PatternSyntaxException;

public class StringOperation extends Operation {
    
    public StringOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, String.class));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        String l = left.toString();
        String r = right.toString();
        
        try {
            switch(op) {
            case Operator.EQ:
                return new Value(l.compareTo(r) == 0);
            case Operator.NE:
                return new Value(l.compareTo(r) != 0);
            case Operator.GT:
                return new Value(l.compareTo(r) > 0);
            case Operator.GE:
                return new Value(l.compareTo(r) >= 0);
            case Operator.LT:
                return new Value(l.compareTo(r) < 0);
            case Operator.LE:
                return new Value(l.compareTo(r) <= 0);
            case Operator.ME:
                return new Value(l.matches(r));
            case Operator.UE:
                return new Value(! l.matches(r));
            case Operator.PLUS:
                return new Value(l + r);
            }
        } catch(PatternSyntaxException e) {
            throw new OperationException(e.getMessage());
        }

        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, String.class, String.class));
    }
}
