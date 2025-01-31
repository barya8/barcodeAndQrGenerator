# Build stage
FROM maven:3.9.1-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine AS runner

WORKDIR /app
RUN ls -l
COPY --from=builder /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
