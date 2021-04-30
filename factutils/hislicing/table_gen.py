#!/usr/bin/env python3
import argparse
import json
from util import init_logging
from hislicing.env_const import Subjects
import logging
import numpy as np
import matplotlib.pyplot as plt
import matplotlib
from matplotlib import rcParams

matplotlib.use('TkAgg')

logger = logging.getLogger(__name__)

# font = {'family': 'serif',
#         'weight': 'regular',
#         'size': 26}

# matplotlib.rc('font', **font)
rcParams['font.family'] = 'serif'
rcParams['font.serif'] = ['Linux Libertine']


class ExpData:
    def __init__(self, name: str, fact_lines, fact_time, grok_time, cslicer_time):
        self.name = name
        self.fact_lines = fact_lines
        self.fact_time = fact_time
        self.grok_time = grok_time
        self.cslicer_time = cslicer_time

    def __str__(self):
        return f"{self.name}:{self.fact_lines},{self.fact_time},{self.grok_time},{self.cslicer_time}"

    def to_dict(self):
        return {"name": self.name, "fact_lines": self.fact_lines, "fact_time": self.fact_time,
                "grok_time": self.grok_time, "cslicer_time": self.cslicer_time}


def is_int(s: str) -> bool:
    try:
        int(s)
        return True
    except ValueError:
        return False


def process_data(line_count_f, fact_time_f, grok_time_f, cslicer_time_f) -> dict:
    results = dict()
    with open(line_count_f) as lf, open(fact_time_f) as ff, open(grok_time_f) as gf, open(cslicer_time_f) as cf:
        lf_dict = json.load(lf)
        ff_dict = json.load(ff)
        gf_dict = json.load(gf)
        cf_dict = json.load(cf)
        for k, v in lf_dict.items():
            lo_static = v.get("static")
            lo_diff = v.get("diff_tuple")
            fact_lines = {"static": lo_static}
            lo_dynamic = 0
            tests_count = 0
            for num in v.keys():
                if is_int(num):
                    lo_this_test = int(v.get(num))  # type: int
                    if lo_this_test == 0:
                        logger.warning(
                            f"Skip zero values for {k}:{num}, there must be errors in facts extraction process.")
                        continue
                    fact_lines.update({num: lo_this_test})
                    lo_dynamic += lo_this_test
                    tests_count += 1
            if lo_dynamic == 0:
                fact_lines.update({"ratio": "infinity"})
            else:
                fact_lines.update({"ratio": lo_diff / (lo_dynamic / tests_count)})
            if k not in results.keys():
                results[k] = ExpData(k, fact_lines, None, None, dict())
            else:
                logger.error("This should not happen.")
                # results[k].fact_lines = fact_lines
        for k, v in ff_dict.items():
            fact_time = {"static": v.get("general")}
            for num in v.keys():
                if is_int(num):
                    fact_time.update({num: v.get(num)})
            if k in results.keys():
                results[k].fact_time = fact_time
            else:
                logger.error("This should not happen.")
        for k, v in gf_dict.items():
            grok_time = {}
            for num in v.keys():
                if is_int(num):
                    grok_time.update({num: v.get(num)})
            if k in results.keys():
                results[k].grok_time = grok_time
            else:
                logger.error("This should not happen.")
        for k, v in cf_dict.items():
            name, num = k.split("-")
            for find_key, contents in results.items():
                if name in find_key and num in contents.fact_lines.keys():
                    results[find_key].cslicer_time[num] = v

    return results


def process_subjects_info(subjects_file: str):
    with open(subjects_file, 'r') as sf:
        return json.load(sf)


def main():
    args = handle_args()

    line_count_f = args.linecount
    fact_time_f = args.facttime
    grok_time_f = args.groktime
    cslicer_time_f = args.cslicer

    results = process_data(line_count_f, fact_time_f, grok_time_f, cslicer_time_f)
    if not results:
        logger.warning("Empty dictionary generated from input files")

    output_results = args.o
    if output_results:
        with open(output_results, 'w') as rf:
            json.dump({k: v.to_dict() for k, v in results.items()}, rf, indent=2)

    time_json = args.time
    time_dict = compare_time(results, time_json)
    if args.g:
        output_pgf_table(time_dict, "pgftable.tex")
        draw_graph(time_dict)

    tex_tbl_file = args.tbl
    if tex_tbl_file:
        subjects_f = args.subjects
        subjects_info = process_subjects_info(subjects_f)
        output_tex_table(results, time_dict, subjects_info, tex_tbl_file)


def compare_time(results: dict, output: str):
    output_dict = dict()
    for k, v in results.items():
        if k not in Subjects:
            logger.warning(f"Skip {k}")
            continue
        else:
            exp_id = Subjects.get(k)
            good_test = set()  # type: set
            for label in v.fact_lines.keys():
                if label is "static":
                    if v.fact_lines[label] == 0:
                        logger.error("This should not happen.")
                else:
                    if is_int(label) and v.fact_lines[label] != 0:
                        good_test.add(label)
            time_data = {"F_total": 0, "F_static": 0, "F_test": 0, "F_query": 0, "C_total": 0,
                         "N": len(good_test), "FvC": 1}
            for label, time_val in v.fact_time.items():
                time_val = float(time_val)
                if label is "static":
                    time_data["F_total"] += time_val
                    time_data["F_static"] += time_val
                elif label in good_test:
                    time_data["F_total"] += time_val
                    time_data["F_test"] += time_val
            for label, time_val in v.grok_time.items():
                if label in good_test:
                    time_data["F_total"] += time_val
                    time_data["F_query"] += time_val

            for label, time_val in v.cslicer_time.items():
                if label in good_test:
                    time_data["C_total"] += time_val
            time_data["FvC"] = float(time_data["F_total"]) / float(time_data["C_total"])
            output_dict[exp_id] = time_data
    logger.info(f"{len(output_dict)} effective subjects in total.")
    ftotal = sum(v["F_total"] for k, v in output_dict.items())
    ctotal = sum(v["C_total"] for k, v in output_dict.items())
    print("total time ratio: ", ftotal / ctotal)

    with open(output, 'w') as of:
        json.dump(output_dict, of, indent=2)
    return output_dict


def output_pgf_table(time_dict: dict, result_file: str) -> None:
    # xticks, staticTime, testTime, grokTime, staticPlusTest, cslicer_time = (list() for i in range(0, 6))
    with open(result_file, 'w') as of:
        str_write_to_file = "Project CSlicer Static Test Query\n"
        for k, v in time_dict.items():
            t_static = round(v["F_static"], 2)
            t_test = round(v["F_test"], 2)
            # t_static_test = v["F_static"] + v["F_test"]
            t_query = round(v["F_query"], 2)
            t_cslicer = round(v["C_total"], 2)
            str_write_to_file += f"{k} {t_cslicer} {t_static} {t_test} {t_query}\n"
        of.write(str_write_to_file)


def output_tex_table(results: dict, time_dict: dict, subjects_info: dict, result_file: str):
    with open(result_file, 'w') as of:
        str_write_to_file = "\\begin{table}[t]\n\\begin{small}\n\\centering\n\\caption{}\n\\label{}\n\\begin{tabular}{llllllcccc}\n\\toprule\n"
        str_write_to_file += "\\multirow{2}{*}{\\textbf{ID}}  & \\multirow{2}{*}{\\textbf{Test}} & \\multicolumn{3}{c}{\\textbf{Facts}} & \\multirow{2}{*}{\\textbf{\\cslicer}} & \\multirow{2}{*}{$\\cfrac{\\textbf{T(\\cslicer)}}{\\textbf{T(Facts)}}$}  & \\multirow{2}{*}{$\\cfrac{\\textbf{\\#(diff)}}{\\textbf{\\#(avg.tests)}}$} & \\multirow{2}{*}{\\textbf{HistoryLength}} & \\multirow{2}{*}{\\textbf{LOC (+/-)}}\\\\\n"
        str_write_to_file += "& & \\# Facts & Ext & Query & \\\\ \\midrule\n"
        for k, v in results.items():
            if k not in Subjects:
                logger.warning(f"Skip {k}")
                continue
            n_multi_row = len(v.fact_lines.keys()) - 1
            subject_name = Subjects.get(k)
            sinfo = subjects_info.get(subject_name)
            if not sinfo:
                logger.error(f"[{subject_name}] not found in subjects data. This should not happen.")
            loc_total = sinfo.get("addedLOC") + sinfo.get("deletedLOC")  # type: int
            history_len = sinfo.get("historyLength")  # type: int
            for t in v.fact_lines.keys():
                if t is "static":
                    str_write_to_file += f"\\multirow{{{n_multi_row}}}{{*}}{{{subject_name}}} & static & {v.fact_lines[t]} & {v.fact_time[t]:.2f} & -- & -- & \\multirow{{{n_multi_row}}}{{*}}{{{1 / time_dict[subject_name].get('FvC'):.2f}}} & \\multirow{{{n_multi_row}}}{{*}}{{{v.fact_lines.get('ratio'):.2f}}} & \\multirow{{{n_multi_row}}}{{*}}{{{history_len}}} & \\multirow{{{n_multi_row}}}{{*}}{{{loc_total}}} \\\\\n"
                elif is_int(t):
                    no_lines = v.fact_lines.get(t)
                    if int(no_lines) == 0:
                        continue
                    str_write_to_file += f" & {t} & {no_lines} & {v.fact_time[t]:.2f} & {v.grok_time[t]:.2f} & {v.cslicer_time[t]:.2f} &  &  &  & \\\\\n"
            str_write_to_file += "\\midrule\n"
        str_write_to_file = str_write_to_file[:-9]
        str_write_to_file += "\\bottomrule\n\\end{tabular}\n\\end{small}\n\\end{table}"
        of.write(str_write_to_file)


def handle_args():
    parser = argparse.ArgumentParser(description="compute difference of two generated list of commits")
    parser.add_argument("-l", metavar="LOG_LEVEL", type=str)
    parser.add_argument("--linecount", required=False, metavar="LINE_COUNT")
    parser.add_argument("--facttime", required=False, metavar="EXT_TIME")
    parser.add_argument("--groktime", required=False, metavar="QUERY_TIME")
    parser.add_argument("--cslicer", required=False, metavar="CSLICER_TIME")
    parser.add_argument("--subjects", required=False, metavar="SUBJECTS_INFO")
    parser.add_argument("-o", required=False, metavar="OUTPUT_RESULTS_FILE")
    parser.add_argument("--tbl", required=False, metavar="OUTPUT_TEX_TBL_FILE")
    parser.add_argument("--time", required=False, metavar="TIME_STAT")
    parser.add_argument("-g", action="store_true")
    args = parser.parse_args()
    init_logging(args.l)
    logger.debug(args)
    return args


def draw_graph(time_dict: dict):
    plt.style.use('ggplot')
    N = 10
    xticks, staticTime, testTime, grokTime, staticPlusTest, cslicer_time = (list() for i in range(0, 6))
    for k, v in time_dict.items():
        xticks.append(k)
        staticTime.append(v["F_static"])
        testTime.append(v["F_test"])
        staticPlusTest.append(v["F_static"] + v["F_test"])
        grokTime.append(v["F_query"])
        cslicer_time.append(v["C_total"])

    ind = np.arange(N)  # the x locations for the groups
    width = 0.35  # the width of the bars: can also be len(x) sequence
    # fig, ax = plt.subplots()

    logger.info(staticTime)
    logger.info(testTime)
    logger.info(grokTime)
    p1 = plt.bar(ind - width / 2, staticTime, width, hatch="/")
    p2 = plt.bar(ind - width / 2, testTime, width, bottom=staticTime)
    p3 = plt.bar(ind - width / 2, grokTime, width, bottom=staticPlusTest)
    p4 = plt.bar(ind + width / 2, cslicer_time, width)

    plt.ylabel('Time (seconds)')
    plt.title('')
    plt.xticks(ind, xticks, rotation=20)
    # plt.yticks(np.arange(0, 81, 10))
    plt.legend((p1[0], p2[0], p3[0], p4[0]), ('Static', 'Test', 'Query', 'CSlicer'))
    plt.savefig('slicingTime.png', transparent=False, dpi=300, bbox_inches="tight")
    plt.show()


if __name__ == "__main__":
    main()
