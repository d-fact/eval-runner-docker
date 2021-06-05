#!/usr/bin/env python3

# for running original doop on each version as baseline
import argparse
import logging
import time
from run_config import RunConfig, DirsTuple
from git import Repo
from util import init_logging, ErrorCode, check_dir, get_cur_time_str
from doop_util import one_step_run
from shared_util import store_stat, build_step

logger = logging.getLogger(__name__)


def main():
    args = handle_args()
    cfg = RunConfig(args.c, args.out)
    logger.debug(cfg)
    cfg.expect_flavor_or_exit("orig", "This script is for running original doop")

    repo = Repo(cfg.repo_path)
    outdirs: DirsTuple = cfg.out_dirs
    check_dir(str(outdirs.stat), make_if_not=True)
    time_dict = {"name": cfg.project_name, "flavor": "orig", "compile": -1, "doop": -1}

    if "compile" in cfg.steps:
        build_step(args.incr, cfg, repo, time_dict)

    sha1_digit: int = cfg.doop.sha1digit
    atype: str = cfg.doop.atype
    if "doop" in cfg.steps:
        start_time: float = time.time()
        for x in outdirs.jar.iterdir():
            if not x.is_file() or x.suffix != '.jar':
                logger.warning(f"Skip non-jar entry: {x}")
                continue
            abs_jar_path = x.resolve()
            doop_run_id: str = f"{cfg.project_name}_{abs_jar_path.stem[:sha1_digit]}_{get_cur_time_str()}"
            one_step_run(jar_path=abs_jar_path, atype=atype, doop_path=cfg.doop.path, doop_id=doop_run_id,
                         log_dir=outdirs.log, log_level=args.l)
        time_dict["doop"] = time.time() - start_time

    store_stat(outdirs.stat, time_dict)


def handle_args():
    parser = argparse.ArgumentParser(description='D-Pad driver: run original doop (baseline)')
    parser.add_argument("-c", metavar="config", type=str, required=True, help="configuration file")
    parser.add_argument('-l', metavar='loglevel', type=str, required=False, help='logging level, default WARNING')
    parser.add_argument('-o', '--out',
                        help='specify output path instead of auto-generation, useful when running single step ')
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == '__main__':
    main()
