import logging
import os
import shutil
import sys
import warnings
from enum import unique, IntEnum

logger = logging.getLogger(__name__)


@unique
class ErrorCode(IntEnum):
    OTHERS = 1
    INVALID_CMDLINE = 2
    NOT_IMPLEMENTED = 3
    USER_TERM = 4
    PATH_ERROR = 5
    JACOCO_GEN_FAILED = 6


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
        # self._style._fmt = color + '[%(asctime)s] [%(levelname)8s] [%(name)s > %(funcName)s()] ' + color_reset + '%(message)s'
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
    if os.path.isdir(dir_path):
        return True
    elif not os.path.exists(dir_path):
        if make_if_not:
            logger.info(f"Create directory @ {os.path.abspath(dir_path)}")
            os.makedirs(dir_path)
            return True
        logger.warning(f"{dir_path} does not exist and make_if_not is not set to True")
        return False
    elif os.path.isfile(dir_path):
        logger.error(f"{dir_path} exists and is not a dir")
        return False


def restore_clean_repo(repo_path: str, suffix: str = "-fake"):
    if os.path.isdir(repo_path):
        logger.info(f'remove old repo "{repo_path}"')
        shutil.rmtree(repo_path)
    shutil.copytree(repo_path + suffix, repo_path)


def deprecation(message):
    warnings.warn(message, DeprecationWarning, stacklevel=2)
