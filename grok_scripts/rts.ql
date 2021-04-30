// grok scripts for slicing on facts with un-versionized hash
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

alldeps = call + contain + reference;
allchange = Update + Insert + Delete;
affected = (alldeps*) . (dom allchange);
affectedTestclass = (dom TestClass) ^ affected;
print affectedTestclass >> affected_output
