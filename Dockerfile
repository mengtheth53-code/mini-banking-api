# ----------------------------------------------------
# Stage 1: Build Application with Maven & Java 21
# ----------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ----------------------------------------------------
# Stage 2: Minimal Production JRE Runtime
# ----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as non-root user for enterprise container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from builder stage
COPY --from=builder /build/target/mini-banking-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
