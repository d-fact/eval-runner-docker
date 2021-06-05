import shutil
from pathlib import Path
import logging
import tempfile
from multiprocessing import Pool
from functools import partial
from git import Repo, Commit
from mvn import Mvn
from util import check_dir
from commits_util import checkout_commit

logger = logging.getLogger(__name__)


def gen_full_bytecode(project_path: str, output_dir: Path, cmt: str) -> bool:
    """ generate bytecode for project located at project_path, and store them in output_dir
    :param cmt: commit-id
    :param project_path: path to project root directory
    :param output_dir: path to output directory
    :return: bool, True indicating success
    """
    # TODO: compile and generate jar
    # jar_abspath = ""  # type:str
    # shutil.move(jar_abspath, output_dir)

    # TODO: clarify if additional configs are required
    # TODO: once done, uncomment the code below
    mvn = Mvn(project_path)
    mvn.package()
    commit_jar = f"{cmt}.jar"
    commit_jar_path = output_dir / commit_jar
    shutil.move(mvn.get_absolute_jar_path(), str(commit_jar_path))
    return True


def gen_incr_bytecode(project_path: str, output_dir: str) -> bool:
    return True


def co_build(repo: Repo, output_dir: Path, cmt: str):
    """checkout and build."""
    with tempfile.TemporaryDirectory(prefix="alt_tree_") as alt_worktree:
        logger.debug(f"Checkout commit {cmt} at {alt_worktree}")
        checkout_commit(repo, cmt, alt_dir=alt_worktree)
        gen_full_bytecode(alt_worktree, output_dir, cmt)


def step_compile(jar_dir: Path, repo: Repo, commit_list: list, njobs: int, incr: bool):
    check_dir(str(jar_dir), make_if_not=True)
    if not incr:
        partial_worker = partial(co_build, repo, jar_dir)
        with Pool(processes=njobs) as p:
            ret = p.map(partial_worker, commit_list)
    if incr:
        pass
