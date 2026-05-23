# 数据库设计

## 迁移管理

- 工具：**Flyway**
- 路径：`quickboot-web/src/main/resources/db/migration/`
- 命名：`V{版本}__{描述}.sql`

## 核心表（RBAC）

| 表 | 说明 |
|----|------|
| `sys_user` | 用户 |
| `sys_role` | 角色 |
| `sys_menu` | 菜单/按钮权限 |
| `sys_dept` | 部门 |
| `sys_user_role` / `sys_role_menu` | 关联 |

## 系统配置

| 表 | 说明 |
|----|------|
| `sys_config` | 参数键值 |
| `sys_dict_type` / `sys_dict_data` | 字典 |
| `sys_notice` | 公告 |

## 审计与任务

| 表 | 说明 |
|----|------|
| `sys_oper_log` | 操作日志 |
| `sys_logininfor` | 登录日志 |
| `sys_job` / `sys_job_log` | 定时任务 |
| Quartz 表 | 调度引擎 |

## OAuth2

| 表 | 说明 |
|----|------|
| `sys_oauth_client` | 客户端 + API 路径授权 |
| `sys_oauth_provider` | 外部 IdP |
| `sys_oauth_user_openid` | 用户与外部 openid 绑定 |

## 代码生成

| 表 | 说明 |
|----|------|
| `gen_table` / `gen_table_column` | 生成元数据 |

## 约定

- 逻辑删除：`del_flag`
- 主键：雪花 ID（`assign_id`）

完整版本索引见 [后端模块总览](../backend/modules/index#数据库迁移索引)。
