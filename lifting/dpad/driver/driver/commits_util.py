import logging
from git import Repo, Tag, Commit, BadName
from typing import List

logger = logging.getLogger(__name__)


def checkout_commit(repo: Repo, commit, alt_dir=None) -> bool:
    """
    checkout specific commit at repo.working_dir or alt_dir
    :param repo: Repo object
    :param commit: commit could be of one of the types: Commit, Tag, str
    :param alt_dir: target directory for `git worktree add`
    :return:
    """
    if isinstance(commit, str):
        try:
            repo.commit(commit)
        except BadName:
            logger.error(f"Invalid commit: {commit}")
            return False
    elif not (isinstance(commit, Commit) or isinstance(commit, Tag)):
        logger.error(f"Wrong type '{type(commit)}' for commit")
        return False
    
    # logger.info(cmt.committed_date)
    cmt_str = str(commit)
    if alt_dir:
        logger.info(f"Checkout {cmt_str} @ {alt_dir}")
        repo.git.worktree("add", alt_dir, cmt_str)
    else:
        logger.info(f"Checkout {cmt_str} @ {repo.working_dir}")
        repo.git.checkout(cmt_str)

    return True


def interp_rev_range(repo: Repo, rev_range: str) -> List[str]:
    commit_list: List[str] = [str(c) for c in Commit.iter_items(repo, rev_range)]
    return commit_list
