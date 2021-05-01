#!/usr/bin/env python3
import argparse
import logging
import time

from util import init_logging, check_dir, ErrorCode
from pathlib import Path
import shutil
from hislicing.benchmark import Benchmark
import os
import json
from subprocess import run

logger = logging.getLogger(__name__)

EXEC_GROK_BASEDIR = "/data/grok-run"
tmp_facts_dir = os.path.join(EXEC_GROK_BASEDIR, "tmp_facts_dir")
grok_log_dir = os.path.join(EXEC_GROK_BASEDIR, "grok_logs")
grok_result_dir = os.path.join(EXEC_GROK_BASEDIR, "grok_results")


# grokRun ~/Projects/code_change/FuncDiff/scripts/grok_scripts/sliceWithoutVersionizedHash.ql  ~/.local/tmp/results/
def run_grok(grok_script: Path, grok_result: Path, facts_dir: Path, grok_log: str) -> float:
    grok_cmd = ["/tool/grokRun", grok_script, facts_dir, grok_result]

    grok_start_time = time.time()
    run(grok_cmd, stdout=open(grok_log, 'w'))
    grok_time = time.time() - grok_start_time
    return grok_time
    # grok_run = Popen(shlex.split(grok_cmd), stdout=PIPE, stderr=PIPE)
    # out, err = grok_run.communicate()


def prepare_tmp_dir():
    if os.path.exists(tmp_facts_dir):
        logger.error(f"{tmp_facts_dir} exists, abort")
        exit(ErrorCode.PATH_ERROR)


def prepare_misc_dir(dir_list):
    for each in dir_list:
        if not os.path.exists(each):
            os.mkdir(each)


def main():
    args = handle_args()
    prepare_tmp_dir()
    prepare_misc_dir([EXEC_GROK_BASEDIR, grok_log_dir, grok_result_dir])
    facts_dir = os.path.abspath(args.p)
    group_file = args.g
    output_file = args.o
    grok_script_path = Path(args.s)
    time_dict = dict()
    with open(group_file, 'r') as gf:
        groups = json.load(gf)  # type: dict
    for g, test_num_list in groups.items():
        sub_dir_path = os.path.join(facts_dir, g)
        shutil.copytree(sub_dir_path, tmp_facts_dir)
        project_name, h_start, h_end = Benchmark.split_uniq_to_tuple(g)
        for num in test_num_list:
            key = f"{project_name}-{num}--{h_start}-{h_end}"
            cov_dir_path = os.path.join(facts_dir, key)
            logger.debug(cov_dir_path)
            cov_files = os.listdir(cov_dir_path)
            logger.debug(cov_files)
            assert (len(cov_files) == 1)
            cov_f_name = cov_files[0]
            cov_f = os.path.join(cov_dir_path, cov_f_name)
            shutil.copy(cov_f, tmp_facts_dir)
            logger.debug(os.listdir(tmp_facts_dir))
            log_file = os.path.join(grok_log_dir, key + ".log")
            slicing_result = Path(grok_result_dir) / key
            run_time = run_grok(grok_script_path, grok_result=slicing_result, facts_dir=Path(tmp_facts_dir),
                                grok_log=log_file)
            # remove cov.ta for next testcase
            os.remove(os.path.join(tmp_facts_dir, cov_f_name))
            if g in time_dict:
                time_dict[g].update({num: run_time})
            else:
                time_dict[g] = {num: run_time}
        # clean tmp_facts_dir for next group
        shutil.rmtree(tmp_facts_dir)
    with open(output_file, 'w') as of:
        json.dump(time_dict, of, indent=2)


def handle_args():
    parser = argparse.ArgumentParser(description="compute difference of two generated list of commits")

    parser.add_argument("-p", required=True, metavar="FACTS_DIR",
                        help="Path to the directory of generated facts")
    parser.add_argument("-g", required=True, metavar="GROUP_FILE",
                        help="A JSON file listing all groups for easy processing")
    parser.add_argument("-s", required=True, metavar="GROK_SCRIPT",
                        help="Path of grok script")
    parser.add_argument("-o", metavar="OUTPUT_FILE", type=str)
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == "__main__":
    main()
