-- quick-ui Client HMAC：补充通用文件上传/分类查询 API（C7Upload 依赖 /file/classifies/**）

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
/report/**
/api/captcha/**
/ai/**
/workflow/**
/knowledge/**
/file/**'
WHERE client_id = 'quick-ui';
