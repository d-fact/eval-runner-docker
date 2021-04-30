import os
from collections import namedtuple
from hislicing import env_const
import logging

logger = logging.getLogger(__name__)
Cfg = namedtuple("Config",
                 ["repoPath", "execPath", "sourceRoot", "classRoot", "startCommit", "endCommit", "buildScriptPath",
                  "testScope", "touchSetPath"])

ExtractedCfg = namedtuple("ExtractedCfg", "start, end, repo_name, test_suite, repo_path, lines, config_file")


def extractInfoFromCSlicerConfigs(example: str) -> ExtractedCfg:
    """
    read start commit, end commit, repo, and test suite
    """
    # find the config file
    config_file = search_file(env_const.NEW_CONFIGS_DIR, example + '.properties')
    if config_file is None:
        logger.error(f'Cannot find config file for "{example}"')
        exit(0)
    fr = open(config_file, 'r')
    lines = fr.readlines()
    fr.close()
    for i in range(len(lines)):
        if lines[i].startswith('startCommit'):
            start = lines[i].strip().split()[-1]
        elif lines[i].startswith('endCommit'):
            end = lines[i].strip().split()[-1]
        elif lines[i].startswith('repoPath'):
            repo_name = lines[i].split('/')[-2]
        elif lines[i].startswith('testScope'):
            test_suite = lines[i].strip().split()[-1]
    repo_path = env_const.DOWNLOADS_DIR + '/' + repo_name
    # print (start, end, repo_name, test_suite, repo_path)
    cfg = ExtractedCfg(start, end, repo_name, test_suite, repo_path, lines, config_file)
    logger.debug(cfg)
    return cfg


def search_file(dir_root, file_name):
    for dir_path, subpaths, files in os.walk(dir_root):
        for f in files:
            if f == file_name:
                return dir_path + '/' + f
    return None


