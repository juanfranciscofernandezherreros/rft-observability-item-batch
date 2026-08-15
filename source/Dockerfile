# syntax=docker/dockerfile:1

# ---- Build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean verify -DskipITs

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/output && chown -R app:app /app

USER app

EXPOSE 9001

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=5 \
    CMD wget -q -O - http://localhost:9001/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
