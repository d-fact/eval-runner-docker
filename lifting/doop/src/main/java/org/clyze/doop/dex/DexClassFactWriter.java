package org.clyze.doop.dex;

import org.clyze.doop.common.Database;
import org.clyze.doop.common.JavaFactWriter;
import org.clyze.doop.util.TypeUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.*;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.clyze.doop.common.PredicateFile.*;

/**
 * Writes facts for a single class found in a .dex entry.
 */
class DexClassFactWriter extends JavaFactWriter {

    public final Collection<FieldOp> fieldOps = new LinkedList<>();
    public final Collection<String> definedMethods = new LinkedList<>();
    public final Collection<FieldInfo> definedFields = new LinkedList<>();
    public String superClass;

    DexClassFactWriter(Database db) {
        super(db);
    }

    public void generateFacts(DexBackedClassDef dexClass, String className,
                              DexParameters dexParams, Map<String, MethodSig> cachedMethodDescriptors) {
        if (dexParams.isApplicationClass(className))
            _db.add(APP_CLASS, null, className);

        for (DexBackedMethod dexMethod : dexClass.getMethods()) {
            DexMethodFactWriter mWriter = new DexMethodFactWriter(dexMethod, _db, cachedMethodDescriptors);
            mWriter.writeMethod(fieldOps, definedMethods);
        }

        for (DexBackedField dexField : dexClass.getFields())
            writeField(dexField);

        writeClassOrInterfaceType(dexClass, className);

        for (DexBackedAnnotation annotation : dexClass.getAnnotations())
            _db.add(CLASS_ANNOTATION, null, className, TypeUtils.raiseTypeId(annotation.getType()));
    }

    private void writeClassOrInterfaceType(ClassDef dexClass, String className) {
        boolean isInterface = false;
        for (AccessFlags flag : AccessFlags.getAccessFlagsForClass(dexClass.getAccessFlags()))
            if (flag == AccessFlags.INTERFACE)
                isInterface = true;
            else
                writeClassModifier(className, flag.toString(), null);
        if (isInterface)
            _db.add(INTERFACE_TYPE, null, className);
        else
            _db.add(CLASS_TYPE, null, className);

        this.superClass = TypeUtils.raiseTypeId(dexClass.getSuperclass());
        _db.add(DIRECT_SUPER_CLASS, null, className, superClass);

        for (String intf : dexClass.getInterfaces())
            _db.add(DIRECT_SUPER_IFACE, null, className, TypeUtils.raiseTypeId(intf));
    }

    private void writeField(Field fieldRef) {
        FieldInfo fi = new FieldInfo(fieldRef);
        String fieldId = fi.getFieldId();
        _db.add(FIELD_SIGNATURE, null, fieldId, fi.definingClass, fi.name, fi.type);
        EncodedValue e = fieldRef.getInitialValue();
        if (e != null) {
            InitialValue initialValue = new InitialValue(e);
            String val = initialValue.value;
            if (val != null) {
                _db.add(FIELD_INITIAL_VALUE, null, fieldId, val);
                if (initialValue.type == InitialValue.IVType.NUMBER)
                    _db.add(NUM_CONSTANT_RAW, null, val);
                else if (initialValue.type == InitialValue.IVType.STRING)
                    writeStringConstant(val, null);
            }
        }

        AccessFlags[] flags = AccessFlags.getAccessFlagsForField(fieldRef.getAccessFlags());
        for (AccessFlags f : flags)
            _db.add(FIELD_MODIFIER, null, f.toString(), fieldId);

        for (Annotation annotation : fieldRef.getAnnotations())
            _db.add(FIELD_ANNOTATION, null, fieldId, TypeUtils.raiseTypeId(annotation.getType()));

        definedFields.add(fi);
    }
}
