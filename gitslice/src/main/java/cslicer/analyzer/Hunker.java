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
import cslicer.callgraph.BcelStaticCallGraphBuilder;
import cslicer.callgraph.CGEdgeType;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.callgraph.StaticCallGraph;
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
import cslicer.utils.graph.Vertex;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Hunker extends HistoryAnalyzer {

    private String depFactsFile;
    private String covFactsFile;
    private String diffTupleFile;
    private String diffAttrFile;
    private String plainStringDiffTupleFile;
    private String plainStringDiffAttrFile;
    private String covPlainStringFile;
    private String depsMD5ToPlainStringMapFile;
    private String depsPlainStringFile;
    private static HashMap<String,String> md5HashMap = new HashMap<>();
    private static boolean genPlainFiles = true;

    private final static String GENERIC_FILTER = "<[\\p{L}][\\p{L}\\p{N}]*>";
    private final static String CLASS_PATTERN = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";

    public Hunker(ProjectConfiguration config) throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {
        this(config, true);
    }

    public Hunker(ProjectConfiguration config, boolean auxFiles) throws RepositoryInvalidException, CommitNotFoundException,
            BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException,
            BranchNotFoundException, CoverageDataMissingException, IOException {
        super(config);
        fClassRootPath = config.getClassRootPath();
        depFactsFile = Paths.get(fOutputPath, "20-deps.ta").toString();
        diffTupleFile = Paths.get(fOutputPath, "30-diff_tuple.ta").toString();
        diffAttrFile = Paths.get(fOutputPath, "40-diff_attr.ta").toString();
        covFactsFile = Paths.get(fOutputPath, "60-cov.ta").toString();
        plainStringDiffTupleFile = Paths.get(fOutputPath, "plain-diff_tuple.ta").toString();
        plainStringDiffAttrFile = Paths.get(fOutputPath, "plain-diff_attr.ta").toString();
        depsMD5ToPlainStringMapFile = Paths.get(fOutputPath, "plain-md5-map.txt").toString();
        depsPlainStringFile = Paths.get(fOutputPath, "deps-plain.txt").toString();
        covPlainStringFile = Paths.get(fOutputPath, "plain-cov.txt").toString();
        genPlainFiles = auxFiles;
    }

    public String generateHunkDependencyFacts() {
        PrintUtils.print("COMPUTING HUNK FACTS...");
        DependencyCache hunkGraph = new DependencyCache();
        computeHunkDepSet(fHistory, hunkGraph);
        return hunkGraph.toString();
    }

    private static String findFactOperatorOfDepsEdge(Edge edge) {
        if (edge.getLabel() == CGEdgeType.FIELD_READ
                || edge.getLabel() == CGEdgeType.FIELD_WRITE || edge.getLabel() == CGEdgeType.STATIC_READ
                || edge.getLabel() == CGEdgeType.STATIC_WRITE || edge.getLabel() == CGEdgeType.CLASS_REFERENCE
                || edge.getLabel() == CGEdgeType.FIELD_REFERENCE) {
            return "reference";
        } else if (edge.getLabel() == CGEdgeType.INVOKE_INTERFACE || edge.getLabel() == CGEdgeType.INVOKE_SPECIAL
                || edge.getLabel() == CGEdgeType.INVOKE_STATIC || edge.getLabel() == CGEdgeType.INVOKE_VIRTUAL) {
            return "call";
        } else if (edge.getLabel() == CGEdgeType.CLASS_FIELD || edge.getLabel() == CGEdgeType.CLASS_METHOD) {
            return "contain";
        } else { // default
            return "reference";
        }
    }

    public String generateDependencyFacts() throws ClassPathInvalidException {
        PrintUtils.print("COMPUTING DEPS FACTS..."); // dependency facts
        String classPath = fClassRootPath;
        String depsFactsFile = this.depFactsFile;
        String depsMD5ToPlainStringMapFile = this.depsMD5ToPlainStringMapFile;
        String depsPlainStringFile = this.depsPlainStringFile;
        return computeDependencyFacts(classPath, depsFactsFile, depsMD5ToPlainStringMapFile, depsPlainStringFile, true, genPlainFiles);
    }

    public static String computeDependencyFacts(String classPath, String depFactsFile, String depsMD5ToPlainStringMapFile, String depsPlainStringFile)
            throws ClassPathInvalidException {
        return computeDependencyFacts(classPath, depFactsFile, depsMD5ToPlainStringMapFile, depsPlainStringFile, false, true);
    }

    private static String computeDependencyFacts(String classPath, String depFactsFile, String depsMD5ToPlainStringMapFile, String depsPlainStringFile, boolean fuzzy, boolean aux_files)
            throws ClassPathInvalidException {
        StringBuilder factsFileContent = new StringBuilder();
        Map<String, String> MD5ToPlainMap = new TreeMap<>();
        // file header
        factsFileContent.append("FACT TUPLE :\n");
        BcelStaticCallGraphBuilder depsCGBuilder = new BcelStaticCallGraphBuilder(
                classPath);
        depsCGBuilder.buildCallGraph();
        StaticCallGraph depsCG = depsCGBuilder.getCallGraph();
        //depsCG.outputDOTFile("/tmp/deps-graph.txt");
        for (Edge e : depsCG.getEdges()) {
            String frVertexName = e.getFrom().getName();
            String toVertexName = e.getTo().getName();
            String operator = findFactOperatorOfDepsEdge(e);
            String frNameMD5, toNameMD5;
            if (fuzzy) {
                frNameMD5 = calcMD5(matchWithGenericType(frVertexName));
                toNameMD5 = calcMD5(matchWithGenericType(toVertexName));
            } else {
                frNameMD5 = calcMD5(frVertexName);
                toNameMD5 = calcMD5(toVertexName);
            }
            String fact = String.format("%s %s %s", operator, frNameMD5, toNameMD5);
            MD5ToPlainMap.put(frNameMD5, frVertexName);
            MD5ToPlainMap.put(toNameMD5, toVertexName);
            factsFileContent.append(fact).append("\n");
        }
        try {
            PrintUtils.print(String.format("Start writing to file @ %s", depFactsFile), PrintUtils.TAG.DEBUG);
            FileWriter fWriter = new FileWriter(depFactsFile, false);
            fWriter.write(factsFileContent.toString());
            fWriter.flush();
            fWriter.close();
            if (aux_files) {
                PrintUtils.print(String.format("Start writing to file @ %s", depsMD5ToPlainStringMapFile), PrintUtils.TAG.DEBUG);
                fWriter = new FileWriter(depsMD5ToPlainStringMapFile, false);
                for (Map.Entry entry : MD5ToPlainMap.entrySet()) {
                    fWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
                }
                fWriter.flush();
                fWriter.close();

                PrintUtils.print(String.format("Start writing to file @ %s", depsPlainStringFile), PrintUtils.TAG.DEBUG);
                fWriter = new FileWriter(depsPlainStringFile, false);
                StringBuilder plainDepsContent = new StringBuilder();
                for (String tuple : factsFileContent.toString().split("\\n")) {
                    if (tuple.contains("FACT TUPLE :"))
                        continue;
                    String md5_1 = tuple.split(" ")[1];
                    String md5_2 = tuple.split(" ")[2];
                    String plainTuple = tuple.replace(md5_1, MD5ToPlainMap.get(md5_1)).replace(md5_2, MD5ToPlainMap.get(md5_2)) + "\n";
                    plainDepsContent.append(plainTuple);

                }
                fWriter.write(plainDepsContent.toString());
                fWriter.flush();
                fWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return factsFileContent.toString();
    }

    public String generateCoverageFacts(ProjectConfiguration config) throws CoverageControlIOException, TestFailureException {
        PrintUtils.print("GENERATING Coverage FACTS..."); // coverage facts
        try {
            initializeCoverage(config);
            FileWriter fWriter = new FileWriter(covFactsFile, false);
            fWriter.write("FACT TUPLE :\n");
            StringBuilder plainFacts = new StringBuilder("FACT TUPLE :\n");
            CoverageDatabase cov = fCoverage.analyseCoverage();
            Set<SourceCodeEntity> coveredEntities =cov.getAllRelevantEntities();
            String eTag = "test";
            for (SourceCodeEntity e : coveredEntities) {
                String origName = e.getUniqueName();
                String fuzzyEntityName = matchWithGenericType(origName);
                fWriter.append(String.format("Coverage %s %s\n", eTag, calcMD5(fuzzyEntityName)));
                if (genPlainFiles) {
                    plainFacts.append(String.format("Coverage %s %s %s %s\n", origName, fuzzyEntityName, calcMD5(origName), calcMD5(fuzzyEntityName)));
                }
            }
            fWriter.flush();
            fWriter.close();
            if (genPlainFiles){
                FileWriter plainWriter = new FileWriter(covPlainStringFile, false);
                plainWriter.write(plainFacts.toString());
                plainWriter.flush();
                plainWriter.close();
            }
        } catch (IOException | CoverageDataMissingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean generateDifferentialFacts() throws CommitNotFoundException {
        PrintUtils.print("COMPUTING DIFF FACTS..."); // diff facts
        return preProcessHistory();
    }

    private static String calcMD5(String input){
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
            FileWriter fPlainTupleWriter = null;
            FileWriter fPlainAttrWriter = null;
            if (genPlainFiles) {
                fPlainTupleWriter = new FileWriter(plainStringDiffTupleFile, false);
                fPlainAttrWriter = new FileWriter(plainStringDiffAttrFile, false);
            }
            fTupleWriter.write("FACT TUPLE :\n");
            fAttrWriter.write("FACT ATTRIBUTE :\n");
            // Set<String> fChangedClasses = new HashSet<>();
            List<RevCommit> A = new LinkedList<>(fHistory);
            VersionTracker fTracker = new VersionTracker(A, fComparator);

            ChangeExtractor extractor = new ChangeExtractor(fJGit,
                    fConfig.getProjectJDKVersion());

            StatsUtils.resume("history.preprocess");

            int i = 0; // commit sequence number
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

                String verA = c.getParent(0).name();
                String verB = c.name();
                StringBuilder factsTuplePerCommit = new StringBuilder();
                StringBuilder factsAttrPerCommit = new StringBuilder();
                StringBuilder plainStringTuplePerCommit = new StringBuilder();
                StringBuilder plainStringAttrPerCommit = new StringBuilder();
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

                    // fChangedClasses.add(gitChange.getEnclosingClassName());

                    // parent entity is field/method/class which contains the
                    // change
                    String operand1 = "", operand2 = "", op = "";
                    if (change instanceof Delete) {
                        Delete del = (Delete) change;
                        uniqueName = del.getChangedEntity().getUniqueName();
                        String parentUniqueName = del.getParentEntity().getUniqueName();
                        operand1 = calcMD5(matchWithGenericType(uniqueName));
                        operand2 = verB;
                        chgType = AtomicChange.CHG_TYPE.DEL;
                        op = "Delete";
                    } else if (change instanceof Insert) {
                        Insert ins = (Insert) change;
                        uniqueName = ins.getChangedEntity().getUniqueName();
                        String parentUniqueName = ins.getParentEntity().getUniqueName();
                        operand1 = calcMD5(matchWithGenericType(uniqueName));
                        operand2 = verB;
                        chgType = AtomicChange.CHG_TYPE.INS;
                        op = "Insert";
                    } else if (change instanceof Update) {
                        Update upd = (Update) change;
                        uniqueName = upd.getNewEntity().getUniqueName();
                        // is signature updated?
                        boolean signatureChange = !upd.getChangedEntity()
                                .getUniqueName().equals(uniqueName);
                        assert !signatureChange;
                        operand1 = calcMD5(matchWithGenericType(uniqueName));
                        operand2 = verB;
                        chgType = AtomicChange.CHG_TYPE.UPD;
                        op = "Update";
                    } else if (change instanceof Move) {
                        // shouldn't detect move for structure nodes
                        assert false;
                    } else
                        assert false;

                    String tuple = String.format("%s %s %s\n", op, operand1, operand2);
                    String attr = String.format("(%s %s %s) { name_hash = \"%s\" version_a = \"%s\" version_b = \"%s\" }\n",
                            op, operand1, operand2, operand1, verA, verB);
                    factsTuplePerCommit.append(tuple);
                    factsAttrPerCommit.append(attr);
                    if (genPlainFiles) {
                        String plainTuple = String.format("%s \"%s\" \"%s\"\n", op, uniqueName, operand2);
                        String plainAttr = String.format("(%s \"%s\" \"%s\") { name = \"%s\" version_a = \"%s\" version_b = \"%s\" }\n",
                                op, uniqueName, operand2, uniqueName, verA, verB);
                        plainStringTuplePerCommit.append(plainTuple);
                        plainStringAttrPerCommit.append(plainAttr);
                    }
                    // track this atomic change
                    /*
                    fTracker.trackAtomicChangeAdd(new AtomicChange(uniqueName,
                            filePath, gitChange.getPreImage(),
                            gitChange.getPostImage(), i, depType, chgType));
                     */
                }
                fTupleWriter.append(factsTuplePerCommit.toString());
                fAttrWriter.append(factsAttrPerCommit.toString());
                if (genPlainFiles && fPlainTupleWriter!=null) {
                    fPlainTupleWriter.append(plainStringTuplePerCommit);
                    fPlainAttrWriter.append(plainStringAttrPerCommit);
                }
                i++;
            }
            fTupleWriter.flush();
            fAttrWriter.flush();
            fTupleWriter.close();
            fAttrWriter.close();
            if (genPlainFiles && fPlainTupleWriter!=null) {
                fPlainTupleWriter.flush();
                fPlainAttrWriter.flush();
                fPlainTupleWriter.close();
                fPlainAttrWriter.close();
            }
            return true;
            // return factsFileContent.toString();
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
}
