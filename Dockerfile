FROM ubuntu:latest

RUN apt-get -y update && apt-get -y install openjdk-8-jdk git openjdk-8-jdk wget unzip

RUN mkdir /opt/gradle && wget https://services.gradle.org/distributions/gradle-4.8-bin.zip
RUN unzip -d /opt/gradle gradle-* && ln -s /opt/gradle/gradle-4.8/bin/gradle /usr/bin/
RUN git clone https://github.com/BhavyanshM/SlackBot.git

WORKDIR SlackBot

CMD bash
