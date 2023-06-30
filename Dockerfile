FROM  openjdk:8-jre
RUN mkdir -p /app
ADD ./target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]