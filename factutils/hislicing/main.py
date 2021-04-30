#!/usr/bin/env python3
import argparse
import logging
import os
from pathlib import Path
from pprint import pprint

from hislicing import prepare
from hislicing import run_cslicer
from hislicing.benchmark import load_project_names, load_groups
from hislicing.find_diff import read_commits_from_cslicer_log
from hislicing.env_const import CSLICER_STANDALONE_OUTPUT_DIR
from util import init_logging

logger = logging.getLogger(__name__)


def main():
    args = handle_args()

    # get abspath first, since runJacoco call chdir()
    # if args.names:
    #     names_file = os.path.abspath(args.names)
    # if args.groups:
    #     groups_file = os.path.abspath(args.groups)
    if args.prepare:
        names_file = Path(args.prepare)
        project_names = load_project_names(names_file)
        # replace_cfg() must precede others
        logger.info("Preparation ONLY")
        prepare.replace_cfg(project_names)
        prepare.git_clone(project_names)
        prepare.prepare()
        logger.info("Preparation DONE")
    if args.cslicer:
        names_file = os.path.abspath(args.cslicer)
        project_names = load_project_names(names_file)
        logger.info("--cslicer enabled, running on all names given by --names")
        run_cslicer.run_names(project_names)
        root, _, f = next(os.walk(CSLICER_STANDALONE_OUTPUT_DIR))
        results = list(map(lambda x: read_commits_from_cslicer_log(os.path.join(root, x)),
                           filter(lambda y: y.endswith(".log"), f)))
        #for i in results:
        #    pprint(i.slicing_result())
        #    pprint(i.DROP)

    if args.fact:
        groups_file = os.path.abspath(args.fact)
        project_groups = load_groups(groups_file)
        pprint(project_groups)
        logger.info("--facts enabled, running on all groups given by --groups")
        run_cslicer.run_groups(project_groups)


def handle_args():
    parser = argparse.ArgumentParser(description="compute difference of two generated list of commits")
    parser.add_argument("--cslicer", metavar="NAMES", help="Run cslicer (old way) on a list of project names")
    parser.add_argument("--prepare", metavar="NAMES", help="Preparing work for a list of project names")
    parser.add_argument("--fact", metavar="GROUPS", help="Run cslicer with facts collection")
    parser.add_argument("--verify", action="store_true", help="verify results")
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == "__main__":
    main()
