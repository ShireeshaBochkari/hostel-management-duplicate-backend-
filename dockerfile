# ==========================================
# Stage 1: Build stage with Maven and Java 21
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies (efficient caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Package Spring Boot executable jar without running unit tests during build
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime stage with minimal Java 21 JRE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled jar artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Default port for Render web service
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]