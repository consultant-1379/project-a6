FROM openjdk:11
ADD target/main-controller-1.0-SNAPSHOT.jar main-controller-1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker-dev" ,"-jar","main-controller-1.0-SNAPSHOT.jar"]

#COPY . /usr/src/myapp
#WORKDIR /usr/src/myapp
#RUN javac Main.java
#CMD ["java", "Main"]