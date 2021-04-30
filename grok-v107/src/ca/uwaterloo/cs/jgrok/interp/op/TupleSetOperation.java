package ca.uwaterloo.cs.jgrok.interp.op;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.ErrorMessage;
import ca.uwaterloo.cs.jgrok.interp.Operation;
import ca.uwaterloo.cs.jgrok.interp.OperationException;
import ca.uwaterloo.cs.jgrok.interp.Operator;
import ca.uwaterloo.cs.jgrok.interp.Value;

public class TupleSetOperation extends Operation {
    
    public TupleSetOperation() {}
    
    public Value eval(int op, Value v)
        throws OperationException {
        TupleSet tSet = (TupleSet)v.objectValue();
        
        switch(op) {
        case Operator.NUMBER:
            return new Value(tSet.size());
        case Operator.op_id:
            return new Value(AlgebraOperation.id(tSet));
        case Operator.op_inv:
            return new Value(AlgebraOperation.inverse(tSet) );
        case Operator.op_ent: 
            return new Value(AlgebraOperation.entityOf(tSet));
        case Operator.op_dom:
            return new Value(AlgebraOperation.domainOf(tSet));
        case Operator.op_rng:
            return new Value(AlgebraOperation.rangeOf(tSet) );
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, v.getType()));
    }
    
    public Value eval(int op, Value left, Value right)
        throws OperationException {
        
        if(op == Operator.IN) {
            TupleSet tSet = (TupleSet)right.objectValue();
            if(tSet instanceof NodeSet) {
                return new Value(((NodeSet)tSet).contain(left.toString()));
            } 
        } else {
            TupleSet l = (TupleSet)left.objectValue();
            TupleSet r = (TupleSet)right.objectValue();
            
            switch(op) {
            case Operator.PLUS:
                return new Value(AlgebraOperation.union(l, r));
            case Operator.MINUS:
                return new Value(AlgebraOperation.difference(l, r));
            case Operator.COMPOSE:
                return new Value(AlgebraOperation.composition(l, r));
            case Operator.MULTIPLY:
                return new Value(AlgebraOperation.composition(l, r));
            case Operator.RCOMPOSE:
                return new Value(AlgebraOperation.compositionRel(l, r));
            case Operator.INTERSECT:
                return new Value(AlgebraOperation.intersection(l, r));
//             case Operator.GT:
//                 return new Value(RelationalOperation.GT(l, r));
//             case Operator.LT:
//                 return new Value(RelationalOperation.LT(l, r));
//             case Operator.EQ:
//                 return new Value(RelationalOperation.EQ(l, r));
//             case Operator.NE:
//                 return new Value(RelationalOperation.NE(l, r));
//             case Operator.GE:
//                 return new Value(RelationalOperation.GE(l, r));
//             case Operator.LE:
//                 return new Value(RelationalOperation.LE(l, r));
            }
            
        }
        
        throw new OperationException(ErrorMessage.errUnsupportedOperation(op, left.getType(), right.getType()));
    }
}
