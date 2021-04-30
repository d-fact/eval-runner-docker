// grok scripts for RTS: consider call chain only,
// but fallback to all when finding nothing
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

allchange = Update + Insert + Delete;
testClazz = dom TestClass;

affected = (call*) . (dom allchange);
affectedTestclass = (contain . affected) ^ testClazz;
affectedAbstract = affectedTestclass ^ (dom AbstractClass)
result = affectedTestclass . (inv Inherit) + affectedTestclass - affectedAbstract
print result >> affected_output

if (affectedTestclass == set) {
    alldeps = call + contain + reference;
    print outputFile + ": try all dependencies"
    affected = (alldeps*) . (dom allchange);
    affectedTestclass = testClazz ^ affected;
    print affectedTestclass >> affected_output
}
