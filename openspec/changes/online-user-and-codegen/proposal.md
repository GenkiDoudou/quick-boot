## Why

现网已完成 Modulith 分层与多项系统/监控能力，但 **在线用户** 与 **代码生成** 仍留在 `bak`，运营侧无法强退会话，开发侧无法在现网一键生成对齐分层的 CRUD 骨架。两者一并迁入可补齐监控与工具链缺口，并与已定稿设计对齐落地。

## What Changes

- **在线用户**：在 `quickboot-module-system` 落地基于 Sa-Token 会话的列表与强退；登录成功写入会话展示字段；前端监控页 + Flyway 菜单权限。不新建业务表。
- **代码生成**：新建 Maven / Modulith 模块 **`quickboot-module-tool`**（包根 `io.github.genkidoudou.tool`），迁入 bak 全量 gen 能力（导入表、建表 SQL、配置编辑、预览、同步、Zip、写盘），FreeMarker 模板适配现网包路径/分层；配置前缀统一为 `qc.gen.*`。
- 前端：`monitor/online`、`tool/gen`（含编辑/导入/建表/预览）与对应 API。
- **BREAKING（相对 bak）**：生成配置键由 `quickboot.gen.*` 改为 `qc.gen.*`；包名改为 `io.github.genkidoudou.tool.*`；在线落在 `module-system`，生成落在独立 `module-tool`。

权威产品设计：`docs/superpowers/specs/2026-08-08-online-user-and-codegen-migration-design.md`。

## Capabilities

### New Capabilities

- `monitor-online`: 在线会话列表（IP/用户名筛选、内存分页）、强制下线；登录成功写入 Token-Session 展示字段。
- `tool-gen`: `/tool/gen` 下代码生成全流程（配置 CRUD、导入/建表、预览、同步、Zip、写盘）及 FreeMarker 生成物约定。
- `maven-module-tool`: `quickboot-module-tool` 脚手架、Modulith 边界、`app` 依赖与基包注册。

### Modified Capabilities

- （无既有主 specs 能力需改写；登录挂钩为 system 内部实现细节，不另立 delta。）

## Impact

- 后端：`module-system` 增加 `online`；新建 `quickboot-module-tool`；父 POM / `quickboot-app` 依赖；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.tool`；登录成功路径挂钩 `OnlineSessionRecorder`；Flyway 新迁移（online 菜单 + `gen_table` / `gen_table_column` + gen 菜单）。
- 前端：`quick-ui` 增加 `monitor/online`、`tool/gen` 及 API；分页参数对齐 C7 / job-log 扁平映射。
- 依赖：FreeMarker（随 gen 迁入）；无新增业务表用于在线用户。
- 权限字：`monitor:online:list|forceLogout`；`tool:gen:list|import|create|edit|remove|preview|code`。
