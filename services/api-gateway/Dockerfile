FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY build/libs/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
