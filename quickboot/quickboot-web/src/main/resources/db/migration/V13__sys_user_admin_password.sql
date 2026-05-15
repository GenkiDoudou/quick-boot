-- 内置 admin 默认密码（明文 admin），与 Hutool BCrypt 一致，供数据库登录联调；生产环境请首次登录后立即修改。

UPDATE sys_user
SET password = '{bcrypt}$2a$10$ehzg9AnHDqCVks6Rlcv0H.9E4A3V1QJHCgAmd1p51cdpKNST4N/Km'
WHERE user_id = 1
  AND user_name = 'admin';
