#!/bin/bash

# 더미 데이터 로드 스크립트
# Usage: ./load-dummy-data.sh

set -e

echo "🔄 더미 데이터 로딩 시작..."
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# MySQL 컨테이너 확인
if ! docker ps | grep -q cc_mysql; then
    echo -e "${RED}❌ MySQL 컨테이너가 실행중이지 않습니다.${NC}"
    echo "먼저 'docker-compose up -d mysql'을 실행하세요."
    exit 1
fi

echo -e "${GREEN}✅ MySQL 컨테이너 확인 완료${NC}"
echo ""

# MySQL이 준비될 때까지 대기
echo "⏳ MySQL 준비 대기 중..."
until docker exec cc_mysql mysqladmin ping -h localhost -u root -prootpass --silent 2>/dev/null; do
    echo -n "."
    sleep 1
done
echo ""
echo -e "${GREEN}✅ MySQL 준비 완료${NC}"
echo ""

# 기존 데이터 확인
echo "📊 현재 데이터 확인..."
MEMBER_COUNT=$(docker exec cc_mysql mysql -u ccuser -pdevpass ccdb -se "SELECT COUNT(*) FROM members;" 2>/dev/null || echo "0")
POST_COUNT=$(docker exec cc_mysql mysql -u ccuser -pdevpass ccdb -se "SELECT COUNT(*) FROM posts;" 2>/dev/null || echo "0")

echo "  현재 회원 수: ${MEMBER_COUNT}"
echo "  현재 게시글 수: ${POST_COUNT}"
echo ""

# 데이터 삭제 확인
if [ "$MEMBER_COUNT" -gt 0 ] || [ "$POST_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  기존 데이터가 존재합니다.${NC}"
    read -p "기존 데이터를 삭제하고 새로 로드하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "취소되었습니다."
        exit 0
    fi
    
    echo ""
    echo "🗑️  기존 데이터 삭제 중..."
    
    # 외래키 제약조건 비활성화 및 데이터 삭제
    docker exec cc_mysql mysql -u ccuser -pdevpass ccdb << 'EOF'
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE comment;
TRUNCATE TABLE post_like;
TRUNCATE TABLE post_scrap;
TRUNCATE TABLE PostIngredient;
TRUNCATE TABLE posts;
TRUNCATE TABLE diary;
TRUNCATE TABLE refrigerator_item;
TRUNCATE TABLE recipe_ingredient;
TRUNCATE TABLE recipe;
TRUNCATE TABLE category;
TRUNCATE TABLE member_block;
TRUNCATE TABLE password_reset_token;
TRUNCATE TABLE member_password_history;
TRUNCATE TABLE members;

SET FOREIGN_KEY_CHECKS = 1;
EOF
    
    echo -e "${GREEN}✅ 기존 데이터 삭제 완료${NC}"
    echo ""
fi

# 더미 데이터 로드
echo "📥 더미 데이터 로드 중..."
docker exec -i cc_mysql mysql -u ccuser -pdevpass ccdb < CC_BE/mysql-init/02-dummy-data.sql

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ 더미 데이터 로드 성공!${NC}"
    echo ""
    
    # 최종 데이터 확인
    echo "📊 로드된 데이터 요약:"
    docker exec cc_mysql mysql -u ccuser -pdevpass ccdb << 'EOF'
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT CONCAT('👥 회원: ', COUNT(*), '명') AS '' FROM members;
SELECT CONCAT('📂 카테고리: ', COUNT(*), '개') AS '' FROM category;
SELECT CONCAT('📖 레시피: ', COUNT(*), '개') AS '' FROM recipe;
SELECT CONCAT('🥬 냉장고 재료: ', COUNT(*), '개') AS '' FROM refrigerator_item;
SELECT CONCAT('📝 게시글: ', COUNT(*), '개') AS '' FROM posts;
SELECT CONCAT('💬 댓글: ', COUNT(*), '개') AS '' FROM comment;
SELECT CONCAT('❤️  좋아요: ', COUNT(*), '개') AS '' FROM post_like;
SELECT CONCAT('⭐ 스크랩: ', COUNT(*), '개') AS '' FROM post_scrap;
SELECT CONCAT('🍽️  식단 일기: ', COUNT(*), '개') AS '' FROM diary;
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
EOF
    
    echo ""
    echo -e "${GREEN}🎉 모든 더미 데이터 로드 완료!${NC}"
    echo ""
    echo "📌 테스트 계정 정보:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  1. 일반 사용자 1"
    echo "     Username: testuser1"
    echo "     Password: password123"
    echo "     Email: user1@test.com"
    echo ""
    echo "  2. 일반 사용자 2"
    echo "     Username: testuser2"
    echo "     Password: password123"
    echo "     Email: user2@test.com"
    echo ""
    echo "  3. 일반 사용자 3"
    echo "     Username: testuser3"
    echo "     Password: password123"
    echo "     Email: user3@test.com"
    echo ""
    echo "  4. 모더레이터"
    echo "     Username: moderator"
    echo "     Password: password123"
    echo "     Email: mod@test.com"
    echo ""
    echo "  5. 관리자"
    echo "     Username: admin"
    echo "     Password: password123"
    echo "     Email: admin@test.com"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
else
    echo ""
    echo -e "${RED}❌ 더미 데이터 로드 실패${NC}"
    exit 1
fi
