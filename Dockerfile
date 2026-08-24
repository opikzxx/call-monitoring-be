# Development stage with hot-reload (Gradle continuous build + Spring Boot DevTools)
FROM eclipse-temurin:21-jdk-jammy AS development

WORKDIR /app

# Copy only the files needed to resolve dependencies first, so this layer is
# cached and reused unless build.gradle/settings.gradle actually change.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# Copy source code (in docker compose, a bind mount shadows this at runtime)
COPY . .
COPY docker/dev-entrypoint.sh ./docker/dev-entrypoint.sh
RUN chmod +x ./docker/dev-entrypoint.sh ./gradlew

EXPOSE 8080

# bootRun itself never finishes (it blocks on the running server), so Gradle's
# continuous build cannot cancel/rerun it. Instead run two processes: a
# continuous `classes` recompile, and a plain `bootRun`. Spring Boot DevTools
# (running inside the bootRun JVM) watches the compiled output directly and
# restarts the app in-place whenever the continuous build rewrites it.
ENTRYPOINT ["sh", "./docker/dev-entrypoint.sh"]

# Production build stage
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

COPY . .
RUN ./gradlew bootJar --no-daemon

# Production final stage
FROM eclipse-temurin:21-jre-jammy AS production

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
