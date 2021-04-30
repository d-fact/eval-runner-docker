#!/usr/bin/env bash
CONFIG_FILE=$1
CSLICER_HOME="../gitslice"
CSLICER_JAR="$CSLICER_HOME/target/cslicer-1.0.0-jar-with-dependencies.jar"
export M2_HOME="/usr/share/maven/" 

ProgName=$(basename "$0")
function helpmsg() {
    printf "Usage: %s <CONFIG> <EXT>... [OPTIONS]\n" "$ProgName"
    printf "CONFIG  configuration file\n"
    printf "EXT can be following extractors:\n"
    printf "\thunk  hunk dependency\n"
    printf "\tdep   static dependency\n"
    printf "\tdiff  atomic changes\n"
    printf "\tcov   test coverage \n"
    printf "Example:\n"
    printf "\t%s ./CSV-159.properties hunk\n" "$ProgName"
    printf "OPTIONS\n"
    printf "\t-h, --help  show help message\n"
}

if [[ $# -lt 2 ]] ; then
    helpmsg
    exit 1
fi

if [[ ! -f "$CSLICER_JAR" ]]; then
	printf "File '%s' does not exist, please build the project using maven and set CSLICER_HOME in this script.\n" "$CSLICER_JAR"
	exit 2
fi

shift
java -jar "$CSLICER_JAR" -c "$CONFIG_FILE" -p -e fact -ext "$@"
