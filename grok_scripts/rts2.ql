// grok scripts for slicing on facts with un-versionized hash
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

cc = call + contain;
allchange = Update + Insert + Delete;
testclazz = dom testclass;

affected = (cc*) . (dom allchange);
affectedTestclass = testClazz ^ affected;
print affectedTestclass >> affected_output
