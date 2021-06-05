from enum import unique, IntEnum
import datetime
import logging
import os
from pathlib import Path
from subprocess import Popen, PIPE

import sys

logger = logging.getLogger(__name__)


@unique
class ErrorCode(IntEnum):
    OTHERS = 1
    INVALID_CMDLINE = 2
    INVALID_CONF = 3
    USER_TERM = 4
    PATH_ERROR = 5
    JACOCO_GEN_FAILED = 6
    NOT_IMPLEMENTED = 7


class ColorFormatter(logging.Formatter):
    COLORS = {
        logging.DEBUG: "\033[96m",
        logging.INFO: "\033[92m",
        logging.WARNING: "\033[93m",
        logging.ERROR: "\033[91m",
        logging.CRITICAL: "\033[01;91m\033[47m",  # bold red on white background
        'RESET': "\033[0m"
    }

    def format(self, record):
        color = self.COLORS[record.levelno]
        color_reset = self.COLORS["RESET"]
        self.datefmt = "%m-%d %H:%M:%S"
        self._style._fmt = color + '[%(asctime)s] [%(levelname)8s] ' + color_reset + '%(message)s'
        return super().format(record)


def init_logging(log_level="warning"):
    rootlogger = logging.getLogger()
    if log_level is None:
        log_level = "warning"
    numeric_level = getattr(logging, log_level.upper(), None)
    if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % log_level)
    rootlogger.setLevel(numeric_level)
    handler = logging.StreamHandler(sys.stderr)
    handler.setFormatter(ColorFormatter())
    rootlogger.addHandler(handler)


def check_dir(dir_path: str, make_if_not: bool = True) -> bool:
    """
    check if dir exists, and (optinal) mkdir -p if not.
    :param dir_path: path of target directory
    :param make_if_not: if True, create the dir and necessary parent dirs if path does not exist
    :return: True if dir_path is created or is existing dir, False otherwise
    """
    if os.path.isdir(dir_path):
        return True
    elif not os.path.exists(dir_path):
        if make_if_not:
            logger.info(f"Create directory @ {os.path.abspath(dir_path)}")
            os.makedirs(dir_path)
            return True
        logger.error(f"{dir_path} does not exist and make_if_not is not set to True")
        return False
    else:
        logger.error(f"{dir_path} exists and is not a dir")
        return False


def get_cur_time_str() -> str:
    return str(datetime.datetime.now().isoformat()).replace(':', '-')


def log_popen(p: Popen, log_dir: Path) -> bool:
    """
    write stdout, stderr from popen to log_dir
    :param p: Popen object
    :param log_dir: path to logs
    :return: False if err, True otherwise
    """
    check_dir(str(log_dir), make_if_not=True)
    out, err = p.communicate()
    if out:
        with (log_dir / 'out.log').open('w') as f:
            f.write(out.decode())
    if err:
        with (log_dir / 'err.log').open('w') as f:
            f.write(err.decode())
        return False
    return True
