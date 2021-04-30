package ca.uwaterloo.cs.jgrok.interp;

import java.io.*;

public class Location {
    private File file;
    private int line = -1;
    private int column = -1;
    
    public Location(File file, int line) {
        this.file = file;
        this.line = line;
    }
    
    public Location(File file, int line, int col) {
        this.file = file;
        this.line = line;
        this.column = col;
    }
    
    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        buffer.append("[");
        
        buffer.append("file: ");
        if(file != null) buffer.append(file.toString());
        
        buffer.append(" line: ");
        if(line != -1) buffer.append(line);
        
        if(column != -1) {
            buffer.append(" column: ");
            buffer.append(column);
        }
        
        buffer.append("]");
        return buffer.toString();
    }
    
    public File getFile() {
        return file;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public Location shortForm() {
        return new Location(file, line);
    }
}
