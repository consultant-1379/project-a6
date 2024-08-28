FROM openjdk:11
ADD target/frontend-1.0-SNAPSHOT.jar frontend-1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=docker-dev" ,"-jar","frontend-1.0-SNAPSHOT.jar"]