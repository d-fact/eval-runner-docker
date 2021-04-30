#!/usr/bin/env python3
from scipy.stats.stats import pearsonr

data = [
    [2.51, 2.75, 136, 7328],
    [0.66, 0.11, 50, 1856],
    [1.42, 6.05, 101, 17839],
    [2.38, 4.72, 269, 9238],
    [2.41, 5.31, 100, 25528],
    [1.10, 2.69, 51, 2529],
    [4.00, 10.7, 262, 8817],
    [1.24, 0.89, 97, 8575],
    [1.47, 1.05, 79, 2353]
]


def main():
    cor_lof = pearsonr([x[0] for x in data], [x[1] for x in data])
    cor_history_len = pearsonr([x[0] for x in data], [x[2] for x in data])
    cor_loc = pearsonr([x[0] for x in data], [x[3] for x in data])
    print(f"correlation coefficient: {cor_lof}")
    print(f"correlation coefficient: {cor_history_len}")
    print(f"correlation coefficient: {cor_loc}")


if __name__ == "__main__":
    main()
