-- quick-ui 登录页联邦 IdP 列表等接口（Client HMAC 通过后仍需 api_path_patterns 授权）

UPDATE sys_oauth_client
SET api_path_patterns = '/login
/login/captcha-config
/logout
/getInfo
/getRouters
/oauth/login/**
/oauth2/client/**
/phoneLogin
/sendSms
/qrcodeLogin
/qrcodeImage
/system/**
/monitor/**
/api/captcha/**'
WHERE client_id = 'quick-ui';
