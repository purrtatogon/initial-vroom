# Multi-stage build: first stage compiles, second stage only has the runtime JAR
# This keeps the final image small (no Maven, no source code, just the JRE + app)

# Build stage: uses Maven + JDK 21 to compile the Spring Boot app
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml first and download deps — this layer is cached unless pom.xml changes
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Now copy source and build — only this layer rebuilds when code changes
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage: lightweight JRE-only image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy just the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
