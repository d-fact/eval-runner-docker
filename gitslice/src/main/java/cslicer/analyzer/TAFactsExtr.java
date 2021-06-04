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
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.DependencyCache;
import cslicer.utils.PrintUtils;
import cslicer.utils.StatsUtils;
import cslicer.utils.graph.Edge;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class TAFactsExtr extends FactsExtr {

    private final String depFactsFile;
    private final String inheritFactsFile;
    private final String covFactsFile;
    private final String diffTupleFile;
    private final String diffAttrFile;
	private final String testClassFactsFile;
    private String plainStringDiffTupleFile;
    private String plainStringDiffAttrFile;
    private String covPlainStringFile;
    private String depsMD5ToPlainStringMapFile;
    private String depsPlainStringFile;
    private static HashMap<String, String> md5HashMap = new HashMap<>();
    // private static boolean genPlainFiles = true;
    private static String endCommit;
    private static boolean genAuxFiles;
    private static boolean fuzzyNames;


    public TAFactsExtr(ProjectConfiguration config) throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {
        this(config, true, true);
    }

    public TAFactsExtr(ProjectConfiguration config, boolean auxFiles, boolean fuzzy) throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {
        super(config, fuzzy);
        fClassRootPath = config.getClassRootPath();
        depFactsFile = Paths.get(fOutputPath, "20-deps.ta").toString();
        inheritFactsFile = Paths.get(fOutputPath, "25-inherit.ta").toString();
        diffTupleFile = Paths.get(fOutputPath, "30-diff_tuple.ta").toString();
        diffAttrFile = Paths.get(fOutputPath, "40-diff_attr.ta").toString();
        covFactsFile = Paths.get(fOutputPath, "60-cov.ta").toString();
        testClassFactsFile = Paths.get(fOutputPath, "90-test_classes.ta").toString();
        genAuxFiles = auxFiles;
        /*
        plainStringDiffTupleFile = Paths.get(fOutputPath, "plain-diff_tuple.ta").toString();
        plainStringDiffAttrFile = Paths.get(fOutputPath, "plain-diff_attr.ta").toString();
        depsMD5ToPlainStringMapFile = Paths.get(fOutputPath, "plain-md5-map.txt").toString();
        depsPlainStringFile = Paths.get(fOutputPath, "deps-plain.txt").toString();
        covPlainStringFile = Paths.get(fOutputPath, "plain-cov.txt").toString();
        genPlainFiles = auxFiles;
        */
    }

    public String generateHunkDependencyFacts() {
        PrintUtils.print("COMPUTING HUNK FACTS...");
        DependencyCache hunkGraph = new DependencyCache();
        computeHunkDepSet(fHistory, fHistory, hunkGraph, FactFormat.TA);
        return hunkGraph.toString();
    }

    public void generateDependencyFacts() throws ClassPathInvalidException {
        PrintUtils.print("COMPUTING DEPS FACTS..."); // dependency facts
        BcelStaticCallGraphBuilder srcCG = new BcelStaticCallGraphBuilder(fClassRootPath);

        BcelStaticCallGraphBuilder testCG = buildCallGraphForTestClasses(fClassRootPath);
		generateTestClassesFacts(testCG, testClassFactsFile);
		// TODO: may generate inherit facts for all classes in the future
        generateInheritFacts(testCG, inheritFactsFile);
        computeDependencyFacts(srcCG, testCG, depFactsFile, fuzzyNames);
    }

    public static void computeDependencyFacts(BcelStaticCallGraphBuilder cgBuilder, String depFactsFile)
            throws ClassPathInvalidException {
        computeDependencyFacts(cgBuilder, null, depFactsFile, false);
    }

    /*
	private static void writeStringToFile(String contents, String filename){
        try {
            PrintUtils.print(String.format("Start writing to file @ %s", filename), PrintUtils.TAG.DEBUG);
            FileWriter fWriter = new FileWriter(filename, false);
            fWriter.write(contents);
            fWriter.flush();
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}*/

	private static void generateTestClassesFacts(BcelStaticCallGraphBuilder cgBuilder,
                                                 String outputFile) {
		generateClassesFacts(cgBuilder, "TestClass", outputFile);
	}

	private static void generateInheritFacts(BcelStaticCallGraphBuilder cgBuilder, String outputFile) {
        // cgBuilder.getMoreClassInfo(classParents, abstractClazz);
        HashMap<String, String> classParents = cgBuilder.getInheritPairs();
        ArrayList<String> abstractClazz = cgBuilder.getAbstractClasses();
        try {
            BufferedWriter factsFileWriter = new BufferedWriter(new FileWriter(outputFile, false));
            factsFileWriter.write("FACT TUPLE :\n");
            for (Map.Entry<String, String> e : classParents.entrySet()) {
                String fact = String.format("Inherit \"%s\" \"%s\"\n", e.getKey(), e.getValue());
                factsFileWriter.write(fact);
            }
            for (String c : abstractClazz) {
                String fact = String.format("AbstractClass \"%s\" NULL\n", c);
                factsFileWriter.write(fact);
            }
            factsFileWriter.flush();
            factsFileWriter.close();
        } catch (IOException e) {
            PrintUtils.print(String.format("Exception when writing facts file [%s]", outputFile), PrintUtils.TAG.WARNING);
            e.printStackTrace();
        }
    }

	private static void generateClassesFacts(BcelStaticCallGraphBuilder cgBuilder, String operator,
                                             String outputFile) {
		ArrayList<String> classNames = cgBuilder.getClassNames();
		try {
            BufferedWriter factsFileWriter = new BufferedWriter(new FileWriter(outputFile, false));
            factsFileWriter.write("FACT TUPLE :\n");
            for (String e : classNames) {
                String fact = String.format("%s \"%s\" NULL\n", operator, e);
                factsFileWriter.write(fact);
            }
            factsFileWriter.flush();
            factsFileWriter.close();
        } catch (IOException e) {
            PrintUtils.print(String.format("Exception when writing facts file [%s]", outputFile), PrintUtils.TAG.WARNING);
            e.printStackTrace();
        }
	}

	private static void writeFactsFromCGEdges(StaticCallGraph cg, boolean fuzzy, BufferedWriter w,
                                              HashMap<CGNodeType, HashSet<String>> nodes) {
        for (Edge<CGNode> e : cg.getEdges()) {
            String frVertexName = e.getFrom().getName();
            String toVertexName = e.getTo().getName();
            String operator = findFactOperatorOfDepsEdge(e).toString().toLowerCase();
            String frName, toName;
            if (fuzzy) {
                frName = matchWithGenericType(frVertexName);
                toName = matchWithGenericType(toVertexName);
            } else {
                frName = frVertexName;
                toName = toVertexName;
            }
            String fact = String.format("%s \"%s\" \"%s\"\n", operator, frName, toName);
            // factsFileContent.append(fact).append("\n");
            try {
                w.write(fact);
            } catch (IOException exp) {
                PrintUtils.print(String.format("Exception when writing fact:\n%s" , fact),
                        PrintUtils.TAG.WARNING);
                exp.printStackTrace();
            }

            nodes.get(e.getFrom().getData().getNodeType()).add(frName);
            nodes.get(e.getTo().getData().getNodeType()).add(toName);
        }
    }

	private static void computeDependencyFacts(BcelStaticCallGraphBuilder srcCGBuilder,
                                                 BcelStaticCallGraphBuilder testCGBuilder,
                                                 String depFactsFile, boolean fuzzy) {
        // StringBuilder factsFileContent = new StringBuilder();
        // factsFileContent.append("FACT TUPLE :\n");
        BufferedWriter factsFileWriter;
        try {
            factsFileWriter = new BufferedWriter(new FileWriter(depFactsFile));
            factsFileWriter.write("FACT TUPLE :\n");
        } catch (IOException e) {
            PrintUtils.print(String.format("Exception when initializing BufferedWriter for file %s", depFactsFile),
                    PrintUtils.TAG.WARNING);
            e.printStackTrace();
            return;
        }

        // init nodes map
        HashMap<CGNodeType, HashSet<String>> nodes = new HashMap<>();
        //depsCG.outputDOTFile("/tmp/deps-graph.txt");
        // HashMap<CGNodeType, HashSet<CGNode>> nodes = new HashMap<>();
        for (CGNodeType t : CGNodeType.values()) {
            if (t == CGNodeType.CGNode) { continue; }
            nodes.put(t, new HashSet<String>());
        }

        // write facts about src classes
        srcCGBuilder.buildCallGraph();
        StaticCallGraph depsCG = srcCGBuilder.getCallGraph();

        // writeFactsFromCGEdges(depsCG, fuzzy, );
        writeFactsFromCGEdges(depsCG, fuzzy, factsFileWriter, nodes);
        // write facts about test classes
        if (testCGBuilder != null) {
            testCGBuilder.buildCallGraph();
            StaticCallGraph depsCGTest = testCGBuilder.getCallGraph();
            writeFactsFromCGEdges(depsCGTest, fuzzy, factsFileWriter, nodes);
        }

        try {
            for (Map.Entry<CGNodeType, HashSet<String>> entry : nodes.entrySet()) {
                CGNodeType nodeType = entry.getKey();
                for (String e : entry.getValue()) {
                    factsFileWriter.write(String.format("CGNodeType %s \"%s\"\n", nodeType.name(), e));
                }
            }
        } catch (IOException exp) {
            PrintUtils.print(String.format("Exception when writing CGNodeType facts to %s.", depFactsFile),
                    PrintUtils.TAG.WARNING);
            exp.printStackTrace();
        }

        try {
            PrintUtils.print(String.format("Flush buffered writer to %s", depFactsFile), PrintUtils.TAG.DEBUG);
            factsFileWriter.flush();
            factsFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateCoverageFacts(ProjectConfiguration config) throws CoverageControlIOException, TestFailureException {
        PrintUtils.print("GENERATING Coverage FACTS..."); // coverage facts
        try {
            initializeCoverage(config);
            FileWriter fWriter = new FileWriter(covFactsFile, false);
            fWriter.write("FACT TUPLE :\n");
            CoverageDatabase cov = fCoverage.analyseCoverage();
            Set<SourceCodeEntity> coveredEntities = cov.getAllRelevantEntities();
            String testEntityName = "test";
            // PrintUtils.print(String.format("Generate coverage facts on commit [%s]", endCommit));
            for (SourceCodeEntity e : coveredEntities) {
                String origName = e.getUniqueName();
                String entityName = origName;
                if (fuzzyNames) {
                    entityName = matchWithGenericType(origName);
                }
                fWriter.append(String.format("Coverage \"%s\" \"%s\"\n", testEntityName, entityName));
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
        return preProcessHistory();
    }

    private static String calcMD5(String input) {
        String hash = md5HashMap.get(input);
        if (hash == null) {
            String md5Value = realCalcMD5(input);
            md5HashMap.put(input, md5Value);
            return md5Value;
        } else {
            return hash;
        }
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
        try {
            FileWriter fTupleWriter = new FileWriter(diffTupleFile, false);
            FileWriter fAttrWriter = new FileWriter(diffAttrFile, false);
            fTupleWriter.write("FACT TUPLE :\n");
            fAttrWriter.write("FACT ATTRIBUTE :\n");
            ChangeExtractor extractor = new ChangeExtractor(fJGit,
                    fConfig.getProjectJDKVersion());
            StatsUtils.resume("history.preprocess");
            PrintUtils.print("Length of history is " + fHistory.size() + "\n",
                    PrintUtils.TAG.STATS);
            for (RevCommit c : fHistory) {
                Set<GitRefSourceCodeChange> changes;
                try {
                    changes = extractor.extractChangesMerge(c);
                } catch (ChangeDistillerException e) {
                    PrintUtils.print(
                            "Exception occurs in change distilling! Result will be unreliable!",
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
                    // parent entity is field/method/class which contains the
                    // change
                    String operand1 = "", operand2 = "", op = "";
                    if (change instanceof Delete) {
                        Delete del = (Delete) change;
                        uniqueName = del.getChangedEntity().getUniqueName();
                        // String parentUniqueName = del.getParentEntity().getUniqueName();
                        op = "Delete";
                    } else if (change instanceof Insert) {
                        Insert ins = (Insert) change;
                        uniqueName = ins.getChangedEntity().getUniqueName();
                        // String parentUniqueName = ins.getParentEntity().getUniqueName();
                        op = "Insert";
                    } else if (change instanceof Update) {
                        Update upd = (Update) change;
                        uniqueName = upd.getNewEntity().getUniqueName();
                        // is signature updated?
                        boolean signatureChange = !upd.getChangedEntity()
                                .getUniqueName().equals(uniqueName);
                        assert !signatureChange;
                        op = "Update";
                    } else if (change instanceof Move) {
                        // shouldn't detect move for structure nodes
                        assert false;
                    } else
                        assert false;

                    operand1 = uniqueName;
                    if (fuzzyNames) {
                        operand1 = matchWithGenericType(uniqueName);
                    }
                    String tuple = String.format("%s \"%s\" \"%s\"\n", op, operand1, currentVersion);
                    String attr = String.format("(%s \"%s\" \"%s\") { name = \"%s\" commit_parent = \"%s\" commit = \"%s\" }\n",
                            op, operand1, currentVersion, operand1, parentVersion, currentVersion);
                    factsTuplePerCommit.append(tuple);
                    factsAttrPerCommit.append(attr);
                }
                fTupleWriter.append(factsTuplePerCommit.toString());
                fAttrWriter.append(factsAttrPerCommit.toString());
            }
            fTupleWriter.flush();
            fAttrWriter.flush();
            fTupleWriter.close();
            fAttrWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initializeCoverage(ProjectConfiguration config)
            throws CoverageDataMissingException {

        // setup coverage analyzer
        if (config.isClassRootPathSet() && config.isSourceRootPathSet()) {
            if (config.isJacocoExecPathSet()) {
                fCoverage = new FullCoverageAnalyzer(config.getJacocoExecPath(),
                        config.getSourceRootPath(), config.getClassRootPath());
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
