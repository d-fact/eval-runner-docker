from enum import unique, IntEnum
import toml
import os
from collections import OrderedDict, namedtuple
from typing import Tuple
from pathlib import Path
import logging
import pprint

from util import check_dir, ErrorCode, get_cur_time_str

logger = logging.getLogger(__name__)


@unique
class CfgErrType(IntEnum):
    OTHERS = 1
    MISSING = 2
    INVALID = 3


DirsTuple = namedtuple("Dirs", ['log', 'jar', 'facts', 'merged', 'analysis', 'stat', 'gumtree', 'ddlog'])
DoopCfg = namedtuple("DoopCfg", ['path', 'atype', 'annotate', 'sha1digit', 'main_class', 'flavor', 'program'])


class RunConfig:
    def __init__(self, config_fname: str, override_outdir: str):
        self.config_file = config_fname
        _cfg = self.load_config()
        self.check_key({"input", "exe-ctrl", "output"}, _cfg)

        self.repo_path = None
        self.rev_range = None
        self.project_name = None

        self.steps: list = _cfg.get("exe-ctrl").get("steps_to_run")
        self.set_input(_cfg)
        # self.exectrl = _cfg.get("exe-ctrl")
        self.njobs: int = self.get_njobs(_cfg)
        self.output_dir: str = override_outdir if override_outdir else self.create_output_dir(_cfg)
        # self.store_metadata = _cfg.get("output").get("store_metadata")
        self.store_metadata: bool = _cfg.get("output").get("store_metadata", True)

        self.doop: DoopCfg = self.set_doop_cfg(_cfg.get("doop"))
        self.merge: dict = _cfg.get("merge")
        self.facts_diff: dict = _cfg.get("facts-diff")
        self.out_dirs: DirsTuple = self.set_out_dirs()
        ddlog_config = _cfg.get("ddlog")
        if ddlog_config:
            self.ddlog_pe_bin: Path = Path(ddlog_config.get("pe_bin")).expanduser()
        self.log_level = "warn"

    @staticmethod
    def set_doop_cfg(doop: dict) -> DoopCfg:
        path = Path(doop["doop_root"]).expanduser()
        atype: str = doop.get("analysis", "context-insensitive")
        annotate: bool = doop.get("annotation").get("annotate", False)
        sha1_digit: int = doop.get("annotation").get("sha1digits", 8)
        main_class: str = doop.get("main_class", "")
        flavor: str = doop.get("flavor")
        ddlog_config = doop.get("ddlog")
        if ddlog_config:
            program_path: Path = Path(ddlog_config.get("doop_program")).expanduser()
        else:
            program_path = Path("")
        return DoopCfg(path, atype, annotate, sha1_digit, main_class, flavor, program_path)

    def set_out_dirs(self) -> DirsTuple:  # Tuple[Path, Path, Path, Path, Path, Path, Path]:
        output_dir = Path(self.output_dir)
        log_dir: Path = output_dir / 'logs'
        jar_dir: Path = output_dir / "bytecode"
        facts_storage: Path = output_dir / 'facts'
        merged_facts: Path = output_dir / 'merged'
        analysis_storage: Path = output_dir / "analysis"
        stat_storage: Path = output_dir / "stat"
        gumtree: Path = output_dir / "gumtree"
        ddlog: Path = output_dir / "ddlog"
        return DirsTuple(log_dir, jar_dir, facts_storage, merged_facts,
                         analysis_storage, stat_storage, gumtree, ddlog)

    def __str__(self):
        info_dict = {self.config_file: {"repo_path": self.repo_path, "project_name": self.project_name,
                                        "rev_range": self.rev_range, "njobs": self.njobs,
                                        "output_dir": self.output_dir}}
        return pprint.pformat(info_dict, indent=2)

    def load_config(self) -> OrderedDict:
        return toml.load(self.config_file, _dict=OrderedDict)

    @staticmethod
    def prompt_err_and_exit(err_type: CfgErrType, err_key: tuple = (), err_msg: str = ""):
        if err_type == 2:
            for k in err_key:
                logger.error(f"'{k}' is required in the config.")
        elif err_type == 3:
            for k in err_key:
                logger.error(f"Value of '{k}' in the config is invalid.")
        elif err_type == 1:
            logger.error(f"Invalid configuration.")
        if not err_msg:
            logger.error(err_msg)
        exit(ErrorCode.INVALID_CONF)

    def check_key(self, required_keys: set, cfg_dict: dict) -> bool:
        have_keys = cfg_dict.keys()
        if required_keys.issubset(have_keys):
            return True
        else:
            self.prompt_err_and_exit(CfgErrType.MISSING, tuple(required_keys - have_keys), "Check configuration")

    def set_input(self, cfg: dict) -> None:
        # input has been checked above, no need to check AttributeError
        repo_path = cfg.get("input").get("repo_path")
        if repo_path is None:
            self.prompt_err_and_exit(CfgErrType.MISSING, ("repo_path",))
        if repo_path == "":
            self.prompt_err_and_exit(CfgErrType.INVALID, ("repo_path",))
        if not os.path.isabs(repo_path):
            repo_path = os.path.join(os.getcwd(), repo_path)
        self.repo_path = repo_path
        self.rev_range = cfg.get("input").get("rev_range")

        self.project_name = cfg.get("input").get("project_name")
        if not self.project_name:
            self.project_name = os.path.basename(self.repo_path)

    def create_output_dir(self, cfg: dict) -> str:
        """
        Create output dir according to project name and output_dir in configuration file
        :param cfg: cfg dict
        :return: output dir created in str
        """
        output_dir = cfg.get("output").get("output_dir")
        time_sfx = cfg.get("output").get("time_suffix", True)
        if not os.path.isabs(output_dir):
            output_dir = os.path.join(self.repo_path, output_dir)
        subdir = self.project_name
        if time_sfx:
            cur_time = get_cur_time_str()
            subdir = f"{subdir}_{cur_time}"
        output_dir = os.path.join(output_dir, subdir)  # type: str
        if check_dir(output_dir, make_if_not=True):
            logger.info("Results will be in {}".format(output_dir))
        else:
            exit(ErrorCode.PATH_ERROR)
        return output_dir

    def get_njobs(self, cfg):
        njobs = cfg.get("exe-ctrl").get("multi_process")  # type: int
        if njobs <= 0:
            njobs = os.cpu_count()
        return njobs

    def expect_flavor_or_exit(self, flavor: str, info: str):
        if self.doop.flavor != flavor:
            logger.error(f"{info}, abort.")
            exit(ErrorCode.INVALID_CONF)
# CLASS__RunConfig__END
