CREATE DATABASE fakefilter;
USE fakefilter;


CREATE TABLE `posts` (
  `post_id` int NOT NULL AUTO_INCREMENT,
  `video_path` varchar(255) NOT NULL,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `password` varchar(50) NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- MySQL에 접속 후 실행
SELECT user, host FROM mysql.user WHERE user = '';
ALTER USER ''@'localhost' IDENTIFIED BY '';
CREATE USER ''@'%' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON *.* TO ''@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


INSERT INTO posts (video_path, title, content, password, created_date)
VALUES 
    ('E:/android_project/dfdc_deepfake_challenge/community_data/1.png', 'Example Title1', 'This is the content1.', '12345', '2024-11-27 00:10:00'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/2.png', 'Example Title2', 'This is the content2.', '12345', '2024-11-27 01:00:00'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/3.png', 'Example Title3', 'This is the content3.', '12345', '2024-11-27 02:00:00'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/4.png', 'Example Title4', 'This is the content4.', '12345', '2024-11-27 03:00:00'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/5.png', 'Example Title5', 'This is the content5.', '12345', '2024-11-27 04:00:00'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/6.png', 'Example Title6', 'This is the content5.', '12345', '2024-11-27 22:55:36'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/7.png', 'Example Title7', 'This is the content7.', '12345', '2024-11-27 23:46:44'),
    ('E:/android_project/dfdc_deepfake_challenge/community_data/8.png', 'Example Title8', 'This is the content8.', '12345', '2024-11-27 23:49:00');
