package org.clyze.doop.dex;

import org.clyze.doop.common.Driver;
import org.clyze.doop.extractor.Annotations;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

class DexDriver extends Driver<DexBackedClassDef, DexThreadFactory> {

    DexDriver(DexThreadFactory factory, int totalClasses, Integer cores, boolean ignoreFactGenErrors,
              Annotations annotations) {
        super(factory, totalClasses, cores, ignoreFactGenErrors, annotations);
    }

    @Override
    protected Runnable getFactGenRunnable() {
        return _factory.newFactGenRunnable(_tmpClassGroup);
    }

    @Override
    protected Runnable getIRGenRunnable() {
        throw new RuntimeException("Parallel IR generation is not supported.");
    }

}
