#!/usr/bin/env python3
import argparse
import yaml
try:
    from yaml import CLoader as Loader, CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper
from util import init_logging, ErrorCode
from pathlib import Path
from hislicing.env_const import DoSC_PATH
from hislicing.slicing_util import search_file
from pprint import pprint
from hislicing import run_cslicer
from hislicing.benchmark import load_project_names, load_groups
import os
from hislicing import prepare
import logging

logger = logging.getLogger(__name__)


def main():
    args = handle_args()
    name = args.f
    dosc_data_path = f"{DoSC_PATH}/meta-data"
    found = search_file(dosc_data_path, f"{name}.yml")
    if not found:
        logger.error(f"{name} does not exist in {dosc_data_path}")
    else:
        with open(found, 'r') as yf:
            data = yaml.safe_load(yf)
            slice = data["history slice"]
            for x in sorted(slice):
                print(f"{x}")
            # print(sorted(slice))


def handle_args():
    parser = argparse.ArgumentParser(description="Read ground truth from DoSC dataset")
    parser.add_argument("-f", required=True, metavar="FUNCTIONALITY", help="List slice for a specific functionality")
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == "__main__":
    main()
