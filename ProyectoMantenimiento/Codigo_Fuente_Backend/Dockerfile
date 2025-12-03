# Multi-stage Dockerfile: build with Maven, run with a lightweight JRE
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application as an executable jar (skip tests to speed up)
RUN mvn clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the packaged jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring

# Expose the port that Render provides via environment variable
EXPOSE $PORT

# Set the PORT environment variable to 8080 as default (Render will override this)
ENV PORT=8080

# Use shell form to allow environment variable expansion
ENTRYPOINT java -Dserver.port="$PORT" -Dserver.address=0.0.0.0 -jar app.jar
