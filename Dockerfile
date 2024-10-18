FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/tourguide-*.jar /app/tourguide.jar

ENTRYPOINT ["java", "-jar", "tourguide.jar"]