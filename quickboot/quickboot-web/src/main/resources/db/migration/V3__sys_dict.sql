CREATE TABLE IF NOT EXISTS sys_dict_type (
  dict_id BIGINT NOT NULL PRIMARY KEY,
  dict_name VARCHAR(100) NOT NULL,
  dict_type VARCHAR(100) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NULL,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  CONSTRAINT uk_sys_dict_type UNIQUE (dict_type, del_flag)
);

CREATE TABLE IF NOT EXISTS sys_dict_data (
  dict_code BIGINT NOT NULL PRIMARY KEY,
  dict_sort INT NOT NULL DEFAULT 0,
  dict_label VARCHAR(100) NOT NULL,
  dict_value VARCHAR(100) NOT NULL,
  dict_type VARCHAR(100) NOT NULL,
  css_class VARCHAR(100) NULL,
  list_class VARCHAR(100) NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(500) NULL,
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  create_by VARCHAR(64) NULL,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_by VARCHAR(64) NULL,
  update_time DATETIME NULL,
  CONSTRAINT uk_sys_dict_data UNIQUE (dict_type, dict_value, del_flag)
);

CREATE INDEX idx_sys_dict_type_type ON sys_dict_type (dict_type, del_flag);
CREATE INDEX idx_sys_dict_data_type ON sys_dict_data (dict_type, del_flag);
