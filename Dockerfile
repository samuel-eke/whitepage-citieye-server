# ============================================================================
# STAGE 1: BUILD STAGE
# ============================================================================
# Purpose: Compile and package the Spring Boot application using Maven + JDK 21
# This stage is discarded after producing the fat JAR

FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder

WORKDIR /app

# Copy POM first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
# -DskipTests prevents test-phase plugins from attempting DB connections
RUN mvn dependency:go-offline -DskipTests -q

# Copy source code
COPY src ./src

# Build the application fat JAR
# -DskipTests: Tests should run in CI, not during image build
RUN mvn clean package -DskipTests -q

# ============================================================================
# STAGE 2: EXTRACT FAT JAR INTO LAYERS
# ============================================================================
# Purpose: Use Spring Boot layertools to split the fat JAR into cacheable layers
# Dependencies change rarely; application code changes frequently

FROM eclipse-temurin:21-jre-jammy AS extractor

WORKDIR /extraction

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar application.jar

# Extract the JAR using the official Spring Boot layertools mode
# This creates: dependencies/, spring-boot-loader/, snapshot-dependencies/, application/
RUN java -Djarmode=layertools -jar application.jar extract

# ============================================================================
# STAGE 3: RUNTIME IMAGE (FINAL)
# ============================================================================
# Purpose: Minimal runtime image with only what is needed to run the app
# Layers are ordered by change frequency to maximize Docker cache hits

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for container health checks (used by docker-compose healthcheck)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# ============================================================================
# LAYER 1: DEPENDENCIES (Rarely changing)
# ============================================================================
# Third-party libraries from pom.xml — only changes when dependencies update
COPY --from=extractor /extraction/dependencies/ ./

# ============================================================================
# LAYER 2: SPRING BOOT LOADER (Rarely changing)
# ============================================================================
# Spring Boot loader classes — only changes when Spring Boot version changes
COPY --from=extractor /extraction/spring-boot-loader/ ./

# ============================================================================
# LAYER 3: SNAPSHOT DEPENDENCIES (Occasionally changing)
# ============================================================================
# Snapshot dependencies — changes when SNAPSHOT versions are updated
COPY --from=extractor /extraction/snapshot-dependencies/ ./

# ============================================================================
# LAYER 4: APPLICATION CODE (Frequently changing)
# ============================================================================
# Your compiled classes and resources — changes on every code change
COPY --from=extractor /extraction/application/ ./

# ============================================================================
# RUNTIME CONFIGURATION
# ============================================================================

# Create a non-root user for security (best practice)
# Running as root in containers is a security risk
RUN useradd -u 1000 springuser && \
    chown -R springuser:springuser /app

# Switch to non-root user
USER springuser

# Expose the port the Spring Boot app runs on
EXPOSE 8080

# JVM options for containerized environments
# -XX:+UseG1GC: G1 garbage collector, good for server workloads
# -XX:MaxRAMPercentage: Use 75% of container memory limit for heap
# -XX:InitialRAMPercentage: Start with 50% of available memory
# -XX:MinRAMPercentage: Minimum heap for small containers
# -XX:+UseStringDeduplication: Reduce memory for duplicate strings
ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:MinRAMPercentage=25.0 \
               -XX:+UseStringDeduplication"

# Application entry point
# Spring Boot 3.x/4.x relocated JarLauncher to org.springframework.boot.loader.launch
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]