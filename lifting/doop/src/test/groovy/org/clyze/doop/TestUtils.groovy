package org.clyze.doop

import org.clyze.analysis.Analysis
import org.clyze.doop.utils.SouffleScript

/**
 * Utility class with checker methods used by other tests.
 */
class TestUtils {
	static void relationHasApproxSize(Analysis analysis, String relation, int expectedSize) {
		int actualSize = 0

		forEachLineIn("${analysis.database}/${relation}.csv", { actualSize++ })

		// We expect numbers to deviate by 10%.
		assert actualSize > (expectedSize * 0.9)
		assert actualSize < (expectedSize * 1.1)
	}

	static void relationHasExactSize(Analysis analysis, String relation, int expectedSize) {
		int actualSize = 0
		forEachLineIn("${analysis.database}/${relation}.csv", { actualSize++ })
		assert actualSize == expectedSize
	}

	/**
	 * Replacement of Groovy's eachLine(), to work with large files.
	 */
	static void forEachLineIn(String path, Closure cl) {
		File file = new File(path)
		BufferedReader br = new BufferedReader(new FileReader(file))
		br.withCloseable {
			String line
			while ((line = it.readLine()) != null) { cl(line) }
		}
	}

	static void metricIsApprox(Analysis analysis, String metric, long expectedVal) {
		long actualVal = -1

		String metrics = "${analysis.database}/Stats_Metrics.csv"
		(new File(metrics)).eachLine { line ->
			String[] values = line.split('\t')
			if ((values.size() == 3) && (values[1] == metric)) {
				actualVal = values[2] as long
			}
		}
		// We expect numbers to deviate by 10%.
		assert actualVal > (expectedVal * 0.9)
		assert actualVal < (expectedVal * 1.1)
	}

	// Check that the binary generated by Souffle exists.
	static void execExists(Analysis analysis) {
		assert true == (new File("${analysis.outDir}/${SouffleScript.EXE_NAME}")).exists()
	}

	// Trivial check.
	static void metaExists(Analysis analysis) {
		assert true == (new File("${analysis.outDir}/meta")).exists()
	}

	/*
	 * Check that a local variable points to a value.
	 *
	 * @param analysis	  the analysis object
	 * @param local		  the name of the local
	 * @param value		  the value
	 * @param qualified	  if true, qualify relation name
	 */
	static void varPointsTo(Analysis analysis, String local, String value, boolean qualified) {
		String rel = qualified ? "mainAnalysis-VarPointsTo" : "VarPointsTo"
		findPair(analysis, rel, local, 3, value, 1)
	}
	// Simpler overloaded version.
	static void varPointsTo(Analysis analysis, String local, String value) {
		varPointsTo(analysis, local, value, false)
	}
	// Simpler overloaded version.
	static void varPointsToQ(Analysis analysis, String local, String value) {
		varPointsTo(analysis, local, value, true)
	}

	// Check that a static field points to a value.
	static void staticFieldPointsTo(Analysis analysis, String fld, String value) {
		findPair(analysis, "mainAnalysis-StaticFieldPointsTo", fld, 2, value, 1)
	}

	static void varValue(Analysis analysis, String local, String value) {
		findPair(analysis, "Server_Var_Values", local, 1, value, 2)
	}

	static void invoValue(Analysis analysis, String local, String value) {
		findPair(analysis, "Server_Invocation_Values", local, 1, value, 2)
	}

	static void invokedynamicCGE(Analysis analysis, String instr, String meth) {
		findPair(analysis, "mainAnalysis-InvokedynamicCallGraphEdge", instr, 1, meth, 3)
	}

	static void proxyCGE(Analysis analysis, String instr, String meth) {
		findPair(analysis, "mainAnalysis-ProxyCallGraphEdge", instr, 1, meth, 3)
	}

	// Check that an instance field points to a value.
	static void instanceFieldPointsTo(Analysis analysis, String fld, String value) {
		findPair(analysis, "mainAnalysis-InstanceFieldPointsTo", fld, 2, value, 1)
	}

	// Check that a method is reachable.
	static void methodIsReachable(Analysis analysis, String meth) {
		assert true == find(analysis, "Reachable", meth, true)
	}

	static void findPair(Analysis analysis, String relation,
						 String s1, int idx1, String s2, int idx2) {
		boolean found = false
		forEachLineIn("${analysis.database}/${relation}.csv",
					  { line ->
						  if (line) {
							  String[] values = line.split('\t')
							  String a = values[idx1]
							  String b = values[idx2]
							  if ((a == s1) && (b == s2)) {
								  found = true
							  }
						  }
					  })
		assert found == true
	}

	/**
	 * Finds a line in a relation. Useful for single-column relations.
	 *
	 * @param analysis	 the analysis object
	 * @param relation	 the relation name
	 * @param val		 the string to match against each line
	 * @param db		 true for database relations, false for facts
	 * @return			 true if the value was found, false otherwise
	 */
	static boolean find(Analysis analysis, String relation,
						String val, boolean db) {
		boolean found = false
		String rel = db ? "${analysis.database}/${relation}.csv" : "${analysis.factsDir}/${relation}.facts"
		forEachLineIn(rel, { if (it && (it == val)) { found = true }})
		return found
	}

	static void noSanityErrors(Analysis analysis) {
		relationHasExactSize(analysis, "VarHasNoType", 0)
		relationHasExactSize(analysis, "TypeIsNotConcreteType", 0)
		relationHasExactSize(analysis, "InstructionIsNotConcreteInstruction", 0)
		relationHasExactSize(analysis, "ValueHasNoType", 0)
		relationHasExactSize(analysis, "ValueHasNoDeclaringType", 0)
		relationHasExactSize(analysis, "NotReachableVarPointsTo", 0)
		relationHasExactSize(analysis, "VarPointsToWronglyTypedValue", 0)
		relationHasExactSize(analysis, "VarPointsToMergedHeap", 0)
		relationHasExactSize(analysis, "HeapAllocationHasNoType", 0)
		relationHasExactSize(analysis, "ValueIsNeitherHeapNorNonHeap", 0)
		relationHasExactSize(analysis, "ClassTypeIsInterfaceType", 0)
		relationHasExactSize(analysis, "PrimitiveTypeIsReferenceType", 0)
	}
}
