import os
import os.path
import sys
import csv
import shutil
import subprocess as sub

SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) # Dir of this script
TEST_CLASSES_BACKUP_DIR = SCRIPT_DIR + '/../_test_classes_backup'

def isexec (fpath):
    if fpath == None: return False
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK) 

def which(program):
    fpath, fname = os.path.split(program)
    if fpath:
        if isexec (program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            exe_file = os.path.join(path, program)
            if isexec (exe_file):
                return exe_file
    return None

if __name__ == '__main__':
    option = sys.argv[1]
    repopath = sys.argv[2]
    mvn = which('mvn')
    os.chdir(os.path.join(repopath, 'flume-ng-core'))
    
    if option == 'compile':
        os.system("find .. -name classes -type d | xargs rm -rf")
        os.chdir(repopath)
        ret = sub.call ([mvn, 'compile'])
        if ret == 0:
            sys.exit(0)
        else:
            sys.exit(1)
    elif option == 'test':
        if not os.path.isdir(repopath + '/flume-ng-core/target/test-classes'):
            print 'copy test classes to repo!'
            shutil.copytree(TEST_CLASSES_BACKUP_DIR + '/test-classes', \
                            repopath + '/flume-ng-core/target/test-classes')
        ret = sub.call ([mvn, 'org.apache.maven.plugins:maven-surefire-plugin:2.20:test', '-Dtest=TestSpoolDirectorySource#testPutBasenameHeader'])
        print ret
        if ret == 0:
            sys.exit(0)
        else:
            sys.exit(1)
