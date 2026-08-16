/*
 * lite-trace-chain ①期：链路索引/片段表 + 可选 RUM 明细 + 控制台菜单。
 * 依赖：V8（监控目录 2100）、V23（overview 2169–2170）。menu_id 使用 2171+。
 * TTL：热数据建议 7～14 天，由应用侧定时清理（本期仅注释约定，不强制建清理 Job）。
 */

-- ========== sys_trace_index（一条 Trace 一行） ==========

CREATE TABLE IF NOT EXISTS sys_trace_index (
  trace_id         VARCHAR(64)   NOT NULL COMMENT '链路 ID（主键）',
  app_id           VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '应用标识',
  root_source      VARCHAR(16)   NOT NULL DEFAULT 'browser' COMMENT '来源：browser/api/job',
  entry_name       VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '根调用 entry',
  caller_name      VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '调用方 caller',
  operation_id     VARCHAR(64)   NULL COMMENT '前端操作 ID',
  action_name      VARCHAR(128)  NULL COMMENT '操作名 action',
  page_path        VARCHAR(500)  NULL COMMENT '当前页',
  from_page        VARCHAR(500)  NULL COMMENT '上一页',
  uin              VARCHAR(64)   NULL COMMENT '用户标识',
  ok_flag          CHAR(1)       NOT NULL DEFAULT '1' COMMENT '整链成败(0否1是)',
  status_code      VARCHAR(32)   NULL COMMENT '状态摘要',
  duration_ms      BIGINT        NOT NULL DEFAULT 0 COMMENT '整链耗时毫秒',
  started_at       TIMESTAMP     NULL COMMENT '开始时间',
  ended_at         TIMESTAMP     NULL COMMENT '结束时间',
  client_ip        VARCHAR(128)  NULL COMMENT '客户端 IP',
  ua               VARCHAR(500)  NULL COMMENT 'User-Agent',
  error_summary    VARCHAR(500)  NULL COMMENT '首错摘要',
  create_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (trace_id)
);

CREATE INDEX idx_sys_trace_index_operation_id ON sys_trace_index (operation_id, started_at);
CREATE INDEX idx_sys_trace_index_uin ON sys_trace_index (uin, started_at);
CREATE INDEX idx_sys_trace_index_entry ON sys_trace_index (entry_name, started_at);
CREATE INDEX idx_sys_trace_index_action ON sys_trace_index (action_name, started_at);
CREATE INDEX idx_sys_trace_index_source ON sys_trace_index (root_source, started_at);
CREATE INDEX idx_sys_trace_index_started ON sys_trace_index (started_at);

-- ========== sys_trace_span（链上片段） ==========

CREATE TABLE IF NOT EXISTS sys_trace_span (
  span_id          BIGINT        NOT NULL COMMENT '片段主键',
  trace_id         VARCHAR(64)   NOT NULL COMMENT '链路 ID',
  parent_span_id   BIGINT        NULL COMMENT '父片段',
  source_type      VARCHAR(32)   NOT NULL DEFAULT '' COMMENT 'fe_action/fe_api/fe_error/gateway/service/sql/be_error',
  span_name        VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '展示名',
  service_name     VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '服务名',
  start_offset_ms  BIGINT        NOT NULL DEFAULT 0 COMMENT '相对起点偏移毫秒',
  duration_ms      BIGINT        NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  ok_flag          CHAR(1)       NOT NULL DEFAULT '1' COMMENT '成败(0否1是)',
  status_code      VARCHAR(32)   NULL COMMENT 'HTTP/业务状态',
  attrs_json       TEXT          NULL COMMENT '扩展 JSON',
  create_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (span_id)
);

CREATE INDEX idx_sys_trace_span_trace_start ON sys_trace_span (trace_id, start_offset_ms);

-- ========== sys_rum_event（可选前端原始事件） ==========

CREATE TABLE IF NOT EXISTS sys_rum_event (
  event_id         BIGINT        NOT NULL COMMENT '事件主键',
  app_id           VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '应用标识',
  event_type       VARCHAR(16)   NOT NULL DEFAULT '' COMMENT 'pv/action/api/error',
  trace_id         VARCHAR(64)   NULL COMMENT '链路 ID',
  operation_id     VARCHAR(64)   NULL COMMENT '操作 ID',
  page_path        VARCHAR(500)  NULL COMMENT '页面',
  from_page        VARCHAR(500)  NULL COMMENT '上一页',
  session_id       VARCHAR(64)   NULL COMMENT '会话',
  payload_json     TEXT          NULL COMMENT '事件载荷 JSON',
  client_ip        VARCHAR(128)  NULL COMMENT '客户端 IP',
  ua               VARCHAR(500)  NULL COMMENT 'UA',
  event_time       TIMESTAMP     NULL COMMENT '客户端事件时间',
  create_time      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (event_id)
);

CREATE INDEX idx_sys_rum_event_app_type_time ON sys_rum_event (app_id, event_type, create_time);
CREATE INDEX idx_sys_rum_event_trace_id ON sys_rum_event (trace_id, create_time);
CREATE INDEX idx_sys_rum_event_operation_id ON sys_rum_event (operation_id, create_time);

-- ========== 菜单（parent 2100） ==========

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2171, 2100, '链路Trace', 'C', 'liteTrace', 'monitor/liteTrace/index', 'MonitorLiteTrace', 'monitor:liteTrace:query', 'tree', 11, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2171);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2172, 2171, '链路Trace查询', 'F', NULL, NULL, NULL, 'monitor:liteTrace:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2172);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2173, 2100, '链路查询台', 'C', 'liteTraceQuery', 'monitor/liteTrace/query', 'MonitorLiteTraceQuery', 'monitor:liteTrace:query', 'search', 12, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2173);

INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, route_name, perms, icon, order_num, status, del_flag, create_time)
SELECT 2174, 2173, '链路查询台查询', 'F', NULL, NULL, NULL, 'monitor:liteTrace:query', NULL, 1, '0', '0', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2174);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2171 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2171);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2172 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2172);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2173 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2173);
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, 2174 WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 2174);
