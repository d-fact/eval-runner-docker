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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DatalogFactsExtr extends HistoryAnalyzer {

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
	private static String endCommit;
	private static boolean fuzzyNames;
	private static String versionize;

	private final static String GENERIC_FILTER = "<[\\p{L}][\\p{L}\\p{N}]*>";
	private final static String CLASS_PATTERN = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";

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
		super(config);
		fClassRootPath = config.getClassRootPath();
		endCommit = fEnd.name();
		fuzzyNames = fuzzy;
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


	public String generateDependencyFacts() throws ClassPathInvalidException {
		PrintUtils.print("COMPUTING DEPS FACTS..."); // dependency facts
		try {
			Files.createDirectories(depFactsDir);
		} catch (IOException e) {
			PrintUtils.print(String.format("Cannot create directory for dep-facts @ %s", depFactsDir),
					PrintUtils.TAG.WARNING);
			return "";
		}
		BcelStaticCallGraphBuilder cgBuilder = new BcelStaticCallGraphBuilder(fClassRootPath);
        generateInheritFacts(cgBuilder, depFactsDir);
		return computeDependencyFacts(cgBuilder, depFactsDir, versionize, fuzzyNames);
	}

	public static String computeDependencyFacts(BcelStaticCallGraphBuilder cgBuilder, Path depFactsDir) throws ClassPathInvalidException {
		return computeDependencyFacts(cgBuilder, depFactsDir, "", false);
	}

	private static String computeDependencyFacts(BcelStaticCallGraphBuilder cgBuilder, Path depFactsDir,
												 String versionize, boolean fuzzy) {
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

		cgBuilder.buildCallGraph();
		StaticCallGraph depsCG = cgBuilder.getCallGraph();
		// depsCG.outputDOTFile("/tmp/deps-graph.txt");
		for (Edge<CGNode> e : depsCG.getEdges()) {
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
			BufferedWriter w = factsWritersMap.get(depType.getName());
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

		try{
			for (Map.Entry<CGNodeType, HashSet<CGNode>> entry : nodes.entrySet()) {
				CGNodeType nodeType = entry.getKey();
				for (CGNode e: entry.getValue()) {
					BufferedWriter w = factsWritersMap.get("Is" + nodeType.name());
					if (nodeType == CGNodeType.Method) {
						MethodNode method = (MethodNode)e;
						String fqName = method.getName();
						String fqClassName = method.getFqClassName();
						String simpleName = rmParams(method.getMethodName());
						int numOfParams = method.getArgs().size();
						String retType = method.getRetType();
						boolean isCtor = isCtor(fqName);
						// w.write(String.format("%s\t%s\t%s\t%s\t%s", qualifiedName, simpleName, numOfParams, retType, isCtor));
						w.write(String.format("%s\t%s\t%s\t%s\t%s\t%s", fqName, simpleName, fqClassName, numOfParams, retType, isCtor));
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
		HashMap<String, String> classParents = new HashMap<>();
		ArrayList<String> abstractClazz = new ArrayList<>();
		cgBuilder.getMoreClassInfo(classParents, abstractClazz);
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
			PrintUtils.print(String.format("Cannot create directory for cov-facts @ %s", diffFactsDir),
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


	private static String realCalcMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			return Hex.encodeHexString(md5digest);
		} catch (NoSuchAlgorithmException e) {
			PrintUtils.print("MD5 is not supported now.", PrintUtils.TAG.WARNING);
			e.printStackTrace();
			return "";
		}
	}

	private boolean preProcessHistory() throws CommitNotFoundException {
		HashMap<ChangeType, String> factsContents = new HashMap<>();
		// for (ChangeType t : ChangeType.values()) {
		// 	String comment = String.format("// %s\n", t);
		// 	factsContents.put(t, comment);
		// }
		try {
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
				StringBuilder factsTuplePerCommit = new StringBuilder();
				StringBuilder factsAttrPerCommit = new StringBuilder();
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
					String parentUniqueName = "";

					String operand1 = "";
					ChangeType changeType = null;
					if (change instanceof Delete) {
						Delete del = (Delete) change;
						uniqueName = del.getChangedEntity().getUniqueName();
						parentUniqueName = del.getParentEntity().getUniqueName();
						// op = "Delete";
						changeType = ChangeType.DELETE;
						// row = String.format("%s\t%s\t%s\t%s\n", uniqueName, parentUniqueName, currentVersion, parentVersion);
					} else if (change instanceof Insert) {
						Insert ins = (Insert) change;
						uniqueName = ins.getChangedEntity().getUniqueName();
						parentUniqueName = ins.getParentEntity().getUniqueName();
						// op = "Insert";
						changeType = ChangeType.INSERT;
						// row = String.format("%s\t%s\t%s\t%s\n", uniqueName, parentUniqueName, currentVersion, parentVersion);
					} else if (change instanceof Update) {
						Update upd = (Update) change;
						uniqueName = upd.getNewEntity().getUniqueName();
						// is signature updated?
						boolean signatureChange = !upd.getChangedEntity().getUniqueName().equals(uniqueName);
						assert !signatureChange;
						// op = "Update";
						changeType = ChangeType.UPDATE;
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
					factsContents.put(changeType, row);
					// String attr = String.format(
					// 		"(%s \"%s\" \"%s\")" + " { name = \"%s\" commit_parent = \"%s\" commit = \"%s\" }\n", op,
					// 		operand1, currentVersion, uniqueName, parentVersion, currentVersion);
					// factsTuplePerCommit.append(tuple);
					// factsAttrPerCommit.append(attr);
				}
				// fTupleWriter.append(factsTuplePerCommit.toString());
				// fAttrWriter.append(factsAttrPerCommit.toString());
			}
			// fTupleWriter.flush();
			// fAttrWriter.flush();
			// fTupleWriter.close();
			// fAttrWriter.close();
			// write hashmap contents to files
			for (ChangeType t : ChangeType.values()) {
				Path outFile = diffFactsDir.resolve(t.toString()+".facts");
				FileWriter fFactWriter = new FileWriter(outFile.toString(), false);
				fFactWriter.append(factsContents.get(t));
				fFactWriter.flush();
				fFactWriter.close();
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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

	private static String filterGenericType(String astName) {
		return astName.replaceAll(GENERIC_FILTER, StringUtils.EMPTY);
	}

	/**
	 * Simplify signature considering matching generics.
	 */
	private static String matchWithGenericType(final String name) {
		String result = filterGenericType(name);
		if (isFieldName(result)) {
			result = result.substring(0, result.indexOf(":")).trim();
		}
		if (isMethodName(result)) {
			String params = result.substring(result.indexOf("("));
			int numOfPar = Math.min(params.split(",").length, params.replaceAll("[)|(|\\s+]", "").length());
			result = result.substring(0, result.indexOf("(")) + "(" + numOfPar + ")";
		}
		return result;
	}

	private static boolean isFieldName(String sigcp1) {
		return sigcp1.contains(" : ");
	}

	private static boolean isClassName(String key) {
		return key.matches(CLASS_PATTERN);
	}

	private static boolean isMethodName(String key) {
		// return key.matches(METHOD_PATTERN) ||
		// key.matches(METHOD_PATTERN_NOP);
		return key.indexOf("(") > 0 && key.indexOf(")") > 0;
	}

	private static String concatVersion(String entity) {
		return concatVersion(entity, endCommit);
	}

	private static String concatVersion(String entity, String versionStr) {
		return entity + "@" + versionStr;
	}

	private static DependencyType findFactOperatorOfDepsEdge(Edge<CGNode> edge) {
		if (edge.getLabel() == CGEdgeType.FIELD_READ || edge.getLabel() == CGEdgeType.FIELD_WRITE
				|| edge.getLabel() == CGEdgeType.STATIC_READ || edge.getLabel() == CGEdgeType.STATIC_WRITE
				|| edge.getLabel() == CGEdgeType.CLASS_REFERENCE || edge.getLabel() == CGEdgeType.FIELD_REFERENCE) {
			return DependencyType.REFERENCE;
		} else if (edge.getLabel() == CGEdgeType.INVOKE_INTERFACE || edge.getLabel() == CGEdgeType.INVOKE_SPECIAL
				|| edge.getLabel() == CGEdgeType.INVOKE_STATIC || edge.getLabel() == CGEdgeType.INVOKE_VIRTUAL) {
			return DependencyType.CALL;
		} else if (edge.getLabel() == CGEdgeType.CLASS_FIELD || edge.getLabel() == CGEdgeType.CLASS_METHOD) {
			return DependencyType.CONTAIN;
		} else { // default
			return DependencyType.REFERENCE;
		}
	}

	private enum ChangeType {
		UPDATE, INSERT, DELETE;
	}

	private enum DependencyType {
		REFERENCE ("Reference"), CALL ("Call"), CONTAIN ("Contain");
		private final String normalName;

		DependencyType(String name) {
			this.normalName = name;
		}

		String getName() {
			return normalName;
		}
	}
}
