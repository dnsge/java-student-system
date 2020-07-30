# Build stage
FROM maven:3.6.3-openjdk-14-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

# Run stage
FROM openjdk:14-jdk-alpine
COPY --from=build /app/target/StudentSystem.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
