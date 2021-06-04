# build stage
FROM maven:3.6.3-jdk-8-slim AS build
COPY . /home/app
RUN mvn -f /home/app/pom.xml package -DskipTests
# run stage
FROM openjdk:8-jre-slim
RUN mkdir /data
COPY --from=build /home/app/target/cslicer-1.0.0-jar-with-dependencies.jar /usr/local/lib/cslicer.jar
ENV M2_HOME=/usr/share/maven
ENTRYPOINT ["java","-jar","/usr/local/lib/cslicer.jar"]
# ENTRYPOINT "/bin/sh"
