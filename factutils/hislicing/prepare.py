from hislicing.slicing_util import extractInfoFromCSlicerConfigs
from util import check_dir
from git import Repo
from hislicing import env_const
import os
import logging

logger = logging.getLogger(__name__)


def prepare():
    check_dir(env_const.CSLICER_FACTS_OUTPUT_DIR, True)
    check_dir(env_const.CSLICER_STANDALONE_OUTPUT_DIR, True)
    check_dir(env_const.FACTS_RESULTS_DIR, make_if_not=True)


def git_clone(project_names):
    """
    git clone repos and return a list of repo names
    :param project_names:
    :return:
    """
    names = set()
    for x in project_names:
        cfg = extractInfoFromCSlicerConfigs(x)
        names.add(cfg.repo_name)
    for n in names:
        repo_dir = os.path.join(env_const.DOWNLOADS_DIR, n + env_const.REPO_DIR_SFX)
        if not os.path.exists(repo_dir):
            os.makedirs(repo_dir)
        else:
            logger.warning(f"\"{repo_dir}\" exists, skip cloning {n}")
            continue
        logger.info(f"Clone {n} to {repo_dir}")
        Repo.clone_from(env_const.GitPath.get(n), repo_dir)
    return names


def replace_cfg(project_names):
    if not os.path.exists(env_const.NEW_CONFIGS_DIR):
        logger.info(f"Makedirs @ {env_const.NEW_CONFIGS_DIR}")
        os.makedirs(env_const.NEW_CONFIGS_DIR)
    for name in project_names:
        existing_cf = os.path.join(env_const.CONFIGS_DIR, name + ".properties")
        new_cf = os.path.join(env_const.NEW_CONFIGS_DIR, name + ".properties")
        with open(existing_cf, 'r') as cf, open(new_cf, 'w') as nf:
            new_lines = [l.replace(env_const.BASE_DIR_IN_CFG, env_const.BASE_DIR) for l in cf.readlines()]
            nf.writelines(new_lines)
            logger.info(f"Generate new .properties file @ {new_cf}")
