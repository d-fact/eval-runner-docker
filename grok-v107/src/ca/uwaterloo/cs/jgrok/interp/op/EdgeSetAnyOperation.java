package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class EdgeSetAnyOperation extends Operation {
    
    public EdgeSetAnyOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, v.getType()));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        EdgeSet  l = (EdgeSet)left.objectValue();
        TupleSet r = (TupleSet)right.objectValue();
        
        if(r instanceof NodeSet) {
            switch(op) {
            case Operator.PROJECT:
                return new Value(AlgebraOperation.project(l, (NodeSet)r));
            case Operator.COMPOSE:
            case Operator.RCOMPOSE:
            case Operator.MULTIPLY:
                return new Value(AlgebraOperation.composition(l,(NodeSet)r));
            }
        } else if(r instanceof TupleSet) {
            switch(op) {
            case Operator.COMPOSE:
            case Operator.MULTIPLY:
                return new Value(AlgebraOperation.composition(l, (TupleSet)r));
            case Operator.RCOMPOSE:
                return new Value(AlgebraOperation.compositionRel(l, (TupleSet)r));
            }
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, left.getType(), right.getType()));
    }
}
