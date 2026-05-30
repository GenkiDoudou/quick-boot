-- quick-ui 菜单维护：积木报表/BI 目录下拉（/report/jimu/catalog/**）

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
/api/captcha/**'
WHERE client_id = 'quick-ui';
