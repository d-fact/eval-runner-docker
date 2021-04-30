package ca.uwaterloo.cs.jgrok.interp;

import java.util.regex.PatternSyntaxException;

public class ValueMath {
    
    public static Value eval(int op, Value left, Value right)
        throws EvaluationException {
        
        switch(op) {
        case Operator.GT:
            return eval_GT(left, right);
            
        case Operator.LT:
            return eval_LT(left, right);
            
        case Operator.EQ:
            return eval_EQ(left, right);
            
        case Operator.NE:
            return eval_NE(left, right);
            
        case Operator.GE:
            return eval_GE(left, right);
            
        case Operator.LE:
            return eval_LE(left, right);
            
        case Operator.ME:
            return eval_ME(left, right);
            
        case Operator.UE:
            return eval_UE(left, right);
        }
        return Value.VOID;

    }
    
    public static Value eval_GT(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
            return new Value(doubles[0] > doubles[1]);
        }
        String sLeft  = left.toString();
        String sRight = right.toString();

        return new Value(sLeft.compareTo(sRight) > 0);
    }
    
    public static Value eval_LT(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
			return new Value(doubles[0] < doubles[1]);
		}
		
        String sLeft  = left.toString();
        String sRight = right.toString();
        
        return new Value(sLeft.compareTo(sRight) < 0);
    }

    public static Value eval_EQ(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
            return new Value(doubles[0] == doubles[1]);
        }
  
        String sLeft  = left.toString();
        String sRight = right.toString();
      
        return new Value(sLeft.compareTo(sRight) == 0);
    }
    
    public static Value eval_NE(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
            return new Value(doubles[0] != doubles[1]);
        }
       
        String sLeft  = left.toString();
        String sRight = right.toString();
        return new Value(sLeft.compareTo(sRight) != 0);
    }
    
    public static Value eval_GE(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
			return new Value(doubles[0] >= doubles[1]);
		}
        
        String sLeft  = left.toString();
        String sRight = right.toString();
      
        return new Value(sLeft.compareTo(sRight) >= 0);
    }

    public static Value eval_LE(Value left, Value right) {
        
        double[] doubles = bothDouble(left, right);
        if(doubles != null) {
            return new Value(doubles[0] <= doubles[1]);
        }
        
        String sLeft  = left.toString();
        String sRight = right.toString();
        return new Value(sLeft.compareTo(sRight) <= 0);
    }
    
    public static Value eval_ME(Value left, Value right)
        throws EvaluationException {
        String sLeft, sRight;
        sLeft = left.toString();
        sRight = right.toString();
        
        try {
            if(sLeft.matches(sRight))
                return new Value(true);
        } catch(PatternSyntaxException e) {
            throw e;
        }
        
        return new Value(false);
    }
    
    public static Value eval_UE(Value left, Value right)
        throws PatternSyntaxException {
        String sLeft, sRight;
        sLeft = left.toString();
        sRight = right.toString();
        
        try {
            if(! sLeft.matches(sRight))
                return new Value(true);
        } catch(PatternSyntaxException e) {
            throw e;
        }
        
        return new Value(false);
    }
    
/*
    private static long[] bothLong(Value v0, Value v1) {
        try {
			long l0, l1;
            l0 = v0.longValue();
            l1 = v1.longValue();
            
            long[] l = new long[2];
            l[0] = l0;
            l[1] = l1;
            return l;
        } catch(Exception e) {
            return null;
        }
    }
*/
    
    private static double[] bothDouble(Value v0, Value v1) {
        try {
			double d0, d1;
			
            d0 = v0.doubleValue();
            d1 = v1.doubleValue();
            
            double[] d = new double[2];
            d[0] = d0;
            d[1] = d1;
            return d;
        } catch(Exception e) {
            return null;
        }
    }
}
