package org.clyze.doop.soot;

import org.clyze.doop.common.BasicJavaSupport;
import org.clyze.doop.common.Database;
import org.clyze.doop.common.JavaFactWriter;
import org.clyze.doop.common.PredicateFile;
import org.clyze.doop.common.SessionCounter;
import org.clyze.doop.extractor.Annotations;
import org.clyze.doop.extractor.Pos;
import org.clyze.doop.extractor.PresenceCondition;
import org.clyze.doop.util.TypeUtils;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.typing.fast.BottomType;
import soot.tagkit.*;
import soot.util.backend.ASMBackendUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.clyze.doop.common.JavaRepresentation.*;
import static org.clyze.doop.common.PredicateFile.*;

/**
 * FactWriter determines the format of a fact and adds it to a
 * database.
 */
class FactWriter extends JavaFactWriter {
    private final Representation _rep;
    private final Map<String, Type> _varTypeMap = new ConcurrentHashMap<>();
    private final boolean _reportPhantoms;
    private final Annotations annotations;

    FactWriter(Database db, Representation rep, boolean reportPhantoms, Annotations a) {
        super(db);
        _rep = rep;
        _reportPhantoms = reportPhantoms;
        annotations = a;
    }

    int getPosition(Host h) {
        return h.getJavaSourceStartLineNumber();
    }

    PresenceCondition getFeatures(String className, Host h) {
        int dollarSignIndex = className.indexOf('$');
        String outerClassName = (dollarSignIndex > 0) ? className.substring(0, dollarSignIndex) : className;
        String filename = outerClassName + ".java";
        if (annotations == null) {
            return null;
        } else {
            return annotations.getPC(filename, getPosition(h));
        }
    }

    String getFactPC(String clsName, Host hd, Host... tl) {
        PresenceCondition _pc = getFeatures(clsName, hd);
        if (_pc == null) {
            return "";
        }

        String pc = _pc.toString();
        for (Host h: tl) {
            String hpc = getFeatures(clsName, h).toString();
            if (!pc.isEmpty() && !hpc.isEmpty()) {
                pc = "(" + pc + ") /\\ (" + hpc + ")";
            } else if (!hpc.isEmpty()) {
                pc = hpc;
            }
        }
        return pc;
    }

    String writeMethod(SootMethod m) {
        String methodRaw = _rep.signature(m);
        String result = hashMethodNameIfLong(methodRaw);
        String arity = Integer.toString(m.getParameterCount());

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition mPC = new PresenceCondition(getFactPC(clsName, m));

        _db.add(STRING_RAW, mPC, result, methodRaw);
        _db.add(METHOD, mPC, result, _rep.simpleName(m), Representation.params(m), writeType(m.getDeclaringClass()), writeType(m.getReturnType(), mPC), ASMBackendUtils.toTypeDesc(m.makeRef()), arity);
        if (m.getTag("VisibilityAnnotationTag") != null) {
            VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) m.getTag("VisibilityAnnotationTag");
            for (AnnotationTag aTag : vTag.getAnnotations()) {
                _db.add(METHOD_ANNOTATION, mPC, result, soot.coffi.Util.v().jimpleTypeOfFieldDescriptor(aTag.getType()).toQuotedString());
            }
        }
        if (m.getTag("VisibilityParameterAnnotationTag") != null) {
            VisibilityParameterAnnotationTag vTag = (VisibilityParameterAnnotationTag) m.getTag("VisibilityParameterAnnotationTag");

            ArrayList<VisibilityAnnotationTag> annList = vTag.getVisibilityAnnotations();
            for (int i = 0; i < annList.size(); i++) {
                if (annList.get(i) != null) {
                    for (AnnotationTag aTag : annList.get(i).getAnnotations()) {
                        _db.add(PARAM_ANNOTATION, mPC, result, str(i), soot.coffi.Util.v().jimpleTypeOfFieldDescriptor(aTag.getType()).toQuotedString());
                    }
                }
            }
        }
        return result;
    }

    void writeAndroidEntryPoint(SootMethod m) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition mPC = new PresenceCondition(getFactPC(clsName, m));

        _db.add(ANDROID_ENTRY_POINT, mPC, _rep.signature(m));
    }

    void writeClassOrInterfaceType(SootClass c) {
        String classStr = c.getName();
        PresenceCondition cPC = new PresenceCondition(getFactPC(classStr, c));

        boolean isInterface = c.isInterface();
        if (isInterface && c.isPhantom()) {
            if (_reportPhantoms)
                System.out.println("Interface " + classStr + " is phantom.");
            writePhantomType(c, cPC);
        }

        _db.add(isInterface ? INTERFACE_TYPE : CLASS_TYPE, cPC, classStr);
        _db.add(CLASS_HEAP, cPC, Representation.classConstant(c), classStr);
        if (c.getTag("VisibilityAnnotationTag") != null) {
            VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) c.getTag("VisibilityAnnotationTag");
            for (AnnotationTag aTag : vTag.getAnnotations()) {
                _db.add(CLASS_ANNOTATION, cPC, classStr, soot.coffi.Util.v().jimpleTypeOfFieldDescriptor(aTag.getType()).toQuotedString());
            }
        }
    }

    void writeDirectSuperclass(SootClass sub, SootClass sup) {
        String clsName = sub.getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, sub, sup));
        _db.add(DIRECT_SUPER_CLASS, pc, writeType(sub), writeType(sup));
    }

    void writeDirectSuperinterface(SootClass clazz, SootClass iface) {
        String clsName = clazz.getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, clazz, iface));
        _db.add(DIRECT_SUPER_IFACE, pc, writeType(clazz), writeType(iface));
    }

    private String writeType(SootClass c) {
        // The type itself is already taken care of by writing the
        // SootClass declaration, so we don't actually write the type
        // here, and just return the string.
        return c.getName();
    }

    private String writeType(Type t, PresenceCondition pc) {
        String result = t.toString();

        if (t instanceof ArrayType) {
            Type componentType = ((ArrayType) t).getElementType();
            writeArrayTypes(result, writeType(componentType, pc), pc);
        }
        else if (t instanceof PrimType || t instanceof NullType ||
                t instanceof RefType || t instanceof VoidType || t instanceof BottomType) {
            // taken care of by the standard facts
        }
        else
            throw new RuntimeException("Don't know what to do with type " + t);

        return result;
    }

    void writePhantomType(Type t, PresenceCondition pc) {
        writePhantomType(writeType(t, pc), pc);
    }

    private void writePhantomType(SootClass c, PresenceCondition pc) {
        writePhantomType(writeType(c.getType(), pc), pc);
    }

    @Override
    protected String writeStringConstant(String constant, PresenceCondition pc) {
        return super.writeStringConstant(constant, pc);
    }

    void writePhantomMethod(SootMethod m) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));
        String sig = writeMethod(m);
        if (_reportPhantoms)
            System.out.println("Method " + sig + " is phantom.");
        writePhantomMethod(sig, pc);
    }

    void writePhantomBasedMethod(SootMethod m) {
        String sig = writeMethod(m);
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));
        if (_reportPhantoms)
            System.out.println("Method signature " + sig + " contains phantom types.");
        _db.add(PHANTOM_BASED_METHOD, pc, sig);
    }

    void writeEnterMonitor(SootMethod m, EnterMonitorStmt stmt, Local var, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        _db.add(ENTER_MONITOR, pc, insn, str(index), Representation.local(m, var), methodId);
    }

    void writeExitMonitor(SootMethod m, ExitMonitorStmt stmt, Local var, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        _db.add(EXIT_MONITOR, pc, insn, str(index), Representation.local(m, var), methodId);
    }

    void writeAssignLocal(SootMethod m, Stmt stmt, Local to, Local from, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        writeAssignLocal(insn, index, Representation.local(m, from), Representation.local(m, to), methodId, pc);
    }

    void writeAssignThisToLocal(SootMethod m, Stmt stmt, Local to, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        writeAssignLocal(insn, index, Representation.thisVar(m), Representation.local(m, to), methodId, pc);
    }

    void writeAssignLocal(SootMethod m, Stmt stmt, Local to, ParameterRef ref, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        writeAssignLocal(insn, index, Representation.param(m, ref.getIndex()), Representation.local(m, to), methodId, pc);
    }

    void writeAssignInvoke(SootMethod inMethod, Stmt stmt, Local to, InvokeExpr expr, Session session) {
        String insn = writeInvokeHelper(inMethod, stmt, expr, session);

        String clsName = inMethod.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));
        _db.add(ASSIGN_RETURN_VALUE, pc, insn, Representation.local(inMethod, to));
    }

    void writeAssignHeapAllocation(SootMethod m, Stmt stmt, Local l, AnyNewExpr expr, Session session) {
        String heap = Representation.heapAlloc(m, expr, session);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        _db.add(NORMAL_HEAP, pc, heap, writeType(expr.getType(), pc));

        if (expr instanceof NewArrayExpr) {
            NewArrayExpr newArray = (NewArrayExpr) expr;
            Value sizeVal = newArray.getSize();

            if (sizeVal instanceof IntConstant) {
                IntConstant size = (IntConstant) sizeVal;

                if(size.value == 0)
                    _db.add(EMPTY_ARRAY, pc, heap);
            }
        }

        // statement
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);
        _db.add(ASSIGN_HEAP_ALLOC, pc, insn, str(index), heap, Representation.local(m, l), methodId, ""+getLineNumberFromStmt(stmt));
    }

    private static int getLineNumberFromStmt(Stmt stmt) {
        LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
        return tag == null ? 0 : tag.getLineNumber();
    }

    private Type getComponentType(ArrayType type) {
        // Soot calls the component type of an array type the "element
        // type", which is rather confusing, since in an array type
        // A[][][], the JVM Spec defines A to be the element type, and
        // A[][] is the component type.
        return type.getElementType();
    }

    /**
     * NewMultiArray is slightly complicated because an array needs to
     * be allocated separately for every dimension of the array.
     */
    void writeAssignNewMultiArrayExpr(SootMethod m, Stmt stmt, Local l, NewMultiArrayExpr expr, Session session) {
        writeAssignNewMultiArrayExprHelper(m, stmt, l, Representation.local(m,l), expr, (ArrayType) expr.getType(), session);
    }

    private void writeAssignNewMultiArrayExprHelper(SootMethod m, Stmt stmt, Local l, String assignTo, NewMultiArrayExpr expr, ArrayType arrayType, Session session) {
        String heap = Representation.heapMultiArrayAlloc(m, /* expr, */ arrayType, session);
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        String methodId = writeMethod(m);

        _db.add(NORMAL_HEAP, pc, heap, writeType(arrayType, pc));
        _db.add(ASSIGN_HEAP_ALLOC, pc, insn, str(index), heap, assignTo, methodId, ""+getLineNumberFromStmt(stmt));

        Type componentType = getComponentType(arrayType);
        if (componentType instanceof ArrayType) {
            String childAssignTo = Representation.newLocalIntermediate(m, l, session);
            writeAssignNewMultiArrayExprHelper(m, stmt, l, childAssignTo, expr, (ArrayType) componentType, session);
            int storeInsnIndex = session.calcUnitNumber(stmt);
            String storeInsn = Representation.instruction(m, stmt, storeInsnIndex);

            _db.add(STORE_ARRAY_INDEX, pc, storeInsn, str(storeInsnIndex), childAssignTo, assignTo, methodId);
            writeLocal(childAssignTo, writeType(componentType, pc), methodId, pc);
        }
    }

    // The commented-out code below is what used to be in Doop2. It is not
    // equivalent to code in old Doop. I (YS) tried to have a more compatible
    // approach for comparison purposes.
    /*
    public void writeAssignNewMultiArrayExpr(SootMethod m, Stmt stmt, Local l, NewMultiArrayExpr expr, Session session) {
        // what is a normal object?
        String heap = _rep.heapAlloc(m, expr, session);

        _db.addInput("NormalObject",
                _db.asEntity(heap),
                writeType(expr.getType()));

        // local variable to assign the current array allocation to.
        String assignTo = _rep.local(m, l);

        Type type = (ArrayType) expr.getType();
        int dimensions = 0;
        while(type instanceof ArrayType)
            {
                ArrayType arrayType = (ArrayType) type;

                // make sure we store the type
                writeType(type);

                type = getComponentType(arrayType);
                dimensions++;
            }

        Type elementType = type;

        int index = session.calcInstructionNumber(stmt);
        String rep = _rep.instruction(m, stmt, index);

        _db.addInput("AssignMultiArrayAllocation",
                _db.asEntity(rep),
                _db.asIntColumn(str(index)),
                _db.asEntity(heap),
                _db.asIntColumn(str(dimensions)),
                _db.asEntity(assignTo),
                _db.asEntity("Method", _rep.method(m)));

    // idea: do generate the heap allocations, but not the assignments
    // (to array indices). Do store the type of those heap allocations
    }
    */

    void writeAssignStringConstant(SootMethod m, Stmt stmt, Local l, StringConstant s, Session session) {
        String constant = s.toString();
        String content = constant.substring(1, constant.length() - 1);
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        String heapId = writeStringConstant(content, pc);

        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        _db.add(ASSIGN_HEAP_ALLOC, pc, insn, str(index), heapId, Representation.local(m, l), methodId, ""+getLineNumberFromStmt(stmt));
    }

    void writeAssignNull(SootMethod m, Stmt stmt, Local l, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_NULL, pc, insn, str(index), Representation.local(m, l), methodId);
    }

    void writeAssignNumConstant(SootMethod m, Stmt stmt, Local l, NumericConstant constant, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_NUM_CONST, pc, insn, str(index), constant.toString(), Representation.local(m, l), methodId);
    }

    private void writeAssignMethodHandleConstant(SootMethod m, Stmt stmt, Local l, MethodHandle constant, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String handleName = constant.getMethodRef().toString();
        String heap = methodHandleConstant(handleName);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        writeMethodHandleConstant(heap, handleName, pc);
        _db.add(ASSIGN_HEAP_ALLOC, pc, insn, str(index), heap, Representation.local(m, l), methodId, "0");
    }

    void writeAssignClassConstant(SootMethod m, Stmt stmt, Local l, ClassConstant constant, Session session) {
        String s = constant.getValue().replace('/', '.');
        String heap;
        char first = s.charAt(0);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        /* There is some weirdness in class constants: normal Java class
           types seem to have been translated to a syntax with the initial
           L, but arrays are still represented as [, for example [C for
           char[] */
        if (TypeUtils.isLowLevelType(first, s)) {
            // array type
            Type t = ClassHeapFinder.raiseTypeWithSoot(s);
            String actualType = t.toString();
            heap = Representation.classConstant(t);
            _db.add(CLASS_HEAP, pc, heap, actualType);
        } else if (first == '(') {
            // method type constant (viewed by Soot as a class constant)
            heap = s;
            writeMethodTypeConstant(heap, pc);
        } else {
//            SootClass c = soot.Scene.v().getSootClass(s);
//            if (c == null) {
//                throw new RuntimeException("Unexpected class constant: " + constant);
//            }
//
//            heap =  _rep.classConstant(c);
//            actualType = c.getName();
////              if (!actualType.equals(s))
////                  System.out.println("hallelujah!\n\n\n\n");
            // The code above should be functionally equivalent with the simple code below,
            // but the above causes a concurrent modification exception due to a Soot
            // bug that adds a phantom class to the Scene's hierarchy, although
            // (based on their own comments) it shouldn't.
            heap = classConstant(s);
            _db.add(CLASS_HEAP, pc, heap, s);
        }

        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        // REVIEW: the class object is not explicitly written. Is this always ok?
        _db.add(ASSIGN_HEAP_ALLOC, pc, insn, str(index), heap, Representation.local(m, l), methodId, "0");
    }

    void writeAssignCast(SootMethod m, Stmt stmt, Local to, Local from, Type t, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_CAST, pc, insn, str(index), Representation.local(m, from), Representation.local(m, to), writeType(t, pc), methodId);
    }

    void writeAssignCastNumericConstant(SootMethod m, Stmt stmt, Local to, NumericConstant constant, Type t, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_CAST_NUM_CONST, pc, insn, str(index), constant.toString(), Representation.local(m, to), writeType(t, pc), methodId);
    }

    void writeAssignCastNull(SootMethod m, Stmt stmt, Local to, Type t, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_CAST_NULL, pc, insn, str(index), Representation.local(m, to), writeType(t, pc), methodId);
    }

    void writeStoreInstanceField(SootMethod m, Stmt stmt, SootField f, Local base, Local from, Session session) {
        writeInstanceField(m, stmt, f, base, from, session, STORE_INST_FIELD);
    }

    void writeLoadInstanceField(SootMethod m, Stmt stmt, SootField f, Local base, Local to, Session session) {
        writeInstanceField(m, stmt, f, base, to, session, LOAD_INST_FIELD);
    }

    private void writeInstanceField(SootMethod m, Stmt stmt, SootField f, Local base, Local var, Session session, PredicateFile storeInstField) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        String fieldId = writeField(f);
        _db.add(storeInstField, pc, insn, str(index), Representation.local(m, var), Representation.local(m, base), fieldId, methodId);
    }

    void writeStoreStaticField(SootMethod m, Stmt stmt, SootField f, Local from, Session session) {
        writeStaticField(m, stmt, f, from, session, STORE_STATIC_FIELD);
    }

    void writeLoadStaticField(SootMethod m, Stmt stmt, SootField f, Local to, Session session) {
        writeStaticField(m, stmt, f, to, session, LOAD_STATIC_FIELD);
    }

    private void writeStaticField(SootMethod m, Stmt stmt, SootField f, Local var, Session session, PredicateFile staticFieldFacts) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        String fieldId = writeField(f);
        _db.add(staticFieldFacts, pc, insn, str(index), Representation.local(m, var), fieldId, methodId);
    }

    void writeLoadArrayIndex(SootMethod m, Stmt stmt, Local base, Local to, Local arrIndex, Session session) {
        writeLoadOrStoreArrayIndex(m, stmt, base, to, arrIndex, session, LOAD_ARRAY_INDEX);
    }

    void writeStoreArrayIndex(SootMethod m, Stmt stmt, Local base, Local from, Local arrIndex, Session session) {
        writeLoadOrStoreArrayIndex(m, stmt, base, from, arrIndex, session, STORE_ARRAY_INDEX);
    }

    private void writeLoadOrStoreArrayIndex(SootMethod m, Stmt stmt, Local base, Local var, Local arrIndex, Session session, PredicateFile predicateFile) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(predicateFile, pc, insn, str(index), Representation.local(m, var), Representation.local(m, base), methodId);

        if (arrIndex != null)
            _db.add(ARRAY_INSN_INDEX, pc, insn, Representation.local(m, arrIndex));
    }

    private void writeApplicationClass(SootClass application)
    {
        String clsName = application.getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, application));
        _db.add(APP_CLASS, pc, writeType(application));
    }

    String writeField(SootField f) {
        String fieldId = Representation.signature(f);

        String clsName = f.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, f));

        _db.add(FIELD_SIGNATURE, pc, fieldId, writeType(f.getDeclaringClass()), Representation.simpleName(f), writeType(f.getType(), pc));
        if (f.getTag("VisibilityAnnotationTag") != null) {
            VisibilityAnnotationTag vTag = (VisibilityAnnotationTag) f.getTag("VisibilityAnnotationTag");
            for (AnnotationTag aTag : vTag.getAnnotations()) {
                _db.add(FIELD_ANNOTATION, pc, fieldId, soot.coffi.Util.v().jimpleTypeOfFieldDescriptor(aTag.getType()).toQuotedString());
            }
        }
        return fieldId;
    }

    void writeFieldModifier(SootField f, String modifier) {
        String fieldId = writeField(f);

        String clsName = f.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, f));

        _db.add(FIELD_MODIFIER, pc, modifier, fieldId);
    }

    void writeClassModifier(SootClass c, String modifier) {
        String clsName = c.getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, c));
        writeClassModifier(c.getName(), modifier, pc);
    }

    void writeMethodModifier(SootMethod m, String modifier) {
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));

        _db.add(METHOD_MODIFIER, pc, modifier, methodId);
    }

    void writeReturn(SootMethod m, Stmt stmt, Local l, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(RETURN, pc, insn, str(index), Representation.local(m, l), methodId);
    }

    void writeReturnVoid(SootMethod m, Stmt stmt, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(RETURN_VOID, pc, insn, str(index), methodId);
    }

    // The return var of native methods is exceptional, in that it does not
    // correspond to a return instruction.
    void writeNativeReturnVar(SootMethod m) {
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));

        if (!(m.getReturnType() instanceof VoidType)) {
            String  var = Representation.nativeReturnVar(m);
            _db.add(NATIVE_RETURN_VAR, pc, var, methodId);
            writeLocal(var, writeType(m.getReturnType(), pc), methodId, pc);
        }
    }

    void writeGoto(SootMethod m, GotoStmt stmt, Session session) {
        Unit to = stmt.getTarget();
        session.calcUnitNumber(stmt);
        int index = session.getUnitNumber(stmt);
        session.calcUnitNumber(to);
        int indexTo = session.getUnitNumber(to);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(GOTO, pc, insn, str(index), str(indexTo), methodId);
    }

    /**
     * If
     */
    void writeIf(SootMethod m, IfStmt stmt, Session session) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        Unit to = stmt.getTarget();
        // index was already computed earlier
        int index = session.getUnitNumber(stmt);
        session.calcUnitNumber(to);
        int indexTo = session.getUnitNumber(to);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        writeIf(insn, index, indexTo, methodId, pc);

        Value condStmt = stmt.getCondition();
        if (condStmt instanceof ConditionExpr) {
            ConditionExpr condition = (ConditionExpr) condStmt;

            Local dummy = new JimpleLocal("tmp" + insn, BooleanType.v());
            writeDummyIfVar(insn, Representation.local(m, dummy), pc);

            if (condition instanceof EqExpr)
                writeOperatorAt(insn, "==", pc);
            else if (condition instanceof NeExpr)
                writeOperatorAt(insn, "!=", pc);
            else if (condition instanceof GeExpr)
                writeOperatorAt(insn, ">=", pc);
            else if (condition instanceof GtExpr)
                writeOperatorAt(insn, ">", pc);
            else if (condition instanceof LeExpr)
                writeOperatorAt(insn, "<=", pc);
            else if (condition instanceof LtExpr)
                writeOperatorAt(insn, "<", pc);

            // TODO: create table entry for constants (?)
            if (condition.getOp1() instanceof Local) {
                Local op1 = (Local) condition.getOp1();
                writeIfVar(insn, L_OP, Representation.local(m, op1), pc);
            }
            if (condition.getOp2() instanceof Local) {
                Local op2 = (Local) condition.getOp2();
                writeIfVar(insn, R_OP, Representation.local(m, op2), pc);
            }
        }
    }

    void writeTableSwitch(SootMethod inMethod, TableSwitchStmt stmt, Session session) {
        int stmtIndex = session.getUnitNumber(stmt);

        String clsName = inMethod.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));

        Value v = writeImmediate(inMethod, stmt, stmt.getKey(), session);

        if(!(v instanceof Local))
            throw new RuntimeException("Unexpected key for TableSwitch statement " + v + " " + v.getClass());

        Local l = (Local) v;
        String insn = Representation.instruction(inMethod, stmt, stmtIndex);
        String methodId = writeMethod(inMethod);

        _db.add(TABLE_SWITCH, pc, insn, str(stmtIndex), Representation.local(inMethod, l), methodId);

        for (int tgIndex = stmt.getLowIndex(), i = 0; tgIndex <= stmt.getHighIndex(); tgIndex++, i++) {
            session.calcUnitNumber(stmt.getTarget(i));
            int indexTo = session.getUnitNumber(stmt.getTarget(i));

            _db.add(TABLE_SWITCH_TARGET, pc, insn, str(tgIndex), str(indexTo));
        }

        session.calcUnitNumber(stmt.getDefaultTarget());
        int defaultIndex = session.getUnitNumber(stmt.getDefaultTarget());

        _db.add(TABLE_SWITCH_DEFAULT, pc, insn, str(defaultIndex));
    }

    void writeLookupSwitch(SootMethod inMethod, LookupSwitchStmt stmt, Session session) {
        int stmtIndex = session.getUnitNumber(stmt);

        String clsName = inMethod.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));

        Value v = writeImmediate(inMethod, stmt, stmt.getKey(), session);

        if(!(v instanceof Local))
            throw new RuntimeException("Unexpected key for TableSwitch statement " + v + " " + v.getClass());

        Local l = (Local) v;
        String insn = Representation.instruction(inMethod, stmt, stmtIndex);
        String methodId = writeMethod(inMethod);

        _db.add(LOOKUP_SWITCH, pc, insn, str(stmtIndex), Representation.local(inMethod, l), methodId);

        for(int i = 0, end = stmt.getTargetCount(); i < end; i++) {
            int tgIndex = stmt.getLookupValue(i);
            session.calcUnitNumber(stmt.getTarget(i));
            int indexTo = session.getUnitNumber(stmt.getTarget(i));

            _db.add(LOOKUP_SWITCH_TARGET, pc, insn, str(tgIndex), str(indexTo));
        }

        session.calcUnitNumber(stmt.getDefaultTarget());
        int defaultIndex = session.getUnitNumber(stmt.getDefaultTarget());

        _db.add(LOOKUP_SWITCH_DEFAULT, pc, insn, str(defaultIndex));
    }

    void writeUnsupported(SootMethod m, Stmt stmt, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.unsupported(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(UNSUPPORTED_INSTRUCTION, pc, insn, str(index), methodId);
    }

    /**
     * Throw statement
     */
    void writeThrow(SootMethod m, Unit unit, Local l, Session session) {
        int index = session.calcUnitNumber(unit);
        String insn = Representation.throwLocal(m, l, session);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, unit));

        _db.add(THROW, pc, insn, str(index), Representation.local(m, l), methodId);
    }

    /**
     * Throw null
     */
    void writeThrowNull(SootMethod m, Stmt stmt, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(THROW_NULL, pc, insn, str(index), methodId);
    }

    void writeExceptionHandlerPrevious(SootMethod m, Trap current, Trap previous, SessionCounter counter) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));

        _db.add(EXCEPT_HANDLER_PREV, pc, _rep.handler(m, current, counter), _rep.handler(m, previous, counter));
    }

    void writeExceptionHandler(SootMethod m, Trap handler, Session session) {
        SootClass exc = handler.getException();

        Local caught;
        {
            Unit handlerUnit = handler.getHandlerUnit();
            IdentityStmt stmt = (IdentityStmt) handlerUnit;
            Value left = stmt.getLeftOp();
            Value right = stmt.getRightOp();

            if (right instanceof CaughtExceptionRef && left instanceof Local) {
                caught = (Local) left;
            }
            else {
                throw new RuntimeException("Unexpected start of exception handler: " + handlerUnit);
            }
        }

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));

        String insn = _rep.handler(m, handler, session);
        int handlerIndex = session.getUnitNumber(handler.getHandlerUnit());
        session.calcUnitNumber(handler.getBeginUnit());
        int beginIndex = session.getUnitNumber(handler.getBeginUnit());
        session.calcUnitNumber(handler.getEndUnit());
        int endIndex = session.getUnitNumber(handler.getEndUnit());
        _db.add(EXCEPTION_HANDLER, pc, insn, _rep.signature(m), str(handlerIndex), exc.getName(), Representation.local(m, caught), str(beginIndex), str(endIndex));
    }

    void writeThisVar(SootMethod m) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));
        String methodId = writeMethod(m);
        String thisVar = Representation.thisVar(m);
        String type = writeType(m.getDeclaringClass());
        writeThisVar(methodId, thisVar, type, pc);
    }

    void writeMethodDeclaresException(SootMethod m, SootClass exception) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, exception));
        writeMethodDeclaresException(writeMethod(m), writeType(exception), pc);
    }

    void writeFormalParam(SootMethod m, int i) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));
        String methodId = writeMethod(m);
        String var = Representation.param(m, i);
        String type = writeType(m.getParameterType(i), pc);
        writeFormalParam(methodId, var, type, i, pc);
    }

    void writeLocal(SootMethod m, Local l) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m));
        String local = Representation.local(m, l);
        Type type;

        if (_varTypeMap.containsKey(local))
            type = _varTypeMap.get(local);
        else {
            type = l.getType();
            _varTypeMap.put(local, type);
        }

        writeLocal(local, writeType(type, pc), writeMethod(m), pc);
    }

    Local writeStringConstantExpression(SootMethod inMethod, Stmt stmt, StringConstant constant, Session session) {
        // introduce a new temporary variable
        String basename = "$stringconstant";
        String varname = basename + session.nextNumber(basename);
        Local l = new JimpleLocal(varname, RefType.v("java.lang.String"));
        writeLocal(inMethod, l);
        writeAssignStringConstant(inMethod, stmt, l, constant, session);
        return l;
    }

    Local writeNullExpression(SootMethod inMethod, Stmt stmt, Type type, Session session) {
        // introduce a new temporary variable
        String basename = "$null";
        String varname = basename + session.nextNumber(basename);
        Local l = new JimpleLocal(varname, type);
        writeLocal(inMethod, l);
        writeAssignNull(inMethod, stmt, l, session);
        return l;
    }

    Local writeNumConstantExpression(SootMethod inMethod, Stmt stmt, NumericConstant constant, Session session) {
        // introduce a new temporary variable
        String basename = "$numconstant";
        String varname = basename + session.nextNumber(basename);
        Local l = new JimpleLocal(varname, constant.getType());
        writeLocal(inMethod, l);
        writeAssignNumConstant(inMethod, stmt, l, constant, session);
        return l;
    }

    Local writeClassConstantExpression(SootMethod inMethod, Stmt stmt, ClassConstant constant, Session session) {
        // introduce a new temporary variable
        String basename = "$classconstant";
        String varname = basename + session.nextNumber(basename);
        Local l = new JimpleLocal(varname, RefType.v("java.lang.Class"));
        writeLocal(inMethod, l);
        writeAssignClassConstant(inMethod, stmt, l, constant, session);
        return l;
    }

    Local writeMethodHandleConstantExpression(SootMethod inMethod, Stmt stmt, MethodHandle constant, Session session) {
        // introduce a new temporary variable
        String basename = "$mhandleconstant";
        String varname = basename + session.nextNumber(basename);
        Local l = new JimpleLocal(varname, RefType.v("java.lang.invoke.MethodHandle"));
        writeLocal(inMethod, l);
        writeAssignMethodHandleConstant(inMethod, stmt, l, constant, session);
        return l;
    }

    private Value writeActualParam(SootMethod inMethod, Stmt stmt, InvokeExpr expr, Session session, Value v, int idx) {
        if (v instanceof StringConstant)
            return writeStringConstantExpression(inMethod, stmt, (StringConstant) v, session);
        else if (v instanceof ClassConstant)
            return writeClassConstantExpression(inMethod, stmt, (ClassConstant) v, session);
        else if (v instanceof NumericConstant)
            return writeNumConstantExpression(inMethod, stmt, (NumericConstant) v, session);
        else if (v instanceof MethodHandle)
            return writeMethodHandleConstantExpression(inMethod, stmt, (MethodHandle) v, session);
        else if (v instanceof NullConstant) {
            // Giving the type of the formal argument to be used in the creation of
            // temporary var for the actual argument (whose value is null).
            Type argType = expr.getMethodRef().parameterType(idx);
            return writeNullExpression(inMethod, stmt, argType, session);
        } else if (v instanceof Constant)
            throw new RuntimeException("Value has unknown constant type: " + v);
        else if (!(v instanceof JimpleLocal))
            System.err.println("Warning: value has unknown non-constant type: " + v.getClass().getName());
        return v;
    }

    private void writeActualParams(SootMethod inMethod, Stmt stmt, InvokeExpr expr, String invokeExprRepr, Session session) {
        for(int i = 0; i < expr.getArgCount(); i++) {
            Value v = writeActualParam(inMethod, stmt, expr, session, expr.getArg(i), i);
            String clsName = inMethod.getDeclaringClass().getName();
            PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));
            if (v instanceof Local)
                writeActualParam(i, invokeExprRepr, Representation.local(inMethod, (Local)v), pc);
            else
                throw new RuntimeException("Actual parameter is not a local: " + v + " " + v.getClass());
        }
        if (expr instanceof DynamicInvokeExpr) {
            String clsName = inMethod.getDeclaringClass().getName();
            PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));

            DynamicInvokeExpr di = (DynamicInvokeExpr)expr;
            for (int j = 0; j < di.getBootstrapArgCount(); j++) {
                Value v = di.getBootstrapArg(j);
                if (v instanceof Constant) {
                    Value vConst = writeActualParam(inMethod, stmt, expr, session, v, j);
                    if (vConst instanceof Local) {
                        Local l = (Local) vConst;
                        _db.add(BOOTSTRAP_PARAMETER, pc, str(j), invokeExprRepr, Representation.local(inMethod, l));
                    } else
                        throw new RuntimeException("Unknown actual parameter: " + v + " of type " + v.getClass().getName());
                } else
                    throw new RuntimeException("Found non-constant argument to bootstrap method: " + di);
            }
        }
    }

    void writeInvoke(SootMethod inMethod, Stmt stmt, Session session) {
        InvokeExpr expr = stmt.getInvokeExpr();
        writeInvokeHelper(inMethod, stmt, expr, session);
    }

    private String writeInvokeHelper(SootMethod inMethod, Stmt stmt, InvokeExpr expr, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.invoke(inMethod, expr, session);
        String methodId = writeMethod(inMethod);

        writeActualParams(inMethod, stmt, expr, insn, session);

        String clsName = inMethod.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, inMethod, stmt));

        LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
        if (tag != null) {
            _db.add(METHOD_INV_LINE, pc, insn, str(tag.getLineNumber()));
        }

        if (expr instanceof StaticInvokeExpr) {
            _db.add(STATIC_METHOD_INV, pc, insn, str(index), _rep.signature(expr.getMethod()), methodId);
        }
        else if (expr instanceof VirtualInvokeExpr || expr instanceof InterfaceInvokeExpr) {
            _db.add(VIRTUAL_METHOD_INV, pc, insn, str(index), _rep.signature(expr.getMethod()), Representation.local(inMethod, (Local) ((InstanceInvokeExpr) expr).getBase()), methodId);
        }
        else if (expr instanceof SpecialInvokeExpr) {
            _db.add(SPECIAL_METHOD_INV, pc, insn, str(index), _rep.signature(expr.getMethod()), Representation.local(inMethod, (Local) ((InstanceInvokeExpr) expr).getBase()), methodId);
        }
        else if (expr instanceof DynamicInvokeExpr) {
            writeDynamicInvoke((DynamicInvokeExpr)expr, index, insn, methodId);
        }
        else {
            throw new RuntimeException("Cannot handle invoke expr: " + expr);
        }

        return insn;
    }

    private String getBootstrapSig(DynamicInvokeExpr di) {
        SootMethodRef bootstrapMeth = di.getBootstrapMethodRef();
        String clsName = bootstrapMeth.declaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, bootstrapMeth.declaringClass()));

        if (bootstrapMeth.declaringClass().isPhantom()) {
            String bootstrapSig = bootstrapMeth.toString();
            if (_reportPhantoms)
                System.out.println("Bootstrap method is phantom: " + bootstrapSig);
            _db.add(PHANTOM_METHOD, pc, bootstrapSig);
            return bootstrapSig;
        } else
            return _rep.signature(bootstrapMeth.resolve());
    }

    private void writeDynamicInvoke(DynamicInvokeExpr di, int index, String insn, String methodId) {
        SootMethodRef dynInfo = di.getMethodRef();
        String clsName = dynInfo.declaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, di.getMethod()));
        int dynArity = dynInfo.parameterTypes().size();
        for (int pIdx = 0; pIdx < dynArity; pIdx++)
            writeInvokedynamicParameterType(insn, pIdx, dynInfo.parameterType(pIdx).toString(), pc);

        StringBuffer dpTypes = new StringBuffer("(");
        dynInfo.parameterTypes().forEach(p -> dpTypes.append(p.toString()));
        String dynParamTypes = dpTypes.append(")").toString();

        writeInvokedynamic(insn, index, getBootstrapSig(di), dynInfo.name(), dynInfo.returnType().toString(), dynArity, dynParamTypes, di.getHandleTag(), methodId, pc);
    }

    private Value writeImmediate(SootMethod inMethod, Stmt stmt, Value v, Session session) {
        if (v instanceof StringConstant)
            v = writeStringConstantExpression(inMethod, stmt, (StringConstant) v, session);
        else if (v instanceof ClassConstant)
            v = writeClassConstantExpression(inMethod, stmt, (ClassConstant) v, session);
        else if (v instanceof NumericConstant)
            v = writeNumConstantExpression(inMethod, stmt, (NumericConstant) v, session);

        return v;
    }

    void writeAssignBinop(SootMethod m, AssignStmt stmt, Local left, BinopExpr right, Session session) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        writeAssignBinop(insn, index, Representation.local(m, left), methodId, pc);

        if (right instanceof AddExpr)
                writeOperatorAt(insn, "+", pc);
        else if (right instanceof SubExpr)
                writeOperatorAt(insn, "-", pc);
        else if (right instanceof MulExpr)
                writeOperatorAt(insn, "*", pc);
        else if (right instanceof DivExpr)
                writeOperatorAt(insn, "/", pc);
        else if (right instanceof RemExpr)
                writeOperatorAt(insn, "%", pc);
        else if (right instanceof AndExpr)
                writeOperatorAt(insn, "&", pc);
        else if (right instanceof OrExpr)
                writeOperatorAt(insn, "|", pc);
        else if (right instanceof XorExpr)
                writeOperatorAt(insn, "^", pc);
        else if (right instanceof ShlExpr)
                writeOperatorAt(insn, "<<", pc);
        else if (right instanceof ShrExpr)
                writeOperatorAt(insn, ">>", pc);
        else if (right instanceof UshrExpr)
                writeOperatorAt(insn, ">>>", pc);


        if (right.getOp1() instanceof Local) {
            Local op1 = (Local) right.getOp1();
            writeAssignOperFrom(insn, L_OP, Representation.local(m, op1), pc);
        }

        if (right.getOp2() instanceof Local) {
            Local op2 = (Local) right.getOp2();
            writeAssignOperFrom(insn, R_OP, Representation.local(m, op2), pc);
        }
    }

    void writeAssignUnop(SootMethod m, AssignStmt stmt, Local left, UnopExpr right, Session session) {
        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        writeAssignUnop(insn, index, Representation.local(m, left), methodId, pc);
        writeOperatorAt(insn, "-", pc);

        if (right.getOp() instanceof Local) {
            Local op = (Local) right.getOp();
            writeAssignOperFrom(insn, L_OP, Representation.local(m, op), pc);
        }
    }

    void writeAssignInstanceOf(SootMethod m, AssignStmt stmt, Local to, Local from, Type t, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_INSTANCE_OF, pc, insn, str(index), Representation.local(m, from), Representation.local(m, to), writeType(t, pc), methodId);
    }

    void writeAssignPhantomInvoke(SootMethod m, AssignStmt stmt, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(ASSIGN_PHANTOM_INVOKE, pc, insn, str(index), methodId);
    }

    void writeBreakpointStmt(SootMethod m, Stmt stmt, Session session) {
        int index = session.calcUnitNumber(stmt);
        String insn = Representation.instruction(m, stmt, index);
        String methodId = writeMethod(m);

        String clsName = m.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, m, stmt));

        _db.add(BREAKPOINT_STMT, pc, insn, str(index), methodId);
    }

    void writeFieldInitialValue(SootField f) {
        String fieldId = Representation.signature(f);
        List<Tag> tagList = f.getTags();

        String clsName = f.getDeclaringClass().getName();
        PresenceCondition pc = new PresenceCondition(getFactPC(clsName, f));

        for (Tag tag : tagList)
            if (tag instanceof ConstantValueTag) {
                String val = ((ConstantValueTag)tag).getConstant().toString();
                _db.add(FIELD_INITIAL_VALUE, pc, fieldId, val);
                // Put constant in appropriate "raw" input facts.
                if ((tag instanceof IntegerConstantValueTag) ||
                    (tag instanceof DoubleConstantValueTag) ||
                    (tag instanceof LongConstantValueTag) ||
                    (tag instanceof FloatConstantValueTag)) {
                    // Trim last non-digit qualifier (e.g. 'L' in long constants).
                    int len = val.length();
                    if (!Character.isDigit(val.charAt(len-1)))
                        val = val.substring(0, len-1);
                    _db.add(NUM_CONSTANT_RAW, pc, val);
                } else if (tag instanceof StringConstantValueTag) {
                    writeStringConstant(val, pc);
                } else
                    System.err.println("Unsupported field tag " + tag.getClass());
            }
    }

    public void writePreliminaryFacts(Collection<SootClass> classes, BasicJavaSupport java, SootParameters sootParameters) {
        classes.stream().filter(SootClass::isApplicationClass).forEachOrdered(this::writeApplicationClass);
        writePreliminaryFacts(java, sootParameters);
    }

}
