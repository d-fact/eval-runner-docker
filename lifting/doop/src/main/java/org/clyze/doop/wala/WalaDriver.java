package org.clyze.doop.wala;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import org.clyze.doop.common.Driver;
import org.clyze.doop.extractor.Annotations;

class WalaDriver extends Driver<IClass, WalaThreadFactory> {
    private final IAnalysisCacheView _cache;

    WalaDriver(WalaThreadFactory factory, int totalClasses,
               Integer cores, IAnalysisCacheView cache,
               boolean ignoreFactGenErrors,
               Annotations annots) {
        super(factory, totalClasses, cores, ignoreFactGenErrors, annots);
        _cache = cache;
    }

    @Override
    protected Runnable getFactGenRunnable() {
        return _factory.newFactGenRunnable(_tmpClassGroup, _cache);
    }

    @Override
    protected Runnable getIRGenRunnable() {
        throw new RuntimeException("Parallel IR generation is not supported.");
    }
}
