#!/usr/bin/env python3
import argparse
import os
from collections import namedtuple
from pathlib import Path

from util import ErrorCode, init_logging
from hislicing.slicing_util import Cfg
from pprint import pprint
import logging
import json

logger = logging.getLogger(__name__)

RootPath = namedtuple("RootPath", ["old", "new"])
root_path = RootPath("/home/cgzhu/projects/gitslice", "/home/wuxh/Projects/gitslice")


# Functionality = namedtuple("Functionality", ["projectName", "funcNum"])
# TestCase = namedtuple("TestCase", ["projectName"])


class Benchmark:
    def __init__(self, name: str, func_num: str, history_start: str, history_end: str, slice_size: int = 0):
        self.projectName = name  # type: str
        self.funcNum = func_num  # type: str
        self.historyStart = history_start  # type: str
        self.historyEnd = history_end  # type: str
        self.sliceSize = slice_size  # type: int

    def config_name(self):
        return self.projectName + "-" + self.funcNum

    def only_test_diff(self, another) -> bool:
        """
        see if benchmarks are in the same project with same history range
        :return:
        """
        if not isinstance(another, Benchmark):
            return False
        return self.projectName == another.projectName and self.historyStart == another.historyStart and self.historyEnd == another.historyEnd

    def fullname_w_range(self) -> str:
        return f"{self.projectName}-{self.funcNum}--{self.historyStart}-{self.historyEnd}"

    @classmethod
    def split_fullname_to_tuple(cls, fullname) -> tuple:
        res = list()
        for x in fullname.split("--"):
            res.extend(x.split("-"))
        return tuple(res)

    def uniq_history_str(self) -> str:
        return f"{self.projectName}##{self.historyStart}##{self.historyEnd}"

    @classmethod
    def split_uniq_to_tuple(cls, uniq: str) -> tuple:
        return tuple(uniq.split("##"))

    def __str__(self):
        return f"{self.config_name()}, {self.historyStart}-{self.historyEnd}"

    def __repr__(self):
        return f"{self.config_name()}, {self.historyStart}-{self.historyEnd}"


def execTest():
    pass


def replacePOM():
    pass


def get_all_benchmark(selection_file: str) -> list:
    if not selection_file or not os.path.isfile(selection_file):
        logger.error(f"{selection_file} is not a file")
        exit(ErrorCode.PATH_ERROR)
    all_benchmarks = list()
    with open(selection_file, 'r') as in_file:
        for x in in_file.readlines():
            elements = x.strip("\n").split()
            benchmark_tuple = (*elements[0].split("-"), *elements[1:])
            all_benchmarks.append(Benchmark(*benchmark_tuple))
    return all_benchmarks


def find_existing_config_file(orig_config_dir: str, all_benchmarks: list, output_names: str, output_groups: str):
    if not os.path.isdir(orig_config_dir):
        logger.error(f"{orig_config_dir} does not exist or is not a dir")
        exit(ErrorCode.PATH_ERROR)
    existing_config = list()
    config_group = dict()
    for x in all_benchmarks:  # type: Benchmark
        name = x.config_name()
        config_file_name = os.path.join(orig_config_dir, name + ".properties")
        logger.debug(config_file_name)
        if os.path.isfile(config_file_name):
            with open(config_file_name, 'r') as config_file:
                config = Cfg._make(line.strip("\n").split("=")[1].strip() for line in config_file.readlines())
                if config.startCommit == x.historyStart and config.endCommit == x.historyEnd:
                    existing_config.append(name)
                    uniq_history = x.uniq_history_str()

                    if uniq_history in config_group:
                        config_group[uniq_history].append(x.funcNum)
                    else:
                        config_group[uniq_history] = [x.funcNum]
    with open(output_names, 'w') as of:
        json.dump(existing_config, of, indent=2)
    with open(output_groups, 'w') as of:
        json.dump(config_group, of, indent=2)
    return existing_config


def load_project_names(benchmark_list_file_name: Path) -> list:
    """
    load json file containing names generated by @func{find_existing_config_file}
    :param benchmark_list_file_name: json file name
    :return:
    """
    if not os.path.isfile(benchmark_list_file_name):
        logger.error(f"{benchmark_list_file_name} is not a valid file")
        exit(ErrorCode.INVALID_CMDLINE)
    with open(benchmark_list_file_name, 'r') as json_file:
        return json.load(json_file)


def load_groups(groups_json: str) -> dict:
    """
    load json file containing groups generated by @func{find_existing_config_file}
    :param groups_json: path of data file (in JSON)
    :return: a dict, with the string key in input converted to a tuple
    Input JSON format:
    {
      "CSV##b230a6f5##7310e5c6": [
        "159",
        "175",
        "179",
        "180"
      ]
    }
    Output will convert
      CSV##b230a6f5##7310e5c6
    to
      tuple("CSV", "b230a6f5", "7310e5c6")
    """
    if not os.path.isfile(groups_json):
        logger.error(f"{groups_json} is not found, abort")
        exit(ErrorCode.INVALID_CMDLINE)
    with open(groups_json, 'r') as json_file:
        contents = json.load(json_file)
        return {Benchmark.split_uniq_to_tuple(k): v for k, v in contents.items()}


def run():
    pass


def show():
    args = handle_args()
    benchmark = get_all_benchmark(args.s)
    if args.all_benchmark:
        pprint(str(benchmark))
    if args.existing_config:
        find_existing_config_file(args.c, benchmark, args.os, args.og)


def handle_args():
    parser = argparse.ArgumentParser(description="compute difference of two generated list of commits")
    parser.add_argument("-s", metavar="SEL_FILE", type=str, required=True)
    parser.add_argument("-c", metavar="ORIG_CFG_DIR", type=str)
    parser.add_argument("--os", metavar="OUT_NAMES_JSON", type=str, help="output json file for store benchmark names")
    parser.add_argument("--og", metavar="OUT_GROUP_JSON", type=str, help="output json file for store benchmark groups")
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    parser.add_argument("--all-benchmark", action="store_true")
    parser.add_argument("--existing-config", action="store_true")
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == "__main__":
    show()
