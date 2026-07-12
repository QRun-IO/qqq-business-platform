# QQQ Business Platform Application
FROM openjdk:21-jdk-slim AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml .
COPY platform-bom/pom.xml platform-bom/
COPY platform-app/pom.xml platform-app/
COPY platform-api/pom.xml platform-api/
COPY qbits/identity/pom.xml qbits/identity/
COPY qbits/content/pom.xml qbits/content/
COPY qbits/newsletter/pom.xml qbits/newsletter/
COPY qbits/crm/pom.xml qbits/crm/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build application
RUN mvn clean package -DskipTests -B

# Runtime image
FROM openjdk:21-jdk-slim

WORKDIR /app

# Create application user
RUN groupadd -r qqq && useradd -r -g qqq qqq

# Copy built JAR
COPY --from=builder /app/platform-app/target/platform-app-*.jar app.jar
COPY --from=builder --chown=qqq:qqq /app/platform-app/target/platform-app-*.jar app.jar

# JVM optimizations for containers
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

USER qqq

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]