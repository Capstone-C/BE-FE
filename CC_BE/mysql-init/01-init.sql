-- 초기화 스크립트: 애플리케이션용(ccdb) + 테스트용(ccdb_test) DB 생성 및 권한 부여
-- 이 스크립트는 MySQL 컨테이너가 최초 초기화될 때(빈 데이터 디렉터리) 한 번만 실행됩니다.

CREATE DATABASE IF NOT EXISTS `ccdb` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ccdb_test` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'ccuser'@'%' IDENTIFIED BY 'devpass';
GRANT ALL PRIVILEGES ON `ccdb`.* TO 'ccuser'@'%';
GRANT ALL PRIVILEGES ON `ccdb_test`.* TO 'ccuser'@'%';
FLUSH PRIVILEGES;
