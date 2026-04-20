# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM gradle:8.7-jdk17-alpine AS build

WORKDIR /app

# Copia apenas os arquivos de configuração do build primeiro (cache de dependências)
COPY build.gradle settings.gradle gradle.properties ./

# Copia o código-fonte
COPY src ./src

# Gera o fat JAR (shadow jar)
RUN gradle shadowJar --no-daemon -q

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
