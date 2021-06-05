FROM debian:sid-slim
WORKDIR /lifting
RUN apt update && apt install -y bash python3 python3-pip wget tar autoconf automake bison build-essential doxygen flex g++ git libncurses5-dev libsqlite3-dev libtool make mcpp pkg-config zlib1g-dev
COPY . .

WORKDIR /lifting/souffle
# build and install cudd
RUN wget http://davidkebo.com/source/cudd_versions/cudd-3.0.0.tar.gz && tar xf cudd-3.0.0.tar.gz
RUN cd cudd-3.0.0 && ./configure --enable-shared --enable-dddmp --enable-obj && make && make install
# get souffle
RUN git clone git://github.com/ramyshahin/souffle.git
# apply a small patch, build and install var-aware-souffle
RUN cd souffle && git apply ../fix.patch && ./bootstrap  && ./configure && make && make install

WORKDIR /lifting/dpad/driver/driver
CMD ./run_exp.sh