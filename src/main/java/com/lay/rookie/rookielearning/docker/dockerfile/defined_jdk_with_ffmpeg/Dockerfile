FROM jrottenberg/ffmpeg AS base
MAINTAINER leizhuang
ADD jdk-8u201-linux-x64.tar.gz /usr/local/
ENV JAVA_HOME /usr/local/jdk1.8.0_201
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH $PATH:$JAVA_HOME/bin

FROM base AS build
RUN mkdir /usr/local/java/
ENTRYPOINT ["java","-jar","/usr/local/java/app.jar"]
