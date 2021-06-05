#!/usr/bin/env bash
set -eux
pip3 install -r /lifting/dpad/driver/requirements.txt
for x in "commons-csv"; do
    echo $x;
    git clone "https://github.com/apache/$x.git" "/lifting/.test/$x"
    mkdir -p "/lifting/.test/$x/.dpad/fse21-eval/" && ln -s "/lifting/bytecode/$x" "/lifting/.test/$x/.dpad/fse21-eval/bytecode"
    python3 main.py -l INFO -c "/lifting/.test/$x.toml" -o "/lifting/.test/$x/.dpad/fse21-eval"
    mkdir -p "/lifting/.test/$x/.dpad/fse21-eval-orig/" && ln -s "/lifting/bytecode/$x" "/lifting/.test/$x/.dpad/fse21-eval-orig/bytecode"
    python3 doop_baseline.py -l INFO -c "/lifting/.test/$x-orig.toml" -o "/lifting/.test/$x/.dpad/fse21-eval-orig"
done
