🚧 **Work In Progress**

--- 

# DiffBase


**DiffBase** is a framework for extracting, storing and supporting efficient querying and
manipulation of differential facts, a uniform exchangeable representation of information extracted
from software artifacts. Differential facts can be any information about software artifacts and
especially highlight their changes and linkage between versions along the software evolution.

We implement multiple software evolution management tasks with our tools to prove its usefulness and
efficiency.


#### Pre-requisites
+ Clone this repo `git clone https://github.com/d-fact/eval-runner-docker diffbase`
+ Docker (tested with version 20.10.5, recent version should also work)
+ docker-compose (tested with version 1.25.0)


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
```

There are multiple Dockerfiles inside this repo, for replicating the evaluations in the paper and
showing the reusability of components.

| Directories  | Descriptions                                      |
|--------------|---------------------------------------------------|
| gitslice     | Core fact extractors implemented based on CSlicer |
| ext-gitfacts | Git history facts extractor                       |
| factutils    | Scripts for running software evolution tasks      |

## Evaluation Replication
Assuming `docker` and `docker-compose` is installed properly and current workding directory is at
the root path of the cloned repo, we can run the following command to build the image for
evaluation.

```sh
docker-compose build -f docker-compose.yml
```

If successfully built, there would be two images built --- *diffbase_slicing-driver* and
*diffbase_rts-driver*. (They should be shown in the image list if issuing `docker image ls` on
cmdline, and their names are usually prefixed by the folder name. So you did not clone the repo as
`diffbase`, the image names can be different.)

### Evaluate Semantic Hisotry Slicing
```sh
docker-compose up slicing-driver
```

#### Inspect results
Outputs and intermediate data are in the data volume created during `docker-compose up
slicing-driver`.

Mountpoint of the volume can be checked using `docker volume inspect diffbase_datavol`. (If there is
no such data volume, you can check the correct name of the data volume using `docker volume ls`.) 
The path is `/var/lib/docker/volumes/diffbase_datavol/_data` on my system.

Following directories (under data volume) contain output.
+ `resources/file-level/output/facts`: facts generated 
+ `grok_run/grok_results`: slicing results, they should be same with results in `data/slicing-results` in this repo. 

### Evaluate Regression Test Selection

## Reusing Components
Besides the replication of evaluation in the paper, all the components in the artifacts can be
reused independently. This section will go through the details of usages of each components.

### Java Facts Extractor
There is a `Dockerfile` inside `gitslice` sub-directory.
```sh
cd gitslice && docker build . -t java-ext-standalone
docker run -it --rm -v /path/on/the/host:/data java-ext-standalone -c /data/project.properties -e fact -exp dep diff hunk
```

Replace `/path/on/the/host` with the directory where a `project.properties` and a repo exists.


### History Facts Extractor
Check [ext-gitfacts](ext-gitfacts) for details.
