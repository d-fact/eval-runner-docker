import os
from enum import Enum
from pathlib import Path

# SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))  # Dir of this script
BASE_DIR = "/data"  # change this to the path to CSlicer
BASE_DIR_IN_CFG = "/home/cgzhu/projects/gitslice"  # do not change this, this path is hardcoded in orig-config.
DOWNLOADS_DIR = BASE_DIR + '/_downloads'
POMS_DIR = BASE_DIR + '/resources/file-level/example-poms'
CONFIGS_DIR = BASE_DIR + '/resources/file-level/orig-configs'
NEW_CONFIGS_DIR = os.path.join(DOWNLOADS_DIR, "configs")
JACOCOS_DIR = BASE_DIR + '/resources/file-level/jacoco-files'

OUTPUT_DIR = os.path.join(BASE_DIR, "resources/file-level/output")
CSLICER_STANDALONE_OUTPUT_DIR = os.path.join(OUTPUT_DIR, 'cslicer')
CSLICER_FACTS_OUTPUT_DIR = os.path.join(OUTPUT_DIR, 'cslicer_facts')
FACTS_RESULTS_DIR = os.path.join(OUTPUT_DIR, "facts")
CSLICER_TIMING_FILE = os.path.join(OUTPUT_DIR, 'cslicer_time.json')
FACTS_TIMING_FILE = os.path.join(OUTPUT_DIR, 'fact_time.json')

DoSC_PATH = os.path.expanduser("~/Projects/DoSC")  # change this to the path to DoSC dataset
CSLICER_JAR_PATH = '/tool/gitslice/target/cslicer-1.0.0-jar-with-dependencies.jar'
REPO_DIR_SFX = "-fake"

GitPath = {
    'commons-compress': "https://github.com/apache/commons-compress",
    'commons-configuration': "https://github.com/apache/commons-configuration",
    'commons-csv': "https://github.com/apache/commons-csv",
    'commons-io': "https://github.com/apache/commons-io",
    'commons-lang': "https://github.com/apache/commons-lang",
    'commons-net': "https://github.com/apache/commons-net",
    'flume': "https://github.com/apache/flume",
    'maven': "https://github.com/apache/maven",
    # "calcite": "https://github.com/apache/calcite",
    # 'pdfbox': "https://github.com/apache/pdfbox"
}

Subjects = {
    "COMPRESS##99bc508##b29395d": "COMPRESS",
    "CONFIGURATION##89428f1##9fb4ad8": "CONFIG",
    "CSV##b230a6f5##7310e5c6": "CSV",
    "FLUME##cda3bd10##31d45f1b": "FLUME-1",
    "FLUME##f7560038##5e400ea8": "FLUME-2",
    "IO##8de491fc##b1b9f1af": "IO",
    "LANG##24767d6##76cc69c": "LANG",
    "MNG##b175144##308d4d4": "MAVEN-1",
    "MNG##b7e3ce2##ea8b2b0": "MAVEN-2",
    "NET##d483631##abd6711": "NET",
}


class FactFmt(Enum):
    fact = 1
    dl = 2
