// grok scripts for slicing on facts with un-versionized hash
// deprecated
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

alldeps = call + contain + reference;
// callref = call + reference;
allchange = Update + Insert + Delete;
// affected = (alldeps*) . (dom allchange);

testClazz = dom TestClass;
// testClazzRefRel contains reference relations between test classes
testClazzRefRel = testClazz o (reference*) o testClazz;
cleanDep = (alldeps*) - testClazzRefRel;

affected = cleanDep . (dom allchange);
affectedTestclass = testClazz ^ affected;
print affectedTestclass >> affected_output
