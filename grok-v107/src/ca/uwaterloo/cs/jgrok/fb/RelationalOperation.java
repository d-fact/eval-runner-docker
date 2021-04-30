package ca.uwaterloo.cs.jgrok.fb;

public class RelationalOperation {
    
    public static boolean GT(NodeSet nSet1, NodeSet nSet2) {
if(nSet1.size() > nSet2.size()) {
    nSet1.trySort(0);
    nSet2.trySort(0);
    
    int cmp = Operation.comparison(nSet1.data, nSet2.data);
    if(cmp == 1) return true;
    return false;
}
return false;
    }
    public static boolean GE(NodeSet nSet1, NodeSet nSet2) {
if(nSet1.size() >= nSet2.size()) {
    nSet1.trySort(2);
    nSet2.trySort(2);
    
    int cmp = Operation.comparison(nSet1.data, nSet2.data);
    if(cmp == 0 || cmp == 1) return true;
    return false;
}
return false;
    }

    public static boolean LT(NodeSet nSet1, NodeSet nSet2) {
if(nSet1.size() < nSet2.size()) {
    nSet1.trySort(2);
    nSet2.trySort(2);
    
    int cmp = Operation.comparison(nSet1.data, nSet2.data);
    if(cmp == -1) return true;
    return false;
}
return false;
    }
    
    public static boolean LE(NodeSet nSet1, NodeSet nSet2) {
if(nSet1.size() <= nSet2.size()) {
    nSet1.trySort(2);
    nSet2.trySort(2);
    
    int cmp = Operation.comparison(nSet1.data, nSet2.data);
    if(cmp == -1 || cmp == 0) return true;
    return false;
}
return false;
    }
    
    public static boolean EQ(NodeSet nSet1, NodeSet nSet2) {
if(nSet1.size() == nSet2.size()) {
    nSet1.trySort(2);
    nSet2.trySort(2);
    
    int cmp = Operation.comparison(nSet1.data, nSet2.data);
    if(cmp == 0) return true;
    return false;
}
return false;
    }
    
    public static boolean NE(NodeSet nSet1, NodeSet nSet2) {
return !EQ(nSet1, nSet2);
    }
    
    //////////////////////////////////////////////////////////////////////
    
    public static boolean GT(EdgeSet eSet1, EdgeSet eSet2) {
if(eSet1.size() > eSet2.size()) {
    eSet1.trySort(2);
    eSet2.trySort(2);
    
    int cmp = Operation.comparison(eSet1.data, eSet2.data);
    if(cmp == 1) return true;
    return false;
}
return false;
    }
    
    public static boolean GE(EdgeSet eSet1, EdgeSet eSet2) {
if(eSet1.size() >= eSet2.size()) {
    eSet1.trySort(2);
    eSet2.trySort(2);
    
    int cmp = Operation.comparison(eSet1.data, eSet2.data);
    if(cmp == 0 || cmp == 1) return true;
    return false;
}
return false;
    }

    public static boolean LT(EdgeSet eSet1, EdgeSet eSet2) {
if(eSet1.size() < eSet2.size()) {
    eSet1.trySort(2);
    eSet2.trySort(2);
    
    int cmp = Operation.comparison(eSet1.data, eSet2.data);
    if(cmp == -1) return true;
    return false;
}
return false;
    }
    
    public static boolean LE(EdgeSet eSet1, EdgeSet eSet2) {
if(eSet1.size() <= eSet2.size()) {
    eSet1.trySort(2);
    eSet2.trySort(2);
    
    int cmp = Operation.comparison(eSet1.data, eSet2.data);
    if(cmp == -1 || cmp == 0) return true;
    return false;
}
return false;
    }

    public static boolean EQ(EdgeSet eSet1, EdgeSet eSet2) {
if(eSet1.size() == eSet2.size()) {
    eSet1.trySort(2);
    eSet2.trySort(2);
    
    int cmp = Operation.comparison(eSet1.data, eSet2.data);
    if(cmp == 0) return true;
    return false;
}
return false;
    }
    
    public static boolean NE(EdgeSet eSet1, EdgeSet eSet2) {
return !EQ(eSet1, eSet2);
    }
}
