# Use a Java 21 base image (suitable for Spring Boot 3.x)
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR into the container (adjust the pattern if your JAR has a different name)
COPY build/libs/user-service-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]