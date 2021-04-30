package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.env.Clazz;
import ca.uwaterloo.cs.jgrok.lib.FunctionLib;
import ca.uwaterloo.cs.jgrok.lib.FunctionLibManager;

public class MultiplicativeExpressionNode extends ExpressionNode {
    int op;
    ExpressionNode left;
    ExpressionNode right;
    
    public MultiplicativeExpressionNode(int op,
                                 ExpressionNode left,
                                 ExpressionNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }
    
    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        left.propagate(env, userObj);
        right.propagate(env, userObj);
    }
    
    /**
     * Evaluate 'right' as constructor, class method/field, JGrok library function.
     *  
     * @param env JGrok Environment
     * 
     * @throws NotQualifiedNameException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws EvaluationException
     */
    private Value evaluateQualifiedName(Env env)
        throws NotQualifiedNameException,
               NoSuchMethodException,
               NoSuchFieldException,
               EvaluationException {
        NotQualifiedNameException nameException;
        nameException = new NotQualifiedNameException();
        
        String leftName = QualifiedNameExpression.evalQualifiedName(env, left).replaceAll(" ", "");
        
        if(right instanceof FunctionExpressionNode) {
            FunctionExpressionNode funNode;
            funNode = (FunctionExpressionNode)right;
            
            /* Example:
             *   java.lang.Object()       myLib.compute()
             *          .                        .
             *         / \                      / \
             *        /   \                    /   \
             *       .  Object()           myLib compute()
             *      / \
             *     /   \
             *   java lang
             */
            
            // Evaluate qualified name as java class constructor.
            try {
                String className = leftName + "." + funNode.nameNode.evalName(env);
                Class<?> clazz = Clazz.forName(className);
                return funNode.evalConstructor(env, clazz);
            } catch(ClassNotFoundException e2) {}
            
            // Evaluate qualified name as java class method.
            try {
                Class<?> clazz = Clazz.forName(leftName);
                return funNode.evalClassMethod(env, clazz);
            } catch(ClassNotFoundException e2) {}
            
            // Evaluate qualified name as registered function.
            FunctionLib lib = FunctionLibManager.findLib(leftName);
            if(lib != null) {
                 return funNode.evalFunction(env, lib); 
            }
        } else if(right instanceof AttrSignNode) {
            throw nameException;
        } else if(right instanceof VariableNode) {
            // Evaluate qualified name as class field
            
            /* Example:
             *   java.lang.System.out
             *             .
             *            / \
             *           /   \
             *          .   out
             *         / \
             *        /   \
             *       .  System
             *      / \
             *     /   \
             *   java lang
             */
            try {
                Class<?> clazz = Clazz.forName(leftName);
                return ((VariableNode)right).evalClassField(env, clazz);
            } catch(ClassNotFoundException e2) {}
            
        }
        
        throw nameException;
    }
    
    /**
     * Evaluate 'right' as an object instance field/method.
     * 
     * @param env JGrok environment
     * @param obj object instance to evaluate
     * 
     * @throws NotQualifiedNameException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws EvaluationException
     */
    private Value evaluateInstance(Env env, Object obj)
        throws NotQualifiedNameException,
               NoSuchMethodException,
               NoSuchFieldException,
               EvaluationException {
        NotQualifiedNameException nameException;
        nameException = new NotQualifiedNameException();
        
        if(right instanceof FunctionExpressionNode) {
            // Evaluation instance method
            return ((FunctionExpressionNode)right).evalInstanceMethod(env, obj);
        } else if(right instanceof AttrSignNode) {
            throw nameException;
        } else if(right instanceof VariableNode) {
            // Evaluation instance field
            return ((VariableNode)right).evalInstanceField(env, obj);
        }
        throw nameException;
    }

    private boolean isQualifiedPossible() {
        if((op == Operator.PROJECT)) {
            if(left instanceof MultiplicativeExpressionNode) {
                MultiplicativeExpressionNode multiExp;
                multiExp = (MultiplicativeExpressionNode)left;
                
                if(multiExp.op != Operator.PROJECT)
                    return false;
                
                if((multiExp.left instanceof AttrSignNode)||
                   (multiExp.right instanceof AttrSignNode))
                    return false;
                
                if((multiExp.left instanceof VariableNode)&&
                   (multiExp.right instanceof VariableNode))
                    return true;
            } else if(left instanceof AttrSignNode) {
                return false;
            } else if(left instanceof VariableNode) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isQualifiedMoreLikely() {
        if(isQualifiedPossible()) {
            int opCol = getLocation().getColumn(); 
            int rightCol = right.getLocation().getColumn();
            return ((opCol + 1) == rightCol);
        }
        
        return false;
    }
    
    private Value evaluateQualifiedMoreLikely(Env env) throws EvaluationException {
        Value valL = null;
        Value valR = null;
        
        try {
            return evaluateQualifiedName(env);
        } catch(EvaluationException e1) {
            throw e1;
        } catch(Exception e2) {}
        
        // Evaluate left expression
        valL = left.evaluate(env);
        
        try {
            return evaluateInstance(env, valL.objectValue());
        } catch(EvaluationException e1) {
            throw e1;
        } catch(Exception e2) {
            // Do nothing if the right is not an instance field/method of the left.
        }
        
        // Evaluate right expression
        valR = right.evaluate(env);
        
        // "^", "*", "o", "/", "%", "X", ".", "**"
        Operation operation;
        TypeOperation typeOp;
        
        typeOp = TypeOperation.analyze(op, valL.getType(), valR.getType());
        if(typeOp != null) {
            try {
                operation = typeOp.getOperation();
                return operation.eval(op, valL, valR);
            } catch(Exception e) {
                throw new EvaluationException(this, e.getMessage());
            }
        } else {
            String err;
            err = ErrorMessage.errUnsupportedOperation(op, valL.getType(), valR.getType());
            throw new EvaluationException(this, err);
        }
    }
    
    private Value evaluateQualifiedLessLikely(Env env) throws EvaluationException {
        Value valL = null;
        Value valR = null;
        
        // Evaluate left and right expression
        try {
            valL = left.evaluate(env);
            valR = right.evaluate(env);
        } catch(EvaluationException e) {
            if(op == Operator.PROJECT) {
                if(valL == null) {
                    try {
                        return evaluateQualifiedName(env);
                    } catch(EvaluationException e1) {
                        throw e1;
                    } catch(Exception e2) {
                        throw e;
                    }
                } else {
                    try {
                        return evaluateInstance(env, valL.objectValue());
                    } catch(EvaluationException e1) {
                        throw e1;
                    } catch(Exception e2) {
                        throw e;
                    }
                }
            } else {
                throw e;
            }
        }
        
        // "^", "*", "o", "/", "%", "X", ".", "**"
        Operation operation;
        TypeOperation typeOp;
        
        typeOp = TypeOperation.analyze(op, valL.getType(), valR.getType());
        if(typeOp != null) {
            try {
                operation = typeOp.getOperation();
                return operation.eval(op, valL, valR);
            } catch(Exception e) {
                throw new EvaluationException(this, e.getMessage());
            }
        } else {
            String err;
            err = ErrorMessage.errUnsupportedOperation(op, valL.getType(), valR.getType());
            throw new EvaluationException(this, err);
        }
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        if(isQualifiedMoreLikely()) {
            return evaluateQualifiedMoreLikely(env);
        } else {
            return evaluateQualifiedLessLikely(env);
        }
    }
    
    @Override
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(left);
        if(op == Operator.PROJECT) {
            int opCol = getLocation().getColumn(); 
            int rightCol = right.getLocation().getColumn();
            if((opCol + 1) == rightCol) {
                buffer.append(Operator.key(op));
            } else {
                buffer.append(' ');
                buffer.append(Operator.key(op));
                buffer.append(' ');
            }
        } else {
            buffer.append(' ');
            buffer.append(Operator.key(op));
            buffer.append(' ');
        }
        buffer.append(right);
        
        return buffer.toString();
    }
}
