# --- Build stage ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache dependency resolution separately from source compilation
COPY pom.xml .
RUN apk add --no-cache maven && mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/TeleportAssignment-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
