# Stage 1: Build the app
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copy Maven wrapper scripts
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Copy the rest of the source
COPY src src

# Give execute permission to Maven wrapper
RUN chmod +x mvnw

# Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

RUN ls -l /app/target
RUN find /app/target

# Stage 2: Run the app
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
