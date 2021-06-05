🚧 **Work In Progress**

--- 

# DiffBase


**DiffBase** is a toolset for extracting, storing and supporting efficient querying and manipulation
of *differential facts*, a uniform exchangeable representation of information extracted from
software development artifacts.  Differential facts can be any information about software artifacts
with a focus on their changes and tractability across versions throughout software evolution.
Multiple software evolution management tasks have been implemented with DiffBase, demonstrating its
usefulness and efficiency.


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

There are multiple Dockerfiles inside this repository, for replicating the evaluations in the paper
and showing the reusability of components.

| Files/Dirs       | Descriptions                                           |
|------------------|--------------------------------------------------------|
| gitslice         | Core fact extractors implemented based on CSlicer      |
| ext-gitfacts     | Git history facts extractor                            |
| factutils        | Scripts for running software evolution tasks           |
| diffbase.pdf     | Copy of the accepted paper                             |
| quick-slicing.sh | Script for building dockers and run slicing evaluation |
| quick-rts.sh     | Script for building dockers and run RTS evaluation     |


#### Pre-requisites
+ Clone this repository using: `git clone --depth 1 https://github.com/d-fact/eval-runner-docker
  diffbase`
+ Docker (tested with version 20.10.5, recent version should also work)
  - Official Docker Installation Guide at https://docs.docker.com/get-docker/ 
+ docker-compose (tested with version 1.25.0)
  - Installation Guide at https://docs.docker.com/compose/install/


## Scripts for Quick Start
The easiest way to reproduce the experiment results is to use the provided quick-start scripts.  We
provide two scripts for building and running docker containers, i.e., `quick-slicing.sh` and
`quick-rts.sh`, for reproducing the two experiments described in the paper, respectively.

You can also check the following section for detailed step-by-step explanations.

## Evaluation Replication
Assuming `docker` and `docker-compose` is installed properly and current working directory is at the
root path of the cloned repo, we can run the following command to build the image for evaluation.

```sh
docker-compose -f docker-compose.yml build 
```

After successful completion, there will be two images--- *diffbase_slicing-driver* and
*diffbase_rts-driver*. They should be shown in the image list, and their names are usually prefixed
by the folder name. So you did not clone the repo as `diffbase`, the image names can be different.

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

This takes about three minutes (on my machine with Xeon(R) E5-1650 v3, 16GB RAM and HDD).

#### Inspect results in data volume
The outputs and intermediate data are stored in the data volume created by `docker-compose up
slicing-driver`.

The mount point of the volume can be checked using `docker volume
inspect diffbase_datavol`. (If there is no such data volume, you can
check the correct name of the data volume using `docker volume ls`.)
A typical path looks like this: `/var/lib/docker/volumes/diffbase_datavol/_data`.

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

After `docker-compose up` finished and exited, the following
directories (under data volume) should contain the generated output.

#### Generated Facts
`resources/file-level/output/facts` is the directory for facts generated.
E.g., in `resources/file-level/output/facts/CSV##b230a6f5##7310e5c6` there are following files.
```sh
├── 20-deps.ta
├── 25-inherit.ta
├── 30-diff_tuple.ta
├── 40-diff_attr.ta
├── 50-hunkdep.ta
└── 90-test_classes.ta
```

E.g.`20-deps.ta` contains static dependencies, such as the following snippet. It contains following information:
+ the method `next()` calls `getNextRecord()` in the first anonymous inner class of `CSVParser`;
+ `next()` is a method;
+ `Token` is a class;
+ `Constants.TAB` is a field.

```
call "org.apache.commons.csv.CSVParser.1.next()" "org.apache.commons.csv.CSVParser.1.getNextRecord()"
CGNodeType Method "org.apache.commons.csv.CSVParser.1.next()"
CGNodeType Class "org.apache.commons.csv.Token"
CGNodeType Field "org.apache.commons.csv.Constants.TAB"
```

#### Slicing Results
`grok_run/grok_results` is the directory for slicing results. 
They should be same with results in `data/slicing-results` in this repo. 

E.g., `_data/grok_run/grok_results/` should contain the same set of commits with file `data/slicing-results/CSV-159--b230a6f5-7310e5c6.all`


#### Run Full Evaluation
To save time, we only run one group of subjects in the slicing evaluation. If you want to replicate
the evaluation on all subjects, please uncomment the command in `docker-compose.yml` and comment out the current one as shown below.

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

The last JSON is the percentage of testing methods selected by each tool.
They are the same in the example output since we only run one subject here.

To run all examples, remove all `--debug` in the following command in the `docker-compose.yml` file.

```
command: bash -c "
  tar xf /data/defects4j/rts-repos.tar.xz -C /data/defects4j/project_repos
  && python3 -m rts.main --debug -l info --alt-config rts/config/docker.cfg -f --ensure-all-change-types
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg -s /data/grok-scripts/rts5-imprecise.ql
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg -v --count-method-json /data/rts-exp/results.json
  && python3 -m rts.main --debug -l INFO --alt-config rts/config/docker.cfg --percent /data/rts-exp/results.json
  "
```

If you run the experiments on all subjects (without `--debug` options), the output should be something similar to the below.

```
facts-rts | {'Lang-28': 20, 'Lang-29': 1452, 'Lang-30': 1418, 'Lang-31': 1406, 'Lang-32': 62, 'Lang-33': 1387, 'Lang-34': 1387, 'Lang-35': 13, 'Lang-36': 143, 'Lang-37': 12, 'Lang-38': 59, 'Lang-39': 1337, 'Lang-40': 1287, 'Lang-41': 1191, 'Lang-42': 1640, 'Lang-43': 15, 'Lang-44': 22, 'Lang-45': 1637, 'Lang-46': 1585, 'Lang-47': 223, 'Lang-48': 1600, 'Lang-49': 27, 'Lang-50': 37, 'Lang-51': 1533, 'Lang-52': 1533, 'Lang-53': 53, 'Math-5': 1709, 'Math-6': 214, 'Math-7': 169, 'Math-8': 24, 'Math-9': 36, 'Math-10': 2968, 'Math-11': 10, 'Math-12': 1837, 'Math-13': 93, 'Math-14': 109, 'Math-15': 3413, 'Math-16': 3411, 'Math-17': 121, 'Math-18': 43, 'Math-19': 41, 'Math-20': 40, 'Math-21': 13, 'Math-22': 1495, 'Math-23': 25, 'Math-24': 24, 'Math-25': 7, 'Math-26': 2065, 'Math-27': 2065, 'Math-28': 27, 'Math-29': 4, 'Math-30': 3, 'Math-31': 1494, 'Math-32': 55, 'Math-33': 29, 'Math-34': 41, 'Math-35': 17, 'Math-36': 929, 'Math-37': 1114, 'Math-38': 40, 'Math-39': 101, 'Math-40': 177, 'Math-41': 376, 'Math-42': 27, 'Math-43': 210, 'Math-44': 156, 'Math-45': 799, 'Math-46': 989, 'Math-47': 989, 'Math-48': 161, 'Math-49': 65, 'Math-50': 161, 'Math-51': 161, 'Math-52': 33, 'Math-53': 913, 'Math-54': 66, 'Math-55': 34, 'Math-56': 5, 'Math-57': 4, 'Math-58': 8, 'Math-59': 1781, 'Math-60': 56, 'Math-61': 42, 'Math-62': 2, 'Math-63': 1869, 'Math-64': 113, 'Math-65': 151, 'Math-66': 1191, 'Math-67': 2, 'Math-68': 71, 'Math-69': 15, 'Math-70': 169, 'Math-71': 132, 'Math-72': 320, 'Math-73': 320, 'Math-74': 58, 'Math-75': 32, 'Math-76': 17, 'Math-77': 1051, 'Math-78': 132, 'Math-79': 1700, 'Math-80': 36, 'Math-81': 35, 'Math-82': 14, 'Math-83': 17, 'Math-84': 10, 'Math-85': 164, 'Math-86': 12, 'Math-87': 16, 'Math-88': 15, 'Math-89': 31, 'Math-90': 31, 'Math-91': 62, 'Math-92': 885, 'Math-93': 884, 'Math-94': 884, 'Math-95': 21, 'Math-96': 328, 'Math-97': 247, 'Math-98': 180, 'Math-99': 927, 'Math-100': 77, 'Math-101': 382, 'Math-102': 42, 'Math-103': 60, 'Math-104': 121, 'Time-1': 4192, 'Time-2': 4192, 'Time-3': 4189, 'Time-4': 863, 'Time-5': 4164, 'Time-6': 4149, 'Time-7': 4131, 'Time-8': 4122, 'Time-9': 4122, 'Time-10': 4106, 'Time-11': 11, 'Time-12': 4087, 'Time-13': 4067, 'Time-14': 4057, 'Time-15': 4045, 'Time-16': 4044, 'Time-17': 4034, 'Time-18': 4024, 'Time-19': 4022, 'Time-20': 28, 'Time-22': 4024, 'Time-23': 4022, 'Time-24': 4020, 'Time-25': 4004, 'Time-26': 4000}
facts-rts | Read existing data in /tool/factutils/rts/rts_data/defects4j-numbers.tex
facts-rts | {'Lang': 26, 'Math': 100, 'Time': 25}
facts-rts | {'Lang': 26, 'Math': 100, 'Time': 25}
facts-rts | {'Lang': 26, 'Math': 100, 'Time': 25}
facts-rts | {'Lang': 26, 'Math': 100, 'Time': 25}
facts-rts | {'clover': {'Lang': 0.10442886452133189,
facts-rts |             'Math': 0.09457395642337793,
facts-rts |             'Time': 1.0},
facts-rts |  'ekstazi': {'Lang': 0.26403920728983915,
facts-rts |              'Math': 0.12655773469592618,
facts-rts |              'Time': 0.47777247822555213},
facts-rts |  'fact': {'Lang': 0.47676445559736,
facts-rts |           'Math': 0.17560541259225215,
facts-rts |           'Time': 0.9899043252167427},
facts-rts |  'starts': {'Lang': 0.5392278477185252,
facts-rts |             'Math': 0.19836781254126096,
facts-rts |             'Time': 1.0}
```
#### Inspect selection results
Similar to the history slicing evaluation above, facts and results reside in the data volume after
the execution finishes.

Generated facts reside in `_data/run_grok/facts` and look similar to facts generated in the slicing evaluation above.

The selected test classes are shown under `_data/run_grok/grok_results/`. 
E.g., if run with `--debug` option, there would be file `Lang-28.affected` with following contents.

```
org.apache.commons.lang3.StringEscapeUtilsTest
org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest
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

