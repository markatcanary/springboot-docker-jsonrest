FROM amazoncorretto:8
MAINTAINER dcss
COPY target/docker-message-server-1.0.0.jar demo.jar
ENTRYPOINT ["java","-jar","/demo.jar"]