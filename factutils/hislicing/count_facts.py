#!/usr/bin/env python3
import argparse
import logging
from util import init_logging, check_dir
from hislicing.benchmark import Benchmark
import os
import json

logger = logging.getLogger(__name__)


class FactsNum:
    def __init__(self, deps, diff_tuple, diff_attr, hunkdep):
        self.deps = deps
        self.diff_tuple = diff_tuple
        self.diff_attr = diff_attr
        self.hunkdep = hunkdep

    def total(self):
        return self.deps + self.diff_attr + self.diff_tuple + self.hunkdep


def get_lines_of_facts(in_file: str) -> int:
    with open(in_file, 'r') as f:
        return len(f.readlines()) - 1


def main():
    args = handle_args()
    facts_dir = os.path.abspath(args.p)
    output_file = args.o
    result = dict()
    check_dir(facts_dir)
    logger.info(f"Check {facts_dir}")
    for sub_dir in os.listdir(facts_dir):
        sub_dir = os.path.join(facts_dir, sub_dir)
        if os.path.isdir(sub_dir):
            name = os.path.basename(sub_dir)  # type: str
            logger.info(f"Now in {name}")
            if r"##" in name:
                lines_dict = dict()
                for ta_file in os.listdir(sub_dir):
                    ta_file_path = os.path.join(sub_dir, ta_file)
                    assert (os.path.isfile(ta_file_path))
                    fact_type = os.path.splitext(ta_file)[0].split("-")[1]  # type: str
                    print(fact_type)
                    n_line = get_lines_of_facts(ta_file_path)  # type: int
                    lines_dict[fact_type] = n_line
                facts_num = FactsNum(**lines_dict)
                static_total = facts_num.total()
                if name in result:
                    result[name].update(lines_dict)
                else:
                    result[name] = lines_dict
                result[name]["static"] = static_total
            else:
                project_name, test_num, h_start, h_end = Benchmark.split_fullname_to_tuple(os.path.basename(sub_dir))
                key_name = f"{project_name}##{h_start}##{h_end}"
                for cov_ta_file in os.listdir(sub_dir):
                    cov_ta_path = os.path.join(sub_dir, cov_ta_file)
                    assert (os.path.isfile(cov_ta_path))
                    n_line = get_lines_of_facts(cov_ta_path)
                    lines_dict = {test_num: n_line}
                    if key_name in result:
                        result[key_name].update(lines_dict)
                    else:
                        result[key_name] = lines_dict
        else:
            logger.info(f"Skip file {sub_dir}")
    with open(output_file, 'w') as of:
        json.dump(result, of, indent=2)


def handle_args():
    parser = argparse.ArgumentParser(description="compute difference of two generated list of commits")

    parser.add_argument("-p", required=True, metavar="FACTS_DIR",
                        help="Path to the directory of generated facts")
    parser.add_argument("-o", metavar="OUTPUT_FILE", type=str)
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


if __name__ == "__main__":
    main()
