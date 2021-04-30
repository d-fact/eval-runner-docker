package ca.uwaterloo.cs.jgrok.io;

import java.io.FileNotFoundException;

import ca.uwaterloo.cs.jgrok.fb.Factbase;

public interface FactbaseReader {
    public Factbase read(String dataFile)
        throws FileNotFoundException;
}
