#!/bin/bash

# Gemini AI 기능 빠른 테스트 스크립트

echo "🚀 Capstone Gemini AI 기능 테스트 시작"
echo ""

# 1. 환경 변수 확인
echo "📋 1단계: 환경 변수 확인"
echo "----------------------------------------"

if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ GEMINI_API_KEY가 설정되지 않았습니다."
    echo "   export GEMINI_API_KEY='your-key' 실행 필요"
    MISSING_ENV=true
else
    echo "✅ GEMINI_API_KEY: ${GEMINI_API_KEY:0:8}****"
fi

if [ "$MISSING_ENV" = true ]; then
    echo ""
    echo "⚠️  API 키 미설정으로 이미지 인식 기능을 사용할 수 없습니다."
    echo "   다른 기능은 정상 작동합니다."
    echo ""
fi

# 2. Docker Compose 실행
echo ""
echo "🐳 2단계: Docker Compose 실행"
echo "----------------------------------------"
echo "컨테이너를 빌드하고 실행합니다 (시간이 걸릴 수 있습니다)..."
echo ""

docker compose up -d --build

# 3. 서버 시작 대기
echo ""
echo "⏳ 3단계: 서버 시작 대기 (최대 60초)"
echo "----------------------------------------"

COUNTER=0
MAX_ATTEMPTS=30

while [ $COUNTER -lt $MAX_ATTEMPTS ]; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/health 2>/dev/null)
    
    if [ "$HTTP_STATUS" = "200" ]; then
        echo "✅ 서버가 정상적으로 시작되었습니다!"
        break
    fi
    
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER + 1))
done

echo ""

if [ $COUNTER -eq $MAX_ATTEMPTS ]; then
    echo "❌ 서버 시작 실패. 로그를 확인하세요:"
    echo "   docker compose logs backend"
    exit 1
fi

# 4. OCR 설정 확인
echo ""
echo "🔧 4단계: OCR 설정 확인"
echo "----------------------------------------"
curl -s http://localhost:8080/api/v1/health/ocr-config | python3 -m json.tool

# 5. 접속 정보 출력
echo ""
echo "✅ 테스트 환경 준비 완료!"
echo "----------------------------------------"
echo ""
echo "📍 접속 정보:"
echo "   - Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "   - API Docs: http://localhost:8080/v3/api-docs"
echo "   - 헬스체크: http://localhost:8080/api/v1/health"
echo ""
echo "📝 다음 단계:"
echo "   1. Swagger UI 접속"
echo "   2. POST /api/v1/auth/signup 으로 회원가입"
echo "   3. POST /api/v1/auth/login 으로 로그인 (JWT 토큰 획득)"
echo "   4. Authorize 버튼으로 JWT 토큰 입력"
echo "   5. POST /api/v1/refrigerator/scan/purchase-history 로 영수증 스캔"
echo ""
echo "📖 자세한 가이드: ./OCR_TEST_GUIDE.md"
echo ""
echo "🛑 종료하려면: docker compose down"
echo ""
