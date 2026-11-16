# syntax=docker/dockerfile:1

# ---- Build stage: compile and package the application with the Gradle wrapper ----
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /workspace

# Dependencies first, so they stay cached while only the source code changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon --quiet dependencies > /dev/null

# Tests run in CI (they need Docker for Testcontainers), not inside the image build.
COPY src src
RUN ./gradlew --no-daemon --quiet bootJar -x test \
    && java -Djarmode=tools -jar build/libs/warehouse-management-app-*.jar \
        extract --layers --launcher --destination extracted

# ---- Runtime stage: small JRE image, unprivileged user, layered application ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S warehouse && adduser -S -G warehouse warehouse
WORKDIR /app

# Layers from the least to the most frequently changing.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER warehouse
EXPOSE 8080

# Production profile: every secret must come from environment variables.
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q -O /dev/null http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
