#!/usr/bin/env python3
from typing import TypedDict, Tuple, Dict
from enum import Enum
from pathlib import Path
import re
import logging

logger = logging.getLogger(__name__)
re_match_pbid = re.compile(
    r'\\DefMacro{([a-zA-z]+\d{1,3})(notool|ekstazi|clover|starts|hyrts)(NumOfRunTests|NumOfFailedTests|FixedNumOfRunTests|FixedNumOfFailedTests)}{(\d+)}')


class RTStool(Enum):
    notool = 1
    ekstazi = 2
    clover = 3
    starts = 4
    hyrts = 5


class ToolResult(Enum):
    NumOfRunTests = 1
    NumOfFailedTests = 2
    FixedNumOfRunTests = 3
    FixedNumOfFailedTests = 4


def gen_key(pbid: str, tool: RTStool, numkey: ToolResult) -> str:
    """
    concat a string used as key in the data dict
    :param pbid: such as lang28
    :param tool: one of the RTS tool
    :param numkey: one of the ToolResult
    :return: a string used as key in the data dict
    """
    return f"{pbid}-{tool.name}-{numkey.name}"


def get_nums(data_dict, pbid, tool, numkey) -> int:
    return data_dict[gen_key(pbid, tool, numkey)]


def read_tex(tex_file: Path) -> Dict[str, int]:
    logger.info(f"Read existing data in {tex_file.resolve()}")
    with tex_file.open() as in_f:
        r: Dict[str, int] = dict()
        for line in in_f:
            if "FlakyFlag" in line:
                continue
            m = re_match_pbid.match(line.strip())
            if not m:
                logger.warning(f"Cannot match {line}")
                continue
            pbid, tool, numkey, numval = tuple((m.group(i) for i in range(1, 5)))
            r[gen_key(pbid, RTStool[tool], ToolResult[numkey])] = int(numval)
        return r
