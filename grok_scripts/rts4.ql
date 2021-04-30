// grok scripts for RTS: consider call+contain to changed entities
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

cc = call + contain;
allchange = Update + Insert + Delete;
testClazz = dom TestClass;

affected = (cc*) . (dom allchange);
affectedTestclass = testClazz ^ affected;
print affectedTestclass >> affected_output

if (affectedTestclass == set) {
    print outputFile + ": need all dependencies"
    alldeps = call + contain + reference;
    affected = (alldeps*) . (dom allchange);
    affectedTestclass = testClazz ^ affected;
    print affectedTestclass >> affected_output
}
