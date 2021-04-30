package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class NodeSetAnyOperation extends Operation {
    
    public NodeSetAnyOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, v.getType()));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        NodeSet l = (NodeSet)left.objectValue();
        TupleSet r = (TupleSet)right.objectValue();
        
        if(r instanceof EdgeSet) {
            switch(op) {
            case Operator.PROJECT:
                return new Value(AlgebraOperation.project(l, (EdgeSet)r));
            case Operator.COMPOSE:
            case Operator.MULTIPLY:
            case Operator.RCOMPOSE:
                return new Value(AlgebraOperation.composition(l,(EdgeSet)r));
            }
        } else if(r instanceof TupleSet) {
            switch(op) {
            case Operator.PROJECT:
                return new Value(AlgebraOperation.project(l, (TupleSet)r));
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
