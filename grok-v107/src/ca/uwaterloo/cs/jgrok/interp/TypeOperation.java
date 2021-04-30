package ca.uwaterloo.cs.jgrok.interp;

import java.util.*;

import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.op.*;

public class TypeOperation {

    public static TypeOperation analyze(int op, Class<?> param) {
        if(   param == null
           || param == void.class) return null;
        
        TypeOperation vp = new TypeOperation(op, null, param);
        return vp.lookup();
    }
    
    public static TypeOperation analyze(int op, Class<?> param1, Class<?> param2) {
        if(   param1 == null
           || param2 == null
           || param1 == void.class
           || param2 == void.class) return null;
        
        TypeOperation vp = new TypeOperation(op, null, param1, param2);
        return vp.lookup();
    }
    
    private static Hashtable<String, ArrayList<TypeOperation>> allTOPs = new Hashtable<String, ArrayList<TypeOperation>>();
    
    /**---------------------------------*
     * Java encoding of types:
     *   B            byte
     *   C            char
     *   D            double
     *   F            float
     *   I            int
     *   J            long
     *   Lclassname;  class or interface
     *   S            short
     *   Z            boolean
     **---------------------------------*
     * JGrok encoding of types:
     *
     *   A            Any
     *
     *   I            int
     *   F            float
     *   S            string
     *   B            boolean
     *
     *   T            TupleSet
     *   E            EdgeSet
     *   N            NodeSet
     *----------------------------------*/
    
    /////////////////////////////////////////////////////////////////////////////////////////    
    static {
        Operation intOPs = new IntOperation();
        Operation floatOPs = new FloatOperation();
        Operation stringOPs = new StringOperation();
        Operation booleanOPs = new BooleanOperation();
        
        Operation nsetOPs = new NodeSetOperation ();
        Operation esetOPs = new EdgeSetOperation ();
        Operation tsetOPs = new TupleSetOperation();
        
        Operation _NxOPs = new NodeSetAnyOperation ();
        Operation _ExOPs = new EdgeSetAnyOperation ();
        Operation _TxOPs = new TupleSetAnyOperation();
        
        TypeOperation I_I = new TypeOperation(-1, int.class, int.class, intOPs);
        add("-", I_I);
        
        TypeOperation F_F = new TypeOperation(-1, float.class, float.class, floatOPs);
        add("-", F_F);
        
        TypeOperation B_B = new TypeOperation(-1, boolean.class, boolean.class, booleanOPs);
        add("!", B_B);
        add("!", B_B);
        
        TypeOperation I_T = new TypeOperation(-1, int.class, TupleSet.class, tsetOPs);
        add("#", I_T);
        
        TypeOperation T_T = new TypeOperation(-1, TupleSet.class, TupleSet.class, tsetOPs);
        add("id" , T_T);
        add("inv", T_T);
        add("ent", T_T);
        add("dom", T_T);
        add("rng", T_T);
        
        TypeOperation B_OT = new TypeOperation(-1, boolean.class, Object.class, TupleSet.class, tsetOPs);
        add("in", B_OT);
        
        TypeOperation I_II = new TypeOperation(-1, int.class, int.class, int.class, intOPs);
        TypeOperation B_II = new TypeOperation(-1, boolean.class, int.class, int.class, intOPs);
        add("+", I_II);
        add("-", I_II);
        add("*", I_II);
        add("/", I_II);
        add("%", I_II);
        
        add("==", B_II);
        add("!=", B_II);
        add(">" , B_II);
        add(">=", B_II);
        add("<" , B_II);
        add("<=", B_II);
        
        TypeOperation F_FF = new TypeOperation(-1, float.class, float.class, float.class, floatOPs);
        TypeOperation B_FF = new TypeOperation(-1, boolean.class, float.class, float.class, floatOPs);
        add("+", F_FF);
        add("-", F_FF);
        add("*", F_FF);
        add("/", F_FF);
        
        add("==", B_FF);
        add("!=", B_FF);
        add(">" , B_FF);
        add(">=", B_FF);
        add("<" , B_FF);        
        add("<=", B_FF);
        
        TypeOperation B_SS = new TypeOperation(-1, boolean.class, String.class, String.class, stringOPs);
        TypeOperation S_SO = new TypeOperation(-1, String.class , String.class, Object.class, stringOPs);
        TypeOperation S_OS = new TypeOperation(-1, String.class , Object.class, String.class, stringOPs);
        add("==", B_SS);
        add("!=", B_SS);
        add("<" , B_SS);
        add("<=", B_SS);
        add(">" , B_SS);
        add(">=", B_SS);
        add("=~", B_SS);
        add("!~", B_SS);
        add("+" , S_SO);
        add("+" , S_OS);
        
        TypeOperation B_BB = new TypeOperation(-1, boolean.class, boolean.class, boolean.class, booleanOPs);
        add("==", B_BB);
        add("!=", B_BB);

        TypeOperation B_NN = new TypeOperation(-1, boolean.class, NodeSet.class, NodeSet.class, nsetOPs);
        add("==", B_NN);
        add("!=", B_NN);
        add("<" , B_NN);
        add("<=", B_NN);
        add(">" , B_NN);
        add(">=", B_NN);
        
        TypeOperation B_EE = new TypeOperation(-1, boolean.class, EdgeSet.class, EdgeSet.class, esetOPs);
        add("==", B_EE);
        add("!=", B_EE);
        add("<" , B_EE);
        add("<=", B_EE);
        add(">" , B_EE);
        add(">=", B_EE);
        
        TypeOperation B_TT = new TypeOperation(-1, boolean.class,  TupleSet.class,  TupleSet.class, tsetOPs);
        add("==", B_TT);
        add("!=", B_TT);
        add("<" , B_TT);
        add("<=", B_TT);
        add(">" , B_TT);
        add(">=", B_TT);
        
        TypeOperation N_NN = new TypeOperation(-1, NodeSet.class, NodeSet.class, NodeSet.class, nsetOPs);
        add("+", N_NN);
        add("-", N_NN);
        add("^", N_NN);
        add("X", N_NN);
        
        TypeOperation E_EE = new TypeOperation(-1, EdgeSet.class, EdgeSet.class, EdgeSet.class, esetOPs);
        add("+", E_EE);
        add("-", E_EE);
        add("^", E_EE);
        add("o", E_EE);
        add("*", E_EE);

        TypeOperation T_EE = new TypeOperation(-1, TupleSet.class, EdgeSet.class, EdgeSet.class, esetOPs);
        add("**", T_EE);
        
        TypeOperation E_EN = new TypeOperation(-1, EdgeSet.class, EdgeSet.class, NodeSet.class, _ExOPs);
        TypeOperation E_NE = new TypeOperation(-1, EdgeSet.class, NodeSet.class, EdgeSet.class, _NxOPs);
        add("o", E_EN);
        add("o", E_NE);
        add("*", E_EN);
        add("*", E_NE);
        
        TypeOperation N_NE = new TypeOperation(-1, NodeSet.class, NodeSet.class, EdgeSet.class, _NxOPs);
        TypeOperation N_EN = new TypeOperation(-1, NodeSet.class, EdgeSet.class, NodeSet.class, _ExOPs);
        add(".", N_NE);
        add(".", N_EN);        
        
        TypeOperation T_TT = new TypeOperation(-1, TupleSet.class, TupleSet.class, TupleSet.class, tsetOPs);
        add("+", T_TT);
        add("-", T_TT);
        add("o", T_TT);
        add("*", T_TT);
        add("^", T_TT);
        
        TypeOperation T_TN = new TypeOperation(-1, TupleSet.class, TupleSet.class, NodeSet.class, _TxOPs);
        TypeOperation T_NT = new TypeOperation(-1, TupleSet.class, NodeSet.class, TupleSet.class, _NxOPs);
        add("o", T_TN);
        add("o", T_NT);
        add("*", T_TN);
        add("*", T_NT);
        add(".", T_NT);
        add(".", T_TN);
        
        TypeOperation T_TE = new TypeOperation(-1, TupleSet.class, TupleSet.class, EdgeSet.class, _TxOPs);
        TypeOperation T_ET = new TypeOperation(-1, TupleSet.class, EdgeSet.class, TupleSet.class, _ExOPs);
        add("o", T_TE);
        add("o", T_ET);
        add("*", T_TE);
        add("*", T_ET);
        
        // Insert entries for E(NE) E(EN) T(NT) T(TN) T(EE) T(TE) T(ET) T(TT): **
        add("**", E_NE);
        add("**", E_EN);
        add("**", T_NT);
        add("**", T_TN);
        add("**", T_TE);
        add("**", T_ET);
        add("**", T_TT);
        add("**", T_EE);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////    
    
    private static void add(String key, TypeOperation TOP) {
        ArrayList<TypeOperation> list = null;
        TypeOperation typeOP;
        
        list = allTOPs.get(key);
        if(list == null) {
            list = new ArrayList<TypeOperation>(5);
            allTOPs.put(key, list);
        }
        
        switch(TOP.params.length) {
        case 1:
            typeOP = new TypeOperation(Operator.op(key), TOP.type, TOP.params[0], TOP.operation);
            break;
        case 2:
            typeOP = new TypeOperation(Operator.op(key), TOP.type, TOP.params[0], TOP.params[1], TOP.operation);
            break;
        default:
            typeOP = TOP;
        }
        
        list.add(typeOP);
    }
    
    private int op;
    private Class<?> type;
    private Class<?>[] params;
    private Operation operation;
    
    public int getOp() {
        return op;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public Class<?>[] getParams() {
        return params;
    }
    
    public Operation getOperation() {
        return operation;
    }
    
    public String toString() {
        StringBuffer buf;
        buf = new StringBuffer();
        
        if(params.length == 2) {
            buf.append(type.toString());
            buf.append(" = ");
            buf.append(params[0].toString());
            buf.append(' ');
            buf.append(Operator.key(op));
            buf.append(' ');
            buf.append(params[1].toString());
        } else {
            buf.append(type.toString());
            buf.append(" = ");
            buf.append(Operator.key(op));
            buf.append(' ');
            buf.append(params[0].toString());
        }
        
        return buf.toString();
    }
    
    private TypeOperation(int op, Class<?> type, Class<?> param) {
        this.op = op;
        this.type = type;
        params = new Class[1];
        params[0] = param;
    }
    
    private TypeOperation(int op, Class<?> type, Class<?> param1, Class<?> param2) {
        this.op = op;
        this.type = type;
        params = new Class[2];
        params[0] = param1;
        params[1] = param2;
    }

    private TypeOperation(int op, Class<?> type, Class<?> param, Operation operation) {
        this.op = op;
        this.type = type;
        params = new Class[1];
        params[0] = param;
        this.operation = operation;
    }
    
    private TypeOperation(int op, Class<?> type, Class<?> param1, Class<?> param2, Operation operation) {
        this.op = op;
        this.type = type;
        params = new Class[2];
        params[0] = param1;
        params[1] = param2;
        this.operation = operation;
    }
    
    private TypeOperation lookup() {
        String key = Operator.key(op);
        ArrayList<TypeOperation> list = allTOPs.get(key);
        if(list == null) return null;
        
        int distNew;
        int distOld;
        TypeOperation TOP;
        TypeOperation result;
        
        result = null;
        distOld = 1000;
        for(int i = 0; i < list.size(); i++) {
            TOP = list.get(i);
            distNew = distanceMatch(TOP);
            if(distNew >= 0) {
                if(result == null) {
                    result = TOP;
                    distOld = distNew;
                } else {
                    if(distNew < distOld) {
                        result = TOP;
                        distOld = distNew;
                    }
                }
            }
            if(distNew == 0) break;
        }
        
        return result;
    }
    
    private int distanceMatch(TypeOperation TOP) {
        int distance = 0;
        Class<?>[] testParams = TOP.getParams();
        
        if(params.length != testParams.length) return -1000;
        if(params.length == 0) return 0;
        
        for(int i = 0; i < params.length; i++) {
            distance += distanceTo(params[i], testParams[i]);
            if(distance < 0) break;
        }
        
        return distance;
    }
    
    //  O <- F <- I
    //  O <- B
    //  O <- S
    //  O <- T <- N
    //  O <- T <- E
    
    private static int distanceTo(Class<?> v1, Class<?> v2) 
    {
		if (v1 == long.class || v1 == short.class) {
			v1 = int.class;
		} else if (v1 == double.class) {
			v1 = float.class;
		}
		
		if (v1 == v2) {
			return 0;
		} 
		if (v1 == int.class) {
            if (v2 == float.class)  return 1;
            if (v2 == Object.class) return 2;
            return -1000;
        } 
        if (v1 == float.class) {
            if( v2 == Object.class) return 1;
            return -1000;
        } 
        if (v1 == boolean.class) {
            if(v2 == Object.class)   return 1;
            return -1000;
        } 
        if (v1 == String.class) {
            if (v2 == Object.class) return 1;
            return -1000;
        } 
        if(v1 == EdgeSet.class) {
            if (v2 == TupleSet.class) return 1;
            if (v2 == Object.class) return 2;
            return -1000;
        } 
        if (v1 == NodeSet.class) {
            if (v2 == TupleSet.class) return 1;
            if (v2 == Object.class) return 2;
            return -1000;
        } 
        if (v1 == TupleSet.class) {
            if (v2 == Object.class) return 1;
        }
        return -1000;
    }
}
