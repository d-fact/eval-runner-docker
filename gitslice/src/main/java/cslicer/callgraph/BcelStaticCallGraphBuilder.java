package cslicer.callgraph;

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

import cslicer.callgraph.ClassVisitor.DependencyLevel;
import cslicer.utils.PrintUtils;
import cslicer.utils.PrintUtils.TAG;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BcelStaticCallGraphBuilder extends StaticCallGraphBuilder {

	private StaticCallGraph fCallGraph;
	private Set<String> fRootClasses;
	private Set<String> fScope;
	// build a map from class name to BCEL class instance
	private Map<String, JavaClass> fNameToClassMap;

	private Map<String, StaticCallGraph> fNameToCallGraph;

	public BcelStaticCallGraphBuilder() {
		fClassPath = null;
		fCallGraph = new StaticCallGraph();
		fNameToClassMap = new HashMap<>();
		fNameToCallGraph = new HashMap<>();
	}

	public BcelStaticCallGraphBuilder(List<String> classPath)
			throws ClassPathInvalidException {
		this();

		fClassPath = new LinkedList<File>();

		for (String path : classPath) {
			File dir = FileUtils.getFile(path);
			if (!dir.exists() || !dir.isDirectory())
				throw new ClassPathInvalidException(path);

			fClassPath.add(dir);
		}
		fRootClasses = null;
		fScope = null;
	}

	public BcelStaticCallGraphBuilder(List<String> classPath, Set<String> scope)
			throws ClassPathInvalidException {
		this(classPath);
		fScope = scope;
	}

	public BcelStaticCallGraphBuilder(String classPath)
			throws ClassPathInvalidException {
		this(classPath, null);
	}

	public BcelStaticCallGraphBuilder(String classPath, Set<String> scope)
			throws ClassPathInvalidException {
		this(Arrays.asList(classPath), scope);
	}

	private String classPathToString(List<File> fClassPath) {
		String res = "";
		if (fClassPath != null) {
			for (File p : fClassPath) {
				res += p.getAbsolutePath();
				res += ":";
			}
		}
		return res;
	}

	public void buildCallGraph() {
		// buildCallGraph(fRootClasses, DependencyLevel.WHOLE_CLASS);
		buildCallGraph(null, DependencyLevel.WHOLE_CLASS);
	}

	public void buildClassLevelCallGraph() {
		buildCallGraph(fRootClasses, DependencyLevel.IGNORE_FIELDS_METHODS);
	}

	public void buildPartialCallGraph(Set<String> includes,
			Set<String> excludes) {
		incrementalCallGraph(includes, excludes);
	}

	private void incrementalCallGraph(Set<String> includes,
			Set<String> excludes) {
		// fCallGraph = new StaticCallGraph(fScope);
		int counter = 0;

		Set<String> changed = new HashSet<>();
		changed.addAll(includes);
		changed.addAll(excludes);

		try {
			for (File cpath : fClassPath) {
				for (File entry : FileUtils.listFiles(cpath,
						(String[]) Arrays.asList("class").toArray(), true)) {

					// skip unchanged class files
					String shortName = entry.getName().replace(".class", "")
							.replace("$", ".");
					if (!filePossiblyChanged(shortName, changed))
						continue;

					// processing possibly changed class file
					PrintUtils.print((counter++) + " Reading class file: "
							+ entry.getName());

					ClassParser cparser = new ClassParser(
							entry.getAbsolutePath());
					JavaClass clazz = cparser.parse();

					String className = clazz.getClassName().replace("$", ".");
					// fNameToClassMap.put(className, clazz);
					// Repository.addClass(clazz);

					if ((includes.contains(className)
							|| excludes.contains(className))) {

						if (fNameToCallGraph.containsKey(className))
							fCallGraph.removeEdges(
									fNameToCallGraph.get(className).getEdges());

						if (includes.contains(className)) {
							StaticCallGraph modifiedGraph = new StaticCallGraph(
									fScope);
							// Only Definer is using this. No soundness is
							// required. So class-level call graph is fine.
							ClassVisitor visitor = new ClassVisitor(clazz,
									modifiedGraph,
									DependencyLevel.IGNORE_FIELDS_METHODS);
							visitor.start();

							fNameToCallGraph.put(className, modifiedGraph);
							fCallGraph.insertEdges(modifiedGraph.getEdges());
						}
					}
				}
			}
		} catch (IOException e) {
			PrintUtils.print(
					"Error while processing classpath: " + e.getMessage(),
					TAG.WARNING);
			e.printStackTrace();
		}

	}

	private boolean filePossiblyChanged(String shortName, Set<String> changed) {
		for (String c : changed)
			if (c.contains(shortName))
				return true;
		return false;
	}

	interface Predicate<JavaClass> {
		boolean test(JavaClass clazz);
	}

	/**
	 * Return an array list of class names, with ability to filter and
	 * convert them to Type X.
	 * @param tester A tester for filter classes, only those satisfying tester
	 *               would be kept
	 * @param transformer A transformer for transform a JavaClass to an object
	 *                    in type X
	 * @return An ArrayList of classes represented in type X
	 */
	public <X> ArrayList<X> getClasses(Predicate<JavaClass> tester,
									   Function<JavaClass, X> transformer) {
		ArrayList<X> classes = new ArrayList<>();
		int counter = 0;
		try {
			PrintUtils.print("Scanning class files ...", TAG.OUTPUT);

			for (File cpath : fClassPath) {
				for (File entry : FileUtils.listFiles(cpath,
						(String[]) Arrays.asList("class").toArray(), true)) {
					PrintUtils.print((counter++) + " Reading class file: "
							+ entry.getName());

					ClassParser cparser = new ClassParser(
							entry.getAbsolutePath());
					JavaClass clazz = cparser.parse();

					if (tester.test(clazz)) {
						X each = transformer.apply(clazz);
						classes.add(each);
					}
				}
			}
		} catch (IOException e) {
			PrintUtils.print(
					"Error while processing classpath: " + e.getMessage(),
					TAG.WARNING);
			e.printStackTrace();
		}
		return classes;
	}

	String getClassNameWithoutDollarSymbol(JavaClass c) {
		return c.getClassName().replace("$", ".");
	}

	public ArrayList<String> getClassNames() {
		return this.<String>getClasses(c->true, this::getClassNameWithoutDollarSymbol);
	}

	public ArrayList<String> getAbstractClasses() {
		return this.getClasses(AccessFlags::isAbstract, this::getClassNameWithoutDollarSymbol);
	}

	/**
	 * Return a HashMap encoding class inheritance information.
	 * It uses the getClasses() generic method to get a ArrayList first.
	 * @return a hashmap containing (subclass, superclass) pairs
	 */
	public HashMap<String, String> getInheritPairs() {
		HashMap<String, String> result = new HashMap<>();
	    ArrayList<Pair<String, String>> pairList = this.getClasses(c->true,
				c-> new ImmutablePair<>(getClassNameWithoutDollarSymbol(c), c.getSuperclassName()));
	    pairList.forEach(x-> result.put(x.getLeft(), x.getRight()));
	    return result;
	}

	@Deprecated
	public void getMoreClassInfo(HashMap<String, String> classParents, ArrayList<String> abstractClazz) {
		int counter = 0;
		try {
			PrintUtils.print("Scanning class files ...", TAG.OUTPUT);
			for (File cpath : fClassPath) {
				for (File entry : FileUtils.listFiles(cpath,
						(String[]) Arrays.asList("class").toArray(), true)) {
					PrintUtils.print((counter++) + " Reading class file: "
							+ entry.getName());
					ClassParser cparser = new ClassParser(
							entry.getAbsolutePath());
					JavaClass clazz = cparser.parse();
					String clazzName = clazz.getClassName().replace("$", ".");
					String superClassName = clazz.getSuperclassName();
					classParents.put(clazzName, superClassName);
					if (clazz.isAbstract()) {
						abstractClazz.add(clazzName);
					}
				}
			}
		} catch (IOException e) {
			PrintUtils.print(
					"Error while processing classpath: " + e.getMessage(),
					TAG.WARNING);
			e.printStackTrace();
		}
	}

	/**
	 * Returns a hashmap from nested class name to its outer class (which must
	 * not be an nested class).
	 * It will split the nested class name by first $ appearance and check if
	 * it is in nonNestedClasses set.
	 * @return the hashmap containing (nested class name, outer class name), in
	 * return value, dollar symbols ($) are replaced with dots (.)
	 */
	public HashMap<String, String> getNestedClass() {
		HashMap<String, String> nestedMap = new HashMap<>();
		HashSet<String> nonNestedClasses =  new HashSet<>(this.<String>getClasses(c -> !c.isNested(), JavaClass::getClassName));
		ArrayList<String> nestedClasses =  this.<String>getClasses(JavaClass::isNested, JavaClass::getClassName);

		for (String cs: nestedClasses) {
			String outerName = cs.split("\\$")[0];
			if (nonNestedClasses.contains(outerName)){
				nestedMap.put(cs.replace("$", "."), outerName);
			}
		}
		return nestedMap;
    }

	private void buildCallGraph(Set<String> entryClassNames,
			DependencyLevel level) {
		// ClassPath cp = new ClassPath(classPathToString(fClassPath));
		// Repository.setRepository(SyntheticRepository.getInstance(cp));

		fCallGraph = new StaticCallGraph(fScope);
		int counter = 0;

		try {
			PrintUtils.print("Scanning class files ...", TAG.OUTPUT);

			for (File cpath : fClassPath) {
				for (File entry : FileUtils.listFiles(cpath,
						(String[]) Arrays.asList("class").toArray(), true)) {
					PrintUtils.print((counter++) + " Reading class file: "
							+ entry.getName());

					ClassParser cparser = new ClassParser(
							entry.getAbsolutePath());
					JavaClass clazz = cparser.parse();

					String className = clazz.getClassName().replace("$", ".");
					fNameToClassMap.put(className, clazz);
					// Repository.addClass(clazz);
				}
			}
		} catch (IOException e) {
			PrintUtils.print(
					"Error while processing classpath: " + e.getMessage(),
					TAG.WARNING);
			e.printStackTrace();
		}

		Queue<String> workList = new LinkedList<String>();
		if (entryClassNames != null)
			workList.addAll(entryClassNames);
		else
			workList.addAll(fNameToClassMap.keySet());

		Set<String> done = new HashSet<String>();

		int i = 0;

		// build each class graph individually
		while (!workList.isEmpty()) {
			String next = workList.poll();

			if (!fNameToClassMap.containsKey(next))
				continue;

			StaticCallGraph classGraph = new StaticCallGraph(fScope);
			ClassVisitor visitor = new ClassVisitor(fNameToClassMap.get(next),
					classGraph, level);
			visitor.start();
			done.add(next);

			fNameToCallGraph.put(next, classGraph);
			// merge class graph into whole application graph
			fCallGraph.insertEdges(classGraph.getEdges());

			PrintUtils.print(done.size() + ": Done: " + next);
			PrintUtils.printProgress("Processing callgraph: ",
					i++ * 100 / counter);

			Set<String> successor = fCallGraph
					.getTransitiveSuccessorNames(next);
			successor.removeAll(done);
			successor.removeAll(workList);
			successor.retainAll(fNameToClassMap.keySet());
			workList.addAll(successor);
		}
	}

	public StaticCallGraph getCallGraph() {
		return fCallGraph;
	}

	@Override
	public void loadCallGraph(String path) {
		loadCallGraphShallow(path);
	}

	@SuppressWarnings("unused")
	private void loadCallGraphDeep(String path) {
		try {
			FileInputStream inputFileStream = new FileInputStream(path);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					inputFileStream);

			fCallGraph = (StaticCallGraph) objectInputStream.readObject();
			objectInputStream.close();
			inputFileStream.close();
			PrintUtils.print("Call graph is loaded from " + path);
		} catch (ClassNotFoundException | IOException e) {
			PrintUtils.print("Read call graph failed!", TAG.WARNING);
			e.printStackTrace();
		}
	}

	private void loadCallGraphShallow(String path) {
		fCallGraph = new StaticCallGraph(path);
	}

	@Override
	public void saveCallGraph(String path) {
		saveCallGraphShallow(path);
	}

	@SuppressWarnings("unused")
	private void saveCallGraphDeep(String path) {
		try {
			FileOutputStream fileOut = FileUtils
					.openOutputStream(FileUtils.getFile(path));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);

			out.writeObject(fCallGraph);
			out.close();
			fileOut.close();
			PrintUtils.print("Serialized call graph is saved in " + path);
		} catch (IOException e) {
			PrintUtils.print("Save call graph failed!", TAG.WARNING);
			e.printStackTrace();
		}
	}

	private void saveCallGraphShallow(String path) {
		fCallGraph.outputDOTFile(path);
	}

	public void setRootClasses(Set<String> rootClasses) {
		fRootClasses = rootClasses;
	}

	public void setScope(Set<String> scope) {
		fScope = scope;
		fCallGraph.setScope(scope);
	}
}
