-- 더미 데이터 삽입 스크립트
-- 테스트 및 개발용 샘플 데이터

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
-- type: RECIPE, FREE, QA, VEGAN, CARNIVORE
INSERT INTO category (name, type, parent_id) VALUES
-- 레시피 카테고리
('한식 레시피', 'RECIPE', NULL),
('중식 레시피', 'RECIPE', NULL),
('일식 레시피', 'RECIPE', NULL),
('양식 레시피', 'RECIPE', NULL),
('분식 레시피', 'RECIPE', NULL),
('디저트 레시피', 'RECIPE', NULL),
-- 자유 게시판
('자유게시판', 'FREE', NULL),
('정보공유', 'FREE', NULL),
('후기', 'FREE', NULL),
-- Q&A
('레시피 질문', 'QA', NULL),
('재료 질문', 'QA', NULL),
('조리법 질문', 'QA', NULL),
-- 비건/육식
('비건 레시피', 'VEGAN', NULL),
('육식 레시피', 'CARNIVORE', NULL);

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
-- 레시피 게시글
(1, 1, '김치찌개 맛있게 끓이는 법', '김치찌개를 끓일 때는 묵은 김치를 사용하는 것이 포인트입니다.\n돼지고기와 함께 볶다가 물을 부어주세요.\n\n**재료**\n- 김치 300g\n- 돼지고기 200g\n- 두부 반 모\n- 대파 반 대\n\n**조리법**\n1. 돼지고기를 먹기 좋은 크기로 자릅니다.\n2. 냄비에 식용유를 두르고 김치를 볶습니다.\n3. 돼지고기를 넣고 함께 볶습니다.\n4. 물을 붓고 끓입니다.\n5. 두부와 대파를 넣고 한소끔 끓입니다.', 1, 'PUBLISHED', 45, 12, 3, 30, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(1, 1, '얼큰한 된장찌개 레시피', '된장찌개는 멸치육수를 사용하면 더 깊은 맛이 납니다.\n두부와 애호박을 듬뿍 넣어보세요.\n\n**꿀팁**\n- 멸치 육수에 다시마를 함께 넣으면 감칠맛이 더해집니다.\n- 된장은 체에 거르지 말고 그대로 풀어주세요.', 1, 'PUBLISHED', 38, 8, 2, 25, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(2, 4, '집에서 만드는 간단한 파스타', '마늘과 올리브오일만 있으면 맛있는 알리오올리오를 만들 수 있어요!\n\n**재료**\n- 스파게티면 200g\n- 마늘 5쪽\n- 올리브오일 5큰술\n- 페페론치노 1개\n- 파슬리 약간\n\n**조리 시간**: 15분', 1, 'PUBLISHED', 67, 23, 5, 15, 'LOW', 1, 'VEGETARIAN', 'FALSE', 'TRUE', NOW() - INTERVAL 3 DAY, NOW()),
(2, 1, '불고기 양념 황금 레시피', '간장 3: 설탕 2: 참기름 1의 비율로 만들면 완벽한 불고기 양념이 됩니다.\n\n**양념 재료**\n- 간장 3큰술\n- 설탕 2큰술\n- 참기름 1큰술\n- 다진마늘 1큰술\n- 후추 약간\n\n여기에 배를 갈아 넣으면 더욱 부드러운 불고기를 만들 수 있어요!', 1, 'PUBLISHED', 92, 34, 8, 40, 'MEDIUM', 4, 'GENERAL', 'FALSE', 'TRUE', NOW() - INTERVAL 5 DAY, NOW()),
(3, 5, '떡볶이 매운맛 조절하는 법', '고춧가루 양을 조절하고 설탕을 약간 더 넣으면 덜 매워요.\n\n**팁**\n- 물엿을 넣으면 윤기가 나고 덜 맵습니다.\n- 우유를 조금 넣으면 크림떡볶이 같은 맛이 나요.\n- 어묵과 계란을 넣으면 더 맛있어요!', 1, 'PUBLISHED', 56, 18, 4, 20, 'LOW', 2, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(3, 8, '밑반찬으로 좋은 계란말이', '계란말이에 당근과 대파를 다져서 넣으면 영양만점!\n\n계란 4개에 소금 약간, 다진 야채를 넣고 잘 섞어주세요.\n팬에 기름을 두르고 계란물을 부어 돌돌 말아주면 완성!', 1, 'PUBLISHED', 41, 15, 2, 10, 'LOW', 2, 'VEGETARIAN', 'FALSE', 'FALSE', NOW(), NOW()),
(4, 1, '제육볶음 매콤하게 만들기', '고추장 양념에 고춧가루를 추가하면 더 매콤해요.\n\n**양념장**\n- 고추장 2큰술\n- 고춧가루 1큰술\n- 간장 1큰술\n- 설탕 1큰술\n- 다진마늘 1큰술\n\n돼지고기는 목살이나 앞다리살을 추천합니다!', 1, 'PUBLISHED', 78, 25, 6, 30, 'MEDIUM', 3, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(5, 1, '순두부찌개 간단 레시피', '혼자 사는 사람들에게 강추하는 순두부찌개!\n\n**재료**\n- 순두부 1봉지\n- 계란 1개\n- 고춧가루 1큰술\n- 다진마늘 1큰술\n- 멸치 육수 2컵\n\n순두부를 통째로 넣고 끓이다가 계란을 풀어 넣으면 완성!', 1, 'PUBLISHED', 52, 16, 3, 20, 'LOW', 1, 'GENERAL', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(6, 13, '비건 두부 스크램블 레시피', '계란 대신 두부로 만드는 스크램블!\n\n**재료**\n- 두부 1모\n- 강황가루 1/4작은술\n- 양파, 파프리카\n- 소금, 후추\n\n두부를 으깨서 노릇하게 볶아주세요.\n강황가루를 넣으면 색이 예뻐집니다!', 1, 'PUBLISHED', 34, 12, 2, 15, 'LOW', 1, 'VEGAN', 'FALSE', 'FALSE', NOW(), NOW()),
(7, 7, '초보자를 위한 파전 만들기', '처음 요리하는 분들도 쉽게 만들 수 있는 파전!\n\n**재료**\n- 부침가루 1컵\n- 물 1컵\n- 대파 3대\n- 계란 1개\n\n반죽을 너무 되직하게 만들지 마세요.\n팬을 충분히 달궈야 바삭합니다!', 1, 'PUBLISHED', 29, 9, 1, 20, 'LOW', 2, 'VEGETARIAN', 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
-- 자유 게시판 글
(1, 7, '요리 초보인데 추천 레시피 있나요?', '요리를 시작하려고 하는데 어떤 메뉴부터 시작하면 좋을까요?\n간단하면서도 맛있는 레시피 추천 부탁드려요!', 0, 'PUBLISHED', 23, 5, 7, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 3 DAY, NOW()),
(2, 8, '냉장고 정리 꿀팁 공유해요', '유통기한 임박한 재료들을 한눈에 보는 방법!\n\n1. 냉장고 앞쪽에 유통기한 짧은 것 배치\n2. 투명 용기 사용\n3. 라벨 붙이기\n\n여러분의 냉장고 정리 팁도 공유해주세요!', 0, 'PUBLISHED', 45, 15, 8, NULL, NULL, NULL, NULL, 'FALSE', 'TRUE', NOW() - INTERVAL 2 DAY, NOW()),
(3, 9, '김치찌개 맛집 다녀왔어요', '오늘 점심에 김치찌개 맛집 다녀왔는데 정말 맛있었어요.\n묵은 김치로 끓인 것 같은데 집에서도 이렇게 만들 수 있을까요?', 0, 'PUBLISHED', 31, 8, 4, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
-- Q&A 게시글
(7, 10, '김치찌개에 설탕 넣어도 되나요?', '김치찌개 끓일 때 너무 시큼해서 설탕을 넣으려고 하는데\n괜찮을까요? 아니면 다른 방법이 있을까요?', 0, 'PUBLISHED', 18, 3, 5, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 2 DAY, NOW()),
(8, 11, '닭가슴살 보관 방법 질문', '닭가슴살을 대량으로 샀는데 보관 방법을 모르겠어요.\n냉동 보관하면 얼마나 보관할 수 있나요?', 0, 'PUBLISHED', 22, 4, 3, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW() - INTERVAL 1 DAY, NOW()),
(7, 12, '파스타 면 삶을 때 소금 꼭 넣어야 하나요?', '파스타 레시피 보면 면 삶을 때 소금을 넣으라고 하는데\n왜 넣는 건가요? 안 넣으면 맛이 많이 다른가요?', 0, 'PUBLISHED', 27, 6, 4, NULL, NULL, NULL, NULL, 'FALSE', 'FALSE', NOW(), NOW());

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
-- 김치찌개 게시글 댓글
(1, 2, NULL, '김치찌개 정말 맛있어 보여요! 따라해볼게요', 3, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(1, 3, NULL, '묵은 김치가 없으면 어떻게 하나요?', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(1, 1, 2, '신김치도 괜찮지만 맛이 조금 다를 수 있어요', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(1, 4, NULL, '돼지고기 대신 참치 넣어도 맛있어요!', 1, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 불고기 게시글 댓글
(4, 3, NULL, '불고기 양념 비율 감사합니다!', 5, NOW() - INTERVAL 2 DAY, NOW(), 'ACTIVE'),
(4, 1, NULL, '다음에는 배 간 것을 넣어보세요. 더 부드러워져요', 4, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(4, 5, NULL, '양파를 많이 넣으면 더 달콤해집니다', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(4, 6, NULL, '소고기 대신 돼지고기로 해도 되나요?', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(4, 2, 4, '돼지고기로 하면 제육불고기가 되죠! 맛있어요', 3, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 파스타 게시글 댓글
(3, 1, NULL, '파스타 도전해봤는데 대박이에요!', 6, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(3, 4, NULL, '페페론치노가 없으면 청양고추 써도 되나요?', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(3, 2, 2, '네! 청양고추도 좋아요. 매운맛 조절하세요', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(3, 5, NULL, '면 삶는 물에 소금 꼭 넣으세요!', 3, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 떡볶이 게시글 댓글
(5, 2, NULL, '매운걸 못먹는데 도움됐어요 ㅎㅎ', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(5, 4, NULL, '우유 넣는 건 처음 들어봤어요. 신기하네요!', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(5, 7, NULL, '치즈 넣어도 맛있어요!', 2, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 계란말이 게시글 댓글
(6, 2, NULL, '계란말이에 치즈 넣어도 맛있어요!', 3, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(6, 5, NULL, '햄을 넣으면 더 고소해요', 2, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 제육볶음 게시글 댓글
(7, 1, NULL, '고추장 양념 최고예요!', 4, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(7, 3, NULL, '목살이 제일 맛있죠!', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(7, 5, NULL, '양파 많이 넣으면 단맛이 나서 좋아요', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
-- 순두부찌개 게시글 댓글
(8, 2, NULL, '혼밥 메뉴로 딱이네요!', 3, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(8, 4, NULL, '조개를 넣으면 더 시원해요', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
-- 비건 두부 스크램블 댓글
(9, 8, NULL, '비건 레시피 감사합니다!', 2, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(9, 6, 1, '강황가루 꿀팁 좋네요!', 1, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE'),
-- 자유게시판 댓글
(11, 3, NULL, '계란말이부터 시작해보세요!', 2, NOW() - INTERVAL 2 DAY, NOW(), 'ACTIVE'),
(11, 5, NULL, '김치볶음밥 추천합니다', 1, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(11, 1, NULL, '된장찌개가 가장 쉬워요', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(12, 4, NULL, '투명 용기 정말 유용하더라구요', 3, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(12, 5, NULL, '저는 냉장고 앱을 사용해요', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
-- Q&A 댓글
(14, 1, NULL, '설탕보다는 물엿이 더 좋아요!', 2, NOW() - INTERVAL 1 DAY, NOW(), 'ACTIVE'),
(14, 5, NULL, '다진 마늘을 더 넣으면 신맛이 줄어들어요', 1, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(15, 4, NULL, '냉동 보관하면 3개월까지 가능해요', 2, NOW() - INTERVAL 12 HOUR, NOW(), 'ACTIVE'),
(15, 6, NULL, '1회 분량씩 나눠서 냉동하세요', 1, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(16, 2, NULL, '소금을 넣으면 면에 간이 배어요', 3, NOW() - INTERVAL 6 HOUR, NOW(), 'ACTIVE'),
(16, 3, NULL, '소금 넣지 않으면 면이 밍밍해져요', 2, NOW() - INTERVAL 3 HOUR, NOW(), 'ACTIVE');

-- 9. 좋아요 데이터 (post_like)
INSERT INTO post_like (post_id, member_id) VALUES
-- 김치찌개 게시글 좋아요
(1, 2),
(1, 3),
(1, 4),
(1, 5),
-- 불고기 게시글 좋아요
(4, 1),
(4, 2),
(4, 3),
(4, 5),
(4, 6),
-- 파스타 게시글 좋아요
(3, 1),
(3, 2),
(3, 4),
(3, 5),
-- 떡볶이 게시글 좋아요
(5, 1),
(5, 2),
(5, 4),
-- 계란말이 게시글 좋아요
(6, 2),
(6, 3),
(6, 5),
-- 제육볶음 게시글 좋아요
(7, 1),
(7, 3),
(7, 4),
(7, 5),
-- 순두부찌개 게시글 좋아요
(8, 2),
(8, 4),
(8, 7),
-- 비건 게시글 좋아요
(9, 6),
(9, 8),
-- 자유게시판 좋아요
(11, 1),
(11, 3),
(12, 2),
(12, 4),
(12, 5);

-- 10. 스크랩 데이터 (post_scrap)
INSERT INTO post_scrap (post_id, member_id, scrapped_at) VALUES
-- 레시피 스크랩
(1, 2, NOW() - INTERVAL 1 DAY),
(1, 3, NOW() - INTERVAL 12 HOUR),
(1, 7, NOW() - INTERVAL 6 HOUR),
(4, 1, NOW() - INTERVAL 2 DAY),
(4, 2, NOW() - INTERVAL 1 DAY),
(4, 5, NOW() - INTERVAL 12 HOUR),
(3, 1, NOW() - INTERVAL 1 DAY),
(3, 4, NOW() - INTERVAL 12 HOUR),
(3, 7, NOW() - INTERVAL 6 HOUR),
(5, 2, NOW() - INTERVAL 12 HOUR),
(5, 7, NOW() - INTERVAL 6 HOUR),
(6, 2, NOW() - INTERVAL 6 HOUR),
(6, 7, NOW() - INTERVAL 3 HOUR),
(7, 3, NOW() - INTERVAL 12 HOUR),
(7, 5, NOW() - INTERVAL 6 HOUR),
(8, 7, NOW() - INTERVAL 12 HOUR),
(9, 6, NOW() - INTERVAL 6 HOUR),
(9, 8, NOW() - INTERVAL 3 HOUR);

-- 11. 미디어 데이터 (media) - 일부 게시글에 이미지 추가
INSERT INTO media (url, media_type, owner_type, post_id, order_num) VALUES
('/uploads/posts/kimchi_jjigae_1.jpg', 'image', 'post', 1, 1),
('/uploads/posts/kimchi_jjigae_2.jpg', 'image', 'post', 1, 2),
('/uploads/posts/bulgogi_1.jpg', 'image', 'post', 4, 1),
('/uploads/posts/pasta_1.jpg', 'image', 'post', 3, 1),
('/uploads/posts/tteokbokki_1.jpg', 'image', 'post', 5, 1),
('/uploads/posts/egg_roll_1.jpg', 'image', 'post', 6, 1),
('/uploads/posts/jeyuk_1.jpg', 'image', 'post', 7, 1),
('/uploads/recipe/kimchi_jjigae.jpg', 'image', 'recipe', NULL, 1),
('/uploads/recipe/bulgogi.jpg', 'image', 'recipe', NULL, 1),
('/uploads/recipe/pasta.jpg', 'image', 'recipe', NULL, 1);

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

