-- 더미 데이터 삽입 스크립트
-- 테스트 및 개발용 샘플 데이터

-- Character set 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE ccdb;

-- 1. 회원 데이터 (members)
-- 비밀번호는 모두 'password123' (BCrypt 암호화)
INSERT INTO members (email, nickname, password, role, export_score, joined_at, last_login_at, profile, updated_at) VALUES
('user1@test.com', '요리왕비룡', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user2@test.com', '맛있는하루', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user3@test.com', '냉장고지킴이', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user4@test.com', '건강밥상', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user5@test.com', '레시피마스터', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user6@test.com', '비건요리사', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user7@test.com', '초보요리', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('user8@test.com', '다이어터', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', 0, NOW(), NOW(), NULL, NOW()),
('mod@test.com', '모더레이터', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MODERATOR', 0, NOW(), NOW(), NULL, NOW()),
('admin@test.com', '관리자', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 0, NOW(), NOW(), NULL, NOW());

-- 2. 카테고리 데이터 (category)
-- type: RECIPE, FREE, QA, VEGAN
INSERT INTO category (name, type, parent_id) VALUES
-- 레시피 카테고리
('레시피', 'RECIPE', NULL),
-- 자유 게시판
('자유', 'FREE', NULL),
-- Q&A
('Q&A', 'QA', NULL),
-- 비건
('비건', 'VEGAN', NULL);

-- 3. 레시피 데이터 (recipe)
INSERT INTO recipe (name, description, instructions, cook_time, difficulty, servings, image_url, created_at, updated_at) VALUES
('김치찌개', '김치와 돼지고기로 만드는 얼큰한 찌개', '1. 돼지고기를 먹기 좋은 크기로 자릅니다.\n2. 냄비에 식용유를 두르고 김치를 볶습니다.\n3. 돼지고기를 넣고 함께 볶습니다.\n4. 물을 붓고 끓입니다.\n5. 두부와 대파를 넣고 한소끔 끓입니다.', 30, 'EASY', 2, NULL, NOW(), NOW()),
('된장찌개', '구수한 된장 맛이 일품인 찌개', '1. 멸치 육수를 준비합니다.\n2. 육수에 된장을 풀어줍니다.\n3. 감자, 애호박, 두부를 넣습니다.\n4. 끓으면 대파를 넣고 마무리합니다.', 25, 'EASY', 2, NULL, NOW(), NOW()),
('불고기', '달콤짭짤한 소불고기', '1. 소고기를 얇게 썰어 준비합니다.\n2. 간장, 설탕, 참기름, 다진마늘로 양념장을 만듭니다.\n3. 고기에 양념을 버무려 30분 재웁니다.\n4. 팬에 구워줍니다.', 40, 'MEDIUM', 4, NULL, NOW(), NOW()),
('김치볶음밥', '김치와 밥을 볶은 간단한 요리', '1. 김치를 잘게 썹니다.\n2. 팬에 식용유를 두르고 김치를 볶습니다.\n3. 밥을 넣고 함께 볶습니다.\n4. 계란 후라이를 올려 완성합니다.', 15, 'EASY', 1, NULL, NOW(), NOW()),
('된장국', '시원한 된장국', '1. 멸치 육수를 준비합니다.\n2. 된장을 풀어줍니다.\n3. 감자, 양파를 넣고 끓입니다.\n4. 대파를 넣고 마무리합니다.', 20, 'EASY', 2, NULL, NOW(), NOW()),
('계란말이', '부드러운 계란말이', '1. 계란을 풀어줍니다.\n2. 당근, 대파를 다져 넣습니다.\n3. 팬에 기름을 두르고 계란물을 부어 돌돌 말아줍니다.', 10, 'EASY', 2, NULL, NOW(), NOW()),
('떡볶이', '매콤달콤한 떡볶이', '1. 물에 고춧가루, 설탕, 간장을 넣고 소스를 만듭니다.\n2. 떡을 넣고 끓입니다.\n3. 어묵, 양배추를 넣고 졸입니다.', 20, 'EASY', 2, NULL, NOW(), NOW()),
('비빔밥', '건강한 비빔밥', '1. 나물을 각각 볶아 준비합니다.\n2. 밥 위에 나물을 올립니다.\n3. 고추장, 참기름을 넣고 비벼 먹습니다.', 25, 'MEDIUM', 1, NULL, NOW(), NOW()),
('잡채', '달콤한 잡채', '1. 당면을 삶아 준비합니다.\n2. 야채와 고기를 각각 볶습니다.\n3. 당면과 볶은 재료를 섞습니다.\n4. 간장, 설탕, 참기름으로 양념합니다.', 35, 'MEDIUM', 4, NULL, NOW(), NOW()),
('삼계탕', '보양식 삼계탕', '1. 닭 속에 찹쌀, 대추, 마늘을 넣습니다.\n2. 물을 붓고 1시간 이상 끓입니다.\n3. 소금으로 간을 맞춥니다.', 90, 'HARD', 1, NULL, NOW(), NOW()),
('제육볶음', '매콤한 제육볶음', '1. 돼지고기를 고추장 양념에 재웁니다.\n2. 야채를 썰어 준비합니다.\n3. 팬에 고기와 야채를 함께 볶습니다.', 30, 'MEDIUM', 3, NULL, NOW(), NOW()),
('순두부찌개', '얼큰한 순두부찌개', '1. 육수에 고춧가루를 풀어줍니다.\n2. 순두부를 넣습니다.\n3. 계란을 풀어 넣고 끓입니다.', 20, 'EASY', 1, NULL, NOW(), NOW()),
('된장찜', '고소한 된장찜', '1. 된장에 두부를 으깹니다.\n2. 야채를 다져 섞습니다.\n3. 찜기에 쪄줍니다.', 25, 'MEDIUM', 2, NULL, NOW(), NOW()),
('파전', '바삭한 파전', '1. 밀가루 반죽을 만듭니다.\n2. 대파를 길게 썰어 반죽에 섞습니다.\n3. 팬에 부쳐줍니다.', 20, 'EASY', 2, NULL, NOW(), NOW()),
('김치전', '고소한 김치전', '1. 김치를 다져서 밀가루 반죽과 섞습니다.\n2. 팬에 부쳐줍니다.', 15, 'EASY', 2, NULL, NOW(), NOW());

-- 4. 레시피 재료 데이터 (post_ingredient - posts 테이블의 레시피 게시글과 연결)
-- 레시피 게시글이 생성된 후에 해당 ID로 재료를 연결합니다
-- 이 섹션은 게시글 생성 후로 이동됩니다

-- 5. 냉장고 재료 데이터 (refrigerator_items)
INSERT INTO refrigerator_items (member_id, name, quantity, unit, expiration_date, memo, created_at, updated_at) VALUES
-- user1 냉장고
(1, '김치', 500, 'g', DATE_ADD(NOW(), INTERVAL 30 DAY), '배추김치', NOW(), NOW()),
(1, '계란', 10, '개', DATE_ADD(NOW(), INTERVAL 14 DAY), NULL, NOW(), NOW()),
(1, '우유', 900, 'ml', DATE_ADD(NOW(), INTERVAL 5 DAY), '1L 우유', NOW(), NOW()),
(1, '돼지고기', 300, 'g', DATE_ADD(NOW(), INTERVAL 3 DAY), '삼겹살', NOW(), NOW()),
(1, '두부', 1, '모', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NOW(), NOW()),
(1, '대파', 2, '대', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, NOW(), NOW()),
(1, '양파', 3, '개', DATE_ADD(NOW(), INTERVAL 20 DAY), NULL, NOW(), NOW()),
(1, '감자', 5, '개', DATE_ADD(NOW(), INTERVAL 25 DAY), NULL, NOW(), NOW()),
(1, '당근', 2, '개', DATE_ADD(NOW(), INTERVAL 15 DAY), NULL, NOW(), NOW()),
(1, '애호박', 1, '개', DATE_ADD(NOW(), INTERVAL 8 DAY), NULL, NOW(), NOW()),
-- user2 냉장고
(2, '소고기', 400, 'g', DATE_ADD(NOW(), INTERVAL 2 DAY), '불고기용', NOW(), NOW()),
(2, '당근', 2, '개', DATE_ADD(NOW(), INTERVAL 15 DAY), NULL, NOW(), NOW()),
(2, '계란', 6, '개', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, NOW(), NOW()),
(2, '밥', 2, '공기', DATE_ADD(NOW(), INTERVAL 2 DAY), '남은밥', NOW(), NOW()),
(2, '김치', 200, 'g', DATE_ADD(NOW(), INTERVAL 20 DAY), NULL, NOW(), NOW()),
(2, '양배추', 1, '개', DATE_ADD(NOW(), INTERVAL 12 DAY), '반통', NOW(), NOW()),
(2, '양파', 2, '개', DATE_ADD(NOW(), INTERVAL 20 DAY), NULL, NOW(), NOW()),
-- user3 냉장고
(3, '된장', 1, '통', DATE_ADD(NOW(), INTERVAL 180 DAY), NULL, NOW(), NOW()),
(3, '고춧가루', 100, 'g', DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, NOW(), NOW()),
(3, '간장', 500, 'ml', DATE_ADD(NOW(), INTERVAL 365 DAY), NULL, NOW(), NOW()),
(3, '참기름', 200, 'ml', DATE_ADD(NOW(), INTERVAL 180 DAY), NULL, NOW(), NOW()),
(3, '애호박', 1, '개', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NOW(), NOW()),
(3, '두부', 2, '모', DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NOW(), NOW()),
(3, '대파', 3, '대', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, NOW(), NOW()),
(3, '마늘', 1, '봉지', DATE_ADD(NOW(), INTERVAL 30 DAY), NULL, NOW(), NOW()),
-- user4 냉장고
(4, '닭고기', 500, 'g', DATE_ADD(NOW(), INTERVAL 3 DAY), '닭가슴살', NOW(), NOW()),
(4, '브로콜리', 1, '개', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NOW(), NOW()),
(4, '파프리카', 2, '개', DATE_ADD(NOW(), INTERVAL 10 DAY), '빨강, 노랑', NOW(), NOW()),
(4, '현미', 2, 'kg', DATE_ADD(NOW(), INTERVAL 180 DAY), NULL, NOW(), NOW()),
(4, '계란', 12, '개', DATE_ADD(NOW(), INTERVAL 14 DAY), NULL, NOW(), NOW()),
-- user5 냉장고
(5, '떡', 300, 'g', DATE_ADD(NOW(), INTERVAL 5 DAY), '가래떡', NOW(), NOW()),
(5, '어묵', 200, 'g', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NOW(), NOW()),
(5, '순두부', 1, '봉지', DATE_ADD(NOW(), INTERVAL 4 DAY), NULL, NOW(), NOW()),
(5, '계란', 8, '개', DATE_ADD(NOW(), INTERVAL 12 DAY), NULL, NOW(), NOW()),
(5, '김치', 300, 'g', DATE_ADD(NOW(), INTERVAL 25 DAY), NULL, NOW(), NOW()),
-- user6 냉장고 (비건)
(6, '두부', 3, '모', DATE_ADD(NOW(), INTERVAL 6 DAY), NULL, NOW(), NOW()),
(6, '시금치', 1, '봉지', DATE_ADD(NOW(), INTERVAL 4 DAY), NULL, NOW(), NOW()),
(6, '버섯', 200, 'g', DATE_ADD(NOW(), INTERVAL 5 DAY), '느타리버섯', NOW(), NOW()),
(6, '토마토', 5, '개', DATE_ADD(NOW(), INTERVAL 8 DAY), NULL, NOW(), NOW()),
(6, '아보카도', 2, '개', DATE_ADD(NOW(), INTERVAL 3 DAY), NULL, NOW(), NOW()),
-- user7 냉장고
(7, '라면', 5, '개', DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, NOW(), NOW()),
(7, '계란', 6, '개', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, NOW(), NOW()),
(7, '김', 1, '봉지', DATE_ADD(NOW(), INTERVAL 60 DAY), '도시락김', NOW(), NOW()),
(7, '치즈', 10, '장', DATE_ADD(NOW(), INTERVAL 15 DAY), '슬라이스치즈', NOW(), NOW()),
-- user8 냉장고 (다이어트)
(8, '닭가슴살', 500, 'g', DATE_ADD(NOW(), INTERVAL 3 DAY), NULL, NOW(), NOW()),
(8, '샐러드', 1, '봉지', DATE_ADD(NOW(), INTERVAL 3 DAY), '믹스샐러드', NOW(), NOW()),
(8, '방울토마토', 1, '팩', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NOW(), NOW()),
(8, '그릭요거트', 3, '개', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, NOW(), NOW());

-- 6. 식단 일기 데이터 (diary)
INSERT INTO diary (member_id, meal_type, date, content, image_url, recipe_id, created_at, updated_at) VALUES
-- user1 식단
(1, 'BREAKFAST', CURDATE(), '김치찌개와 밥으로 아침 식사', NULL, 1, NOW(), NOW()),
(1, 'LUNCH', CURDATE(), '된장찌개 정식', NULL, 2, NOW(), NOW()),
(1, 'DINNER', CURDATE() - INTERVAL 1 DAY, '불고기와 밥', NULL, 3, NOW(), NOW()),
(1, 'BREAKFAST', CURDATE() - INTERVAL 1 DAY, '계란말이와 김치', NULL, 6, NOW(), NOW()),
(1, 'SNACK', CURDATE(), '과일', NULL, NULL, NOW(), NOW()),
-- user2 식단
(2, 'BREAKFAST', CURDATE(), '계란말이와 김치', NULL, 6, NOW(), NOW()),
(2, 'LUNCH', CURDATE(), '김치볶음밥', NULL, 4, NOW(), NOW()),
(2, 'DINNER', CURDATE() - INTERVAL 1 DAY, '떡볶이', NULL, 7, NOW(), NOW()),
(2, 'SNACK', CURDATE() - INTERVAL 1 DAY, '커피와 빵', NULL, NULL, NOW(), NOW()),
-- user3 식단
(3, 'BREAKFAST', CURDATE(), '된장국과 밥', NULL, 5, NOW(), NOW()),
(3, 'LUNCH', CURDATE(), '비빔밥', NULL, 8, NOW(), NOW()),
(3, 'DINNER', CURDATE() - INTERVAL 1 DAY, '제육볶음', NULL, 11, NOW(), NOW()),
(3, 'BREAKFAST', CURDATE() - INTERVAL 2 DAY, '김치찌개', NULL, 1, NOW(), NOW()),
-- user4 식단 (건강식)
(4, 'BREAKFAST', CURDATE(), '닭가슴살 샐러드', NULL, NULL, NOW(), NOW()),
(4, 'LUNCH', CURDATE(), '현미밥과 구운 닭가슴살', NULL, NULL, NOW(), NOW()),
(4, 'DINNER', CURDATE(), '브로콜리 볶음', NULL, NULL, NOW(), NOW()),
(4, 'SNACK', CURDATE(), '프로틴 쉐이크', NULL, NULL, NOW(), NOW()),
-- user5 식단
(5, 'BREAKFAST', CURDATE(), '순두부찌개', NULL, 12, NOW(), NOW()),
(5, 'LUNCH', CURDATE(), '떡볶이', NULL, 7, NOW(), NOW()),
(5, 'DINNER', CURDATE() - INTERVAL 1 DAY, '파전', NULL, 14, NOW(), NOW()),
-- user6 식단 (비건)
(6, 'BREAKFAST', CURDATE(), '두부 스크램블', NULL, NULL, NOW(), NOW()),
(6, 'LUNCH', CURDATE(), '버섯 볶음', NULL, NULL, NOW(), NOW()),
(6, 'DINNER', CURDATE(), '토마토 파스타', NULL, NULL, NOW(), NOW()),
(6, 'SNACK', CURDATE(), '아보카도 토스트', NULL, NULL, NOW(), NOW()),
-- user7 식단
(7, 'BREAKFAST', CURDATE(), '치즈 토스트', NULL, NULL, NOW(), NOW()),
(7, 'LUNCH', CURDATE(), '라면', NULL, NULL, NOW(), NOW()),
(7, 'DINNER', CURDATE() - INTERVAL 1 DAY, '김밥', NULL, NULL, NOW(), NOW()),
-- user8 식단 (다이어트)
(8, 'BREAKFAST', CURDATE(), '그릭요거트와 방울토마토', NULL, NULL, NOW(), NOW()),
(8, 'LUNCH', CURDATE(), '닭가슴살 샐러드', NULL, NULL, NOW(), NOW()),
(8, 'DINNER', CURDATE(), '샐러드', NULL, NULL, NOW(), NOW()),
(8, 'SNACK', CURDATE(), '방울토마토', NULL, NULL, NOW(), NOW());

-- 7. 게시글 데이터 (posts)
INSERT INTO posts (author_id, category_id, title, content, is_recipe, status, view_count, like_count, comment_count, cook_time_in_minutes, difficulty, servings, diet_type, file, selected, created_at, updated_at) VALUES
-- 레시피 게시글 (category_id = 1)
(1, 1, '김치찌개 맛있게 끓이는 법', '김치찌개를 끓일 때는 묵은 김치를 사용하는 것이 포인트입니다.\n돼지고기와 함께 볶다가 물을 부어주세요.\n\n**재료**\n- 김치 300g\n- 돼지고기 200g\n- 두부 반 모\n- 대파 반 대\n\n**조리법**\n1. 돼지고기를 먹기 좋은 크기로 자릅니다.\n2. 냄비에 식용유를 두르고 김치를 볶습니다.\n3. 돼지고기를 넣고 함께 볶습니다.\n4. 물을 붓고 끓입니다.\n5. 두부와 대파를 넣고 한소끔 끓입니다.', 1, 'PUBLISHED', 45, 12, 3, 30, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(2, 1, '된장찌개 황금 레시피', '구수한 된장 맛을 내려면 멸치육수가 필수!\n\n**재료**\n- 된장 2큰술\n- 두부 1모\n- 애호박 1/2개\n- 감자 1개\n- 대파 1대\n- 멸치 육수 3컵\n\n**조리법**\n1. 멸치 육수를 끓입니다.\n2. 된장을 풀어줍니다.\n3. 감자, 애호박, 두부를 넣고 끓입니다.\n4. 대파를 넣고 마무리합니다.', 1, 'PUBLISHED', 38, 8, 2, 25, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(3, 1, '불고기 양념 황금비율', '간장 3: 설탕 2: 참기름 1의 비율로!\n\n**양념 재료**\n- 간장 3큰술\n- 설탕 2큰술\n- 참기름 1큰술\n- 다진마늘 1큰술\n- 후추 약간\n- 배 1/4개 (갈아서)\n\n**조리법**\n1. 소고기를 얇게 썰어 준비합니다.\n2. 모든 양념을 섞어 양념장을 만듭니다.\n3. 고기에 양념을 버무려 30분 재웁니다.\n4. 팬에 구워줍니다.', 1, 'PUBLISHED', 92, 34, 8, 40, 'MEDIUM', 4, 'GENERAL', 'FALSE', 'TRUE', NOW() - INTERVAL 5 DAY, NOW()),
(1, 1, '김치볶음밥 초간단 레시피', '냉장고 남은 김치로 5분만에 완성!\n\n**재료**\n- 밥 1공기\n- 김치 150g\n- 스팸 또는 햄 100g\n- 김치국물 2큰술\n- 참기름 1큰술\n- 계란 1개\n\n**조리법**\n1. 김치와 스팸을 잘게 썹니다.\n2. 팬에 식용유를 두르고 김치를 볶습니다.\n3. 밥을 넣고 함께 볶습니다.\n4. 김치국물과 참기름을 넣어 마무리합니다.\n5. 계란 후라이를 올려 완성합니다.', 1, 'PUBLISHED', 67, 23, 5, 15, 'LOW', 1, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(4, 1, '제육볶음 매콤하게', '고추장 양념에 고춧가루를 추가하면 더 매콤!\n\n**양념장**\n- 고추장 2큰술\n- 고춧가루 1큰술\n- 간장 1큰술\n- 설탕 1큰술\n- 다진마늘 1큰술\n- 생강가루 약간\n\n**재료**\n- 돼지고기 목살 400g\n- 양파 1개\n- 대파 1대\n- 양배추 1/4통\n\n돼지고기는 목살이나 앞다리살을 추천합니다!', 1, 'PUBLISHED', 78, 25, 6, 30, 'MEDIUM', 3, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(2, 1, '떡볶이 매운맛 조절법', '고춧가루 양을 조절하고 설탕을 약간 더 넣으면 덜 매워요.\n\n**재료**\n- 떡 300g\n- 어묵 150g\n- 양배추 2줌\n- 대파 1대\n- 계란 2개\n\n**소스**\n- 고춧가루 2큰술\n- 설탕 1.5큰술\n- 간장 1큰술\n- 물엿 1큰술\n\n물엿을 넣으면 윤기가 나고 덜 맵습니다!', 1, 'PUBLISHED', 56, 18, 4, 20, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 4 DAY, NOW()),
(5, 1, '계란말이 부드럽게 만들기', '우유를 넣으면 더 부드러워요!\n\n**재료**\n- 계란 4개\n- 우유 2큰술\n- 소금 약간\n- 당근 1/4개 (다져서)\n- 대파 1/2대 (다져서)\n\n**조리법**\n1. 계란을 풀고 우유, 소금, 다진 야채를 넣습니다.\n2. 팬에 기름을 두르고 달굽니다.\n3. 계란물을 3번에 나눠 부으며 돌돌 말아줍니다.\n\n불 조절이 중요해요. 약불에서 천천히!', 1, 'PUBLISHED', 41, 15, 2, 10, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW(), NOW()),
(3, 1, '순두부찌개 혼밥 레시피', '혼자 사는 사람들에게 강추!\n\n**재료**\n- 순두부 1봉지\n- 계란 1개\n- 고춧가루 1큰술\n- 다진마늘 1큰술\n- 멸치 육수 2컵\n- 대파, 청양고추\n\n**조리법**\n1. 육수에 고춧가루를 풀어줍니다.\n2. 순두부를 통째로 넣습니다.\n3. 한소끔 끓으면 계란을 풀어 넣습니다.\n4. 대파와 고추를 넣고 마무리합니다.', 1, 'PUBLISHED', 52, 16, 3, 20, 'LOW', 1, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(1, 1, '비빔밥 고추장 양념장', '비빔밥의 핵심은 양념장!\n\n**양념장 재료**\n- 고추장 3큰술\n- 참기름 2큰술\n- 설탕 1큰술\n- 식초 1큰술\n- 다진마늘 1작은술\n- 깨소금 1큰술\n\n**나물 재료**\n- 시금치, 콩나물, 당근, 애호박, 도라지\n- 계란 후라이\n\n각 나물을 볶아서 밥 위에 예쁘게 담고 양념장과 비벼 드세요!', 1, 'PUBLISHED', 63, 19, 5, 30, 'MEDIUM', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 6 DAY, NOW()),
(4, 1, '파김치 담그는 법', '쪽파로 만드는 아삭한 파김치!\n\n**재료**\n- 쪽파 1kg\n- 멸치액젓 5큰술\n- 고춧가루 1컵\n- 다진마늘 3큰술\n- 생강가루 1작은술\n- 설탕 2큰술\n- 찹쌀풀 1컵\n\n**조리법**\n1. 쪽파를 깨끗이 씻어 물기를 뺍니다.\n2. 찹쌀풀을 쑤어 식힙니다.\n3. 양념을 모두 섞습니다.\n4. 쪽파에 양념을 버무립니다.\n\n하루만 숙성시켜도 맛있어요!', 1, 'PUBLISHED', 34, 11, 2, 45, 'MEDIUM', 10, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 7 DAY, NOW()),

-- 비건 레시피 게시글 (category_id = 4)
(6, 4, '비건 두부 스크램블', '계란 대신 두부로 만드는 스크램블!\n\n**재료**\n- 두부 1모\n- 강황가루 1/4작은술\n- 양파 1/2개\n- 파프리카 1개\n- 소금, 후추\n- 올리브오일\n\n**조리법**\n1. 두부를 으깨서 물기를 뺍니다.\n2. 양파와 파프리카를 다집니다.\n3. 팬에 올리브오일을 두르고 야채를 �볶습니다.\n4. 두부를 넣고 강황가루, 소금, 후추로 간합니다.\n\n강황가루를 넣으면 색이 계란처럼 노래요!', 1, 'PUBLISHED', 34, 12, 2, 15, 'LOW', 1, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(6, 4, '비건 김치찌개', '멸치 육수 대신 다시마 육수로!\n\n**재료**\n- 김치 300g\n- 두부 1모\n- 표고버섯 5개\n- 대파 1대\n- 다시마 육수 3컵\n\n**조리법**\n1. 다시마 육수를 준비합니다.\n2. 김치를 볶다가 육수를 붓습니다.\n3. 두부와 버섯을 넣고 끓입니다.\n4. 대파를 넣고 마무리합니다.\n\n고기 없이도 깊은 맛이 나요!', 1, 'PUBLISHED', 28, 9, 3, 25, 'LOW', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(6, 4, '비건 볶음밥', '영양만점 비건 볶음밥!\n\n**재료**\n- 밥 2공기\n- 브로콜리 1/2개\n- 당근 1/2개\n- 양송이버섯 5개\n- 간장 2큰술\n- 참기름 1큰술\n- 마늘\n\n**조리법**\n1. 모든 야채를 잘게 다집니다.\n2. 팬에 참기름을 두르고 야채를 볶습니다.\n3. 밥을 넣고 함께 볶습니다.\n4. 간장으로 간을 맞춥니다.', 1, 'PUBLISHED', 45, 14, 4, 15, 'LOW', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(6, 4, '비건 토마토 파스타', '크림 없이 만드는 토마토 파스타!\n\n**재료**\n- 스파게티면 200g\n- 토마토 5개\n- 양파 1개\n- 마늘 5쪽\n- 바질\n- 올리브오일\n\n**조리법**\n1. 토마토를 갈거나 다집니다.\n2. 팬에 올리브오일, 마늘, 양파를 볶습니다.\n3. 토마토를 넣고 졸입니다.\n4. 삶은 면을 넣고 버무립니다.\n5. 바질을 올려 완성합니다.', 1, 'PUBLISHED', 51, 18, 5, 25, 'LOW', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 4 DAY, NOW()),
(6, 4, '비건 버섯 덮밥', '각종 버섯으로 만드는 건강 덮밥!\n\n**재료**\n- 표고버섯, 느타리버섯, 양송이버섯 각 100g\n- 양파 1개\n- 간장 3큰술\n- 설탕 1큰술\n- 참기름\n- 밥\n\n**조리법**\n1. 버섯을 먹기 좋게 자릅니다.\n2. 팬에 버섯과 양파를 볶습니다.\n3. 간장, 설탕으로 양념합니다.\n4. 밥 위에 올리고 참기름을 뿌립니다.', 1, 'PUBLISHED', 39, 13, 2, 20, 'LOW', 1, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 5 DAY, NOW()),
(6, 4, '비건 된장찌개', '동물성 재료 없이 구수하게!\n\n**재료**\n- 된장 2큰술\n- 두부 1모\n- 애호박 1개\n- 감자 1개\n- 표고버섯 3개\n- 다시마 육수\n\n**조리법**\n1. 다시마와 말린 표고버섯으로 육수를 냅니다.\n2. 된장을 풀어줍니다.\n3. 야채를 넣고 끓입니다.\n4. 대파를 넣고 마무리합니다.', 1, 'PUBLISHED', 42, 15, 3, 25, 'LOW', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 6 DAY, NOW()),
(6, 4, '비건 김밥', '맛있는 야채 김밥!\n\n**재료**\n- 김 5장\n- 밥 2공기\n- 당근, 시금치, 우엉, 단무지\n- 참기름, 깨소금\n\n**조리법**\n1. 밥에 참기름과 깨소금을 섞습니다.\n2. 각 야채를 볶아 준비합니다.\n3. 김 위에 밥을 펴고 재료를 올립니다.\n4. 돌돌 말아서 썰어줍니다.\n\n소풍 도시락으로 좋아요!', 1, 'PUBLISHED', 47, 16, 4, 30, 'MEDIUM', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 8 DAY, NOW()),
(6, 4, '비건 팟타이', '태국 요리도 비건으로!\n\n**재료**\n- 쌀국수 200g\n- 두부 1/2모\n- 숙주 100g\n- 부추 한 줌\n- 땅콩\n\n**소스**\n- 타마린드 소스 2큰술\n- 간장 2큰술\n- 라임즙 1큰술\n- 설탕 1큰술\n\n**조리법**\n1. 쌀국수를 삶아 준비합니다.\n2. 두부를 구워줍니다.\n3. 팬에 국수와 야채를 볶습니다.\n4. 소스를 넣고 버무립니다.', 1, 'PUBLISHED', 36, 12, 2, 25, 'MEDIUM', 2, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 9 DAY, NOW()),
(6, 4, '비건 카레', '코코넛 밀크로 만드는 크리미한 카레!\n\n**재료**\n- 감자 2개\n- 당근 1개\n- 양파 1개\n- 카레가루 3큰술\n- 코코넛 밀크 1캔\n- 물 2컵\n\n**조리법**\n1. 야채를 한입 크기로 자릅니다.\n2. 냄비에 야채를 볶습니다.\n3. 물과 카레가루를 넣고 끓입니다.\n4. 코코넛 밀크를 넣고 졸입니다.', 1, 'PUBLISHED', 53, 19, 6, 30, 'LOW', 3, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 10 DAY, NOW()),
(6, 4, '비건 라면', '동물성 재료 없이 맛있는 라면!\n\n**재료**\n- 라면 1개 (비건용)\n- 김치 100g\n- 두부 1/4모\n- 대파 1/2대\n- 참기름\n\n**조리법**\n1. 물을 끓입니다.\n2. 라면과 스프를 넣습니다.\n3. 김치, 두부를 넣습니다.\n4. 대파와 참기름으로 마무리합니다.\n\n김치가 들어가면 감칠맛이 살아나요!', 1, 'PUBLISHED', 41, 14, 3, 10, 'LOW', 1, 'VEGAN', 'FALSE', 'FALSE', NOW() - INTERVAL 11 DAY, NOW()),

-- 자유 게시판 글 (category_id = 2)
(1, 2, '요리 초보인데 추천 레시피 있나요?', '요리를 시작하려고 하는데 어떤 메뉴부터 시작하면 좋을까요?\n간단하면서도 맛있는 레시피 추천 부탁드려요!\n\n특히 실패 확률이 낮은 메뉴가 있을까요?', 0, 'PUBLISHED', 23, 5, 7, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(2, 2, '오늘 김치찌개 처음 만들어봤어요', '레시피 보고 김치찌개 처음 만들어봤는데 생각보다 쉽네요!\n다음엔 된장찌개도 도전해볼게요.\n\n근데 돼지고기 대신 참치 넣어도 되나요?', 0, 'PUBLISHED', 18, 8, 4, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(3, 2, '냉장고 파먹기 챌린지 시작!', '냉장고에 있는 재료들 다 떨어질 때까지 장 안 보기로 했어요.\n오늘은 남은 김치랑 계란으로 김치볶음밥 만들어 먹었습니다.\n\n여러분도 냉장고 파먹기 도전해보세요!', 0, 'PUBLISHED', 31, 12, 9, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(4, 2, '1인 가구 장보기 팁 공유해요', '혼자 살면서 장보기가 제일 어려운 것 같아요.\n자주 사면 귀찮고, 한 번에 많이 사면 버리게 되고...\n\n요즘은 주 1회 시장 가서 필요한 것만 사고 있어요.\n여러분은 어떻게 하시나요?', 0, 'PUBLISHED', 45, 15, 12, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 4 DAY, NOW()),
(5, 2, '요리하면서 음악 듣는 분?', '저는 요리할 때 신나는 음악 틀어놓고 해요.\n그럼 더 재밌게 요리하는 것 같아요 ㅎㅎ\n\n여러분은 요리할 때 뭐 하시나요?', 0, 'PUBLISHED', 27, 9, 8, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(7, 2, '처음으로 파전 성공했어요!', '그동안 파전 만들면 항상 눅눅했는데\n이번에 레시피 보고 만들었더니 바삭하게 성공!\n\n팬을 충분히 달구는 게 포인트였네요.', 0, 'PUBLISHED', 34, 14, 6, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW(), NOW()),
(8, 2, '다이어트 중인데 요리하기 힘들어요', '닭가슴살만 먹다보니 질려서...\n맛있으면서 건강한 레시피 없을까요?\n\n칼로리 낮은 레시피 추천 부탁드려요!', 0, 'PUBLISHED', 29, 11, 10, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(2, 2, '반찬 만들어서 일주일 먹기', '주말에 반찬 여러 가지 만들어두면 평일이 편해요.\n계란말이, 멸치볶음, 시금치나물 이런 거 만들어놨어요.\n\n여러분은 어떤 반찬 만들어 드시나요?', 0, 'PUBLISHED', 38, 16, 11, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 5 DAY, NOW()),
(3, 2, '요리 유튜브 추천해주세요', '요리 배우려고 유튜브 보는데 너무 많아서 헷갈려요.\n초보자가 보기 좋은 채널 추천 부탁드려요!\n\n설명이 자세한 채널이면 좋겠어요.', 0, 'PUBLISHED', 42, 13, 15, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(1, 2, '집밥이 최고예요', '밖에서 사먹다가 집에서 해먹으니 건강해지는 느낌이에요.\n돈도 절약되고 좋네요.\n\n요즘 매일 저녁 해먹고 있습니다!', 0, 'PUBLISHED', 36, 18, 7, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 6 DAY, NOW()),

-- Q&A 게시판 글 (category_id = 3)
(7, 3, '김치찌개 돼지고기 대신 뭐 넣어도 되나요?', '돼지고기가 없는데 김치찌개를 끓이고 싶어요.\n참치나 스팸을 넣어도 괜찮을까요?\n\n아니면 고기 없이 두부만 넣어도 맛있나요?', 0, 'PUBLISHED', 15, 3, 5, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(7, 3, '계란말이가 자꾸 부서져요', '계란말이를 만들 때마다 말다가 부서지는데\n어떻게 하면 예쁘게 말 수 있나요?\n\n불 조절을 잘못하는 걸까요?', 0, 'PUBLISHED', 22, 6, 8, NULL, NULL, NULL, NULL, 'FALSE', 'TRUE', NOW() - INTERVAL 2 DAY, NOW()),
(8, 3, '냉동 고기 빨리 해동하는 방법', '저녁에 요리하려는데 고기 꺼내는 걸 깜빡했어요.\n빨리 해동할 수 있는 방법 있을까요?\n\n전자레인지는 없어요 ㅠㅠ', 0, 'PUBLISHED', 28, 5, 6, NULL, NULL, NULL, NULL, 'FALSE', 'TRUE', NOW(), NOW()),
(2, 3, '된장과 고추장 유통기한', '냉장고에 있는 된장이랑 고추장이 개봉한 지 좀 됐는데\n유통기한이 얼마나 되나요?\n\n냄새는 괜찮은 것 같은데 먹어도 될까요?', 0, 'PUBLISHED', 19, 4, 7, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(5, 3, '밥 지을 때 물 양 어떻게 맞추나요?', '전기밥솥으로 밥 지을 때 물을 얼마나 넣어야 할지 모르겠어요.\n쌀과 물의 비율이 어떻게 되나요?\n\n현미는 물을 더 많이 넣어야 하나요?', 0, 'PUBLISHED', 24, 7, 9, NULL, NULL, NULL, NULL, 'FALSE', 'TRUE', NOW() - INTERVAL 1 DAY, NOW()),
(4, 3, '양파 자르면 눈물 안 나는 법', '양파 썰 때마다 눈물이 너무 많이 나요 ㅠㅠ\n눈물 안 나게 하는 팁 있나요?\n\n고글 쓰는 것 말고 다른 방법 알려주세요!', 0, 'PUBLISHED', 31, 8, 10, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 4 DAY, NOW()),
(3, 3, '김치 보관은 어떻게 하나요?', '김치를 사면 어떻게 보관해야 오래 먹을 수 있나요?\n냉장고에 그냥 넣으면 되나요?\n\n김치 통에 담아야 하나요?', 0, 'PUBLISHED', 17, 5, 4, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(1, 3, '칼 잘 드는 법 알려주세요', '집에 있는 칼이 안 들어서 야채 자르기 힘들어요.\n칼 가는 방법이나 관리법 알려주세요.\n\n새로 사는 게 나을까요?', 0, 'PUBLISHED', 26, 6, 8, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 5 DAY, NOW()),
(8, 3, '생강과 마늘 보관법', '생강이랑 마늘을 어떻게 보관하면 좋을까요?\n냉장고에 넣으면 마르고, 실온에 두면 싹이 나고...\n\n좋은 보관법 알려주세요!', 0, 'PUBLISHED', 21, 7, 6, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(5, 3, '프라이팬 눌러붙지 않게 하려면?', '요즘 프라이팬에 음식이 자꾸 눌러붙어요.\n코팅이 벗겨진 걸까요?\n\n프라이팬 관리법이나 교체 시기 알려주세요.', 0, 'PUBLISHED', 33, 9, 11, NULL, NULL, NULL, NULL, 'FALSE', 'TRUE', NOW() - INTERVAL 1 DAY, NOW());

-- 7-1. 레시피 게시글 재료 데이터 (post_ingredient)
-- posts 테이블의 레시피 게시글과 연결
INSERT INTO post_ingredient (recipe_id, name, quantity, unit, memo) VALUES
-- 김치찌개 게시글 (post_id=1)
(1, '김치', 300, 'g', NULL),
(1, '돼지고기', 200, 'g', NULL),
(1, '두부', 1, '모', '반모'),
(1, '대파', 1, '대', '반대'),
(1, '고춧가루', 1, '큰술', NULL),
(1, '다진마늘', 1, '큰술', NULL),
-- 된장찌개 게시글 (post_id=2)
(2, '된장', 2, '큰술', NULL),
(2, '두부', 1, '모', '반모'),
(2, '애호박', 1, '개', '반개'),
(2, '감자', 1, '개', NULL),
(2, '양파', 1, '개', '반개'),
(2, '대파', 1, '대', NULL),
-- 파스타 게시글 (post_id=3)
(3, '스파게티면', 200, 'g', NULL),
(3, '마늘', 5, '쪽', NULL),
(3, '올리브오일', 5, '큰술', NULL),
(3, '페페론치노', 1, '개', NULL),
-- 불고기 게시글 (post_id=4)
(4, '소고기', 500, 'g', NULL),
(4, '양파', 1, '개', NULL),
(4, '당근', 1, '개', '반개'),
(4, '간장', 3, '큰술', NULL),
(4, '설탕', 2, '큰술', NULL),
(4, '참기름', 1, '큰술', NULL),
(4, '다진마늘', 1, '큰술', NULL),
-- 떡볶이 게시글 (post_id=5)
(5, '떡', 300, 'g', NULL),
(5, '어묵', 100, 'g', NULL),
(5, '고춧가루', 2, '큰술', NULL),
(5, '설탕', 1, '큰술', NULL),
(5, '물엿', 1, '큰술', NULL),
-- 계란말이 게시글 (post_id=6)
(6, '계란', 4, '개', NULL),
(6, '당근', 1, '개', '1/4개'),
(6, '대파', 1, '대', '1/4대'),
(6, '소금', 1, '작은술', '약간'),
-- 제육볶음 게시글 (post_id=7)
(7, '돼지고기', 400, 'g', NULL),
(7, '고추장', 2, '큰술', NULL),
(7, '고춧가루', 1, '큰술', NULL),
(7, '양파', 1, '개', NULL),
(7, '대파', 1, '대', NULL),
-- 순두부찌개 게시글 (post_id=8)
(8, '순두부', 1, '봉지', NULL),
(8, '계란', 1, '개', NULL),
(8, '고춧가루', 1, '큰술', NULL),
(8, '다진마늘', 1, '큰술', NULL),
-- 비건 두부 스크램블 게시글 (post_id=9)
(9, '두부', 1, '모', NULL),
(9, '강황가루', 1, '작은술', '1/4작은술'),
(9, '양파', 1, '개', '반개'),
(9, '파프리카', 1, '개', NULL),
-- 파전 게시글 (post_id=10)
(10, '부침가루', 1, '컵', NULL),
(10, '물', 1, '컵', NULL),
(10, '대파', 3, '대', NULL),
(10, '계란', 1, '개', NULL);

-- 8. 댓글 데이터 (comment)
INSERT INTO comment (post_id, author_id, parent_id, content, like_count, created_at, updated_at, status) VALUES
-- 김치찌개 게시글 댓글 (post_id=1)
(1, 2, NULL, '김치찌개 정말 맛있어 보여요! 따라해볼게요', 3, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(1, 3, NULL, '묵은 김치가 없으면 어떻게 하나요?', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(1, 1, 2, '신김치도 괜찮지만 맛이 조금 다를 수 있어요', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
-- 불고기 게시글 댓글 (post_id=3)
(3, 3, NULL, '불고기 양념 비율 감사합니다!', 5, NOW() - INTERVAL 2 DAY, NOW(), 'ACTIVE'),
(3, 1, NULL, '다음에는 배 간 것을 넣어보세요. 더 부드러워져요', 4, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(3, 5, NULL, '양파를 많이 넣으면 더 달콤해집니다', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- 김치볶음밥 게시글 댓글 (post_id=4)
(4, 2, NULL, '김치볶음밥 정말 간단하네요!', 4, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(4, 4, NULL, '스팸 대신 베이컨 써도 되나요?', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- 떡볶이 게시글 댓글 (post_id=6)
(6, 2, NULL, '매운걸 못먹는데 도움됐어요 ㅎㅎ', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(6, 4, NULL, '우유 넣는 건 처음 들어봤어요. 신기하네요!', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
-- 계란말이 게시글 댓글 (post_id=7)
(7, 2, NULL, '계란말이에 치즈 넣어도 맛있어요!', 3, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(7, 5, NULL, '햄을 넣으면 더 고소해요', 2, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 제육볶음 게시글 댓글 (post_id=5)
(5, 1, NULL, '고추장 양념 최고예요!', 4, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(5, 3, NULL, '목살이 제일 맛있죠!', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- 순두부찌개 게시글 댓글 (post_id=8)
(8, 2, NULL, '혼밥 메뉴로 딱이네요!', 3, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(8, 4, NULL, '조개를 넣으면 더 시원해요', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
-- 비건 두부 스크램블 댓글 (post_id=11)
(11, 8, NULL, '비건 레시피 감사합니다!', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(11, 6, 1, '강황가루 꿀팁 좋네요!', 1, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 자유게시판 댓글 (post_id=21)
(21, 3, NULL, '계란말이부터 시작해보세요!', 2, NOW() - INTERVAL 2 DAY, NOW(), 'ACTIVE'),
(21, 5, NULL, '김치볶음밥 추천합니다', 1, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(21, 1, NULL, '된장찌개가 가장 쉬워요', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- 자유게시판 댓글 (post_id=23)
(23, 4, NULL, '저도 냉장고 파먹기 중이에요!', 3, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(23, 5, NULL, '일주일 동안 버틴 적 있어요 ㅎㅎ', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- Q&A 댓글 (post_id=31)
(31, 1, NULL, '참치나 스팸 둘 다 맛있어요!', 2, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(31, 5, NULL, '두부만 넣어도 괜찮습니다', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- Q&A 댓글 (post_id=32)
(32, 4, NULL, '약불에서 천천히 말아보세요', 3, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(32, 6, NULL, '팬을 충분히 달구는 게 중요해요', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE');

-- 9. 좋아요 데이터 (post_like)
INSERT INTO post_like (post_id, member_id) VALUES
-- 레시피 게시글 좋아요
(1, 2), (1, 3), (1, 4), (1, 5),
(2, 1), (2, 2), (2, 3),
(3, 1), (3, 2), (3, 4), (3, 5),
(4, 2), (4, 3), (4, 5),
(5, 1), (5, 3), (5, 4),
(6, 2), (6, 4), (6, 7),
(7, 2), (7, 3), (7, 5),
(8, 2), (8, 4), (8, 7),
-- 비건 레시피 좋아요
(11, 6), (11, 8),
(12, 6), (12, 8),
(13, 6), (13, 8),
-- 자유게시판 좋아요
(21, 1), (21, 3), (21, 5),
(22, 2), (22, 4),
(23, 2), (23, 4), (23, 5);

-- 10. 스크랩 데이터 (post_scrap)
INSERT INTO post_scrap (post_id, member_id, scrapped_at) VALUES
-- 레시피 스크랩
(1, 2, NOW() - INTERVAL 1 DAY),
(1, 3, NOW() - INTERVAL 12 HOUR),
(1, 7, NOW() - INTERVAL 6 HOUR),
(3, 1, NOW() - INTERVAL 2 DAY),
(3, 2, NOW() - INTERVAL 1 DAY),
(3, 5, NOW() - INTERVAL 12 HOUR),
(4, 2, NOW() - INTERVAL 12 HOUR),
(4, 7, NOW() - INTERVAL 6 HOUR),
(5, 3, NOW() - INTERVAL 12 HOUR),
(5, 5, NOW() - INTERVAL 6 HOUR),
(6, 2, NOW() - INTERVAL 6 HOUR),
(7, 2, NOW() - INTERVAL 6 HOUR),
(7, 7, NOW() - INTERVAL 3 HOUR),
(8, 7, NOW() - INTERVAL 12 HOUR),
-- 비건 레시피 스크랩
(11, 6, NOW() - INTERVAL 6 HOUR),
(11, 8, NOW() - INTERVAL 3 HOUR),
(12, 6, NOW() - INTERVAL 1 DAY),
(13, 8, NOW() - INTERVAL 2 DAY);

-- 11. 미디어 데이터 (media) - 일부 게시글에 이미지 추가
INSERT INTO media (url, media_type, owner_type, post_id, order_num) VALUES
('/uploads/posts/kimchi_jjigae.jpg', 'image', 'post', 1, 1),
('/uploads/posts/bulgogi.jpg', 'image', 'post', 3, 1),
('/uploads/posts/tteokbokki.jpg', 'image', 'post', 6, 1),
('/uploads/posts/vegan_scramble.jpg', 'image', 'post', 11, 1);

-- 완료 메시지
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT '✅ 더미 데이터 삽입 완료!' AS MESSAGE;
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';
SELECT CONCAT('👥 회원: ', COUNT(*), '명') AS '' FROM members;
SELECT CONCAT('📂 카테고리: ', COUNT(*), '개') AS '' FROM category;
SELECT CONCAT('📖 레시피: ', COUNT(*), '개') AS '' FROM recipe;
SELECT CONCAT('🥕 레시피 재료: ', COUNT(*), '개') AS '' FROM post_ingredient WHERE recipe_id IS NOT NULL;
SELECT CONCAT('🥬 냉장고 재료: ', COUNT(*), '개') AS '' FROM refrigerator_items;
SELECT CONCAT('📝 게시글: ', COUNT(*), '개') AS '' FROM posts;
SELECT CONCAT('💬 댓글: ', COUNT(*), '개') AS '' FROM comment;
SELECT CONCAT('❤️  좋아요: ', COUNT(*), '개') AS '' FROM post_like;
SELECT CONCAT('⭐ 스크랩: ', COUNT(*), '개') AS '' FROM post_scrap;
SELECT CONCAT('🍽️  식단 일기: ', COUNT(*), '개') AS '' FROM diary;
SELECT CONCAT('📷 미디어: ', COUNT(*), '개') AS '' FROM media;
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' AS '';

