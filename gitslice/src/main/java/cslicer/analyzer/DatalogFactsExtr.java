package cslicer.analyzer;

/*
 * #%L
 * CSlicer
 *    ______ _____  __ _
 *   / ____// ___/ / /(_)_____ ___   _____
 *  / /     \__ \ / // // ___// _ \ / ___/
 * / /___  ___/ // // // /__ /  __// /
 * \____/ /____//_//_/ \___/ \___//_/
 * %%
 * Copyright (C) 2014 - 2021 Department of Computer Science, University of Toronto
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ch.uzh.ifi.seal.changedistiller.model.entities.*;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.*;
import cslicer.coverage.*;
import cslicer.distiller.ChangeDistillerException;
import cslicer.distiller.ChangeExtractor;
import cslicer.distiller.GitRefSourceCodeChange;
import cslicer.jgit.*;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;
import cslicer.utils.graph.Edge;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DatalogFactsExtr extends FactsExtr {

	private final Path depFactsDir = Paths.get(fOutputPath, "20-deps");
	private final Path diffFactsDir = Paths.get(fOutputPath, "30-diff");
	private final Path covFactsDir = Paths.get(fOutputPath, "60-cov");
	// private String diffAttrFile;
	// private String plainStringDiffTupleFile;
	// private String plainStringDiffAttrFile;
	// private String covPlainStringFile;
	// private String depsMD5ToPlainStringMapFile;
	// private String depsPlainStringFile;
	// private static boolean genPlainFiles = true;
	private static String versionize;

	public DatalogFactsExtr(ProjectConfiguration config) throws RepositoryInvalidException, CommitNotFoundException,
			BuildScriptInvalidException, CoverageControlIOException, AmbiguousEndPointException,
			ProjectConfigInvalidException, BranchNotFoundException, CoverageDataMissingException, IOException,
			CheckoutFileFailedException {
		this(config, "", true, true);
	}

	public DatalogFactsExtr(ProjectConfiguration config, String version, boolean auxFiles, boolean fuzzy)
			throws RepositoryInvalidException, CommitNotFoundException, BuildScriptInvalidException,
			CoverageControlIOException, AmbiguousEndPointException, ProjectConfigInvalidException,
			BranchNotFoundException, CoverageDataMissingException, IOException, CheckoutFileFailedException {
		super(config, fuzzy);
		fClassRootPath = config.getClassRootPath();
		if (version == null) {
			versionize = "";
		} else if (version.equals("")) {
			versionize = endCommit;
		} else {
			versionize = version;
		}
		// fJGit.checkOutVersion(fEnd);
		/*
		 * plainStringDiffTupleFile = Paths.get(fOutputPath,
		 * "plain-diff_tuple.ta").toString(); plainStringDiffAttrFile =
		 * Paths.get(fOutputPath, "plain-diff_attr.ta").toString();
		 * depsMD5ToPlainStringMapFile = Paths.get(fOutputPath,
		 * "plain-md5-map.txt").toString(); depsPlainStringFile = Paths.get(fOutputPath,
		 * "deps-plain.txt").toString(); covPlainStringFile = Paths.get(fOutputPath,
		 * "plain-cov.txt").toString(); genPlainFiles = auxFiles;
		 */
	}

	public String generateHunkDependencyFacts() {
		PrintUtils.print("COMPUTING HUNK FACTS...");
		DependencyCache hunkGraph = new DependencyCache();
		computeHunkDepSet(fHistory, fHistory, hunkGraph, FactFormat.SOUFFLE);
		return hunkGraph.toString();
	}

	private static void writeFactsFromCGEdges(StaticCallGraph cg, boolean fuzzy, HashMap<String, BufferedWriter> m,
											  HashMap<CGNodeType, HashSet<CGNode>> nodes) {
		for (Edge<CGNode> e : cg.getEdges()) {
			String frVertexName = e.getFrom().getName();
			String toVertexName = e.getTo().getName();

			DependencyType depType = findFactOperatorOfDepsEdge(e);
			String frName, toName;
			if (fuzzy) {
				frName = matchWithGenericType(frVertexName);
				toName = matchWithGenericType(toVertexName);
			} else {
				frName = frVertexName;
				toName = toVertexName;
			}
			StringBuilder fact = new StringBuilder().append(String.format("%s\t%s", frName, toName));
			if (!versionize.equals("")) {
				fact.append("\t").append(versionize);
			}
			BufferedWriter w = m.get(depType.getName());
			try {
				w.write(fact.append("\n").toString());
			} catch (IOException exp) {
				PrintUtils.print(String.format("Exception when writing %s fact:\n%s" , depType.getName(), fact),
						PrintUtils.TAG.WARNING);
				exp.printStackTrace();
			}
			// collect CGNodes
			CGNode nFrom = e.getFrom().getData();
			CGNode nTo = e.getTo().getData();
			nodes.get(nFrom.getNodeType()).add(nFrom);
			nodes.get(nTo.getNodeType()).add(nTo);
		}

	}

	private static void generateClassesFacts(BcelStaticCallGraphBuilder cgBuilder, Path outputFile) {
		ArrayList<String> classNames = cgBuilder.getClassNames();
		try {
			BufferedWriter factsFileWriter = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE);
			for (String e : classNames) {
				String fact = String.format("%s\n", e);
				factsFileWriter.write(fact);
			}
			factsFileWriter.flush();
			factsFileWriter.close();
		} catch (IOException e) {
			PrintUtils.print(String.format("Exception when writing facts file [%s]", outputFile), PrintUtils.TAG.WARNING);
			e.printStackTrace();
		}
	}

	public void generateDependencyFacts() throws ClassPathInvalidException {
		PrintUtils.print("COMPUTING DEPS FACTS..."); // dependency facts
		try {
			Files.createDirectories(depFactsDir);
		} catch (IOException e) {
			PrintUtils.print(String.format("Cannot create directory for dep-facts @ %s", depFactsDir),
					PrintUtils.TAG.WARNING);
			return;
		}
		BcelStaticCallGraphBuilder cgBuilder = new BcelStaticCallGraphBuilder(fClassRootPath);

		BcelStaticCallGraphBuilder testCG  = buildCallGraphForTestClasses(fClassRootPath);
		// TODO: may generate inherit facts for all classes in the future
		generateInheritFacts(testCG, depFactsDir);
		generateClassesFacts(testCG, depFactsDir.resolve("TestClass.facts"));

		computeDependencyFacts(cgBuilder, testCG, depFactsDir, versionize, fuzzyNames);
	}

	@Deprecated
	public static String computeDependencyFacts(BcelStaticCallGraphBuilder cgBuilder, Path depFactsDir) throws ClassPathInvalidException {
		return computeDependencyFacts(cgBuilder, null, depFactsDir, "", false);
	}

	private static String computeDependencyFacts(BcelStaticCallGraphBuilder cgBuilder,
												 BcelStaticCallGraphBuilder testCGBuilder,
												 Path depFactsDir, String versionize, boolean fuzzy) {
		HashMap<String, BufferedWriter> factsWritersMap = new HashMap<>();
		HashMap<CGNodeType, HashSet<CGNode>> nodes = new HashMap<>();
		try {
			for (DependencyType t : DependencyType.values()) {
				factsWritersMap.put(t.getName(), new BufferedWriter(new FileWriter(depFactsDir.resolve(t.getName() + ".facts").toString())));
			}

			for (CGNodeType t : CGNodeType.values()) {
				if (t == CGNodeType.CGNode) {
					continue;
				}
				String relName = "Is" + t.name();
				factsWritersMap.put(relName, new BufferedWriter(new FileWriter(depFactsDir.resolve(relName + ".facts").toString())));
				nodes.put(t, new HashSet<CGNode>());
			}
		} catch (IOException e) {
			PrintUtils.print(String.format("Exception when initializing BufferedWriter in directory %s", depFactsDir),
					PrintUtils.TAG.WARNING);
			e.printStackTrace();
		}

		// get facts about src classes
		cgBuilder.buildCallGraph();
		StaticCallGraph depsCG = cgBuilder.getCallGraph();
		// depsCG.outputDOTFile("/tmp/deps-graph.txt");
        writeFactsFromCGEdges(depsCG, fuzzy, factsWritersMap, nodes);
		HashMap<String, String> nestedClassMap = cgBuilder.getNestedClass();
		HashSet<String> abstractClasses = new HashSet<>(cgBuilder.getAbstractClasses());
		HashSet<String> testClasses = new HashSet<>();
		HashMap<String, String> superClassMap = new HashMap<>();

		if (testCGBuilder != null) {
			testCGBuilder.buildCallGraph();
			nestedClassMap.putAll(testCGBuilder.getNestedClass());
			abstractClasses.addAll(testCGBuilder.getAbstractClasses());
			testClasses.addAll(testCGBuilder.getClassNames());
			superClassMap.putAll(testCGBuilder.getInheritPairs());
			StaticCallGraph depsCGTest = testCGBuilder.getCallGraph();
			writeFactsFromCGEdges(depsCGTest, fuzzy, factsWritersMap, nodes);
		}

		try{
			for (Map.Entry<CGNodeType, HashSet<CGNode>> entry : nodes.entrySet()) {
				CGNodeType nodeType = entry.getKey();
				for (CGNode e: entry.getValue()) {
					BufferedWriter w = factsWritersMap.get("Is" + nodeType.name());
					if (nodeType == CGNodeType.Method) {
						MethodNode method = (MethodNode) e;
						String fqName = method.getName();
						String fqClassName = method.getFqClassName();
						String simpleName = rmParams(method.getMethodName());
						int numOfParams = method.getArgs().size();
						String retType = method.getRetType();
						boolean isCtor = isCtor(fqName);
						// w.write(String.format("%s\t%s\t%s\t%s\t%s", qualifiedName, simpleName, numOfParams, retType, isCtor));
						w.write(String.format("%s\t%s\t%s\t%s\t%s\t%s", fqName, simpleName, fqClassName, numOfParams, retType, isCtor));
					} else if (nodeType == CGNodeType.Class) {
						ClassNode clazz = (ClassNode) e;
						String fqName = clazz.getName();
						boolean isNested = nestedClassMap.containsKey(fqName);
						String outer = isNested ? nestedClassMap.get(fqName) : fqName;
						boolean isAbstract = abstractClasses.contains(fqName);
						boolean isTestClass = testClasses.contains(fqName);
						String superClass = superClassMap.getOrDefault(fqName, "no-super-class");
						w.write(String.format("%s\t%s\t%s\t%s\t%s\t%s", fqName, isAbstract, superClass, isNested, outer, isTestClass));
					} else {
						w.write(e.getName());
					}
					if (!versionize.equals("")) {
						w.write("\t" + versionize);
					}
					w.write("\n");
				}
			}

			for (BufferedWriter w: factsWritersMap.values()) {
				w.flush();
				w.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
		// return factsFileContent.toString();
	}

	private static String rmParams(String methodName) {
		try {
			return methodName.substring(0, methodName.indexOf('('));
		} catch (IndexOutOfBoundsException e) {
			PrintUtils.print("Possible mistake: no bracket in short method name: " + methodName, PrintUtils.TAG.WARNING);
			return methodName;
		}
	}

	private static boolean isCtor(String qualifiedName) {
		String[] segs = qualifiedName.split("\\(")[0].split("\\.");
		return (segs[segs.length - 1].equals(segs[segs.length - 2]));
	}

	private static void generateInheritFacts(BcelStaticCallGraphBuilder cgBuilder, Path outputDir) {
		// cgBuilder.getMoreClassInfo(classParents, abstractClazz);
		HashMap<String, String> classParents = cgBuilder.getInheritPairs();
		ArrayList<String> abstractClazz = cgBuilder.getAbstractClasses();
		try {
			BufferedWriter inheritFactsW = new BufferedWriter(
					new FileWriter(outputDir.resolve("Inherit.facts").toString(), false));
			BufferedWriter abstractClassFactsW = new BufferedWriter(
					new FileWriter(outputDir.resolve("AbstractClass.facts").toString(), false));
			for (Map.Entry<String, String> e : classParents.entrySet()) {
				String fact = String.format("%s\t%s\n", e.getKey(), e.getValue());
				inheritFactsW.write(fact);
			}
			for (String c : abstractClazz) {
				String fact = String.format("%s\n", c);
				abstractClassFactsW.write(fact);
			}
			for (BufferedWriter w: new BufferedWriter[] {inheritFactsW, abstractClassFactsW} ) {
				w.flush();
				w.close();
			}
		} catch (IOException e) {
			PrintUtils.print("Exception when writing Inherit/AbstractClass facts file.", PrintUtils.TAG.WARNING);
			e.printStackTrace();
		}
	}

	public String generateCoverageFacts(ProjectConfiguration config)
			throws CoverageControlIOException, TestFailureException {
		PrintUtils.print("GENERATING Coverage FACTS..."); // coverage facts
		try {
			Files.createDirectories(depFactsDir);
		} catch (IOException e) {
			PrintUtils.print(String.format("Cannot create directory for cov-facts @ %s", depFactsDir),
					PrintUtils.TAG.WARNING);
			return "";
		}
		try {
			initializeCoverage(config);
			Path covFile = covFactsDir.resolve("Coverage.facts");
			FileWriter fWriter = new FileWriter(covFile.toString(), false);
			fWriter.write("// Coverage\n");
			CoverageDatabase cov = fCoverage.analyseCoverage();
			Set<SourceCodeEntity> coveredEntities = cov.getAllRelevantEntities();
			String testEntityName = "test";
			// PrintUtils.print(String.format("Generate coverage facts on commit [%s]",
			// endCommit));
			for (SourceCodeEntity e : coveredEntities) {
				String origName = e.getUniqueName();
				String entityName = origName;
				if (fuzzyNames) {
					entityName = matchWithGenericType(origName);
				}
				fWriter.append(String.format("%s\t%s\n", testEntityName, entityName));
			}
			fWriter.flush();
			fWriter.close();
		} catch (IOException | CoverageDataMissingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public boolean generateDifferentialFacts() throws CommitNotFoundException {
		PrintUtils.print("COMPUTING DIFF FACTS..."); // diff facts
		try {
			Files.createDirectories(diffFactsDir);
		} catch (IOException e) {
			PrintUtils.print(String.format("Cannot create directory for diff-facts @ %s", diffFactsDir),
					PrintUtils.TAG.WARNING);
			return false;
		}
		return preProcessHistory();
	}


	private boolean preProcessHistory() throws CommitNotFoundException {
		HashMap<FactsChangeType, BufferedWriter> factsWritersMap = new HashMap<>();
		try{
			for (FactsChangeType t : FactsChangeType.values()) {
				Path outputFile = diffFactsDir.resolve(t + ".facts");
				factsWritersMap.put(t, Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE));
			}
		} catch (IOException e) {
			PrintUtils.print(String.format("Exception when initializing BufferedWriter for " +
					"diff facts in directory %s", diffFactsDir), PrintUtils.TAG.WARNING);
			e.printStackTrace();
		}
		// for (ChangeType t : ChangeType.values()) {
		// 	String comment = String.format("// %s\n", t);
		// 	factsContents.put(t, comment);
		// }
		ChangeExtractor extractor = new ChangeExtractor(fJGit, fConfig.getProjectJDKVersion());
		StatsUtils.resume("history.preprocess");
		for (RevCommit c : fHistory) {
			Set<GitRefSourceCodeChange> changes;
			try {
				changes = extractor.extractChangesMerge(c);
			} catch (ChangeDistillerException e) {
				PrintUtils.print("Exception occurs in change distilling! Result will be unreliable!",
						PrintUtils.TAG.WARNING);
				e.printStackTrace();
				continue;
			}

			String parentVersion = c.getParent(0).name();
			String currentVersion = c.name();
			for (GitRefSourceCodeChange gitChange : changes) {
				// get change distiller change
				SourceCodeChange change = gitChange.getSourceCodeChange();
				// get file path to changed entity
				String filePath = gitChange.getChangedFilePath();
				// unique identifier of changed entity
				String uniqueName = null;
				// dependency type (reason for keeping)
				SlicingResult.DEP_FLAG depType = SlicingResult.DEP_FLAG.DROP;
				// change operation type
				AtomicChange.CHG_TYPE chgType = null;
				// parent entity is field/method/class containing the change
				String parentUniqueName = change.getParentEntity().getUniqueName();

				String operand1 = "";
				FactsChangeType changeType = null;
				if (change instanceof Delete) {
					Delete del = (Delete) change;
					uniqueName = del.getChangedEntity().getUniqueName();
					// op = "Delete";
					changeType = FactsChangeType.DELETE;
					// row = String.format("%s\t%s\t%s\t%s\n", uniqueName, parentUniqueName, currentVersion, parentVersion);
				} else if (change instanceof Insert) {
					Insert ins = (Insert) change;
					uniqueName = ins.getChangedEntity().getUniqueName();
					// op = "Insert";
					changeType = FactsChangeType.INSERT;
					// row = String.format("%s\t%s\t%s\t%s\n", uniqueName, parentUniqueName, currentVersion, parentVersion);
				} else if (change instanceof Update) {
					Update upd = (Update) change;
					uniqueName = upd.getNewEntity().getUniqueName();
					// is signature updated?
					boolean signatureChange = !upd.getChangedEntity().getUniqueName().equals(uniqueName);
					assert !signatureChange;
					// op = "Update";
					changeType = FactsChangeType.UPDATE;
					// row = String.format("%s\t%s\t%s\n", uniqueName, currentVersion, parentVersion);
				} else if (change instanceof Move) {
					// shouldn't detect move for structure nodes
					assert false;
				} else
					assert false;

				if (fuzzyNames) {
					operand1 = matchWithGenericType(uniqueName);
				} else {
					operand1 = uniqueName;
				}
				// String tuple = String.format("%s \"%s\" \"%s\"\n", op, operand1, currentVersion);
				String row = String.format("%s\t%s\t%s\t%s\n", operand1, parentUniqueName, currentVersion, parentVersion);
				BufferedWriter w = factsWritersMap.get(changeType);
				try {
					w.write(row);
				} catch (IOException e) {
					PrintUtils.print(String.format("Exception when writing line %s into %s ", row, w),
							PrintUtils.TAG.WARNING);
					e.printStackTrace();
				}
			}
		}
		for (Map.Entry<FactsChangeType, BufferedWriter> e: factsWritersMap.entrySet()) {
			try {
				e.getValue().flush();
				e.getValue().close();
			} catch (IOException exp) {
				PrintUtils.print(String.format("Exception when flushing and closing writer to %s",
						e.getKey().toString()), PrintUtils.TAG.WARNING);
				exp.printStackTrace();
			}
		}
		return true;
}

	private void initializeCoverage(ProjectConfiguration config) throws CoverageDataMissingException {

		// setup coverage analyzer
		if (config.isClassRootPathSet() && config.isSourceRootPathSet()) {
			if (config.isJacocoExecPathSet()) {
				fCoverage = new FullCoverageAnalyzer(config.getJacocoExecPath(), config.getSourceRootPath(),
						config.getClassRootPath());
			} else {
				PrintUtils.print("JacocoExecPath is not set.", PrintUtils.TAG.WARNING);
				fCoverage = null;
			}
		} else {
			PrintUtils.print("ClassRootPath or SourceRootPath is not set.", PrintUtils.TAG.WARNING);
		}
	}


	private static String concatVersion(String entity) {
		return concatVersion(entity, endCommit);
	}

	private static String concatVersion(String entity, String versionStr) {
		return entity + "@" + versionStr;
	}
}
