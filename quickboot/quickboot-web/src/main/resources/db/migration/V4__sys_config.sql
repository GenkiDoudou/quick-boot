CREATE TABLE IF NOT EXISTS sys_config (
  config_id BIGINT NOT NULL PRIMARY KEY,
  config_name VARCHAR(100) NOT NULL,
  config_key VARCHAR(100) NOT NULL,
  config_value VARCHAR(500) NOT NULL,
  config_type CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NULL,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  CONSTRAINT uk_sys_config_key UNIQUE (config_key, del_flag)
);

CREATE INDEX idx_sys_config_key ON sys_config (config_key, del_flag);
CREATE INDEX idx_sys_config_type ON sys_config (config_type, del_flag);
