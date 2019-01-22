FROM openjdk:8-jdk-alpine

ARG JAR_FILE=target/dummy-back-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} dummy-back.jar

RUN mkdir config

EXPOSE 8080

ENTRYPOINT ["java"]
CMD [ "-Dlogging.config=/config/logback.xml", "-Dspring.config.location=/config/", "-jar", "dummy-back.jar" ]