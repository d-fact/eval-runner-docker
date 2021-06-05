# Variability-aware Doop 

This is a variability-aware fork of [Doop](https://bitbucket.org/yanniss/doop/).

## Build Instructions

1. Download and install [Variability-aware Soufflé](https://github.com/ramyshahin/souffle).

2. Download Variability-aware Doop:
```
$ git clone https://rshahin@bitbucket.org/rshahin/doop.git
```

3. Download the Doop platoforms repository next to your Doop base directory:
```
$ git clone https://rshahin@bitbucket.org/yanniss/doop-benchmarks.git
```

4. Doop preprequisites (JDK and [gradle](https://gradle.org/install/))

5. To make sure Doop builds:
```
$ cd doop
$ ./doop -h
```

## Running the Experiments

The benchmark are in the `doop/benchmarks` directory, and feature annotation mappings and feature models can be found at `doop/features`.

### Baseline
```
$ cd doop
$ ./run-baseline-min
$ ./run-baseline
```

### Lifted (Product-line)
```
$ cd doop
$ ./run-lifted
```

### With Feature Model
```
$ cd doop
$ ./run-withFM
```