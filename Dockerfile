# Stage 1: Build
FROM maven:3.10.0-eclipse-temurin-25 AS build
#needed for node.js 26
RUN apt-get update \
    && apt-get install -y --no-install-recommends libatomic1 \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
ENV TZ=Europe/Vienna
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Europe/Vienna"
WORKDIR /app
RUN mkdir -p /data
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
