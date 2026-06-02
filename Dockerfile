# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

# Copia el pom primero para aprovechar la cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el fuente y compila (salta tests; se corren en CI)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario no-root por seguridad
RUN addgroup -S batch && adduser -S batch -G batch
USER batch

# Copia el fat-jar desde el stage de build
COPY --from=build /workspace/target/emir-refit-batch-1.0.0.jar app.jar

# El entrypoint permite pasar argumentos desde K8s
# Ejemplo: --spring.batch.job.name=nightlyImportJob
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
