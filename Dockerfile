# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Wir installieren 'su-exec', um später sicher den User zu wechseln
RUN apk add --no-cache su-exec && \
    addgroup -S spring && \
    adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

# Wir erstellen ein kleines Start-Script direkt im Dockerfile
RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'mkdir -p /data && chown -R spring:spring /data' >> /entrypoint.sh && \
    echo 'exec su-exec spring java -jar /app/app.jar' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

# Best Practice: Nicht als Root-User ausführen
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]