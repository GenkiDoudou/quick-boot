-- 首方 quick-ui Client HMAC 签名（方案 C）
-- client_secret 与 quick-ui VITE_APP_CLIENT_SIGN_KEY 一致；生产请轮换并改为 SM4 入库

INSERT INTO sys_oauth_client (
  client_id,
  client_secret,
  client_name,
  redirect_uris,
  grant_types,
  scopes,
  status,
  is_confidential,
  remark,
  del_flag
) VALUES (
  'quick-ui',
  '0123456789abcdef0123456789abcdef',
  'QuickBoot 管理端',
  'http://localhost:8800/',
  'client_credentials',
  'openid',
  '0',
  '1',
  '首方 SPA Client 签名（VITE_APP_CLIENT_ID=quick-ui）',
  '0'
);
