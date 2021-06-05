#!/usr/bin/env bash
set -eux
pip3 install -r /lifting/dpad/driver/requirements.txt
for x in "commons-csv" "commons-configuration" "commons-io" "commons-compress" "commons-lang" "commons-net"; do
    echo $x;
    git clone "https://github.com/apache/$x.git" "/lifting/.test/$x"
    python3 main.py -c "/lifting/.test/$x.toml" -o "/lifting/.test/$x/.dpad/tse-5vers-1call-plus-heap"
    ln -s /lifting/.test/$x/.dpad/fse21-eval/bytecode /lifting/bytecode/$x
    python3 doop_baseline.py -c "/lifting/.test/$x-orig.toml" -o "/lifting/.test/$x/.dpad/tse-5vers-1callplus-heap-orig"
done
