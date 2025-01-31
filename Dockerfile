# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
 
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests
 
# Run stage
FROM eclipse-temurin:17-jdk-alpine AS runner
 
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
 
CMD ["java", "-jar", "app.jar"]
