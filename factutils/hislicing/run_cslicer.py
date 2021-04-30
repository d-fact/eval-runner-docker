import os
import logging
import time
import shutil
import subprocess
from util import ErrorCode, check_dir, restore_clean_repo
from hislicing.slicing_util import search_file, extractInfoFromCSlicerConfigs
from hislicing.benchmark import Benchmark
from typing import List
import json
from hislicing import env_const

logger = logging.getLogger(__name__)


def log_time(time_val: float, item: Benchmark, time_dict: dict, is_general: bool):
    uniq_history_name = item.uniq_history_str()
    if uniq_history_name in time_dict.keys():
        if is_general:
            time_dict[uniq_history_name]["general"] = time_val
        else:
            time_dict[uniq_history_name][item.funcNum] = time_val
    else:
        if is_general:
            time_dict[uniq_history_name] = {"general": time_val}
        else:
            time_dict[uniq_history_name] = {item.funcNum: time_val}


def move_facts(facts_dir: str, name: str):
    dst_dir = os.path.join(env_const.FACTS_RESULTS_DIR, name)
    if os.path.isdir(dst_dir):
        dst_dir_old = os.path.join(env_const.FACTS_RESULTS_DIR, name + ".old")
        logger.warning(f"Renaming existing {dst_dir} before moving in newly-generated facts, existing .old directories will be overwritten")
        if os.path.isdir(dst_dir_old):
            shutil.rmtree(dst_dir_old)
        shutil.move(dst_dir, dst_dir_old)
    shutil.move(facts_dir, dst_dir)


def run_cslicer_for_facts(uniq_history: tuple, func_num: List[str], time_dict: dict):
    """

    :param uniq_history: a tuple representing a history range of a project
      (project_name, start_rev_id, end_rev_id)
    :param func_num: a list of functionality ID (slicing criteria)
    :param time_dict: storing timing info
    :return:
    """
    first_num = func_num[0]  # type: str
    logger.info(f"Starting group {str(uniq_history)}, with first test '{first_num}'")
    with_first = Benchmark(name=uniq_history[0],
                           func_num=first_num,
                           history_start=uniq_history[1],
                           history_end=uniq_history[2])
    config_filename = with_first.config_name()  # type: str
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
        extractInfoFromCSlicerConfigs(config_filename)
    restore_clean_repo(repo_path)
    runTestsGenJacoco(config_filename, end, repo_path, test_suite)

    if not check_dir(env_const.CSLICER_FACTS_OUTPUT_DIR, make_if_not=True):
        exit(ErrorCode.PATH_ERROR)

    cslicer_fact_log = os.path.join(env_const.CSLICER_FACTS_OUTPUT_DIR, with_first.uniq_history_str() + '.log')
    time_cost = collect_deps_diff_facts(cslicer_fact_log, config_file, 'orig')
    # log time into time_dict
    log_time(time_cost, with_first, time_dict, True)
    move_facts(os.path.join(repo_path, ".facts"), name=with_first.uniq_history_str())

    cslicer_cov_fact_log = os.path.join(env_const.CSLICER_FACTS_OUTPUT_DIR, with_first.fullname_w_range() + '.log')
    time_cost = collect_cov_facts(cslicer_cov_fact_log, config_file, 'orig')
    # log time into time_dict
    log_time(time_cost, with_first, time_dict, False)
    move_facts(os.path.join(repo_path, ".facts"), name=with_first.fullname_w_range())

    if len(func_num) <= 1:
        return
    logger.debug(func_num)
    iter_nums = iter(func_num)
    next(iter_nums)  # skip the first one since we have run it in process above
    for x in iter_nums:
        other_test = Benchmark(name=uniq_history[0],
                               func_num=x,
                               history_start=uniq_history[1],
                               history_end=uniq_history[2])
        logger.info(f"Starting test {other_test}")
        config_filename = other_test.config_name()  # type: str
        start, end, repo_name, test_suite, repo_path, lines, config_file = \
            extractInfoFromCSlicerConfigs(config_filename)
        restore_clean_repo(repo_path)
        runTestsGenJacoco(config_filename, end, repo_path, test_suite)
        cslicer_cov_fact_log = os.path.join(env_const.CSLICER_FACTS_OUTPUT_DIR,
                                            other_test.fullname_w_range() + '.log')
        time_cost = collect_cov_facts(cslicer_cov_fact_log, config_file, 'orig')
        log_time(time_cost, other_test, time_dict, False)
        move_facts(os.path.join(repo_path, ".facts"), name=other_test.fullname_w_range())


def runCSlicerStandalone(example: str, time_dict: dict):
    logger.info('Starting Example :' + example)
    start_time = time.time()
    # extract info from cslicer orig config file
    start, end, repo_name, test_suite, repo_path, lines, config_file = \
        extractInfoFromCSlicerConfigs(example)
    if os.path.isdir(repo_path):
        logger.info(f'remove old repo "{repo_path}"')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + env_const.REPO_DIR_SFX, repo_path)
    if not check_dir(env_const.CSLICER_STANDALONE_OUTPUT_DIR, make_if_not=True):
        exit(ErrorCode.PATH_ERROR)
    # run tests at end commit, generate jacoco files
    runTestsGenJacoco(example, end, repo_path, test_suite)

    cslicer_orig_log = os.path.join(env_const.CSLICER_STANDALONE_OUTPUT_DIR, example + '.log')
    time_cost = runCSlicerTool(cslicer_orig_log, config_file, 'orig')
    time_dict[example] = time_cost

    # -------------------------------- cslicer end -------------------------------------
    # debug: move repo to somewhere else
    end_time = time.time()
    run_time = end_time - start_time
    putTimeinLog(cslicer_orig_log, run_time)
    # countChangedLines(cslicer_orig_log, repo_path, 'cslicer')
    # backupRepoForDebugging(example, repo_path)


def runTestsGenJacoco(example, end, repo_path, test_suite, poms_dir=env_const.POMS_DIR):
    # run mvn test at the end commit, generate jacoco
    os.chdir(repo_path)
    subprocess.run('git checkout ' + end + ' -b orig', shell=True)
    new_pom_file = search_file(poms_dir, example + '.pom.xml')
    replacePomSurefireVersions(example, repo_path, new_pom_file)

    install_cmdline = 'mvn install -DskipTests'
    logger.info(install_cmdline)
    subprocess.run(install_cmdline, shell=True,
                   stdout=open(os.devnull, 'w'), stderr=open(os.devnull, 'w'))

    # multimodule
    submodule_path = getSubModulePathForAGivenProject(example)
    os.chdir(os.path.join(repo_path, submodule_path))

    test_cmdline = 'mvn test -Dtest=' + test_suite
    logger.info(test_cmdline)
    subprocess.run(test_cmdline, shell=True)
    # save jacoco file for analysis
    target_path = getTargetPathForAGivenProject(example)
    jacoco_path = os.path.join(repo_path, target_path, 'jacoco.exec')
    if not os.path.isfile(jacoco_path):
        logger.error(f"{jacoco_path} does not exist, check generation")
        exit(ErrorCode.JACOCO_GEN_FAILED)
    if not os.path.exists(env_const.JACOCOS_DIR):
        os.makedirs(env_const.JACOCOS_DIR)
    elif os.path.isfile(env_const.JACOCOS_DIR):
        logger.error(f"{env_const.JACOCOS_DIR} exists and is a file.")
        exit(ErrorCode.PATH_ERROR)
    shutil.move(jacoco_path, os.path.join(env_const.JACOCOS_DIR, example + '-jacoco.exec'))
    os.chdir(repo_path)


def getTargetPathForAGivenProject(example):
    submodule_path = getSubModulePathForAGivenProject(example)
    return os.path.join(submodule_path, 'target')


def replacePomSurefireVersions(example, repo_path, new_pom_file):
    """
    update pom file to use a newer surefire version to support the "mvn test # +" format
    """
    if example.startswith('PDFBOX'):
        pom_path = repo_path + '/pdfbox/pom.xml'
    else:
        pom_path = repo_path + '/pom.xml'  # single module projects
    logger.info(f"Replace {pom_path} with {new_pom_file}")
    shutil.copyfile(new_pom_file, pom_path)
    # insert argLine for all the submodules
    if example.startswith('MNG') or example.startswith('CALCITE') or example.startswith('FLUME'):
        poms = findAllPomsInDir(repo_path)
        for pom in poms:
            if '/src/test' not in pom:
                insertArgsInOnePom(pom)


def insertArgsInOnePom(pom):
    fr = open(pom, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if '<artifactId>maven-surefire-plugin</artifactId>' in lines[i]:
            for j in range(i, len(lines)):
                if '</plugin>' in lines[j]:
                    break
            for k in range(i, j):
                if '<argLine>' in lines[k]:
                    lines[k] = lines[k].replace('</argLine>', ' ${argLine}</argLine>')
    fw = open(pom, 'w')
    fw.write(''.join(lines))
    fw.close()


def getSubModulePathForAGivenProject(example):
    if example in ['CALCITE-627', 'CALCITE-758', 'CALCITE-811', 'CALCITE-803', 'CALCITE-991',
                   'CALCITE-1288', 'CALCITE-1309']:
        submodule_path = 'core'
    elif example in ['CALCITE-655', 'CALCITE-718']:
        submodule_path = 'avatica-server'
    elif example in ['CALCITE-767']:
        submodule_path = 'avatica'
    elif example in ['MNG-4904', 'MNG-4910', 'MNG-5530', 'MNG-5549']:
        submodule_path = 'maven-core'
    elif example in ['MNG-4909']:
        submodule_path = 'maven-model-builder'
    elif example in ['FLUME-2052', 'FLUME-2056', 'FLUME-2130', 'FLUME-2628', 'FLUME-2982']:
        submodule_path = 'flume-ng-core'
    elif example in ['FLUME-2206']:
        submodule_path = 'flume-ng-sinks/flume-ng-elasticsearch-sink'
    elif example in ['FLUME-2498', 'FLUME-2955']:
        submodule_path = 'flume-ng-sources/flume-taildir-source'
    elif example in ['FLUME-1710']:
        submodule_path = 'flume-ng-sdk'
    elif example.startswith('PDFBOX'):
        submodule_path = 'pdfbox'
    else:
        submodule_path = ''  # single-module project
    return submodule_path


def runCSlicerTool(cslicer_log, config_file, branch) -> float:
    cslicer_start_time = time.time()
    # subprocess.run('git checkout ' + branch, shell=True)
    report_rev()
    subprocess.run('java -jar ' + env_const.CSLICER_JAR_PATH + ' -c ' + config_file + ' -e slicer', shell=True,
                   stdout=open(cslicer_log, 'w'), stderr=subprocess.STDOUT)
    cslicer_time = time.time() - cslicer_start_time
    putTimeinLog(cslicer_log, cslicer_time, label="CSlicer_Only: ")
    return cslicer_time


def report_rev():
    git_cmd = subprocess.Popen(["git", "rev-parse", "HEAD"], stdout=subprocess.PIPE)
    logger.info(f"Currently @ {git_cmd.stdout.read().decode('utf-8')}")


def collect_deps_diff_facts(collect_log: str, config_file, branch) -> float:
    cslicer_start_time = time.time()
    # subprocess.run('git checkout ' + branch, shell=True)
    report_rev()
    cslicer_cmd = ["java", "-jar", env_const.CSLICER_JAR_PATH, "-c", config_file, "-p", "-e", "fact", "-fuzzy", "-ext", "hunk", "dep", "diff"]
    subprocess.run(args=cslicer_cmd, shell=False, stdout=open(collect_log, 'w'), stderr=subprocess.STDOUT, check=True)
    cslicer_time = time.time() - cslicer_start_time
    putTimeinLog(collect_log, cslicer_time, label="CSlicer_Only: ")
    return cslicer_time


def collect_cov_facts(collect_log: str, config_file, branch) -> float:
    cslicer_start_time = time.time()
    # subprocess.run('git checkout ' + branch, shell=True)
    report_rev()
    cslicer_cmd = ["java", "-jar", env_const.CSLICER_JAR_PATH, "-c", config_file, "-p", "-e", "fact", "-fuzzy", "-ext=cov"]
    subprocess.run(cslicer_cmd, shell=False, stdout=open(collect_log, 'w'), stderr=subprocess.STDOUT)
    cslicer_time = time.time() - cslicer_start_time
    putTimeinLog(collect_log, cslicer_time, label="CSlicer_Only: ")
    return cslicer_time


# CZ: old, upgrade to insertTimeDictinLog() in the future
def putTimeinLog(log_file, run_time, label="Total Run Time: "):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    lines.append(label + str(run_time) + '\n')
    fw = open(log_file, 'w')
    fw.write(''.join(lines))
    fw.close()


def insertTimeDictinLog(log_file, time_dict):
    fr = open(log_file, 'r')
    lines = fr.readlines()
    fr.close()
    for key in time_dict:
        lines += key + ': ' + str(time_dict[key]) + '\n'
    fw = open(log_file, 'w')
    fw.write(''.join(lines))
    fw.close()


def findAllPomsInDir(target_dir):
    poms = []
    for dir_path, subpaths, files in os.walk(target_dir):
        for f in files:
            if f == 'pom.xml':
                poms.append(dir_path + '/' + f)
    return poms


def run_names(names: list):
    cslicer_time_dict = {}
    for n in names:
        runCSlicerStandalone(n, cslicer_time_dict)
    with open(env_const.CSLICER_TIMING_FILE, 'w') as tf:
        json.dump(cslicer_time_dict, tf, indent=2)


def run_groups(groups: dict):
    fact_time_dict = {}
    for g in groups:
        run_cslicer_for_facts(g, groups.get(g), fact_time_dict)
    with open(env_const.FACTS_TIMING_FILE, 'w') as tf:
        json.dump(fact_time_dict, tf, indent=2)
