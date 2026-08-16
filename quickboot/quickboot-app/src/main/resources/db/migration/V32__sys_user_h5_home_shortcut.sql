-- H5 首页个人快捷偏好；默认 menu_id 见后端常量（9002/9003/9004/9015）

CREATE TABLE IF NOT EXISTS sys_user_h5_home_shortcut (
  id           BIGINT       NOT NULL COMMENT '主键',
  user_id      VARCHAR(64)  NOT NULL COMMENT '用户ID，对齐 sys_user.user_id',
  menu_id      BIGINT       NOT NULL COMMENT '菜单ID，对齐 sys_menu.menu_id（H5 C 入口）',
  order_num    INT          NOT NULL DEFAULT 0 COMMENT '展示顺序，升序',
  create_by    VARCHAR(64)  NULL COMMENT '创建者',
  create_time  TIMESTAMP    NULL COMMENT '创建时间',
  update_by    VARCHAR(64)  NULL COMMENT '更新者',
  update_time  TIMESTAMP    NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_h5_home_menu (user_id, menu_id),
  KEY idx_user_h5_home_order (user_id, order_num)
) COMMENT='H5 首页个人快捷入口偏好';
