package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class QualifiedNameExpression {
    /**
     * Evaluate a JGrok syntax tree expression as a qualified name.
     * 
     */
    public static String evalQualifiedName(Env env, ExpressionNode nd)
    throws NotQualifiedNameException {
        StringBuffer buffer = new StringBuffer(100);
        evalQualifiedName(env, nd, buffer);
        return buffer.toString();
    }
    
    private static void evalQualifiedName(Env env, ExpressionNode nd, StringBuffer buffer)
    throws NotQualifiedNameException {
        NotQualifiedNameException nameException;
        nameException = new NotQualifiedNameException();
        
        if(nd instanceof AttrSignNode) {
            throw nameException;
        }
        
        if(nd instanceof VariableNode) {
            try {
                buffer.append(((VariableNode)nd).evalName(env));
            } catch(EvaluationException e) {
                throw nameException;
            }
        } else if(nd instanceof MultiplicativeExpressionNode) {
            MultiplicativeExpressionNode multiExp;
            multiExp = (MultiplicativeExpressionNode)nd;
            
            int op = multiExp.op;
            ExpressionNode left = multiExp.left;
            ExpressionNode right = multiExp.right;
            
            if(op == Operator.PROJECT) {
                evalQualifiedName(env, left, buffer);
                
                if(right instanceof AttrSignNode) {
                    throw nameException;
                }
                
                if(right instanceof VariableNode) {
                    int opCol = multiExp.getLocation().getColumn(); 
                    int rightCol = right.getLocation().getColumn();
                    if((opCol + 1) == rightCol) {
                        buffer.append('.');
                    } else {
                        buffer.append(' ');
                        buffer.append('.');
                        buffer.append(' ');
                    }
                    
                    evalQualifiedName(env, right, buffer);
                } else {
                    throw nameException;
                }
            } else {
                throw nameException;
            }
        } else {
            throw nameException;
        }
    }
}
