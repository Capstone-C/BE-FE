# --- Build Stage ---
FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# --- Runtime Stage ---
# Alpine 대신 debian 기반 이미지 사용 (Tesseract 설치를 위해)
FROM eclipse-temurin:17-jdk

# Tesseract OCR 및 한글 언어 데이터 설치
RUN apt-get update && \
    apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-kor \
    libtesseract-dev \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Tesseract 데이터 경로 환경변수 설정
ENV TESSDATA_PREFIX=/usr/share/tesseract-ocr/5/tessdata

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]