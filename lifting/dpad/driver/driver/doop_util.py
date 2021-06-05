#!/usr/bin/env python3

import logging
import shlex
from enum import Enum
from pathlib import Path
from subprocess import Popen, PIPE
from util import log_popen, get_cur_time_str
from run_config import RunConfig

logger = logging.getLogger(__name__)


class DoopLogLevel(Enum):
    debug = 1,
    info = 2,
    error = 3

    @classmethod
    def lvl_convert(cls, py_log_level: str):
        if py_log_level == "debug":
            return cls.debug
        if py_log_level == "info":
            return cls.info
        if py_log_level in ["warning", "warn"]:
            return cls.info
        if py_log_level == "error":
            return cls.error
        if py_log_level == "critical":
            return cls.error
        else:
            return cls.info


def facts_collect_worker(specify_main: str, sha1d: int, annotate: bool, project_name: str,
                         facts_storage: Path, log_level: str, atype: str, doop_path: Path,
                         log_dir: Path, jar_name: Path):
    anno_str = jar_name.stem[:sha1d]
    pc_opt = f'--presence-condition "VER{anno_str}"' if annotate else ""
    abs_jar_path = jar_name.resolve()
    doop_id: str = facts_dir_naming(project_name, anno_str)
    facts_dir: Path = facts_storage / jar_name.stem
    doop_log_lvl = DoopLogLevel.lvl_convert(log_level)
    cmd_collect: str = f'./doop -a {atype} -i {abs_jar_path} --Xstop-at-facts {facts_dir} --platform java_8 ' \
                       f'{pc_opt} --Xunique-facts  --Xstats-none ' \
                       f'--Xfacts-subset APP_N_DEPS {specify_main} -L {doop_log_lvl.name} --id {doop_id}'
    p = Popen(shlex.split(cmd_collect), stdout=PIPE, stderr=PIPE, cwd=str(doop_path))
    log_dir = log_dir / "collect" / jar_name.stem
    return log_popen(p, log_dir)


def analysis(merged_storage: Path, atype: str, doop_path: Path, open_analysis: bool, results_storage: Path,
             doop_id: str, log_dir: Path, log_level: str) -> bool:
    oa_opt: str = "--open-programs concrete-types " if open_analysis else ""
    # oa_opt: str = "--open-programs-context-insensitive-entrypoints" if open_analysis else ""
    doop_log_lvl = DoopLogLevel.lvl_convert(log_level)
    cmd_analysis: str = f'./doop {oa_opt} -a {atype} -i /tmp/tmp.jar --Xstart-after-facts {merged_storage} --id {doop_id} ' \
                        f'--platform java_8 --Xunique-facts  --dont-report-phantoms --Xfacts-subset APP_N_DEPS -L {doop_log_lvl.name} '
    p = Popen(shlex.split(cmd_analysis), stdout=PIPE, stderr=PIPE, cwd=str(doop_path))
    log_dir = log_dir / "analysis"
    return log_popen(p, log_dir)


def one_step_run(jar_path: Path, atype: str, doop_path: Path, doop_id: str, log_dir: Path, log_level: str) -> bool:
    doop_log_lvl = DoopLogLevel.lvl_convert(log_level)
    cmd_collect: str = f'./doop -a {atype} -i {jar_path} --Xunique-facts --dont-report-phantoms ' \
                       f'--platform java_8 --Xfacts-subset APP_N_DEPS -L {doop_log_lvl.name} --id {doop_id}'
    p = Popen(shlex.split(cmd_collect), stdout=PIPE, stderr=PIPE, cwd=str(doop_path))
    log_dir = log_dir / "doop" / jar_path.stem
    return log_popen(p, log_dir)


def facts_dir_naming(project_name: str, sha1: str) -> str:
    return f"{project_name}_{sha1}_Stop_at_Facts_{get_cur_time_str()}"
