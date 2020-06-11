FROM openjdk:8-jre
ARG JAR_FILE=target/webtoon-recomm-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} webtoon-recomm-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/webtoon-recomm-0.0.1-SNAPSHOT.jar"]