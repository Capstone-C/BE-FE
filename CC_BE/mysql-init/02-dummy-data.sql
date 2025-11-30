-- 더미 데이터 삽입 스크립트
-- 테스트 및 개발용 샘플 데이터

USE ccdb;

-- 1. 회원 데이터 (members)
INSERT INTO members (username, password, email, nickname, role, profile_image_url, created_at, updated_at) VALUES
('testuser1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user1@test.com', '요리왕비룡', 'USER', NULL, NOW(), NOW()),
('testuser2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user2@test.com', '맛있는하루', 'USER', NULL, NOW(), NOW()),
('testuser3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user3@test.com', '냉장고지킴이', 'USER', NULL, NOW(), NOW()),
('moderator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'mod@test.com', '모더레이터', 'MODERATOR', NULL, NOW(), NOW()),
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@test.com', '관리자', 'ADMIN', NULL, NOW(), NOW());
-- 비밀번호는 모두 'password123'

-- 2. 카테고리 데이터 (category)
INSERT INTO category (name, description, display_order, is_active) VALUES
('한식', '한국 전통 음식', 1, true),
('중식', '중국 음식', 2, true),
('일식', '일본 음식', 3, true),
('양식', '서양 음식', 4, true),
('분식', '간단한 분식 메뉴', 5, true),
('디저트', '후식 및 간식', 6, true),
('국/찌개', '국물 요리', 7, true),
('반찬', '밑반찬', 8, true);

-- 3. 레시피 데이터 (recipe)
INSERT INTO recipe (name, description, cooking_time, difficulty, servings, calories, image_url, video_url, created_at, updated_at) VALUES
('김치찌개', '김치와 돼지고기로 만드는 얼큰한 찌개', 30, 'EASY', 2, 350, NULL, NULL, NOW(), NOW()),
('된장찌개', '구수한 된장 맛이 일품인 찌개', 25, 'EASY', 2, 280, NULL, NULL, NOW(), NOW()),
('불고기', '달콤짭짤한 소불고기', 40, 'MEDIUM', 4, 450, NULL, NULL, NOW(), NOW()),
('김치볶음밥', '김치와 밥을 볶은 간단한 요리', 15, 'EASY', 1, 520, NULL, NULL, NOW(), NOW()),
('된장국', '시원한 된장국', 20, 'EASY', 2, 180, NULL, NULL, NOW(), NOW()),
('계란말이', '부드러운 계란말이', 10, 'EASY', 2, 220, NULL, NULL, NOW(), NOW()),
('떡볶이', '매콤달콤한 떡볶이', 20, 'EASY', 2, 380, NULL, NULL, NOW(), NOW()),
('비빔밥', '건강한 비빔밥', 25, 'MEDIUM', 1, 480, NULL, NULL, NOW(), NOW()),
('잡채', '달콤한 잡채', 35, 'MEDIUM', 4, 320, NULL, NULL, NOW(), NOW()),
('삼계탕', '보양식 삼계탕', 90, 'HARD', 1, 800, NULL, NULL, NOW(), NOW());

-- 4. 레시피 재료 데이터 (recipe_ingredient)
-- 김치찌개 재료
INSERT INTO recipe_ingredient (recipe_id, name, quantity, unit, is_essential) VALUES
(1, '김치', '300', 'g', true),
(1, '돼지고기', '200', 'g', true),
(1, '두부', '1/2', '모', false),
(1, '대파', '1/2', '대', false),
(1, '고춧가루', '1', '큰술', true),
(1, '다진마늘', '1', '큰술', true);

-- 된장찌개 재료
INSERT INTO recipe_ingredient (recipe_id, name, quantity, unit, is_essential) VALUES
(2, '된장', '2', '큰술', true),
(2, '두부', '1/2', '모', true),
(2, '애호박', '1/2', '개', false),
(2, '감자', '1', '개', false),
(2, '양파', '1/2', '개', true),
(2, '대파', '1', '대', false);

-- 불고기 재료
INSERT INTO recipe_ingredient (recipe_id, name, quantity, unit, is_essential) VALUES
(3, '소고기', '500', 'g', true),
(3, '양파', '1', '개', true),
(3, '당근', '1/2', '개', false),
(3, '간장', '3', '큰술', true),
(3, '설탕', '2', '큰술', true),
(3, '참기름', '1', '큰술', true),
(3, '다진마늘', '1', '큰술', true);

-- 김치볶음밥 재료
INSERT INTO recipe_ingredient (recipe_id, name, quantity, unit, is_essential) VALUES
(4, '밥', '1', '공기', true),
(4, '김치', '100', 'g', true),
(4, '계란', '1', '개', false),
(4, '식용유', '2', '큰술', true),
(4, '김가루', '약간', '', false);

-- 계란말이 재료
INSERT INTO recipe_ingredient (recipe_id, name, quantity, unit, is_essential) VALUES
(6, '계란', '4', '개', true),
(6, '당근', '1/4', '개', false),
(6, '대파', '1/4', '대', false),
(6, '소금', '약간', '', true),
(6, '식용유', '2', '큰술', true);

-- 5. 냉장고 재료 데이터 (refrigerator_item)
-- testuser1의 냉장고
INSERT INTO refrigerator_item (member_id, name, quantity, unit, expiration_date, category, storage_location, memo, is_expired_warning, created_at, updated_at) VALUES
(1, '김치', 500, 'g', DATE_ADD(NOW(), INTERVAL 30 DAY), '채소', '냉장실', '배추김치', false, NOW(), NOW()),
(1, '계란', 10, '개', DATE_ADD(NOW(), INTERVAL 14 DAY), '유제품', '냉장실', NULL, false, NOW(), NOW()),
(1, '우유', 900, 'ml', DATE_ADD(NOW(), INTERVAL 5 DAY), '유제품', '냉장실', '1L 우유', false, NOW(), NOW()),
(1, '돼지고기', 300, 'g', DATE_ADD(NOW(), INTERVAL 3 DAY), '육류', '냉장실', '삼겹살', true, NOW(), NOW()),
(1, '두부', 1, '모', DATE_ADD(NOW(), INTERVAL 7 DAY), '채소', '냉장실', NULL, false, NOW(), NOW()),
(1, '대파', 2, '대', DATE_ADD(NOW(), INTERVAL 10 DAY), '채소', '냉장실', NULL, false, NOW(), NOW()),
(1, '양파', 3, '개', DATE_ADD(NOW(), INTERVAL 20 DAY), '채소', '냉장실', NULL, false, NOW(), NOW()),
(1, '감자', 5, '개', DATE_ADD(NOW(), INTERVAL 25 DAY), '채소', '냉장실', NULL, false, NOW(), NOW());

-- testuser2의 냉장고
INSERT INTO refrigerator_item (member_id, name, quantity, unit, expiration_date, category, storage_location, memo, is_expired_warning, created_at, updated_at) VALUES
(2, '소고기', 400, 'g', DATE_ADD(NOW(), INTERVAL 2 DAY), '육류', '냉장실', '불고기용', true, NOW(), NOW()),
(2, '당근', 2, '개', DATE_ADD(NOW(), INTERVAL 15 DAY), '채소', '냉장실', NULL, false, NOW(), NOW()),
(2, '계란', 6, '개', DATE_ADD(NOW(), INTERVAL 10 DAY), '유제품', '냉장실', NULL, false, NOW(), NOW()),
(2, '밥', 2, '공기', DATE_ADD(NOW(), INTERVAL 2 DAY), '기타', '냉장실', '남은밥', true, NOW(), NOW()),
(2, '김치', 200, 'g', DATE_ADD(NOW(), INTERVAL 20 DAY), '채소', '냉장실', NULL, false, NOW(), NOW());

-- testuser3의 냉장고
INSERT INTO refrigerator_item (member_id, name, quantity, unit, expiration_date, category, storage_location, memo, is_expired_warning, created_at, updated_at) VALUES
(3, '된장', 1, '통', DATE_ADD(NOW(), INTERVAL 180 DAY), '양념', '냉장실', NULL, false, NOW(), NOW()),
(3, '고춧가루', 100, 'g', DATE_ADD(NOW(), INTERVAL 90 DAY), '양념', '냉장실', NULL, false, NOW(), NOW()),
(3, '간장', 500, 'ml', DATE_ADD(NOW(), INTERVAL 365 DAY), '양념', '냉장실', NULL, false, NOW(), NOW()),
(3, '참기름', 200, 'ml', DATE_ADD(NOW(), INTERVAL 180 DAY), '양념', '냉장실', NULL, false, NOW(), NOW()),
(3, '애호박', 1, '개', DATE_ADD(NOW(), INTERVAL 7 DAY), '채소', '냉장실', NULL, false, NOW(), NOW()),
(3, '두부', 2, '모', DATE_ADD(NOW(), INTERVAL 5 DAY), '채소', '냉장실', NULL, false, NOW(), NOW());

-- 6. 식단 일기 데이터 (diary)
INSERT INTO diary (member_id, meal_type, meal_date, description, calories, carbohydrate, protein, fat, image_url, created_at, updated_at) VALUES
(1, 'BREAKFAST', CURDATE(), '김치찌개와 밥', 550, 75, 25, 15, NULL, NOW(), NOW()),
(1, 'LUNCH', CURDATE(), '된장찌개 정식', 680, 85, 30, 20, NULL, NOW(), NOW()),
(1, 'DINNER', CURDATE() - INTERVAL 1 DAY, '불고기와 밥', 720, 80, 45, 25, NULL, NOW(), NOW()),
(2, 'BREAKFAST', CURDATE(), '계란말이와 김치', 380, 35, 25, 20, NULL, NOW(), NOW()),
(2, 'LUNCH', CURDATE(), '김치볶음밥', 520, 70, 15, 18, NULL, NOW(), NOW()),
(2, 'DINNER', CURDATE() - INTERVAL 1 DAY, '떡볶이', 450, 75, 10, 12, NULL, NOW(), NOW()),
(3, 'BREAKFAST', CURDATE(), '된장국과 밥', 420, 65, 15, 12, NULL, NOW(), NOW()),
(3, 'LUNCH', CURDATE(), '비빔밥', 580, 80, 25, 18, NULL, NOW(), NOW());

-- 7. 게시글 데이터 (posts)
INSERT INTO posts (member_id, category_id, title, content, view_count, like_count, scrap_count, comment_count, image_url, created_at, updated_at) VALUES
(1, 1, '김치찌개 맛있게 끓이는 법', '김치찌개를 끓일 때는 묵은 김치를 사용하는 것이 포인트입니다. 돼지고기와 함께 볶다가 물을 부어주세요.', 45, 12, 5, 3, NULL, NOW() - INTERVAL 2 DAY, NOW()),
(1, 7, '얼큰한 된장찌개 레시피', '된장찌개는 멸치육수를 사용하면 더 깊은 맛이 납니다. 두부와 애호박을 듬뿍 넣어보세요.', 38, 8, 3, 2, NULL, NOW() - INTERVAL 1 DAY, NOW()),
(2, 4, '집에서 만드는 간단한 파스타', '마늘과 올리브오일만 있으면 맛있는 알리오올리오를 만들 수 있어요!', 67, 23, 10, 5, NULL, NOW() - INTERVAL 3 DAY, NOW()),
(2, 1, '불고기 양념 황금 레시피', '간장 3: 설탕 2: 참기름 1의 비율로 만들면 완벽한 불고기 양념이 됩니다.', 92, 34, 15, 8, NULL, NOW() - INTERVAL 5 DAY, NOW()),
(3, 5, '떡볶이 매운맛 조절하는 법', '고춧가루 양을 조절하고 설탕을 약간 더 넣으면 덜 매워요.', 56, 18, 7, 4, NULL, NOW() - INTERVAL 1 DAY, NOW()),
(3, 8, '밑반찬으로 좋은 계란말이', '계란말이에 당근과 대파를 다져서 넣으면 영양만점!', 41, 15, 6, 2, NULL, NOW(), NOW());

-- 8. 게시글 재료 데이터 (PostIngredient)
INSERT INTO PostIngredient (post_id, name, quantity, unit) VALUES
(1, '김치', '300', 'g'),
(1, '돼지고기', '200', 'g'),
(1, '두부', '1/2', '모'),
(2, '된장', '2', '큰술'),
(2, '두부', '1', '모'),
(2, '애호박', '1', '개'),
(4, '소고기', '500', 'g'),
(4, '간장', '3', '큰술'),
(4, '설탕', '2', '큰술');

-- 9. 댓글 데이터 (comment)
INSERT INTO comment (post_id, member_id, parent_comment_id, content, like_count, created_at, updated_at, is_deleted) VALUES
(1, 2, NULL, '김치찌개 정말 맛있어 보여요! 따라해볼게요', 3, NOW() - INTERVAL 1 DAY, NOW(), false),
(1, 3, NULL, '묵은 김치가 없으면 어떻게 하나요?', 1, NOW() - INTERVAL 12 HOUR, NOW(), false),
(1, 1, 2, '신김치도 괜찮지만 맛이 조금 다를 수 있어요', 2, NOW() - INTERVAL 6 HOUR, NOW(), false),
(4, 3, NULL, '불고기 양념 비율 감사합니다!', 5, NOW() - INTERVAL 2 DAY, NOW(), false),
(4, 1, NULL, '다음에는 배 간 것을 넣어보세요. 더 부드러워져요', 4, NOW() - INTERVAL 1 DAY, NOW(), false),
(3, 1, NULL, '파스타 도전해봤는데 대박이에요!', 6, NOW() - INTERVAL 1 DAY, NOW(), false),
(5, 2, NULL, '매운걸 못먹는데 도움됐어요 ㅎㅎ', 2, NOW() - INTERVAL 12 HOUR, NOW(), false),
(6, 2, NULL, '계란말이에 치즈 넣어도 맛있어요!', 3, NOW() - INTERVAL 6 HOUR, NOW(), false);

-- 10. 좋아요 데이터 (post_like)
INSERT INTO post_like (post_id, member_id, created_at) VALUES
(1, 2, NOW() - INTERVAL 1 DAY),
(1, 3, NOW() - INTERVAL 12 HOUR),
(4, 1, NOW() - INTERVAL 2 DAY),
(4, 2, NOW() - INTERVAL 1 DAY),
(4, 3, NOW() - INTERVAL 12 HOUR),
(3, 1, NOW() - INTERVAL 1 DAY),
(3, 2, NOW() - INTERVAL 6 HOUR),
(5, 1, NOW() - INTERVAL 12 HOUR),
(6, 2, NOW() - INTERVAL 6 HOUR);

-- 11. 스크랩 데이터 (post_scrap)
INSERT INTO post_scrap (post_id, member_id, created_at) VALUES
(1, 2, NOW() - INTERVAL 1 DAY),
(1, 3, NOW() - INTERVAL 12 HOUR),
(4, 1, NOW() - INTERVAL 2 DAY),
(4, 2, NOW() - INTERVAL 1 DAY),
(3, 1, NOW() - INTERVAL 1 DAY),
(5, 2, NOW() - INTERVAL 12 HOUR);

-- 완료 메시지
SELECT '✅ 더미 데이터 삽입 완료!' AS MESSAGE;
SELECT CONCAT('회원: ', COUNT(*), '명') AS '회원 수' FROM members;
SELECT CONCAT('카테고리: ', COUNT(*), '개') AS '카테고리 수' FROM category;
SELECT CONCAT('레시피: ', COUNT(*), '개') AS '레시피 수' FROM recipe;
SELECT CONCAT('냉장고 재료: ', COUNT(*), '개') AS '냉장고 재료 수' FROM refrigerator_item;
SELECT CONCAT('게시글: ', COUNT(*), '개') AS '게시글 수' FROM posts;
SELECT CONCAT('댓글: ', COUNT(*), '개') AS '댓글 수' FROM comment;
