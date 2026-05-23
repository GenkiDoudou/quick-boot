-- 接口授权改为 Ant 路径（Spring AntPathMatcher），修正 V29 中的正则写法

UPDATE sys_oauth_client
SET api_path_patterns = '/login
/login/captcha-config
/logout
/getInfo
/getRouters
/system/**
/monitor/**
/api/captcha/**'
WHERE client_id = 'quick-ui';
