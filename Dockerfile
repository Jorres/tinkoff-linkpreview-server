FROM openjdk:11-slim

ADD build/libs/tinkoff-linkpreview-server-0.0.1-all.jar /usr/src/service.jar

EXPOSE 8090

ENTRYPOINT java -jar /usr/src/service.jar
