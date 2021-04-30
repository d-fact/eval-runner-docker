// grok scripts for RTS
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

allchange = Update + Insert + Delete;
testClazz = dom TestClass;

alldeps = call + contain + reference;
print outputFile + ": need all dependencies"
affected = call . (alldeps*) . (dom allchange);
affectedTestclass = (contain . affected) ^ testClazz;
// affectedTestclass = testClazz ^ affected;
affectedAbstract = affectedTestclass ^ (dom AbstractClass)
result = affectedTestclass . (inv Inherit) + affectedTestclass - affectedAbstract
print result >> affected_output
