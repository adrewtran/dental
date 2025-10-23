# Multi-stage Dockerfile for the Dental Spring Boot app
# Build stage
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
# Use Maven to build the fat jar
RUN mvn -B -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built JAR from the build stage
COPY --from=build /workspace/target/dental-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

