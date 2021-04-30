package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class NodeSetOperation extends Operation {
    
    public NodeSetOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, v.getType()));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        NodeSet l = (NodeSet)left.objectValue();
        NodeSet r = (NodeSet)right.objectValue();
        
        switch(op) {
        case Operator.PLUS:
            return new Value(AlgebraOperation.union(l, r));
        case Operator.MINUS:
            return new Value(AlgebraOperation.difference(l, r));
        case Operator.CROSS:
            return new Value(AlgebraOperation.crossProduct(l, r));
        case Operator.INTERSECT:
            return new Value(AlgebraOperation.intersection(l, r));
        case Operator.GT:
            return new Value(RelationalOperation.GT(l, r));
        case Operator.LT:
            return new Value(RelationalOperation.LT(l, r));
        case Operator.EQ:
            return new Value(RelationalOperation.EQ(l, r));
        case Operator.NE:
            return new Value(RelationalOperation.NE(l, r));
        case Operator.GE:
            return new Value(RelationalOperation.GE(l, r));
        case Operator.LE:
            return new Value(RelationalOperation.LE(l, r));
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, left.getType(), right.getType()));
    }
}
