package ca.uwaterloo.cs.jgrok.interp;

public final class Operator {
    
    public static final int UNDEFINED  =-1;

    public static final int GT         = 0;                       // ">"
    public static final int LT         = GT + 1;                  // "<"
    public static final int EQ         = LT + 1;                  // "=="
    public static final int LE         = EQ + 1;                  // "<="
    public static final int GE         = LE + 1;                  // ">="
    public static final int NE         = GE + 1;                  // "!="
    public static final int ME         = NE + 1;                  // "=~"  match
    public static final int UE         = ME + 1;                  // "!~"  not match
    public static final int IN         = UE + 1;                  // "in"
    public static final int OR         = IN + 1;                  // "||"
    public static final int AND        = OR + 1;                  // "&&"
    public static final int NOT        = AND + 1;                 // "!"
    public static final int PLUS       = NOT + 1;                 // "+"
    public static final int MINUS      = PLUS + 1;                // "-"
    public static final int INTERSECT  = MINUS + 1;               // "^"
    public static final int MULTIPLY   = INTERSECT + 1;           // "*"
    public static final int COMPOSE    = MULTIPLY + 1;            // "o"    
    public static final int RCOMPOSE   = COMPOSE + 1;             // "**"
    public static final int DIVIDE     = RCOMPOSE + 1;            // "/"
    public static final int MOD        = DIVIDE + 1;              // "%"
    public static final int PROJECT    = MOD + 1;                 // "."
    public static final int CROSS      = PROJECT + 1;             // "X"
    
    public static final int ASSIGN         = CROSS + 1;           // "="
    public static final int ASSIGN_PLUS    = ASSIGN + 1;          // "+="
    public static final int ASSIGN_MINUS   = ASSIGN_PLUS + 1;     // "-="
    public static final int ASSIGN_MULTIPLY= ASSIGN_MINUS + 1;    // "*="
    public static final int ASSIGN_DIVIDE  = ASSIGN_MULTIPLY + 1; // "/="
    
    public static final int NUMBER     = ASSIGN_DIVIDE + 1;       // "#"
    public static final int TILDE      = NUMBER + 1;              // "~"
    public static final int ATTR       = TILDE + 1;               // "@"
    public static final int DOLLAR     = ATTR + 1;                // "$"
    public static final int ADDRESS    = DOLLAR + 1;              // "&"
    public static final int op_id      = ADDRESS + 1;             // "id"
    public static final int op_inv     = op_id + 1;               // "inv"
    public static final int op_dom     = op_inv + 1;              // "dom"
    public static final int op_rng     = op_dom + 1;              // "rng"
    public static final int op_ent     = op_rng + 1;              // "ent"
    
    public static int op(String key) {
        if(key == null) {
            return UNDEFINED;
        } else {
            for(int i = 0; i < keys.length; i++) {
                if(key.equals(keys[i])) return i;
            }
        }
        return UNDEFINED;
    }
    
    public static String key(int op) {
        return keys[op];
    }
    
    private final static String[] keys = {
        ">" ,
        "<" ,
        "==" ,
        "<=" ,
        ">=" ,
        "!=" ,
        "=~" ,
        "!~" ,
        "in" ,
        "||" ,
        "&&" ,
        "!" ,
        "+" ,
        "-" ,
        "^" ,
        "*" ,
        "o" ,
        "**" ,
        "/" ,
        "%" ,
        "." ,
        "X" ,
        "=" ,
        "+=",
        "-=",
        "*=",
        "/=",
        "#" ,
        "~" ,
        "@" ,
        "$" ,
        "&" ,
        "id" ,
        "inv" ,
        "dom" ,
        "rng" ,
        "ent"
    };
}
