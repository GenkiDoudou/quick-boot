-- 接口授权：Java 正则，每行一条，对 servlet path 做全路径匹配（与「授权范围 scope」分离）

ALTER TABLE sys_oauth_client ADD api_path_patterns VARCHAR(4000) NULL;

-- 首方 quick-ui：管理端常用路径（Client HMAC 签名场景）
UPDATE sys_oauth_client
SET api_path_patterns = '^/login$
^/login/captcha-config$
^/logout$
^/getInfo$
^/getRouters$
^/system/.*$
^/monitor/.*$
^/api/captcha/.*$'
WHERE client_id = 'quick-ui';
