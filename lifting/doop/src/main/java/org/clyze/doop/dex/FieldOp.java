package org.clyze.doop.dex;

import org.clyze.doop.common.Database;
import org.clyze.doop.common.PredicateFile;
import org.clyze.doop.extractor.PresenceCondition;

import java.util.Map;

class FieldOp {
    private final PredicateFile target;
    private final String insn;
    private final String strIndex;
    private final String localA;
    private final String localB;
    private final FieldInfo fieldInfo;
    private final String methId;
    private final PresenceCondition pc;

    /**
     * A field operation (read/write instance/static field).
     * @param target      the predicate file to use for writing
     * @param insn        the instruction id
     * @param strIndex    the instruction index
     * @param localA      the first local for instance field operations
     *                    (or the only local for static field operations)
     * @param localB      the second local for instance field operations
     *                    (or null for static field operations)
     * @param fieldInfo   the field id
     * @param methId      the enclosing method id
     */
    FieldOp(PredicateFile target, String insn, String strIndex,
                   String localA, String localB, FieldInfo fieldInfo,
                   String methId, PresenceCondition pc) {
        this.target = target;
        this.insn = insn;
        this.strIndex = strIndex;
        this.localA = localA;
        this.localB = localB;
        this.fieldInfo = fieldInfo;
        this.methId = methId;
        this.pc = pc;
    }

    void writeToDb(Database db, Map<String, String> resolvedFields) {
        String fieldId = fieldInfo.getFieldId();
        String resolvedFieldId = resolvedFields.getOrDefault(fieldId, fieldId);
        if (localB != null)
            db.add(target, pc, insn, strIndex, localA, localB, resolvedFieldId, methId);
        else
            db.add(target, pc, insn, strIndex, localA, resolvedFieldId, methId);
    }
}
