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

import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.BcelStaticCallGraphBuilder;
import cslicer.callgraph.CGEdgeType;
import cslicer.callgraph.CGNode;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.TestFailureException;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.graph.Edge;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class FactsExtr extends HistoryAnalyzer{
    protected static String endCommit;
    protected static boolean fuzzyNames;

    private final static String GENERIC_FILTER = "<[\\p{L}][\\p{L}\\p{N}]*>";
    private final static String CLASS_PATTERN = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";

    public FactsExtr(ProjectConfiguration config, boolean fuzzy) throws RepositoryInvalidException,
            CommitNotFoundException, BuildScriptInvalidException, CoverageControlIOException,
            AmbiguousEndPointException, ProjectConfigInvalidException, BranchNotFoundException,
            CoverageDataMissingException, IOException {
        super(config);
        endCommit = fEnd.name();
        fuzzyNames = fuzzy;
    }

    public String generateHunkDependencyFacts() {
        return "";
    }

    public void generateDependencyFacts() throws ClassPathInvalidException {
    }

    public boolean generateDifferentialFacts() throws CommitNotFoundException {
        return false;
    }

    public String generateCoverageFacts(ProjectConfiguration config) throws
            CoverageControlIOException, TestFailureException {
        return "";
    }

    protected static DependencyType findFactOperatorOfDepsEdge(Edge<CGNode> edge) {
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

    protected enum DependencyType {
        REFERENCE ("Reference"), CALL ("Call"), CONTAIN ("Contain");
        private final String normalName;

        DependencyType(String name) {
            this.normalName = name;
        }

        String getName() {
            return normalName;
        }
    }
    protected enum FactsChangeType {
        UPDATE, INSERT, DELETE;
    }

    private static String filterGenericType(String astName) {
        return astName.replaceAll(GENERIC_FILTER, StringUtils.EMPTY);
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
    /**
     * Simplify signature considering matching generics.
     */
    protected static String matchWithGenericType(final String name) {
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
}
