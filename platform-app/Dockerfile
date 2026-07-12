# QQQ Business Platform Application
FROM eclipse-temurin:21-jdk-jammy AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy source and build only the application dependency graph.
COPY . .
RUN mvn -pl platform-app -am clean package -DskipTests -B

# Runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create application user
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && groupadd -r qqq && useradd -r -g qqq qqq

# Copy built JAR
COPY --from=builder --chown=qqq:qqq /app/platform-app/target/platform-app-*.jar app.jar

# JVM optimizations for containers
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

USER qqq

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]