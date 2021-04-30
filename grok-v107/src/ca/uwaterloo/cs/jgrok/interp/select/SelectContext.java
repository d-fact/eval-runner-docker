package ca.uwaterloo.cs.jgrok.interp.select;

import ca.uwaterloo.cs.jgrok.fb.Tuple;

public interface SelectContext {
    public Tuple getTuple();
    public void setTuple(Tuple t);
}
