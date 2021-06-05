#!/usr/bin/env python3
"""
Run pipeline with ddlog.
+ build jars for a given repo and a list of commits
+ collect facts on all n versions using doop -Xstop-at-facts
+ convert base version doop facts into ddlog style
+ generate ddlog's insert/delete statements for (n-1) consecutive pairs
+ running ddlog -i program < input.dl on the first version
+ running ddlog incrementally using generated insert/delete statements
"""
import argparse
import logging
from run_config import RunConfig, DirsTuple
from git import Repo, Commit
from typing import List
from util import init_logging, ErrorCode, check_dir, get_cur_time_str
import time
from pathlib import Path
from shared_util import store_stat, build_step, collect_step, facts_diff_ddlog, ddlog_analysis
from commits_util import interp_rev_range
from doop_util import facts_dir_naming
import contrib.souffle_converter as sconvert

logger = logging.getLogger(__name__)


def main():
    args = handle_args()
    cfg = RunConfig(args.c, args.out)
    logger.debug(cfg)
    # cfg.expect_flavor_or_exit("orig", "This script is for running original doop")

    repo = Repo(cfg.repo_path)
    outdirs: DirsTuple = cfg.out_dirs
    check_dir(str(outdirs.stat), make_if_not=True)
    time_dict = {"name": cfg.project_name, "flavor": "orig", "compile": -1}

    if "compile" in cfg.steps:
        build_step(args.incr, cfg, repo, time_dict)

    if "collect" in cfg.steps:
        collect_step(cfg, time_dict)

    if "convert-base-facts" in cfg.steps:
        # convert facts of 1st version in outdirs.fact
        check_dir(str(cfg.out_dirs.ddlog), make_if_not=True)
        cvtopt = sconvert.ConversionOptions()
        cvtopt.skip_logic = True
        cvtopt.input_facts = str(get_first_ver_facts(cfg, repo))
        logger.info(f"Convert from doop program @ {cfg.doop.program}")
        logger.info(f"Using facts in {cvtopt.input_facts}")
        start_time: float = time.time()
        sconvert.convert(str(cfg.doop.program), str(cfg.out_dirs.ddlog/"converted"), cvtopt)
        time_dict["convert-base-facts"] = time.time() - start_time

    if "ddlog-base-version" in cfg.steps:
        # run converted facts of earliest version against
        # partial evaluated analysis program
        start_time: float = time.time()
        ddlog_analysis(cfg)
        time_dict["ddlog-base-version"] = time.time() - start_time

    if "facts-diff" in cfg.steps:
        diff_bin = Path(cfg.facts_diff.get("diff_bin")).expanduser()
        facts_diff_ddlog(outdirs.facts, diff_bin, outdirs.log)

    if "ddlog-incr" in cfg.steps:
        pass
    if "all" in cfg.steps:
        pass
    store_stat(outdirs.stat, time_dict)


def get_first_ver_facts(cfg: RunConfig, repo) -> Path:
    commits: List[str] = interp_rev_range(repo, cfg.rev_range)
    return cfg.out_dirs.facts / commits[0]


def handle_args():
    parser = argparse.ArgumentParser(description='D-Pad driver: program analysis with doop and ddlog')
    parser.add_argument("-c", metavar="config", type=str, required=True, help="configuration file")
    parser.add_argument('-l', metavar='loglevel', type=str, required=False, help='logging level, default WARNING')
    parser.add_argument('-o', '--out', help='specify output path')
    parser.add_argument('--incr', action="store_true", help='override configuration, generate incremental jar')
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == '__main__':
    main()
