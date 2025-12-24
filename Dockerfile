# Build stage
FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY backend-springboot/mvnw .
COPY backend-springboot/.mvn .mvn
COPY backend-springboot/pom.xml .
COPY backend-springboot/src src
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
