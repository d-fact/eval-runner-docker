#!/usr/bin/env python3
from typing import List, Dict, Tuple
import os
import shlex
import logging
import tempfile
from git import Commit, Repo
from subprocess import PIPE, Popen
from run_config import RunConfig
from commits_util import checkout_commit
from multiprocessing import Pool

logger = logging.getLogger(__name__)

def get_changed_files(pcmt: Commit, ccmt: Commit) -> List[str]:
    neighbor_diff = pcmt.diff(ccmt)
    m_files = list(neighbor_diff.iter_change_type("M"))
    return m_files

def gumtree_on_file_pairs(file_a, file_b, diff_result):
    base_cmd = "gumtree-my textdiff -f xml {} {} -o {}".format(file_a, file_b, diff_result)
    logger.debug("Execute: {}".format(base_cmd))
    cur_env = os.environ.copy() 
    cur_env['JAVA_HOME'] = cur_env['HOME'] + '/Applications/java/jdk-9.0.4/'
    call_gumtree = Popen(shlex.split(base_cmd), stdout=PIPE, stderr=PIPE, env=cur_env)
    out, err = call_gumtree.communicate()
    if out:
        logger.info(f"stdout: {out.decode()}")
    if err:
        err_headline = err.decode().split("\n", 1)[0]
        logger.warning(f"stderr: {err_headline}")
        call_gumtree.kill()

def run_gumtree(commits: List[str], repo: Repo, output_dir, njobs=None) -> Dict[
    Tuple[str, str], str]:
    output_files = dict()
    for i in range(len(commits) - 1):
        ccmt, pcmt = commits[i], commits[i + 1]  # type: str, str
        logger.info(f"[new -> old] {ccmt} --> {pcmt}")
        commit_diff(ccmt, pcmt, repo, output_dir, output_files, njobs)
    return output_files

def commit_diff(ccmt: str, pcmt: str, repo: Repo, output_dir: str, output_files: Dict[Tuple[str, str], str], njobs=None):
    """
    :param ccmt: current commit
    :param pcmt: parent of ccmt (older commit)
    :param repo: the repo which ccmt and pcmt belong to
    :param output_dir: user-specified absolute path for generated diff.ta
    :param output_files: a dict for keeping sub-dir (str, abspath)
    :param njobs: number of processes
    :return:
    """
    with tempfile.TemporaryDirectory(prefix="alt_tree_a_") as alt_worktree_a, tempfile.TemporaryDirectory(
            prefix="alt_tree_b_") as alt_worktree_b:
        logger.info(f"Gumtree is working on <{pcmt}>..<{ccmt}> of [{repo.working_dir}]")
        vers = (pcmt, ccmt)
        # checkout ccmt at working_dir, checkout pcmt at another dir
        checkout_commit(repo, pcmt, alt_worktree_a)
        checkout_commit(repo, ccmt, alt_worktree_b)
        m_files = get_changed_files(repo.commit(pcmt), repo.commit(ccmt))
        odir = os.path.join(output_dir, f"{vers[0]}_{vers[1]}")
        os.makedirs(odir, exist_ok=True)

        m_tuples = list()  # type: List[Tuple[str, str, str]]
        for c in m_files:
            # TODO consider check extension of both a_path and b_path
            if os.path.splitext(c.a_path)[1] in {'.java'}:
                logger.debug(f"{c.a_path} ---- {c.b_path} is Java source files, checked by gumtree.\n")
                diff_result = os.path.join(odir, f"diff__{no_slash(c.a_path)}__{no_slash(c.b_path)}.xml")
                file_a = os.path.join(alt_worktree_a, c.a_path)
                file_b = os.path.join(alt_worktree_b, c.b_path)
                m_tuples.append((file_a, file_b, diff_result))
            else:
                logger.debug(f"{c.a_path} ---- {c.b_path} is not Java source files, not checked by gumtree.\n")
        logger.info(f"{len(m_tuples)} modified files in total.")
        with Pool(processes=njobs) as p:
            p.starmap(gumtree_on_file_pairs, m_tuples)
        output_files[vers] = odir
        rm_empty_diff(m_tuples)
    # prune temp worktree
    # git of old versions (before 2.17.0) do not have `git worktree remove`
    # so we use tempfile to create/delete temp dirs
    # and prune metadata after tempdir was deleted
    repo.git.worktree("prune")

def no_slash(rel_path: str) -> str:
    return rel_path.replace("/", "-")

def rm_empty_diff(m_tuples):
    pass
