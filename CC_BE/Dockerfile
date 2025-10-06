# --- Build Stage ---
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# --- Runtime Stage ---
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]