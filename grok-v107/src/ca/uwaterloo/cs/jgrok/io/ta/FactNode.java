package ca.uwaterloo.cs.jgrok.io.ta;

import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.Factbase;

class FactNode {
    String rel;
    String source;
    String target;
    
    FactNode(String relTok, String srcTok, String trgTok) {
        rel    = relTok;
        source = srcTok;
        target = trgTok;
    }
    
    void putInto(Factbase factbase) {
        EdgeSet eSet;
        eSet = factbase.getEdgeSet(rel);
        if(eSet == null) {
            eSet = new EdgeSet(rel);
            factbase.addSet(eSet);
        }
        
        eSet.add(source, target);
    }

    void putIntoScheme(Factbase factbase) {
        String name;
        EdgeSet eSet;
        
        name = rel;
        if(name.charAt(0) != '$') name = "$"+name;
        
        eSet = factbase.getEdgeSet(name);
        if(eSet == null) {
            eSet = new EdgeSet(name);
            factbase.addSet(eSet);
        }
        
        eSet.add(source, target);
    }
}
