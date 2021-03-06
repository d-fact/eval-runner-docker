package org.clyze.doop.soot;

import java.util.HashSet;
import java.util.Set;
import org.clyze.doop.common.Driver;
import org.clyze.doop.extractor.Annotations;
import soot.SootClass;
import soot.SootMethod;

class SootDriver extends Driver<SootClass, ThreadFactory> {

    SootDriver(ThreadFactory factory, int totalClasses, Integer cores, boolean ignoreFactGenErrors,
               Annotations annotations) {
        super(factory, totalClasses, cores, ignoreFactGenErrors, annotations);
    }

    void generateMethod(SootMethod dummyMain, FactWriter writer, boolean ssa, boolean reportPhantoms) {
        Set<SootClass> sootClasses = new HashSet<>();
        sootClasses.add(dummyMain.getDeclaringClass());
        FactGenerator factGenerator = new FactGenerator(writer, ssa, sootClasses, reportPhantoms, this, _annotations);
        factGenerator.generate(dummyMain, new Session());
        writer.writeAndroidEntryPoint(dummyMain);
        factGenerator.run();
    }

    @Override
    protected Runnable getFactGenRunnable() {
        return _factory.newFactGenRunnable(_tmpClassGroup);
    }

    @Override
    protected Runnable getIRGenRunnable() {
        return _factory.newJimpleGenRunnable(_tmpClassGroup);
    }
}
