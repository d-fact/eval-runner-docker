🚧 **Work In Progress**

--- 

# DiffBase


**DiffBase** is a toolset for extracting, storing and supporting efficient querying and manipulation
of *differential facts*, a uniform exchangeable representation of information extracted from
software development artifacts.  Differential facts can be any information about software artifacts
with a focus on their changes and tractability across versions throughout software evolution.
Multiple software evolution management tasks have been implemented with DiffBase, demonstrating its
usefulness and efficiency.

A companion website with more detailed documentation can be found at: <https://d-fact.github.io/>.

#### Directory Structure
```
.
├──📦 Dockerfile
├──📦 docker-compose.yml
├──📂 data
├──📂 factutils
│  ├──📂 hislicing
│  ├──📂 rts
│  ├──📜 run_grok.py
│  └──📜 util.py
├──📂 gitslice
│  ├──📦 Dockerfile
│  ├──📜 pom.xml
│  ├──📂 src
│  └──📜 util.py
├──📂 ext-gitfacts
├──📂 grok-v107
├──📂 grok_scripts
└──📜 grokRun
└──📜 diffbase.pdf
```

There are multiple Dockerfiles inside this repository, for replicating the experiments described in
the paper and supporting the standalone reusable components.

| Files/Dirs       | Descriptions                                           |
|------------------|--------------------------------------------------------|
| gitslice         | Core fact extractors implemented based on CSlicer      |
| ext-gitfacts     | Git history facts extractor                            |
| factutils        | Scripts for running software evolution tasks           |
| diffbase.pdf     | Copy of the accepted paper                             |
| quick-slicing.sh | Script for building dockers and run slicing evaluation |
| quick-rts.sh     | Script for building dockers and run RTS evaluation     |
| clean.sh         | Remove built containers and destroy data volume        |


#### Pre-requisites
+ Clone this repository using: `git clone --depth 1 https://github.com/d-fact/eval-runner-docker
  diffbase`
+ Docker (tested with version 20.10.5, recent version should also work)
  - Official Docker Installation Guide at https://docs.docker.com/get-docker/ 
+ Docker Compose (tested with version 1.25.0)
  - Installation Guide at https://docs.docker.com/compose/install/


## Scripts for Quick Start
The easiest way to reproduce the experiment results is to use the provided quick-start scripts.  We
provide two scripts for building and running docker containers, i.e., `quick-slicing.sh` and
`quick-rts.sh`, for reproducing the two experiments described in the paper, respectively.

If users want to modify configurations and rebuild everything again, `clean.sh` can be used to 
remove images and destroy the data volume. Note that users can use `docker-compose up slicing-driver` 
or `docker-compose up rts-driver` directly without rebuilding if changes are only made to the 
`commands` in `docker-compose.yml`.

You can also check the following sections for detailed step-by-step explanations.

## Reproducing Experiment Results
Assuming `docker` and `docker-compose` are installed properly and current working directory is at the
root path of the cloned repository, we can run the following command to build the docker images for 
evaluation.

```sh
docker-compose -f docker-compose.yml build 
```

After successful completion, there will be two images--- *diffbase_slicing-driver* and
*diffbase_rts-driver*. They should be shown in the image list, and their names are usually prefixed
by the folder name. In case you did not clone the repo as `diffbase`, the image names can be different.

```sh
% docker image ls
REPOSITORY                 TAG      IMAGE ID
diffbase_rts-driver        latest   <some hash>
diffbase_slicing-driver    latest   <some hash>
```

### Evaluate Semantic Hisotry Slicing (Sec. 4.2)
```sh
docker-compose up slicing-driver
```

This takes about three minutes to finish (on a machine with Xeon(R) E5-1650 v3, 16GB RAM and HDD).

#### Inspect results in data volume
The outputs and intermediate data are stored in the data volume created by `docker-compose up
slicing-driver`.

The mount point of the volume can be checked using `docker volume inspect diffbase_datavol`. (If
there is no such data volume, you can check the correct name of the data volume using `docker volume
ls`.)  A typical path looks like this: `/var/lib/docker/volumes/diffbase_datavol/_data`.  

This directory can be directly accessed on Linux, but it requires some extra steps on macOS and
Windows.  Assuming the data volume is at the above path, we create a new Docker container with the
volume mounted: `docker run --rm -it -v /:/docker alpine:edge`. 
We can then access its contents within the spawned shell.
A detailed guide for inspecting data volumes on macOS/Windows can be found in this blog post:
<https://www.freshblurbs.com/blog/2017/04/16/inspect-docker-volumes-on-mac.html>.

The data volume contains the following sub-directories.
```
% tree -L 1 /var/lib/docker/volumes/diffbase_datavol/_data
.
├── defects4j
├── grok-scripts
├── json
├── resources
├── rts-configs
├── rts-repos
└── slicing-results
```

After `docker-compose up` finishes, exits, the generated outputs can be found within the 
corresponding directories given below.

#### Generated Facts
`resources/file-level/output/facts` is the directory for facts generated.
For example, there are following files in `resources/file-level/output/facts/CSV##b230a6f5##7310e5c6`.
```sh
├── 20-deps.ta
├── 25-inherit.ta
├── 30-diff_tuple.ta
├── 40-diff_attr.ta
├── 50-hunkdep.ta
└── 90-test_classes.ta
```

For example, the file `20-deps.ta` contains static dependencies, as is shown in the following snippet.

```
call "org.apache.commons.csv.CSVParser.1.next()" "org.apache.commons.csv.CSVParser.1.getNextRecord()"
CGNodeType Method "org.apache.commons.csv.CSVParser.1.next()"
CGNodeType Class "org.apache.commons.csv.Token"
CGNodeType Field "org.apache.commons.csv.Constants.TAB"
```

The above can be interpreted as follows.
+ The method `next()` calls `getNextRecord()` in the first anonymous inner class of `CSVParser`;
+ `next()` is a method;
+ `Token` is a class;
+ `Constants.TAB` is a field.

#### Slicing Results
The slicing results are stored in `grok_run/grok_results`. 
They should be the same as the ground truth in `data/slicing-results` in this repository. 

For example, `_data/grok_run/grok_results/` should contain the same set of commits as what
is in the file `data/slicing-results/CSV-159--b230a6f5-7310e5c6.all`.


#### Run Full Evaluation
By default, we only run one group of the subjects in the quick-start script to save time. 
If you want to replicate the evaluation on all subjects, please uncomment the command in
`docker-compose.yml` and comment out the current one as shown below.

```yml
command: bash -c "
  python3 -m hislicing.main --prepare /data/json/name_eval.json -l info
  && python3 -m hislicing.main --fact /data/json/group_eval.json -l info
  && python3 -m run_grok -p /data/resources/file-level/output/facts -g /data/json/group_eval.json -s /data/grok-scripts/slice_ver.ql -o slice.out -l info
  "
# command: bash -c "
# 	python3 -m hislicing.main --prepare /data/json/name_one.json -l info
# 	&& python3 -m hislicing.main --fact /data/json/group_one.json -l info
# 	&& python3 -m run_grok -p /data/resources/file-level/output/facts -g /data/json/group_one.json -s /data/grok-scripts/slice_ver.ql -o slice.out -l info
# 	"
```
	
--- 

### Evaluate Regression Test Selection (Sec. 4.3)
```sh
docker-compose up rts-driver
```

The following are expected output:
```
Recreating facts-rts ... done
Attaching to facts-rts
facts-rts | [    INFO] Start work on project Lang
facts-rts | [    INFO] Start on pair Lang-28
facts-rts | [    INFO] mvn test-compile
facts-rts | [    INFO] Currently @ 562c1b83fca4e23c3c855596420bc2ae8490e0e7
facts-rts | [    INFO] Grok on Lang-28
facts-rts | Temp .ql file will be in  /data/grok-scripts/rts5-imprecise.ql.mKmFQj0puYf8
facts-rts | Finished inserting getta()
facts-rts | Finished.
facts-rts | [    INFO] START: verify trigger tests on project [Lang]
facts-rts | [    INFO] => START: verify trigger tests on bug [Lang-28]
facts-rts | [    INFO] Read trigger tests from /data/defects4j/framework/projects/Lang/trigger_tests/28
facts-rts | [    INFO] Read grok output from /data/run_grok/grok_results/Lang-28.affected
facts-rts | [    INFO] [Lang-28] <- Check safety property of grok results.
facts-rts | [    INFO] [Lang-28] <- Count test methods of affected test classes.
facts-rts | [    INFO] [Lang-28] <- 2 affected classes
facts-rts | [    INFO] {'Lang-28': 20}
facts-rts | [    INFO] Read existing data in /tool/factutils/rts/rts_data/defects4j-numbers.tex
facts-rts | {'clover': {'Lang': 0.011841326228537596},
facts-rts |  'ekstazi': {'Lang': 0.011841326228537596},
facts-rts |  'fact': {'Lang': 0.011841326228537596},
facts-rts |  'starts': {'Lang': 0.011841326228537596}}
facts-rts exited with code 0
```

The dictionary at the end of the output is the percentage of testing methods selected by each tool.
They are the same in the example output since we only run one subject here and all tools report the
same results on this subject.

To run all examples, remove all `--debug` in the following command in the `docker-compose.yml` file.
And the output dictionary will contain data shown in Table 3 in the paper. 


```
command: bash -c "
  tar xf /data/defects4j/rts-repos.tar.xz -C /data/defects4j/project_repos
  && python3 -m rts.main --debug -l info --alt-config rts/config/docker.cfg -f --ensure-all-change-types
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg -s /data/grok-scripts/rts5-imprecise.ql
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg -v --count-method-json /data/rts-exp/results.json
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg --percent /data/rts-exp/results.json
  "
```

(Readers can also specify bug-ids they prefer to run by modifying the `get_projects(bool)` method in
`diffbase/factutils/rts/main.py`. Note that changes to source files require rebuilding docker
images.)


#### Inspect selection results
Similar to the history slicing evaluation above, facts and results reside in the data volume after
the execution finishes.

The generated facts reside in `_data/run_grok/facts` and look similar to facts generated in the slicing evaluation above.

The selected test classes are shown under `_data/run_grok/grok_results/`. 
E.g., if run with `--debug` option, there would be a file `Lang-28.affected` with following contents.

```
org.apache.commons.lang3.StringEscapeUtilsTest
org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest
```

And if run without `--debug` option (on all subjects), there would be one `.affected` file for each
bug-id. And you can check them against the expected full results in this repository at
[data/rts-results.tar](https://github.com/d-fact/eval-runner-docker/blob/main/data/rts-results.tar).

---

### Evaluate Lifting (Sec. 4.4)
Evaluation of lifting is under `lifting` sub-directory. Cd into `lifting` and build the docker image
as below. 

```sh
cd lifting
docker build . -t lifting-eval
```

After successful build, users can start container with entrypoint set as bash. 
```sh
docker run --rm -it --entrypoint bash lifting-eval
```

It will open a bash and there are `run-exp.sh` and `run-all.sh` in the current directory. By running
`run-exp.sh`, it will fetch a git repo, build it and run a pointer-analysis with doop using lifted
version as well as un-lifted version.

You can check the contents of two scripts [here](https://github.com/d-fact/eval-runner-docker/blob/main/lifting/dpad/driver/driver/run_exp.sh) and [here](https://github.com/d-fact/eval-runner-docker/blob/main/lifting/dpad/driver/driver/run_all.sh)

Users can inspect result in `.dpad/` sub-directory under each repo. E.g., for commons-csv, the
cloned repo should locate at `/lifting/.test/commons-csv` in the container. Then there are
`/lifting/.test/commons-csv/.dpad/fse21-eval/` containing generated facts, logs and time statistics
for the lifted version. and `/lifting/.test/commons-csv/.dpad/fse21-eval-orig` for the un-lifted
version.

```sh
/lifting/.test/commons-csv/.dpad/fse21-eval
├── bytecode
├── facts
├── logs
├── merged
└── stat
    └── step-time.json
```

And the `step-time.json` contains time statistics. Note that `-1` means that we are using
pre-compiled bytecode to reduce the time for review. And since the compile and collect stages are
the same between lifted and un-lifted versions, Table 4 in the paper are time comparison between
`merge` + `analysis` for lifted version and `doop` for un-lifted version.

```
{
  "name": "commons-csv",
  "flavor": "lifted",
  "compile": -1,
  "collect": 139.51318192481995,
  "merge": 0.20537519454956055,
  "analysis": 5.888551712036133
}
```

```
{
  "name": "commons-csv",
  "flavor": "orig",
  "compile": -1,
  "doop": 440.2297124862671
}
```
For space usage in Table 4 in the paper, users can use following command to check the size of facts size before and after merging.

```sh
% du -s facts
27332
% du -s merged
4876
```

---


## Reusing Components
Besides the replication of evaluation in the paper, all the components in the artifacts can be
reused independently. This section will go through the details of usages of each components.

This section is prepared for who want to use tools on their projects with customized scripts thus
require more things installed such as build tools for their projects (e.g., Maven and JDK) to go
through the steps.

### Java Facts Extractor


There is a `Dockerfile` inside `gitslice` sub-directory.
```sh
cd gitslice && docker build . -t java-ext-standalone
docker run -it --rm -v /path/on/the/host:/data java-ext-standalone -c /data/project.properties -e fact -exp dep diff hunk
```

Replace `/path/on/the/host` with the directory where a `project.properties` and a repo exist.

For example, you could clone `commons-csv` by `git clone https://github.com/apache/commons-csv` and put it at `/tmp/diffbase`. 
And put following configuration files at `/tmp/diffbase/csv.properties`.

```ini
# repo path
repoPath = /data/commons-csv/.git
# path to generated bytecode
classRoot = /data/commons-csv/target/classes
# older commit
startCommit = b230a6f5
# newer commit
endCommit = 7310e5c6
```

Then build commons-csv by `cd /tmp/diffbase/commons-csv && mvn test-compile` and run the following command:
```sh
docker run -it --rm -v /tmp/diffbase:/data java-ext-standalone -c "/data/csv.properties" -e fact -exp dep diff hunk
```
There will be a `.facts` directory containing generated facts, similar to the that in the evluation above.
Users can choose between `-e fact` and `-e dl` for different fact formats and `-exp dep diff hunk` for invoking different extractors.

```
usage:
-c,--config <arg>         Path to project configuration file.
-e,--engine <arg>         Select slicing engine: [dl|fact].
-ext,--extractors <arg>   Choose extractors: [dep|diff|hunk|cov]
```

### History Facts Extractor
Check [ext-gitfacts](ext-gitfacts) for details. Note that we have not provided a docker for this
extractor and user need rust/cargo toolchains to use it.

