package ca.uwaterloo.cs.jgrok.interp;

import java.lang.reflect.Method;
import ca.uwaterloo.cs.jgrok.env.Clazz;
import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.lib.Function;
import ca.uwaterloo.cs.jgrok.lib.FunctionLib;

public class FunctionExpressionNode extends ExpressionNode {
    FunctionNameNode nameNode;
    ArgumentsNode argsNode;
    
    public FunctionExpressionNode(FunctionNameNode nameNode, ArgumentsNode argsNode) {
        this.nameNode = nameNode;
        this.argsNode = argsNode;
    }

    public void propagate(Env env, Object userObj)
        throws EvaluationException {
        nameNode.propagate(env, userObj);
        argsNode.propagate(env, userObj);
    }
    
    public Value evaluate(Env env) throws EvaluationException 
    {
        Function function = null;
        String   funcName = null;
        
        evalArguments(env); // Try evaluate function arguments.
        
        try {
            funcName = nameNode.evalName(env);
            function = env.lookupFunction(funcName);
            return function.invoke(env, argVals);
        } catch(Exception e) {
            throw new EvaluationException(this, funcName + "(): " + e.getMessage());
        } finally {
            cleanEvaluatedArguments();
        }
    }
    
    @Override
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(nameNode);
        buffer.append(argsNode);
        
        return buffer.toString();
    }
    
    // Cache arguments evaluation.
    private Value[] argVals = null;
    private Object[] argObjs = null;
    private Class<?>[] argTypes = null;
    private EvaluationException argsEvalException = null;
    
    private void cleanEvaluatedArguments() {
        argVals = null;
        argObjs = null;
        argTypes = null;
        argsEvalException = null;
    }
    
    private void evalArguments(Env env) throws EvaluationException {
        Value argsValue;
        
        // No need to evaluate as the last evaluation failed.
        if(argsEvalException != null) {
            throw argsEvalException;
        }
        
        // No need to evaluate again.
        if(argVals != null) {
            return;
        }
        
        try {
            argsValue = argsNode.evaluate(env);
        } catch(EvaluationException e) {
            argsEvalException = e;
            throw e;
        }
        
        Object o = argsValue.objectValue();
        if(o instanceof Value[]) {
            argVals = (Value[])o;
        } else {
            argVals = new Value[1];
            argVals[0] = argsValue;
        }
        
        argObjs = new Object[argVals.length];
        for(int i = 0; i < argVals.length; i++) {
            argObjs[i] = argVals[i].objectValue();
        }
        
        argTypes = new Class[argVals.length];
        for(int i = 0; i < argVals.length; i++) {
            argTypes[i] = argVals[i].getType();
        }
    }
    
    /**
     * Evaluate java class constructor.
     * 
     * @param env
     * @param clazz
     * @return evaluated result
     * @throws Exception
     */
    Value evalConstructor(Env env, Class<?> clazz)
        throws NoSuchMethodException, EvaluationException {
        evalArguments(env);
        
        try {
            Object result = null;
            if(argObjs.length == 0) {
                result = Clazz.newInstance(clazz);
            } else {
                result = Clazz.newInstance(clazz, argVals);
            }
            
            cleanEvaluatedArguments();
            return new Value(result);
        } catch(InstantiationException e) {
            throw new EvaluationException(this, e);
        }
    }
    
    /**
     * Evaluate java class method call.
     * 
     * @param env
     * @param clazz
     * @return evaluated result
     * @throws Exception
     */
    Value evalClassMethod(Env env, Class<?> clazz)
        throws NoSuchMethodException, EvaluationException {
        evalArguments(env);
        
        String methodName = nameNode.evalName(env);
        Method m = Clazz.getStaticMethod(clazz, methodName, argTypes);
        if(m != null) {
            try {
                Object result = m.invoke(null, argObjs);
                cleanEvaluatedArguments();
                
                if(result == null) return Value.VOID;
                return new Value(result);
            } catch(Exception e) {
                throw new EvaluationException(this, e);
            }
        } else {
            throw new NoSuchMethodException(methodName);
        }
    }
    
    /**
     * Evaluate java instance method call.
     * 
     * @param env
     * @param obj the object instance on which to invoke the method.
     * @return evaluated result
     * @throws Exception
     */
    Value evalInstanceMethod(Env env, Object obj)
        throws NoSuchMethodException, EvaluationException {
        evalArguments(env);
        
        Class<?> clazz = obj.getClass();
        String methodName = nameNode.evalName(env);
        Method m = Clazz.getMethod(clazz, methodName, argTypes);
        if(m != null) {
            try {
                Object result = m.invoke(obj, argObjs);
                cleanEvaluatedArguments();
            
                if(result == null) return Value.VOID;
                return new Value(result);
            } catch (Exception e) {
                throw new EvaluationException(this, e);
            }
        } else {
            throw new NoSuchMethodException(methodName);
        }
    }
    
    /**
     * Evaluate function registered in FunctionLib.
     * 
     * @param env
     * @param lib
     * @return evaluated result
     * @throws EvaluationException
     */
    Value evalFunction(Env env, FunctionLib lib)
        throws NoSuchMethodException, EvaluationException {
        evalArguments(env);
        
        String funcName = nameNode.evalName(env);
        Function function = lib.find(funcName);
        
        if (function == null) {
			throw new NoSuchMethodException(funcName + " not in FunctionLib " + lib.getName());
		}

		try {
			Value result = function.invoke(env, argVals);
            cleanEvaluatedArguments();
            return result;
        } catch(Exception e) {
			throw new EvaluationException(this, funcName + "(): " + e);
        }	
    }
}
