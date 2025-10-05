# --- Build Stage ---
FROM gradle:8.10-jdk17 AS build
WORKDIR /workspace
COPY build.gradle settings.gradle* gradle.properties* ./
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle clean bootJar --no-daemon

# --- Runtime Stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /workspace/build/libs/cc-be.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
