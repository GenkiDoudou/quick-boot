# 发版记录入库 + 企业微信通知设计

**日期：** 2026-08-20  
**状态：** 已批准并实现中 / 已落地（2026-08-20）  
**已确认选项：** 发版说明=手填+git log；新建 callback API；企微 Webhook；仅成功写库 + 成功/失败都发消息；callback 打首台 `http://IP:9993`；同期做管理端列表

## 1. 目标

1. Jenkins 每次 deploy 可手填发版说明，并自动附带 git 提交摘要。  
2. **仅部署成功**时调用后端接口写入 `sys_deploy_record`。  
3. **成功/失败**均通过企业微信机器人 Webhook 发消息。  
4. 管理端提供发布记录列表（分页查询 + 详情中的发版说明）。

## 2. 非目标

- 本期不做 ui/h5 Pipeline 接入（接口预留 `appName`，后续复用）。  
- 失败不写库。  
- 不做复杂审批流、多渠道通知、Vault。

## 3. 数据模型

表：`sys_deploy_record`（Flyway `V33__sys_deploy_record.sql`）

| 列 | 类型 | 说明 |
|----|------|------|
| `record_id` | BIGINT PK | 雪花 |
| `app_name` | VARCHAR(64) | 如 `quickboot` |
| `env` | VARCHAR(32) | test/prod/dev |
| `operate` | VARCHAR(32) | deploy/rollback |
| `branch` | VARCHAR(128) | |
| `hosts` | VARCHAR(512) | 逗号分隔 |
| `build_number` | VARCHAR(32) | |
| `build_url` | VARCHAR(512) | |
| `git_commit` | VARCHAR(64) | 短/长 hash |
| `release_notes` | TEXT | 手填 + git log 合成 |
| `status` | CHAR(1) | `0`=成功（本期仅写成功） |
| `del_flag` / 审计 / `remark` | | 对齐 `BaseEntity` |

## 4. 后端 API

模块：`quickboot-module-system`（监控类路径，与 operlog 并列）

### 4.1 Jenkins Callback（免登录）

```text
POST /monitor/deployRecord/callback
Header: X-Deploy-Token: <DEPLOY_CALLBACK_TOKEN>
Body: DeployRecordCallbackBo（上表业务字段）
返回: R<Void>
```

- `@SaIgnore` + 校验 Header 与配置 `DEPLOY_CALLBACK_TOKEN`（来自 `.env.properties` / 环境）一致；Token 空则拒绝写入。  
- `prod` / `dev` 的 `qc.oauth.ignore-url` 增加 `/monitor/deployRecord/callback`（或仅靠 `@SaIgnore`，以实现时现网安全过滤器为准）。

### 4.2 管理端列表

```text
GET  /monitor/deployRecord/list   # 分页，权限 monitor:deployRecord:list
GET  /monitor/deployRecord/{id}   # 详情，权限 monitor:deployRecord:query
```

- 菜单挂在「系统监控」下，Flyway 种子菜单 + 按钮权限（对齐 operlog 风格）。  
- 前端：`quick-ui` 简单 `C7JsonTable` 列表 + 详情弹窗展示 `releaseNotes`。

## 5. Jenkins（`Jenkinsfile.quickboot`）

### 5.1 新参数

| 参数 | 说明 |
|------|------|
| `RELEASE_NOTES` | 文本域，可空 |
| `WECOM_WEBHOOK_URL` | 企微机器人 URL，可空（空则跳过通知） |
| `DEPLOY_CALLBACK_TOKEN` | 与目标机 `.env` 中 Token 一致；可空则跳过写库并打警告 |

### 5.2 发版说明合成

Checkout 后：

1. `GIT_COMMIT=$(git rev-parse --short HEAD)`  
2. `git log`：优先 `git log ${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..HEAD --oneline`（无则最近 20 条）  
3. `EFFECTIVE_RELEASE_NOTES = RELEASE_NOTES + "\n---\n" + gitlog`（手填空则仅 gitlog）

### 5.3 成功后写库

- Base：`http://<DEPLOY_HOSTS 第一台>:${port}`（`port` 环境变量默认 9993）  
- `POST .../monitor/deployRecord/callback`，Header Token，JSON body  
- HTTP 非 2xx：**打警告，不令整次构建失败**（发版已成功）

### 5.4 企微通知

`post { success / failure }` 中 `curl` POST Webhook：

- 文本/markdown：环境、主机、operate、构建号、结果、notes 截断、BUILD_URL  
- Webhook 空：跳过  
- 通知失败：打日志，不改变构建结果

## 6. 配置

`.env.properties.example` 增加：

```properties
DEPLOY_CALLBACK_TOKEN=change_me_long_random
```

目标机维护真实 Token；Jenkins 参数填相同值。

## 7. 流程

```text
Resolve → Checkout → 合成 RELEASE_NOTES
→ Build → Deploy → Smoke
→ success: callback 写库 + 企微成功
→ failure: 仅企微失败
```

## 8. 成功标准

- [ ] 手填 + git log 出现在入库记录与企微消息摘要  
- [ ] 仅成功调用 callback；Token 错误时构建仍为成功但有警告  
- [ ] 成功/失败均能发企微（Webhook 已配时）  
- [ ] 管理端可分页查看发布记录与发版说明  
- [ ] 密钥不进 Git

## 9. 实现顺序（确认后）

1. Flyway + Entity/Mapper/Service/Controller（callback + list/detail）  
2. 菜单权限种子 + quick-ui 列表页  
3. `.env.properties.example` + Jenkinsfile.quickboot  
4. README 运维说明
