FROM openjdk:8-jdk
LABEL maintainer="contact@suveen.me"
WORKDIR /app
COPY build/libs/organizer-0.0.1-SNAPSHOT.jar /app/backend.jar
ENTRYPOINT ["java","-jar","backend.jar"]