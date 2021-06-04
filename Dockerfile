FROM openjdk:8-jdk
WORKDIR /tool
RUN apt update && apt install -y git maven python3 python3-pip tar xz-utils
COPY . .
# install python util
RUN pip3 install .
# build modified cslicer
ENV M2_HOME=/usr/share/maven
WORKDIR /tool/gitslice
RUN mvn -f /tool/gitslice/pom.xml package -DskipTests
# copy data
COPY data /data
# build jgrok
WORKDIR /tool/grok-v107
RUN /bin/bash c.sh

WORKDIR /tool/factutils
