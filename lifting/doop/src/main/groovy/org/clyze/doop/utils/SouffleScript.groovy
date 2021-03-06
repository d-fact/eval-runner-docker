package org.clyze.doop.utils

import groovy.transform.TupleConstructor
import groovy.util.logging.Log4j
import org.clyze.doop.core.DoopAnalysisFactory
import org.clyze.utils.CheckSum
import org.clyze.utils.Executor
import org.clyze.utils.Helper

import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

import static org.apache.commons.io.FileUtils.deleteQuietly

@TupleConstructor
@Log4j
class SouffleScript {

	static final String EXE_NAME = "exe"

	Executor executor
	long compilationTime = 0L
	long executionTime = 0L

	long timer (Closure closure) {
		def start = System.currentTimeMillis()
		closure.call()
		return System.currentTimeMillis() - start
	}

	File interpret(File origScriptFile, File factsDir, File outDir, File cacheDir, int jobs = 8,
                 boolean profile = false, boolean debug = false,
                 boolean forceRecompile = true, boolean removeContext = false) {

		def scriptFile = File.createTempFile("gen_", ".dl", outDir)
		executor.execute("cpp -P $origScriptFile $scriptFile".split().toList()) { log.info it }

		def c1 = CheckSum.checksum(scriptFile, DoopAnalysisFactory.HASH_ALGO)
		def c2 = c1 + profile.toString()
		def checksum = CheckSum.checksum(c2, DoopAnalysisFactory.HASH_ALGO)
		def cacheFile = new File(cacheDir, checksum)

		forceRecompile = true
		if (!cacheFile.exists() || debug || forceRecompile) {

			if (removeContext) {
				def backupFile = new File("${scriptFile}.backup")
				Files.copy(scriptFile.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES)
				ContextRemover.removeContexts(backupFile, scriptFile)
			}

			def db = new File(outDir, "database")
			//deleteQuietly(db)
			db.mkdirs()

			//def executable = new File(outDir, EXE_NAME)
			//def compilationCommand = "souffle -c -o $executable $scriptFile".split().toList()
			def compilationCommand = "souffle $scriptFile -j$jobs -F$factsDir -D$db".split().toList()
			if (profile)
				compilationCommand << ("-p${outDir}/profile.txt" as String)
			if (debug)
				compilationCommand << ("-r${outDir}/report.html" as String)

			log.info "Compiling Datalog to C++ program and executable"
			log.info "Compilation command: $compilationCommand"

			def ignoreCounter = 0
			compilationTime = timer {
				executor.execute(compilationCommand) { String line ->
					if (ignoreCounter != 0) ignoreCounter--
					else if (line.startsWith("Warning: No rules/facts defined for relation") ||
							line.startsWith("Warning: Deprecated output qualifier was used")) {
						log.info line
						ignoreCounter = 2
					} else if (line.startsWith("Warning: Record types in output relations are not printed verbatim")) ignoreCounter = 2
					else log.info line
				}
			}

			try {
				// COPY_ATTRIBUTES: Keep execute permission
				//Files.copy(executable.toPath(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
			} catch (FileAlreadyExistsException e) {
				// If a cached file is already there, don't overwrite it
				// (it might be used by another analysis), just reuse it.
				//log.info (e.message)
			}

			log.info "Analysis compilation time (sec): $compilationTime"
			log.info "Caching analysis executable $checksum in $cacheDir"
		} else {
			log.info "Using cached analysis executable $checksum from $cacheDir"
		}
		return cacheFile
	}

	//def run(File cacheFile, File factsDir, File outDir,
	//        int jobs, long monitoringInterval, Closure monitorClosure = null, boolean profile = false) {

	//	def db = new File(outDir, "database")
	//	deleteQuietly(db)
	//	db.mkdirs()

	//	def executionCommand = "$cacheFile -j$jobs -F$factsDir -D$db".split().toList()
	//	if (profile)
	//		executionCommand << ("-p${outDir}/profile.txt" as String)

	//	log.debug "Execution command: ${executionCommand.join(" ")}"
	//	log.info "Running analysis"
	//	executionTime = Helper.timing {
	//		executor.enableMonitor(monitoringInterval, monitorClosure).execute(executionCommand).disableMonitor()
	//	}
	//	log.info "Analysis execution time (sec): $executionTime"

	//	return [compilationTime, executionTime]
	//}
}
