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

# WORKDIR /data
# RUN git clone https://github.com/rjust/defects4j.git
# RUN ./get_repos.sh

# extract RTS repos
WORKDIR /data/defects4j/project_repos
RUN tar xf /data/rts-repos/rts-repos.tar.xz

WORKDIR /tool/factutils
# RUN python3 -m hislicing.main --prepare /data/json/name_eval.json -l info
# CMD [ "python3", "-m" , "hislicing.main", "--cslicer", "/data/json/name_one.json" ]
# CMD [ "python3", "-m" , "hislicing.main", "--fact", "/data/json/group_one.json" ]
# ENTRYPOINT [ "python3", "-m" ]
