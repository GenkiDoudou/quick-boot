-- quick-ui Client HMAC：补充 AI 应用 / 工作流 / 知识库 / AI 模型等 SPA 常用 API 路径授权

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
/knowledge/**'
WHERE client_id = 'quick-ui';
