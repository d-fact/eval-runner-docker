package org.clyze.doop.extractor;

public class PresenceCondition {
    private static PresenceCondition DEFAULT_PC = new PresenceCondition("");
   
    public static PresenceCondition getDefault() {
        return DEFAULT_PC;
    }

    public static void resetDefaultPC(String pc) {
        DEFAULT_PC = new PresenceCondition(pc);
    }

    private String pc;

    public PresenceCondition(String _pc) {
        pc = _pc;
    }

    public String toString() {
        return pc;
    }

    public boolean isTrue() {
        return pc.isEmpty();
    }
}

