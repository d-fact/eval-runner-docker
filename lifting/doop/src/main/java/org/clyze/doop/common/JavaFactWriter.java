package org.clyze.doop.common;

import org.clyze.doop.extractor.PresenceCondition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import static org.clyze.doop.common.PredicateFile.*;

/**
 * Common functionality that a fact writer for Java bytecode can reuse.
 */
public class JavaFactWriter {

    protected static final String L_OP = "1";
    protected static final String R_OP = "2";
    protected final Database _db;

    protected JavaFactWriter(Database db) {
        this._db = db;
    }

    public static String str(int i) {
        return String.valueOf(i);
    }

    protected String writeStringConstant(String constant, PresenceCondition pc) {
        String raw = FactEncoders.encodeStringConstant(constant);

        String result;
        if(raw.length() <= 256)
            result = raw;
        else
            result = "<<HASH:" + raw.hashCode() + ">>";

        _db.add(STRING_RAW, pc, result, raw);
        _db.add(STRING_CONST, pc, result);

        return result;
    }

    protected String hashMethodNameIfLong(String methodRaw) {
        if (methodRaw.length() <= 1024)
            return methodRaw;
        else
            return "<<METHOD HASH:" + methodRaw.hashCode() + ">>";
    }

    private void writeClassArtifact(String artifact, String className, String subArtifact, PresenceCondition pc) {
        _db.add(CLASS_ARTIFACT, pc, artifact, className, subArtifact);
    }

    private void writeAndroidKeepMethod(String methodSig, PresenceCondition pc) {
        _db.add(ANDROID_KEEP_METHOD, pc, "<" + methodSig + ">");
    }

    private void writeAndroidKeepClass(String className, PresenceCondition pc) {
        _db.add(ANDROID_KEEP_CLASS, pc, className);
    }

    private void writeProperty(String path, String key, String value, PresenceCondition pc) {
        String pathId = writeStringConstant(path, pc);
        String keyId = writeStringConstant(key, pc);
        String valueId = writeStringConstant(value, pc);
        _db.add(PROPERTIES, pc, pathId, keyId, valueId);
    }

    protected void writeMethodHandleConstant(String heap, String handleName, PresenceCondition pc) {
        _db.add(METHOD_HANDLE_CONSTANT, pc, heap, handleName);
    }

    protected void writeFormalParam(String methodId, String var, String type, int i, PresenceCondition pc) {
        _db.add(FORMAL_PARAM, pc, str(i), methodId, var);
        _db.add(VAR_TYPE, pc, var, type);
        _db.add(VAR_DECLARING_METHOD, pc, var, methodId);
    }

    protected void writeThisVar(String methodId, String thisVar, String type, PresenceCondition pc) {
        _db.add(THIS_VAR, pc, methodId, thisVar);
        _db.add(VAR_TYPE, pc, thisVar, type);
        _db.add(VAR_DECLARING_METHOD, pc, thisVar, methodId);
    }

    public void writeApplication(String applicationName, PresenceCondition pc) {
	_db.add(ANDROID_APPLICATION, pc, applicationName);
    }

    public void writeActivity(String activity, PresenceCondition pc) {
        _db.add(ACTIVITY, pc, activity);
    }

    public void writeService(String service, PresenceCondition pc) {
        _db.add(SERVICE, pc, service);
    }

    public void writeContentProvider(String contentProvider, PresenceCondition pc) {
        _db.add(CONTENT_PROVIDER, pc, contentProvider);
    }

    public void writeBroadcastReceiver(String broadcastReceiver, PresenceCondition pc) {
        _db.add(BROADCAST_RECEIVER, pc, broadcastReceiver);
    }

    public void writeCallbackMethod(String callbackMethod, PresenceCondition pc) {
        _db.add(CALLBACK_METHOD, pc, callbackMethod);
    }

    public void writeLayoutControl(Integer id, String viewClassName, Integer parentID, String appRId, String androidRId,
                                   PresenceCondition pc) {
        _db.add(LAYOUT_CONTROL, pc, id.toString(), viewClassName, parentID.toString());
    }

    public void writeSensitiveLayoutControl(Integer id, String viewClassName, Integer parentID, PresenceCondition pc) {
        _db.add(SENSITIVE_LAYOUT_CONTROL, pc, id.toString(), viewClassName, parentID.toString());
    }

    public void writePreliminaryFacts(BasicJavaSupport java, Parameters params) {
        PropertyProvider propertyProvider = java.getPropertyProvider();

        // Read all stored properties files
        for (Map.Entry<String, Properties> entry : propertyProvider.getProperties().entrySet()) {
            String path = entry.getKey();
            Properties properties = entry.getValue();

            for (String propertyName : properties.stringPropertyNames()) {
                String propertyValue = properties.getProperty(propertyName);
                writeProperty(path, propertyName, propertyValue, null);
            }
        }

        try {
            processSeeds(params._seed);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Last step of writing facts, after all classes have been processed.
     *
     * @param java  the object supporting basic Java functionality
     */
    public void writeLastFacts(BasicJavaSupport java) {
        Map<String, Set<ArtifactEntry>> artifactToClassMap = java.getArtifactToClassMap();

        System.out.println("Generated artifact-to-class map for " + artifactToClassMap.size() + " artifacts.");
        for (String artifact : artifactToClassMap.keySet())
            for (ArtifactEntry ae : artifactToClassMap.get(artifact))
                writeClassArtifact(artifact, ae.className, ae.subArtifact, null);
    }

    // The extra sensitive controls are given as a String
    // "id1,type1,parentId1,id2,type2,parentId2,...".
    public void writeExtraSensitiveControls(Parameters parameters) {
        if (parameters.getExtraSensitiveControls().equals("")) {
            return;
        }
        String[] parts = parameters.getExtraSensitiveControls().split(",");
        int partsLen = parts.length;
        if (partsLen % 3 != 0) {
            System.err.println("List size (" + partsLen + ") not a multiple of 3: \"" + parameters.getExtraSensitiveControls() + "\"");
            return;
        }
        for (int i = 0; i < partsLen; i += 3) {
            String control = parts[i] + "," + parts[i+1] + "," + parts[i+2];
            try {
                int controlId = Integer.parseInt(parts[i]);
                String typeId = parts[i+1].trim();
                int parentId  = Integer.parseInt(parts[i+2]);
                System.out.println("Adding sensitive layout control: " + control);
                writeSensitiveLayoutControl(controlId, typeId, parentId, null);
            } catch (Exception ex) {
                System.err.println("Ignoring control: " + control);
            }
        }
    }

    private void processSeeds(String seed) throws IOException {
        if (seed != null) {
            System.out.println("Reading seeds from: " + seed);
            try (Stream<String> stream = Files.lines(Paths.get(seed))) {
                stream.forEach(this::processSeedFileLine);
            }
        }
    }

    private void processSeedFileLine(String line) {
        if (line.contains("("))
            writeAndroidKeepMethod(line, null);
        else if (!line.contains(":"))
            writeAndroidKeepClass(line, null);
    }

    protected void writeMethodDeclaresException(String methodId, String exceptionType, PresenceCondition pc) {
        _db.add(METHOD_DECL_EXCEPTION, pc, exceptionType, methodId);
    }

    protected void writePhantomType(String t, PresenceCondition pc) {
        _db.add(PHANTOM_TYPE, pc, t);
    }

    protected void writePhantomMethod(String sig, PresenceCondition pc) {
        _db.add(PHANTOM_METHOD, pc, sig);
    }

    protected void writeLocal(String local, String type, String method, PresenceCondition pc) {
        _db.add(VAR_TYPE, pc, local, type);
        _db.add(VAR_DECLARING_METHOD, pc, local, method);
    }

    protected void writeArrayTypes(String arrayType, String componentType, PresenceCondition pc) {
        _db.add(ARRAY_TYPE, pc, arrayType);
        _db.add(COMPONENT_TYPE, pc, arrayType, componentType);
    }

    protected void writeAssignUnop(String insn, int index, String local, String methId, PresenceCondition pc) {
        _db.add(ASSIGN_UNOP, pc, insn, str(index), local, methId);
    }

    protected void writeClassModifier(String c, String modifier, PresenceCondition pc) {
        _db.add(CLASS_MODIFIER, pc, modifier, c);
    }

    protected void writeOperatorAt(String insn, String op, PresenceCondition pc) {
        _db.add(OPERATOR_AT, pc, insn, op);
    }

    protected void writeIf(String insn, int index, int indexTo, String methodId, PresenceCondition pc) {
        _db.add(IF, pc, insn, str(index), str(indexTo), methodId);
    }

    protected void writeIfVar(String insn, String branch, String local, PresenceCondition pc) {
        _db.add(IF_VAR, pc, insn, branch, local);
    }

    protected void writeDummyIfVar(String insn, String local, PresenceCondition pc) {
        _db.add(DUMMY_IF_VAR, pc, insn, local);
    }

    protected void writeAssignBinop(String insn, int index, String local, String methodId, PresenceCondition pc) {
        _db.add(ASSIGN_BINOP, pc, insn, str(index), local, methodId);
    }

    protected void writeAssignOperFrom(String insn, String branch, String local, PresenceCondition pc) {
        _db.add(ASSIGN_OPER_FROM, pc, insn, branch, local);
    }

    protected void writeInvokedynamic(String insn, int index, String bootSig, String dynName, String dynRetType,
                                      int dynArity, String dynParamTypes, int tag, String methodId,
                                      PresenceCondition pc) {
        _db.add(DYNAMIC_METHOD_INV, pc, insn, str(index), bootSig, dynName, dynRetType, str(dynArity), dynParamTypes, str(tag), methodId);
        // Make dynamic name and method type available to the analysis as string constants.
        writeStringConstant(dynName, pc);
        writeStringConstant(dynRetType + dynParamTypes, pc);
    }

    protected void writeInvokedynamicParameterType(String insn, int paramIndex, String type, PresenceCondition pc) {
        _db.add(DYNAMIC_METHOD_INV_PARAM, pc, insn, str(paramIndex), type);
    }

    protected void writeAssignLocal(String insn, int index, String from, String to, String methodId,
                                    PresenceCondition pc) {
        _db.add(ASSIGN_LOCAL, pc, insn, str(index), from, to, methodId);
    }

    protected void writeActualParam(int index, String invo, String var, PresenceCondition pc) {
        _db.add(ACTUAL_PARAMETER, pc, str(index), invo, var);
    }

    protected void writeMethodTypeConstant(String mt, PresenceCondition pc) {
        int rParen = mt.indexOf(")");
        int arity = 0;
        if (mt.startsWith("(") && (rParen != -1))
            arity = mt.substring(1, rParen).split(",").length;
        else
            System.err.println("Warning: cannot compute arity of " + mt);
        _db.add(METHOD_TYPE_CONSTANT, pc, mt, str(arity));
    }
}
