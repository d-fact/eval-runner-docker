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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import cslicer.CSlicer;
import cslicer.analyzer.HistoryAnalyzer.FactFormat;
import cslicer.builder.BuildScriptInvalidException;
import cslicer.callgraph.ClassPathInvalidException;
import cslicer.coverage.CoverageControlIOException;
import cslicer.coverage.CoverageDataMissingException;
import cslicer.coverage.TestFailureException;
import cslicer.jgit.AmbiguousEndPointException;
import cslicer.jgit.BranchNotFoundException;
import cslicer.jgit.CheckoutBranchFailedException;
import cslicer.jgit.CheckoutFileFailedException;
import cslicer.jgit.CommitNotFoundException;
import cslicer.jgit.RepositoryInvalidException;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import cslicer.utils.StatsUtils;

/**
 * Main entry for the GitRef tool.
 * 
 * @author Yi Li
 *
 */
public class Main {

	public static void main(String[] args) {

		System.out.println("===== Git History Slicing Toolkit =====");
		System.out.println(CSlicer.CSLICER_LOGO);
		System.out.println("=======================================");

		// check environment
		// if (!checkEnvVars()) {
		// 	PrintUtils.print("Environment not setup properly. Abort.",
		// 			TAG.WARNING);
		// 	System.exit(1);
		// }

		// help and info options
		Options options1 = new Options();
		options1.addOption("h", "help", false, "Print help messages.");
		options1.addOption("i", "info", false, "Display version information.");

		// required options
		Options options2 = new Options();
		Option c = new Option("c", "config", true,
				"Path to project configuration file.");
		Option e = new Option("e", "engine", true,
				"Select slicing engine: [slicer|refiner|delta|srr|hunker|fact|dl].");
		c.setRequired(true);
		e.setRequired(true);
		options2.addOption(c);
		options2.addOption(e);

		// optional options
		Option d = new Option("d", "diff", true, "Show AST diff of a commit.");
		Option p = new Option("p", "print", false,
				"Output hunk dependency graph.");
		Option n = new Option("n", "nopick", false, "Skip cherry-picking.");
		Option t = new Option("t", "test", false,
				"Verify picking and/or testing result.");
		Option s = new Option("s", "short", false,
				"Try history slice shorten.");
		Option q = new Option("q", "quiet", false, "No debug output.");
		Option v = new Option("v", "verbose", false, "Verbose mode.");
		Option l = new Option("l", "learn", true,
				"Select significance learning schemes: [default|noinv|nolearn|noprob|noinit|nocomp|combined|low3|neg|nonpos].");
		Option i = new Option("i", "intersection", false,
				"Show the intersection result.");
		Option j = new Option("j", "savetojson", false,
				"Save result to json file.");

		// facts-related options
		Option aux = new Option("aux", "auxiliary", false,
				"Generate auxiliary plain-string facts.");
		Option fuzzy = new Option("fuzzy", "fuzzy-names", false,
				"Using fuzzy names for entities.");
		Option ext = new Option("ext", "extractors", true,
				"Choose extractors: [dep|diff|hunk|cov]");
		Option versionize = new Option("ver", "write-version", true,
				"Append version string after facts");
		ext.setArgs(Option.UNLIMITED_VALUES);

		d.setRequired(false);
		p.setRequired(false);
		n.setRequired(false);
		t.setRequired(false);
		s.setRequired(false);
		q.setRequired(false);
		v.setRequired(false);
		l.setRequired(false);
		i.setRequired(false);
		j.setRequired(false);
		aux.setRequired(false);
		fuzzy.setRequired(false);
		ext.setRequired(false);
		versionize.setRequired(false);
		versionize.setOptionalArg(true);

		options2.addOption(d);
		options2.addOption(t);
		options2.addOption(p);
		options2.addOption(s);
		options2.addOption(q);
		options2.addOption(v);
		options2.addOption(n);
		options2.addOption(l);
		options2.addOption(i);
		options2.addOption(j);
		options2.addOption(aux);
		options2.addOption(fuzzy);
		options2.addOption(ext);
		options2.addOption(versionize);

		try {
			CommandLine c1 = new DefaultParser().parse(options1, args, true);

			if (c1.getOptions().length > 0) {
				if (c1.hasOption("info")) {
					displayVersionInfo();
				}

				if (c1.hasOption("help")) {
					// automatically generate the help statement
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp(
							"cslicer -c <CONFIG_FILE> -e <SLICING_ENGINE>",
							options2);
				}

			} else {
				CommandLine line = new DefaultParser().parse(options2, args);

				Path configPath = Paths.get(line.getOptionValue("config"));

				if (!FileUtils.fileExists(configPath.toString())
						|| !FilenameUtils.getExtension(configPath.toString())
								.equals("properties")) {
					PrintUtils.print(
							"The specified project configuration file path is not valid!",
							TAG.WARNING);
					System.exit(1);
				}

				if (line.hasOption("quiet"))
					PrintUtils.supressDebugMessages();

				StatsUtils.resume("total.time");

				ProjectConfiguration config = new ProjectConfiguration(
						configPath);
				config.setOutputHunkGraph(line.hasOption("print"));
				config.setEnableBuilderOutput(line.hasOption("verbose"));
				config.setSkipPicking(line.hasOption("nopick"));

				config.setEnableIntersection(line.hasOption("intersection"));
				config.setEnableJson(line.hasOption("savetojson"));

				if (line.getOptionValue("engine").equals("hunker")) {
					invokeHunker(line, config);
				} else if (line.getOptionValue("engine").equals("fact")) {
					invokeFacts(line, config, FactFormat.TA);
				} else if (line.getOptionValue("engine").equals("dl")) {
					invokeFacts(line, config, FactFormat.SOUFFLE);
				} else if (line.getOptionValue("engine").equals("covfacts")) {
					invokeCovFactsGenerator(line, config);
				} else if (line.getOptionValue("engine").equals("depsutil")) {
					computeDepsFacts(line, config);
				} else {
					PrintUtils.print("Invalid engine name!");
					System.exit(1);
				}
				StatsUtils.stop("total.time");
				StatsUtils.print();
			}

		} catch (ParseException |

				IOException e1) {
			PrintUtils.print(e1.getMessage(), TAG.WARNING);
			System.exit(0);
		}
	}

	private static void computeDepsFacts(CommandLine line, ProjectConfiguration config) {
		try {
			Hunker.computeDependencyFacts(config.getClassRootPath(), config.getDepsFactsFilePath(),
					config.getMD5ToPlainStringMapFilePath(), config.getfDepsPlainFilePath());
		} catch ( ClassPathInvalidException e) {
			e.printStackTrace();
		}
	}


    private static void invokeFacts(CommandLine line, ProjectConfiguration config, FactFormat factsFormat) {
		try {
			boolean fuzzy_opt = line.hasOption("fuzzy");
			boolean aux_opt = line.hasOption("aux");
			String versionize = null;
			if (line.hasOption("ver")) {
				versionize = line.getOptionValue("ver", "");
			}
			PrintUtils.print(versionize);
			HistoryAnalyzer runner;
			switch (factsFormat) {
				case TA:
					runner = new FactsExtr(config, aux_opt, fuzzy_opt);
					break;
				case SOUFFLE:
					runner = new DatalogFactsExtr(config, versionize, aux_opt, fuzzy_opt);
					break;
				default:
					runner = new FactsExtr(config, aux_opt, fuzzy_opt);
			}
			String[] extractors = line.getOptionValues("extractors");
			if (extractors == null) {
				runner.generateHunkDependencyFacts();
				runner.generateDependencyFacts();
				runner.generateDifferentialFacts();
				runner.generateCoverageFacts(config);
			} else {
				for (String ext : extractors) {
                    PrintUtils.print(String.format("Using extractor [%s]", ext),TAG.DEBUG); // dependency facts
					switch (ext) {
						case "hunk":
							runner.generateHunkDependencyFacts();
							break;
						case "dep":
							runner.generateDependencyFacts();
							break;
						case "diff":
							runner.generateDifferentialFacts();
							break;
						case "cov":
							runner.generateCoverageFacts(config);
							break;
						default:
                            PrintUtils.print(String.format("%s is not a valid extractor", ext), TAG.WARNING);
					}
				}
			}
		} catch (IOException | RepositoryInvalidException | CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException | ProjectConfigInvalidException
				| BranchNotFoundException | CoverageDataMissingException | ClassPathInvalidException
				| TestFailureException | CheckoutFileFailedException e) {
			e.printStackTrace();
		}
	}
	private static void invokeHunker(CommandLine line,
									 ProjectConfiguration config) {
		try {
			Hunker debugger = new Hunker(config, line.hasOption("aux"));
			PrintUtils.print("COMPUTING HUNK FACTS..."); // dependency facts
			PrintUtils.print(debugger.generateHunkDependencyFacts());
		} catch (IOException | RepositoryInvalidException
				| CommitNotFoundException | BuildScriptInvalidException
				| CoverageControlIOException | AmbiguousEndPointException
				| ProjectConfigInvalidException | BranchNotFoundException
				| CoverageDataMissingException e) {
			e.printStackTrace();
		}
	}
	private static void invokeCovFactsGenerator(CommandLine line,
												ProjectConfiguration config) {
		try {
			Hunker covGen = new Hunker(config, line.hasOption("aux"));
			PrintUtils.print("GENERATING Coverage FACTS..."); // coverage facts
			covGen.generateCoverageFacts(config);
		} catch (BuildScriptInvalidException | CoverageDataMissingException | CoverageControlIOException
				| BranchNotFoundException | TestFailureException | CommitNotFoundException | AmbiguousEndPointException
				| ProjectConfigInvalidException | RepositoryInvalidException | IOException e) {
			e.printStackTrace();
		}
	}



	private static void displayVersionInfo() throws IOException {

		System.out.println(CSlicer.PROJECT_NAME + " " + CSlicer.PROJECT_VERSION
				+ " (" + CSlicer.BUILD_NUMBER + "; " + CSlicer.BUILD_TIMESTAMP
				+ ")");
		System.out.println("Maven home: " + CSlicer.SYSTEM_MAVEN_HOME);
		System.out.println("Java home: " + CSlicer.SYSTEM_JAVA_HOME);
		System.out.println("Built on: " + CSlicer.OS_NAME + ", "
				+ CSlicer.OS_VERSION + ", " + CSlicer.OS_ARCH);
		System.out.println("Java version: " + CSlicer.JAVA_VERSION + ", "
				+ CSlicer.JAVA_VENDER);
	}

	private static boolean checkEnvVars() {
		if (System.getenv("JAVA_HOME") == null) {
			PrintUtils.print("Variable 'JAVA_HOME' is not set",
					PrintUtils.TAG.WARNING);
			return false;
		} else if (System.getenv("M2_HOME") == null) {
			PrintUtils.print("Variable 'M2_HOME' is not set",
					PrintUtils.TAG.WARNING);
			return false;
		}

		return true;
	}
}
