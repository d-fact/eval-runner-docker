import os
import subprocess as sub
import logging

logger = logging.getLogger(__name__)
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__))
TEST_METHOD_DISTILL_JAR = os.path.join(SCRIPT_DIR, "bin/test_methods_distiller.jar")


def isexec(fpath):
    if fpath is None:
        return False
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK)


def which(program):
    fpath, fname = os.path.split(program)
    if fpath:
        if isexec(program):
            return program
    else:
        for each_path in os.environ["PATH"].split(os.pathsep):
            exe_file = os.path.join(each_path, program)
            if isexec(exe_file):
                return exe_file
    return None


def delRepeat(lst):
    for elem in lst:
        while lst.count(elem) > 1:
            del lst[lst.index(elem)]
    return lst


def extract_test_methods(test_class_file_path):
    java = which('java')
    if not java:
        logger.error("Java not found.")
        exit(2)
    t = sub.check_output([java, '-jar', TEST_METHOD_DISTILL_JAR, test_class_file_path],
                         text=True)
    return t.strip().split('\n')
