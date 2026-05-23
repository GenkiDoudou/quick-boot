-- OAuth2 授权服务器：第三方应用
CREATE TABLE IF NOT EXISTS sys_oauth_client (
  client_id VARCHAR(64) NOT NULL PRIMARY KEY,
  client_secret VARCHAR(256) NOT NULL,
  client_name VARCHAR(100) NOT NULL,
  redirect_uris VARCHAR(2000) NOT NULL,
  grant_types VARCHAR(200) NOT NULL,
  scopes VARCHAR(200) NOT NULL DEFAULT 'openid',
  status CHAR(1) NOT NULL DEFAULT '0',
  is_confidential CHAR(1) NOT NULL DEFAULT '1',
  remark VARCHAR(500) NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NULL,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL
);

CREATE INDEX idx_sys_oauth_client_status ON sys_oauth_client (status, del_flag);

-- OAuth2 AS：用户 openid 映射
CREATE TABLE IF NOT EXISTS sys_oauth_user_openid (
  id BIGINT NOT NULL PRIMARY KEY,
  client_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  openid VARCHAR(128) NOT NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_oauth_openid_client_user UNIQUE (client_id, user_id),
  CONSTRAINT uk_oauth_openid_client_openid UNIQUE (client_id, openid)
);

CREATE INDEX idx_sys_oauth_user_openid_user ON sys_oauth_user_openid (user_id);

-- 用户对 client 的授权记忆（可选）
CREATE TABLE IF NOT EXISTS sys_oauth_approve (
  id BIGINT NOT NULL PRIMARY KEY,
  client_id VARCHAR(64) NOT NULL,
  user_id BIGINT NOT NULL,
  scopes VARCHAR(200) NOT NULL,
  expire_time DATETIME NOT NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_oauth_approve_client_user UNIQUE (client_id, user_id)
);

-- OAuth2 Client：外部 IdP
CREATE TABLE IF NOT EXISTS sys_oauth_provider (
  provider_code VARCHAR(64) NOT NULL PRIMARY KEY,
  provider_name VARCHAR(100) NOT NULL,
  client_id VARCHAR(256) NOT NULL,
  client_secret VARCHAR(512) NOT NULL,
  authorize_url VARCHAR(500) NOT NULL,
  token_url VARCHAR(500) NOT NULL,
  userinfo_url VARCHAR(500) NULL,
  discovery_url VARCHAR(500) NULL,
  redirect_uri VARCHAR(500) NOT NULL,
  enabled CHAR(1) NOT NULL DEFAULT '0',
  auto_register CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NULL,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL
);

-- OAuth2 Client：外部身份与本地用户绑定
CREATE TABLE IF NOT EXISTS sys_oauth_user_bind (
  id BIGINT NOT NULL PRIMARY KEY,
  provider_code VARCHAR(64) NOT NULL,
  external_subject VARCHAR(256) NOT NULL,
  user_id BIGINT NOT NULL,
  bind_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  CONSTRAINT uk_oauth_bind_provider_subject UNIQUE (provider_code, external_subject)
);

CREATE INDEX idx_sys_oauth_user_bind_user ON sys_oauth_user_bind (user_id);
