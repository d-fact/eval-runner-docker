FROM maven:3.8.1-openjdk-8
WORKDIR /lifting
RUN apt update && apt install -y bash python3 python3-pip git
# RUN apt update && apt install -y bash python3 python3-pip wget tar autoconf automake bison build-essential doxygen flex g++ git libncurses5-dev libsqlite3-dev libtool make mcpp pkg-config zlib1g-dev maven openjdk-11-jdk-headless openjdk-11-jre-headless
COPY . .
ENV M2_HOME=/usr/share/maven
RUN cp -r /lifting/prefix/* /
# WORKDIR /lifting/souffle
# # build and install cudd
# RUN wget http://davidkebo.com/source/cudd_versions/cudd-3.0.0.tar.gz && tar xf cudd-3.0.0.tar.gz
# RUN cd cudd-3.0.0 && ./configure --enable-shared --enable-dddmp --enable-obj && make && make install
# # get souffle
# RUN git clone git://github.com/ramyshahin/souffle.git
# # apply a small patch, build and install var-aware-souffle
# RUN cd souffle && git apply ../fix.patch && ./bootstrap  && ./configure && make && make install

WORKDIR /lifting/dpad/driver/driver
# CMD ./run_exp.sh
ENTRYPOINT /bin/bash