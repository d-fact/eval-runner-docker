package examples;

import ca.uwaterloo.cs.jgrok.fb.*;

public class FactbaseExample {
    private Factbase fb;
    
    public FactbaseExample() {
        fb = new Factbase();
    }
    
    public void addSomething() {
        // Create a binary relation "call".
        EdgeSet eSet = new EdgeSet("call");
        
        fb.addSet(eSet);
        eSet.add("A", "B");
        eSet.add("A", "C");
        eSet.add("B", "X");
        
        // Create a set "function";
        NodeSet nSet = new NodeSet("function");
        fb.addSet(nSet);
        nSet.add("A");
        nSet.add("B");
        nSet.add("C");
        nSet.add("X");
    }
    
    public void doSomething() {
        Tuple t;
        TupleSet tSet;
        TupleList tList;
        String src, trg;
        
        // Print "call"
        tSet = fb.getSet("call");
        tList = tSet.getTupleList();
        for(int i = 0; i < tList.size(); i++) {
            t = tList.get(i);
            src = IDManager.get(t.get(0));
            trg = IDManager.get(t.get(1));
            System.out.println(src + " " + trg);
        }
        
        // Print "function"
        tSet = fb.getSet("function");
        tList = tSet.getTupleList();
        for(int i = 0; i < tList.size(); i++) {
            t = tList.get(i);
            src = IDManager.get(t.get(0));
            System.out.println(src);
        }
    }

    public void doSomething2() {
        TupleSet tSet;
        
        // Print "call"
        tSet = fb.getSet("call");
        tSet.print(System.out);
        
        // Print "function"
        tSet = fb.getSet("function");
        tSet.print(System.out);
    }
    
    public static void main(String[] args) {
        FactbaseExample p = new FactbaseExample();
         p.addSomething();
        System.out.println("doSomething");
        p.doSomething();
        System.out.println("doSomething2");
        p.doSomething();
    }
}
