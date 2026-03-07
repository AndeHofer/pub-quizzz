# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Installation von su-exec für den sicheren User-Wechsel
RUN apk add --no-cache su-exec

# Erstellen von User/Gruppe nur, wenn sie noch nicht existieren
RUN getent group spring || addgroup -S spring && \
    getent passwd spring || adduser -S spring -G spring

# Vorbereiten des Datenverzeichnisses
RUN mkdir -p /data && chown -R spring:spring /data

COPY --from=build /app/target/*.jar app.jar

# Start-Script erstellen, um Berechtigungen zur Laufzeit zu korrigieren
RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'chown -R spring:spring /data' >> /entrypoint.sh && \
    echo 'exec su-exec spring java -jar /app/app.jar' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

# Best Practice: Nicht als Root-User ausführen
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]