# Build stage
FROM maven:3.8.6-openjdk-17-alpine AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-alpine AS runner

WORKDIR /app
RUN ls -l
COPY --from=builder /app/target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
