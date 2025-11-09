# --- 1단계: 빌드 환경 (Builder) ---
# Node.js 22.21.0 버전을 기반으로 빌드 환경을 구성합니다.
FROM node:22.21.0-alpine AS builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 의존성 파일들을 먼저 복사합니다. (Docker 캐싱 최적화)
COPY package*.json ./
COPY pnpm-lock.yaml ./

# pnpm을 전역으로 설치합니다.
RUN npm install -g pnpm

# 의존성을 설치합니다.
RUN pnpm install

# 소스 코드를 복사합니다.
COPY . .

# dev용으로 앱을 빌드합니다.
RUN pnpm pnpm build --mode development


# --- 2단계: 프로덕션 환경 (Runner) ---
# 가벼운 웹 서버인 Nginx를 기반으로 최종 이미지를 만듭니다.
FROM nginx:1.27-alpine

# 빌드 단계에서 생성된 결과물(dist 폴더)을 Nginx가 웹 콘텐츠를 서빙하는 기본 폴더로 복사합니다.
COPY --from=builder /app/dist /usr/share/nginx/html

# SPA(Single Page Application)를 위한 Nginx 설정을 복사합니다.
# 이 설정은 어떤 경로로 접속하든 index.html을 먼저 보여주게 하여 React Router가 작동하도록 합니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 80번 포트를 외부에 노출합니다.
EXPOSE 80

# Nginx를 실행합니다.
CMD ["nginx", "-g", "daemon off;"]