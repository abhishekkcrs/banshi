# Use the official OpenJDK 17 image from Docker Hub
FROM eclipse-temurin:17-jdk AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and the pom.xml
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Make the mvnw file executable
RUN chmod +x mvnw

# Download dependencies and build the project
RUN ./mvnw clean package

# Now create a smaller image with only the JAR file
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

RUN ls -l /app/target


# Copy the jar file built in the first stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port that Spring Boot will use
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
