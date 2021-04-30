package ca.uwaterloo.cs.jgrok.io;

import java.io.IOException;

import ca.uwaterloo.cs.jgrok.fb.Factbase;

public interface FactbaseWriter {
    public void write(String fileName, Factbase factbase)
        throws IOException;
}
