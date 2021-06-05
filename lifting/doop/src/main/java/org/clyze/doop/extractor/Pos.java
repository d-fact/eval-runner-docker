package org.clyze.doop.extractor;

public class Pos {
    private int line;
    private int col;

    public Pos(int l, int c) {
        line = l;
        col = c;
    }

    public boolean equals(Pos other) {
        return (line == other.line); // && col == other.col);
    }
}
