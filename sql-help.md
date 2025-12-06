[sql 파일](./02-dummy-data.sql)


# 1. 기존 데이터 삭제
docker exec -i cc_mysql mysql -uccuser -pdevpass ccdb -e "
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE media;
TRUNCATE TABLE post_scrap;
TRUNCATE TABLE post_like;
TRUNCATE TABLE comment;
TRUNCATE TABLE post_ingredient;
TRUNCATE TABLE posts;
TRUNCATE TABLE diary;
TRUNCATE TABLE refrigerator_items;
TRUNCATE TABLE recipe;
TRUNCATE TABLE category;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;
"

# 2. 더미 데이터 삽입
docker exec -i cc_mysql mysql -uccuser -pdevpass ccdb < 02-dummy-data.sql