# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ordner erstellen und Rechte vergeben
RUN mkdir /data && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown spring:spring /data

COPY --from=build /app/target/*.jar app.jar
# Best Practice: Nicht als Root-User ausführen
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]