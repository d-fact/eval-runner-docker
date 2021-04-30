// grok scripts for RTS (static, not-so-precise version)
//#+INSERTION

outputFile = $1;
affected_output = outputFile + ".affected";

allchange = Update + Insert + Delete;
testClazz = dom TestClass;

alldeps = call + contain + reference;
print outputFile + ": need all dependencies"
affected = (alldeps*) . (dom allchange);
affectedTestclass = testClazz ^ affected;
affectedAbstract = affectedTestclass ^ (dom AbstractClass)
result = affectedTestclass . (inv Inherit) + affectedTestclass - affectedAbstract
print result >> affected_output
