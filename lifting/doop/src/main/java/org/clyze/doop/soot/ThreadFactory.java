package org.clyze.doop.soot;

import java.util.Set;
import org.clyze.doop.common.Driver;
import org.clyze.doop.extractor.Annotations;
import soot.SootClass;

class ThreadFactory {
    private final FactWriter _factWriter;
    private final boolean _ssa;
    private final boolean _reportPhantoms;
    private Driver driver;
    private Annotations _annotations;

    ThreadFactory(FactWriter factWriter, boolean ssa, boolean reportPhantoms, Annotations annotations) {
        this._factWriter = factWriter;
        this._ssa = ssa;
        this._reportPhantoms = reportPhantoms;
        this._annotations = annotations;
    }

    void setDriver(Driver driver) {
        this.driver = driver;
    }

    Runnable newFactGenRunnable(Set<SootClass> sootClasses) {
        return new FactGenerator(_factWriter, _ssa, sootClasses, _reportPhantoms, driver, _annotations);
    }

    Runnable newJimpleGenRunnable(Set<SootClass> sootClasses) {
        return new JimpleGenerator(sootClasses);
    }

}
