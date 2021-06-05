#!/usr/bin/env python3
import argparse
import logging
import time
from functools import partial
from git import Repo, Commit
from util import init_logging, ErrorCode, check_dir, get_cur_time_str
from typing import List
from run_config import RunConfig
from multiprocessing import Pool
from build import step_compile
from pathlib import Path
from gumtree import run_gumtree
import doop_util
from shared_util import store_stat, build_step, collect_step, merge_facts

logger = logging.getLogger(__name__)


def main():
    args = handle_args()
    cfg = RunConfig(args.c, args.out)
    cfg.log_level = args.l
    logger.debug(cfg)
    cfg.expect_flavor_or_exit("lifted", "This script is for running doop of lifted version")
    repo = Repo(cfg.repo_path)
    outdirs = cfg.out_dirs
    check_dir(str(outdirs.stat), make_if_not=True)
    time_dict = {"name": cfg.project_name, "flavor": "lifted", "compile": -1, "collect": -1, "merge": -1,
                 "analysis": -1}

    if "gumtree" in cfg.steps:
        check_dir(str(outdirs.gumtree), make_if_not=True)
        commit_list: List[str] = [str(c) for c in Commit.iter_items(repo, cfg.rev_range)]
        run_gumtree(commit_list, repo, outdirs.gumtree)

    if "compile" in cfg.steps:
        build_step(args.incr, cfg, repo, time_dict)

    if "collect" in cfg.steps:
        collect_step(cfg, time_dict)

    if "merge" in cfg.steps:
        merger_bin = Path(cfg.merge.get("merger_bin")).expanduser()
        start_time: float = time.time()
        merge_facts(outdirs.facts, dest=outdirs.merged, rm_dup=True, merge_bin=merger_bin, log_dir=outdirs.log)
        time_dict["merge"] = time.time() - start_time

    if "analysis" in cfg.steps:
        doop_run_id = f"{cfg.project_name}_Start_after_Facts_{get_cur_time_str()}"
        start_time = time.time()
        doop_util.analysis(outdirs.merged, cfg.doop.atype, cfg.doop.path, open_analysis=True,
                           results_storage=outdirs.analysis, doop_id=doop_run_id, log_dir=outdirs.log,
                           log_level=args.l)
        time_dict["analysis"] = time.time() - start_time

    store_stat(outdirs.stat, time_dict)


def handle_args():
    parser = argparse.ArgumentParser(description='D-Pad driver: utilities for program analysis on multiple versions')
    parser.add_argument("-c", metavar="config", type=str, required=True, help="configuration file")
    parser.add_argument('-l', metavar='loglevel', type=str, required=False, help='logging level, default WARNING')
    parser.add_argument('--incr', action="store_true", help='override configuration, generate incremental jar')
    parser.add_argument('-o', '--out',
                        help='specify output path instead of auto-generation, useful when running single step ')
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == '__main__':
    main()
