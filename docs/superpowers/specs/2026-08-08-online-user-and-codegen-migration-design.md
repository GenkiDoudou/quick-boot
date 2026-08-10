# 在线用户 + 代码生成迁移设计

日期：2026-08-08  
状态：已定稿（待实现）  
来源：`bak/quickboot`（`system/online`、`tool/gen`）+ `bak/quick-ui` 对应页面；策略对齐 Modulith 分层。

## 1. 背景与目标

现网已完成 Modulith（`common` / `core` / `module-system` / `module-quartz` / `app`）与多项系统/监控能力；**在线用户**与**代码生成**仍在 `bak`，未迁入。

目标：

1. **在线用户**：基于 Sa-Token 会话的列表与强退；登录成功写入会话展示字段。
2. **代码生成**：迁移 bak 全量能力（导入表、建表 SQL、配置编辑、预览、同步、Zip、写盘），包名与分层适配现网。

非目标：

- 不迁慢 SQL、客户端轨迹、报表等其它 bak 监控/工具。
- 不引入 RuoYi Velocity；沿用 bak FreeMarker。
- 树表/主子表模板若 bak 未开放，保持「配置可存、生成提示未开放」。
- 不自动 `INSERT sys_menu`（可保留 menu.sql 模板片段）。

## 2. 决策摘要

| 议题 | 选择 |
|------|------|
| 交付节奏 | 两个能力同一变更一起交付 |
| 模块落点 | 在线 → `module-system`；生成 → 新建 `quickboot-module-tool` |
| 完整度 | 行为对齐 bak 已实现全量；包名/分层按现网改 |
| 实现路径 | Modulith 迁码 + 前端改造（方案 1） |
| 生成配置前缀 | 统一为 `qc.gen.*`（从 bak `quickboot.gen` 迁移时改名） |
| 生成模块包名 | `io.github.genkidoudou.tool` |

## 3. 在线用户

### 3.1 API 与权限

| 接口 | 说明 | 权限 |
|------|------|------|
| `GET /monitor/online/list` | 按 IP/用户名筛选，内存分页 | `monitor:online:list` |
| `POST /monitor/online/forceLogout` | body 含 `tokenId`，强退会话 | `monitor:online:forceLogout` |

Controller 建议 `@IgnoreLogger(Type.ALL)`，避免监控页刷操作日志。

### 3.2 会话写入

登录成功、`LoginHelper.loginByDevice` 之后调用 `OnlineSessionRecorder`，向 Token-Session 写入：

- 用户名、部门名、IP、登录地（可空）、浏览器、OS、登录时间

列表通过 `StpUtil.searchTokenValue` 扫描有效 token，组装 VO；**不建业务表**。

### 3.3 落点

- 后端：`module-system/internal/.../online/`（controller / service / dto / support）
- 前端：`quick-ui/src/views/monitor/online`、`api/monitor/online.js`
- Flyway：监控下菜单「在线用户」+ 按钮权限
- 分页：注意 `C7JsonTable` 的 `{current,size,param}` 与扁平 GET query 的映射（对齐已修的 job-log）

## 4. 代码生成

### 4.1 Maven / Modulith

新建 `quickboot/quickboot-module-tool`：

- `artifactId`：`quickboot-module-tool`
- 包根：`io.github.genkidoudou.tool`（`api` 可暂空；实现 `internal`）
- 依赖：`core` → `common`；`app` 依赖本模块并纳入结构测试

参照 `openspec/.../new-domain-module-template.md` 与 quartz 模块先例。

### 4.2 API 与权限

前缀 `/tool/gen`，权限 `tool:gen:list|import|create|edit|remove|preview|code`。

能力（与 bak `GenController` 对齐）：

- 配置分页 / 库表候选 / 全局 defaults / 详情 / 保存配置
- 导入表 / 执行建表 SQL / 预览 / 删除 / 同步库表结构
- 批量 Zip 下载 / 自定义路径写盘

### 4.3 数据与实现

- Flyway：`gen_table`、`gen_table_column`（自 bak DDL 迁入适配）+ 菜单权限种子
- FreeMarker 模板：自 bak `vm/quickboot/**/*.ftl` 迁入 module-tool 资源；**生成物**包路径/分层对齐现网（entity/service/controller、`C7` 前端骨架等）
- 支持类：库表内省、模板渲染、Zip 导出、写盘（路径校验防目录穿越）
- 建表：仅允许 `CREATE TABLE`；语句数量上限可配
- 配置键示例：`qc.gen.author`、`package-name`、`module-name`、`zip-file-name`、`create-table-max-statements`、写盘根路径（若 bak 有）

### 4.4 前端

迁 `views/tool/gen`（`index` / `edit` + 导入 / 建表 / 预览弹窗）与 API；对齐现网 `request`、权限指令与分页约定。

## 5. 验收标准

**在线用户**

1. 登录后列表可见本会话关键展示字段  
2. 用户名 / IP 筛选有效  
3. 强退后旧 token 访问需登录接口返回 401  
4. 无权限不可列表 / 强退  

**代码生成**

1. 导入 → 编辑 → 预览 → 同步 → 删除流程可用  
2. Zip 内代码包路径符合现网约定  
3. 写盘在允许配置下可写且拒绝路径穿越  
4. 非法建表 SQL 被拒绝  
5. 菜单与按钮权限可用  

**回归**

- Modulith 结构测试通过；登录与现有监控/系统能力不回归  

## 6. 风险与缓解

| 风险 | 缓解 |
|------|------|
| 一次迁两块体量大 | tasks 分「在线 → 生成后端 → 生成前端 → 联调」；可并行但同一 change |
| 生成模板与现网分层不一致 | 改 ftl 输出路径/类名，对照 `SysUser` / C7 页样板 |
| 在线列表与 C7 分页参数不一致 | 复用 job-log 扁平映射模式 |
| Sa-Token `searchTokenValue` 前缀差异 | 复用 bak `resolveTokenValue` 逻辑并加冒烟 |

## 7. 实现顺序建议

1. Flyway（online 菜单 + gen 表/菜单）  
2. 在线后端 + 登录挂钩 + 前端  
3. `module-tool` 脚手架 + 迁 gen 后端/模板  
4. gen 前端联调  
5. 验收清单勾选  
