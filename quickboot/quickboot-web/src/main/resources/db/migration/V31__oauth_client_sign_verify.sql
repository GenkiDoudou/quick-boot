-- 是否启用 Client HMAC 验签及接口 Ant 路径授权（0=否 1=是）

ALTER TABLE sys_oauth_client ADD sign_verify CHAR(1) NOT NULL DEFAULT '1';

UPDATE sys_oauth_client SET sign_verify = '1' WHERE sign_verify IS NULL OR sign_verify = '';
