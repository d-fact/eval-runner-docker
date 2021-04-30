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

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cslicer.utils.StatsUtils;

//E3. [TEST] set mmn to avoid split brain
//
//JUnit test: mvn clean test -Dtests.class=org.elasticsearch.cluster.allocation.AwarenessAllocationTests#testAwarenessZones
//Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
//Build and test execution: 1 min 10 s
//Target commit: 220842a1b01fe39ee264d38262fdaa831bf11def

public class TestReductionE3 {

	private ProjectConfiguration config;

	@Before
	public void setup() {
		config = new ProjectConfiguration()
				.setBuildScriptPath("/home/liyi/bit/elasticsearch/pom.xml")
				.setRepositoryPath("/home/liyi/bit/elasticsearch/.git")
				.setEndCommitId("220842a1b01fe39ee264d38262fdaa831bf11def")
				.setJacocoExecPath(
						"/home/liyi/Dropbox/Ideas/dep/testdata/elastic/e3/jacoco.exec")
				.setSourceRootPath("/home/liyi/bit/elasticsearch/src/main/java")
				.setClassRootPath("/home/liyi/bit/elasticsearch/target/classes")
				.setTouchSetPath("/tmp/touch-e3.txt");
	}

	@Ignore
	@Test
	public void testMedium() throws Exception {
		config.setAnalysisLength(100);
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	@Ignore
	@Test
	public void testShort() throws Exception {
		config.setAnalysisLength(50);
		Slicer ref = new Slicer(config);
		ref.doSlicing();
		StatsUtils.print();
	}

	// @Ignore
	@Test
	public void testLong() throws Exception {
		config.setAnalysisLength(150);
		Slicer ref = new Slicer(config);
		List<RevCommit> drop = ref.doSlicing().getPicks();
		StatsUtils.print();

		ref.verifyResultPicking(drop);
		ref.tryRevertFiles();
	}
}
