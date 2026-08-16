-- quick-h5 独立 OAuth client（H5 / 小程序 Client Basic；check_captcha=0 便于第一版联调）
INSERT INTO sys_oauth_client (
  id, client_id, client_secret, client_name, api_path_patterns,
  token_timeout, check_captcha,
  status, del_flag, remark, create_by, create_time
) VALUES (
  19,
  'quick-h5',
  'quick-h5-secret',
  'Quick H5',
  '/**',
  604800,
  '0',
  '0',
  '0',
  'uni-app H5/小程序客户端',
  'system',
  CURRENT_TIMESTAMP
);
