# =================================================================
# 1단계: 빌드 환경 (Builder)
# Node.js 22.21.0 버전을 기반으로 프로젝트를 빌드합니다.
# =================================================================
FROM node:22.21.0-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# pnpm 설치 (alpine 버전에는 pnpm이 기본 포함되어 있지 않음)
RUN npm install -g pnpm

# 의존성 설치를 위해 package.json과 lock 파일만 먼저 복사
COPY package.json pnpm-lock.yaml ./

# 의존성 설치 (프로덕션용)
RUN pnpm install --frozen-lockfile

# 프로젝트 전체 소스코드 복사
COPY . .

# React 앱 빌드
RUN pnpm build


# =================================================================
# 2단계: 실행 환경 (Runner)
# 빌드된 정적 파일들을 서빙할 가벼운 웹서버(Nginx)를 사용합니다.
# =================================================================
FROM nginx:1.27.0-alpine

# Nginx 기본 설정을 삭제
RUN rm /etc/nginx/conf.d/default.conf

# 우리가 만든 Nginx 설정을 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드 단계(builder)에서 생성된 결과물(dist 폴더)을 Nginx가 서빙할 위치로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 컨테이너 외부로 80번 포트를 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]