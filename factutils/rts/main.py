#!/usr/bin/env python3
from typing import Dict, List, Set, Tuple
from pathlib import Path
import argparse
import os
import shutil
import shlex
import logging
import re
import csv
import json
import subprocess
from collections import namedtuple
from hislicing.run_cslicer import collect_deps_diff_facts
from run_grok import run_grok, prepare_misc_dir
from rts import testinfo, extract_data
from util import init_logging, ErrorCode, deprecation, restore_clean_repo
from pprint import pprint

logger = logging.getLogger(__name__)

# BugInfo=namedtuple("BugInfo", bugid, rev_bug, rev_fix)
RevPair = namedtuple("RevPair", ["rev_bug", "rev_fix"])

BASE_PATH = Path("~/Projects/defects4j").expanduser()
PROJECTS_DATA_PATH = BASE_PATH / "framework/projects"
CONFIGS_PATH = Path("~/Projects/rts-exp/configs").expanduser()

EXEC_GROK_BASEDIR = Path("~/.local/tmp/run_grok").expanduser()
FACTS_PATH = EXEC_GROK_BASEDIR / "facts"
GROK_LOG_DIR = Path(EXEC_GROK_BASEDIR) / "grok_logs"
GROK_RESULT_DIR = Path(EXEC_GROK_BASEDIR) / "grok_results"

mvn_test_output = re.compile(r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)")


# FACTS_PATH = Path("~/Projects/rts-exp/facts").expanduser()


def get_projects(debug: bool = True):
    if debug:
        return {
            "Lang": {
                "local_repo": "commons-lang",
                "bug_id": [28]
            }
        }
    else:
        return {
            "Lang": {
                "local_repo": "commons-lang",
                "bug_id": list(range(28, 54))
            },
            "Math": {
                "local_repo": "commons-math",
                "bug_id": list(range(5, 105))
                # "bug_id": list(filter(lambda x: x != 97, range(5, 105)))
            },
            "Time": {
                "local_repo": "joda-time",
                "bug_id": list(filter(lambda x: x != 21, range(1, 27)))
            }
        }


def get_rev_id(csv_data_file: Path, bug_ids: List[int]) -> Dict[int, RevPair]:
    bugs = {}
    bug_id_set: Set[int] = set(bug_ids)
    with csv_data_file.open() as f_csv:
        csvreader = csv.reader(f_csv, delimiter=',')
        _ = next(csvreader)  # ignore header
        for row in csvreader:
            bid: int = int(row[0])
            if bid in bug_id_set:
                bugs[bid] = RevPair(row[1], row[2])
        return bugs


def get_class_from_testname(clazz: str) -> str:
    """
    get the test class from the full test method name
    :param clazz: e.g. org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest::testSupplementaryUnescaping
    :return: e.g. org.apache.commons.lang3.text.translate.NumericEntityUnescaperTest
    """
    return clazz[:clazz.rfind("::")]


def get_trigger_tests(data_file: Path) -> List[str]:
    """
    Get the list of trigger tests from data_file
    :param data_file: path to the trigger_tests stacktrace file,
        usually projects/ProjectId/trigger_tests/bid
    :return: the list of trigger tests
    """
    with data_file.open() as df:
        logger.info(f"Read trigger tests from {data_file}")
        return [line[4:].strip() for line in filter(lambda x: x.startswith("--- "), df)]


def get_trigger_tests_path(project_id: str, bid: int) -> Path:
    """
    Given the project id and bug id, return path to the file containing stack traces
    :param project_id: e.g. Lang
    :param bid: e.g. 28 in bug Lang-28
    :return: the path to the file containing trigger test names and stack traces
    """
    return PROJECTS_DATA_PATH / project_id / "trigger_tests" / str(bid)


def get_relevant_tests(data_file: Path) -> List[str]:
    with data_file.open() as df:
        logger.info(f"Read relevant test classes from {data_file}")
        return [row.strip() for row in df]


def get_relevant_tests_path(project_id: str, bid: int) -> Path:
    """
    Given the project id and bug id, return path to the file containing relevant tests
    :param project_id: e.g. Lang
    :param bid: e.g. 28 in bug Lang-28
    :return: the path to the file containing relevant tests, one test on each line
    """
    return PROJECTS_DATA_PATH / project_id / "relevant_tests" / str(bid)


def verify_relevant_tests(projects: dict):
    for p, v in projects.items():
        logger.info(f"Start: verify relevant tests on project {p}")
        for bid in v["bug_id"]:
            data_file = get_relevant_tests_path(p, bid)
            expected: set = set(get_relevant_tests(data_file))
            actual: set = read_test_classes_from_grok_result(GROK_RESULT_DIR / f"{p}-{bid}.affected")
            logger.info(f"[{p}-{bid}] <- Verify relevant tests.")
            # print(f"Expected:\n{expected}")
            # print(f"Actual:\n{actual}")
            if expected - actual:
                print(f"Unsafe! expect {expected - actual}")
            # if actual - expected:
            #     print(f"actual is more: {actual-expected}")


def verify_trigger_testclass(projects: dict) -> Dict[str, Dict[str, Set]]:
    """
    Verify grok result against trigger_tests csv files in defects4j
    :param projects: the dict containing project ids and bug ids
    :return: the dict containing test classes in grok results and class names of
             trigger_tests for each project
             e.g. {"Lang-28":{"expected": {testClass1}, "actual": {testClass2}}}
    """
    results = {}
    for p, v in projects.items():
        logger.info(f"START: verify trigger tests on project [{p}]")
        for bid in v["bug_id"]:
            pbid = f"{p}-{bid}"
            logger.info(f"=> START: verify trigger tests on bug [{pbid}]")
            data_file = get_trigger_tests_path(p, bid)
            expected: Set[str] = set([get_class_from_testname(x) for x in get_trigger_tests(data_file)])
            actual: Set[str] = read_test_classes_from_grok_result(GROK_RESULT_DIR / f"{p}-{bid}.affected")
            unsafe: set = expected - actual
            results[pbid] = {"expected": expected, "actual": actual, "unsafe": unsafe}
            logger.info(f"[{pbid}] <- Check safety property of grok results.")
            if unsafe:
                print(f"Unsafe! expect {expected - actual}")
    return results


def get_bugs_info(pid: str, bids: List[int]) -> Dict[int, RevPair]:
    csv_data_file: Path = PROJECTS_DATA_PATH / pid / "active-bugs.csv"
    bugs: Dict[int, RevPair] = get_rev_id(csv_data_file, bids)
    return bugs


def batch_count_methods(projects, results: Dict[str, Dict[str, Set]], out_f: Path, resume: bool) -> Dict[str, int]:
    """
    :param results: the dict returned by verify_trigger_testclass()
    :return: dict e.g., {"Lang-28": 20}
    """
    if resume:
        try:
            with out_f.open('r') as existf:
                existing = json.load(existf)
        except FileNotFoundError:
            logger.info(f"{out_f} does not exist, re-count all")
            existing = dict()
    else:
        existing = dict()
    bid_methods = existing  # copy existing data
    for p, v in projects.items():
        bugs_info: Dict[int, RevPair] = get_bugs_info(p, v["bug_id"])
        repo_path: Path = get_repo_path(v["local_repo"])
        for bid, rev_pair in bugs_info.items():
            pbid: str = f"{p}-{bid}"
            if pbid in existing.keys():
                logger.info(f"[{pbid}] <- Read from existing data on disk, skip.")
                continue
            logger.info(f"[{pbid}] <- Count test methods of affected test classes.")
            try:
                classes: Set[str] = results.get(pbid).get("actual")
            except KeyError:
                logger.warning(f"Skip {pbid}, which does not exist in verification results")
                continue
            build_path: Path = handle_special_dirstruct(repo_path, p, bid)
            logger.info(f"[{pbid}] <- {len(classes)} affected classes")
            count = count_methods(classes, repo_path, build_path, rev_pair.rev_fix)
            # if pbid == "Math-97":
            #     count = count_methods(classes, repo_path, build_path, rev_pair.rev_fix)
            # else:
            #     count = count_methods_by_runtest(classes, repo_path, build_path, rev_pair.rev_fix)
            # if count == 0:
            #     # count = fix_methods_count(pbid)
            #     # logger.info(f"[{pbid}] <- Fix methods count with manually checked data: {0} => {count}")
            #     count = count_methods(classes, repo_path, build_path, rev_pair.rev_fix)
            #     logger.info(f"[{pbid}] <- Count methods without actual running: {count}")
            bid_methods[pbid] = count
            # Workaround: re-wirte file in each iteration
            with out_f.open('w') as outf:
                json.dump(bid_methods, outf, indent=2)
    # for bid, v in results.items():
    #     actual_classes: Set[str] = v['actual']
    #     bid_methods[bid] = count_methods(actual_classes
    return bid_methods


def find_class_path(base_path: Path, class_name: str) -> Tuple[bool, str]:
    """
    Get class path from class name. It should work with nested classes.
    :param base_path: path to the test class directory, usually {prject_repo}/target/test-classes
    :param class_name: fully qualified class name read from grok result
    :return: a tuple (bool, str). The first value indicate whether the class file
    is found. The second value is the path to class file if found, or the last tried
    path if not found.
    """
    class_path: str = str(base_path / f"{class_name.replace('.', '/')}.class")
    while not os.path.isfile(class_path) and '/' in class_path:
        class_path = r'$'.join(class_path.rsplit('/', 1))
    if not os.path.isfile(class_path):
        return False, class_path
    return True, class_path


def count_methods(classes: Set[str], repo_path: Path, build_path: Path, rev: str) -> int:
    """
    :param rev:
    :param build_path:
    :param repo_path:
    :param classes: the set of class names
    :return:
    """
    restore_clean_repo(str(repo_path))
    # build_repo(rev, repo_path, build_path)
    compile_tests(rev, repo_path, build_path)
    class_base_path: Path = build_path / "target" / "test-classes"
    count = 0
    for c in classes:
        found, cpath = find_class_path(class_base_path, c)
        if not found:
            logger.warning(f"Cannot find {c}. Skip.")
            continue
        methods: List[str] = testinfo.extract_test_methods(cpath)
        count += len(methods)
    return count


def fix_methods_count(pbid: str) -> int:
    """
    For those versions, the mvn test -Dtest=testClassNames would run 0 test, which is possibly
    caused by some issues of surefire plugin.
    Surefire-plugin version changes: 2.12.4 (fine) -> 2.3 (bad) -> 2.4.2 (fine)
    To get following data, we manually add <version>2.4.2</version> in pom.xml and run mvn test -Dtest=...
    """
    manual_data = {'Lang-47': 119, 'Lang-48': 281, 'Lang-49': 25, 'Lang-50': 13, 'Lang-51': 44, 'Lang-52': 74}
    if pbid not in manual_data:
        logger.warning(f"Cannot fix {pbid}, need manual inspection.")
    return manual_data.get(pbid, 0)


def count_methods_by_runtest(classes: Set[str], repo_path: Path, build_path: Path, rev: str) -> int:
    restore_clean_repo(str(repo_path))
    cli_output: str = run_tests(rev, repo_path, build_path, classes)
    num_run: int = 0
    for m in mvn_test_output.findall(cli_output):
        logger.debug(f"m={m}")
        num_run = max(int(m[0]), num_run)
    return num_run


def tex_escape(input_str: str) -> str:
    return input_str.replace('_', '\\_')


def simplify_class_name(fullname: str) -> str:
    replace_dict = {
        "org.apache.commons.lang": "lang",
        "org.apache.commons.math": "math",
        "org.joda.time": "time"
    }
    for k, v in replace_dict.items():
        if fullname.startswith(k):
            return fullname.replace(k, v, 1)


def simplify_class_name_more(fullname: str) -> str:
    return ".".join(fullname.rsplit(".", 2)[-2:])


def process_names(name: str) -> str:
    return simplify_class_name(tex_escape(name))


def output_result_table(results: Dict[str, Dict[str, Set]], out_file: Path):
    """ *DEPRECATED*, use write_num_test_method() now
    Output a LaTeX source file for the result table (list each class name)
    :param results: the dict returned by verify_trigger_testclass()
    :param out_file: path to the output (LaTeX source)
    :return: None
    """
    deprecation("The 'output_result_table' method for listing all class names is deprecated, "
                "use write_num_test_class() instead")
    str_write_to_file = "\\begin{footnotesize}\n\\centering\n\\begin{longtable}{lll}\n\\caption{Results}\n\\label{tab:rts-result}\\\\ \\toprule\n"
    str_write_to_file += "\\textbf{Project ID}  & \\textbf{Trigger Test Classes} & \\textbf{Grok Results} \\\\ \\midrule\n"
    for bid, v in results.items():
        expected, actual = tuple(sorted(s) for s in [v["expected"], v["actual"]])
        expected_num, actual_num = len(expected), len(actual)
        rows = max(expected_num, actual_num)
        expected_first = process_names(expected[0]) if expected_num > 0 else " "
        actual_first = process_names(actual[0]) if actual_num > 0 else " "
        str_write_to_file += f"\\multirow{{{rows}}}{{*}}{{{bid}}} & {expected_first} & {actual_first} \\\\\n"
        for i in range(1, rows):
            expected_i = process_names(expected[i]) if i < expected_num else " "
            actual_i = process_names(actual[i]) if i < actual_num else " "
            str_write_to_file += f" & {expected_i} & {actual_i} \\\\\n"
        str_write_to_file += "\\midrule\n"
    str_write_to_file = str_write_to_file[:-9]
    str_write_to_file += "\\bottomrule\n\\end{longtable}\n\\end{footnotesize}\n"
    with out_file.open("w") as of:
        of.write(str_write_to_file)


def write_num_test_method(bid_methodcount: Dict[str, int], out_file: Path):
    """
    Output a LaTeX source file for the result table (numbers of methods)
    :param bid_methodcount: dict returned by batch_count_method(), e.g. {"Lang-28": 20}
    :param out_file: path to the output (LaTeX source)
    :return: None
    """
    existing_data = extract_data.read_tex(Path("rts/rts_data/defects4j-numbers.tex"))
    str_write_to_file = "\\begin{footnotesize}\n\\centering\n\\begin{longtable}{lrrrrrrc}\n" \
                        "\\caption{Number of Methods}\n\\label{tab:rts-methods-num}\\\\ \\toprule\n"
    str_write_to_file += "\\textbf{Project ID}  & \\textbf{RetestAll} & \\textbf{Ekstazi} & \\textbf{Clover} " \
                         "& \\textbf{STARTS} & \\textbf{HyRTS} & \\textbf{Facts}  & \\textbf{worse than?} \\\\ \\midrule\n"
    for bid, count in bid_methodcount.items():
        tool_nums: [int] = []
        for t in extract_data.RTStool:
            bid_in_data = "".join(bid.split("-")).lower()
            num = extract_data.get_nums(existing_data, bid_in_data, t, extract_data.ToolResult.NumOfRunTests)
            tool_nums.append(num)
        tool_nums_str = " & ".join([str(x) for x in tool_nums])
        worse_than: int = len(list(filter(lambda x: x < count, tool_nums)))
        str_write_to_file += f"{bid} &  {tool_nums_str}  & {count} & {worse_than}\\\\\n"
    str_write_to_file += "\\bottomrule\n\\end{longtable}\n\\end{footnotesize}\n"
    with out_file.open("w") as of:
        of.write(str_write_to_file)


def calc_percent(bid_methodcount: Dict[str, int]):
    existing_data = extract_data.read_tex(Path("rts/rts_data/defects4j-numbers.tex"))
    tool_set = {"ekstazi", "starts", "clover", "notool"}
    percentage: Dict[str, Dict[str, float]] = {t: dict() for t in tool_set - {"notool"}}
    percentage["fact"] = dict()
    for bid, count in bid_methodcount.items():
        bid_in_data = "".join(bid.split("-")).lower()
        nums: Dict[str, int] = dict()
        for t in tool_set:
            num = extract_data.get_nums(existing_data, bid_in_data, extract_data.RTStool[t], extract_data.ToolResult.NumOfRunTests)
            nums[t] = num
        for t in tool_set - {"notool"}:
            percentage[t][bid] = nums[t] / nums["notool"]
            percentage["fact"][bid] = count / nums["notool"]
    # pprint(percentage)
    avg_per_project: Dict[str, Dict[str, float]] = {t: dict() for t in tool_set - {"notool"} | {"fact"}}
    for t, d in percentage.items():
        # avg_per_project[t] = {
        proj = "Lang", "Math", "Time"
        count: Dict[str, int] = {x: 0 for x in proj}
        for p in proj:
            for bid, percent in d.items():
                if bid.startswith(p):
                    count[p] += 1
                    if p in avg_per_project[t]:
                        avg_per_project[t][p] += d[bid]
                    else:
                        avg_per_project[t][p] = d[bid]
        pprint(count)
        for p in "Lang", "Math", "Time":
            avg_per_project[t][p] /= count[p]
    pprint(avg_per_project)


def write_num_test_class(results: Dict[str, Dict[str, Set]], out_file: Path):
    """
    Output a LaTeX source file for the result table (numbers of classes only)
    :param results: the dict returned by verify_trigger_testclass()
    :param out_file: path to the output (LaTeX source)
    :return: None
    """
    str_write_to_file = "\\begin{footnotesize}\n\\centering\n\\begin{longtable}{lccc}\n\\caption{Results}\n\\label{tab:rts-result-num}\\\\ \\toprule\n"
    str_write_to_file += "\\textbf{Project ID}  & \\textbf{\# Trigger} & \\textbf{\# Grok} & \\textbf{Unsafe?} \\\\ \\midrule\n"
    for bid, v in results.items():
        # expected, actual, unsafe = tuple(sorted(s) for s in [v["expected"], v["actual"]])
        expected_num, actual_num, unsafe_num = len(v["expected"]), len(v["actual"]), len(v["unsafe"])
        unsafe_indicator = "" if unsafe_num == 0 else unsafe_num
        str_write_to_file += f"{bid} & {expected_num} & {actual_num} & {unsafe_indicator}\\\\\n"
    str_write_to_file += "\\bottomrule\n\\end{longtable}\n\\end{footnotesize}\n"
    with out_file.open("w") as of:
        of.write(str_write_to_file)


def read_test_classes_from_grok_result(grok_out: Path) -> Set[str]:
    if not grok_out.exists():
        logger.error(f"{grok_out} does not exist, skip")
        return set()
    with grok_out.open() as df:
        logger.info(f"Read grok output from {grok_out}")
        clazz = set([row.strip() for row in df])
        return clazz


def generate_config_file(repo_path: Path, build_path: Path, rev_pair: RevPair, out_file: Path):
    if not build_path:
        build_path = repo_path
    lines = [f"repoPath = {repo_path / '.git'}\n",
             f"startCommit = {rev_pair.rev_bug}\n",
             f"endCommit = {rev_pair.rev_fix}\n",
             f"classRoot = {build_path / 'target/classes'}"]
    with out_file.open("w") as f_out:
        f_out.writelines(lines)


def get_facts_subdir(name: str) -> Path:
    return FACTS_PATH / name


def handle_special_dirstruct(repo_path: Path, pid: str, bid: int) -> Path:
    if pid == "Time" and bid >= 21:
        return repo_path / "JodaTime"
    else:
        return repo_path


def get_repo_path(subdir: str) -> Path:
    return BASE_PATH / "project_repos" / subdir


def collect_facts(projects: dict, skip_exist: bool = False) -> None:
    """
    Generate configuration files, run cslicer, store facts
    :param projects: {project_name : path, bug_id_list}, see get_projects()
    :param skip_exist: will check if subdir exist and skip existing facts
    :return: None
    """
    for p, v in projects.items():
        logger.info(f"Start work on project {p}")
        bugs_info = get_bugs_info(p, v["bug_id"])
        repo_path: Path = get_repo_path(v["local_repo"])
        for bid, rev_pair in bugs_info.items():
            build_path: Path = handle_special_dirstruct(repo_path, p, bid)
            pbid: str = f"{p}-{bid}"
            if skip_exist and get_facts_subdir(pbid).exists():
                logger.info(f"Skip collecting facts for {pbid}. (existing)")
                continue
            logger.info(f"Start on pair {pbid}")
            out_file = CONFIGS_PATH / f"{pbid}.properties"
            restore_clean_repo(str(repo_path))
            # build buggy version
            build_repo(rev_pair.rev_fix, repo_path, build_path)
            # generate config file
            generate_config_file(repo_path, build_path, rev_pair, out_file)
            # run cslicer
            collect_deps_diff_facts("/tmp/rts.log", out_file, None)
            move_facts(repo_path / ".facts", name=pbid)


def batch_post_process_diff_facts():
    for subdir in FACTS_PATH.iterdir():
        if subdir.is_dir() and os.path.splitext(subdir)[1] != ".old":
            diff_file = subdir / "30-diff_tuple.ta"
            if not diff_file.exists():
                logger.error(f"{diff_file} does not exist")
                continue
            post_process_diff_facts(diff_file)


def post_process_diff_facts(facts_path: Path):
    with facts_path.open("a") as ff:
        for x in {"Update", "Insert", "Delete"}:
            ff.write(f"{x} NONE NONE\n")


def batch_run_grok(projects: dict, grok_script: Path, skip_exist: bool = False):
    """
    Run grok on facts, per subdir
    :param projects: {project_name : path, bug_id_list}, see get_projects()
    :param grok_script: path to the grok query script
    :param skip_exist: will check if subdir exist and skip existing facts
    :return: None
    """
    for p, v in projects.items():
        for bid in v["bug_id"]:
            pbid: str = f"{p}-{bid}"
            subdir = get_facts_subdir(pbid)
            if subdir.is_dir():
                grok_results_per_run = GROK_RESULT_DIR / pbid
                grok_log_per_run = GROK_LOG_DIR / pbid
                affected_file: Path = GROK_RESULT_DIR / f"{pbid}.affected"
                if skip_exist and affected_file.exists():
                    logger.info(f"Skip running grok for {pbid}. (existing)")
                    continue
                logger.info(f"Grok on {pbid}")
                run_grok(grok_script, grok_results_per_run, subdir, grok_log_per_run)


def run_tests(rev, repo_path, build_path, classes: Set[str]) -> str:
    cwd = os.getcwd()
    os.chdir(repo_path)
    checkout_cmd = f'git checkout {rev}'
    subprocess.run(shlex.split(checkout_cmd), capture_output=True)
    os.chdir(build_path)
    run_test_cmd = 'mvn test -Dtest=' + ",".join(classes)
    logger.info(f"Run: {run_test_cmd}")
    run = subprocess.run(shlex.split(run_test_cmd), shell=False, cwd=build_path,
                         stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if run.stderr:
        logger.error(run.stderr.decode())
    logger.info(f"Finish: mvn test -Dtest=...")
    os.chdir(cwd)
    return run.stdout.decode()


def compile_tests(rev, repo_path, build_path):
    cwd = os.getcwd()
    os.chdir(repo_path)
    checkout_cmd = f'git checkout {rev}'
    subprocess.run(shlex.split(checkout_cmd), capture_output=True)
    os.chdir(build_path)
    build_cmd = 'mvn test-compile'
    logger.info(f"Run: {build_cmd}")
    run_build = subprocess.run(shlex.split(build_cmd), shell=False, cwd=build_path,
                               stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    logger.debug(run_build.stdout.decode())
    if run_build.stderr:
        logger.error(run_build.stderr.decode())
    logger.info(f"Finish: {build_cmd}")
    os.chdir(cwd)


def build_repo(rev, repo_path, build_path):
    cwd = os.getcwd()
    os.chdir(repo_path)
    checkout_cmd = f'git checkout {rev}'
    subprocess.run(shlex.split(checkout_cmd), capture_output=True)
    os.chdir(build_path)
    # install_cmdline = 'mvn install -DskipTests'
    install_cmdline = 'mvn test-compile'
    logger.info(install_cmdline)
    run_build = subprocess.run(shlex.split(install_cmdline), shell=False, cwd=build_path, capture_output=True)
    logger.debug(run_build.stdout.decode())
    if run_build.stderr:
        logger.error(run_build.stderr.decode())
    os.chdir(cwd)


def move_facts(facts_dir: Path, name: str):
    dst_dir: Path = get_facts_subdir(name)
    if os.path.isdir(dst_dir):
        dst_dir_old = FACTS_PATH / f"{name}.old"
        logger.warning(f"Renaming existing {dst_dir} before moving in "
                       f"newly-generated facts, existing .old directories "
                       f"will be overwritten")
        if os.path.isdir(dst_dir_old):
            shutil.rmtree(dst_dir_old)
        shutil.move(dst_dir, dst_dir_old)
    shutil.move(str(facts_dir), str(dst_dir))


def handle_args():
    """handle cmdline arguments
    usual procedure:
    1. -f  collect facts
    2. --ensure-all-change-types  post-process so that jgrok does not throw exception
    3. -s  run grok
    4. -v  verify
    5.1 --count-class  results in TeX, number of test classes
    5.2.1 --count-method-json  results in json, number of methods
    5.2.2 --count-method-tex   results in TeX, number of methods, in comparison with existing tools
    optional. --resume  skip existing
    optional. --debug  run with a small subset of projects
    optional. -l  specify log level
    :return: arguments
    """
    parser = argparse.ArgumentParser(description="Test selection on Defects4j")
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    parser.add_argument("-f", action="store_true", help="Collect facts")
    parser.add_argument("--resume", action="store_true",
                        help="Resume process, according to the existence of files/dirs. Works with -f, -s")
    parser.add_argument("--ensure-all-change-types", action="store_true",
                        help="Ensure that all change types exist in diff facts by adding NONE facts")
    parser.add_argument("--debug", action="store_true", help="Use one bug pair for testing, default: True")
    parser.add_argument("-s", metavar="GROK_SCRIPT", help="Run grok with specified grok script")
    parser.add_argument("-v", action="store_true", help="Verify grok results")
    parser.add_argument("--count-class", metavar="OUTPUT_TEX_SRC",
                        help="Output TeX source for a results table")
    parser.add_argument("--count-method-json", metavar="OUTPUT_JSON",
                        help="Count test methods of affected test classes and write OUTPUT_JSON")
    parser.add_argument("--count-method-tex", nargs=2, metavar=("JSON_FILE", "OUTPUT_TEX_SRC"),
                        help="Read JSON and write TeX table")
    parser.add_argument("--percent", metavar="JSON_FILE", help="Calculate percentage")
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


def main():
    args = handle_args()
    projects = get_projects(args.debug)
    if args.f:
        if args.resume:
            collect_facts(projects, True)
        else:
            collect_facts(projects)
    if args.ensure_all_change_types:
        batch_post_process_diff_facts()
    if args.s:
        prepare_misc_dir([GROK_LOG_DIR, GROK_RESULT_DIR])
        if args.resume:
            batch_run_grok(projects, Path(args.s), True)
        else:
            batch_run_grok(projects, Path(args.s))
    if args.v:
        results = verify_trigger_testclass(projects)
        if args.count_class:
            write_num_test_class(results, Path(args.count_class))
        if args.count_method_json:
            out_f: Path = Path(args.count_method_json).resolve()
            bid_methods = batch_count_methods(projects, results, out_f, args.resume)
            logger.info(bid_methods)
    if args.count_method_tex:
        in_f: Path = Path(args.count_method_tex[0])
        out_f: Path = Path(args.count_method_tex[1])
        with in_f.open() as df:
            bid_methods = json.load(df)
            write_num_test_method(bid_methods, out_f)
    if args.percent:
        in_f: Path = Path(args.percent)
        with in_f.open() as df:
            bid_methods = json.load(df)
            calc_percent(bid_methods)


if __name__ == '__main__':
    main()
