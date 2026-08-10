## Context

权威产品设计见 `docs/superpowers/specs/2026-08-08-spring-modulith-maven-layering-design.md`。本文件为 OpenSpec 实现向设计。

现状：Maven 模块为 `common → core → system → web`；业务集中在 `quickboot-system`；启动类在 `quickboot-web`；未引入 Spring Modulith。目标是在单体内强制业务模块边界，并为日后按 Maven 模块拆微服务做准备。

约束：保留 `common`（工具）与 `core`（项目间共享），不合并为 `platform`；第一阶段不拆 `system` 为多业务域；不改前端与库表；跨模块默认显式 API。

## Goals / Non-Goals

**Goals:**

1. 引入 Spring Modulith，业务模块一对一对应 Maven 模块，CI/测试可 `verify()`。
2. `system` 演进为 `module-system`，包分为 `api` / `internal`；抽出首批公开 API。
3. `web` 演进为 `app` 组装根；依赖图符合 `app → module-* → core → common`。
4. 收紧 `common`/`core` 职责（无业务实体/Service）；文档化新域模板。

**Non-Goals:**

- 拆分 `system` 为 identity/permission 等多域。
- 独立 `*-api` Maven 模块。
- 强制 Application Events / 消息化。
- 合并 `common`+`core` 为 `platform`。
- 前端、Flyway/库表、HTTP 业务契约变更。

## Decisions

### 1. 一次重命名 artifact（不用长期双名）

- `quickboot-system` → `quickboot-module-system`
- `quickboot-web` → `quickboot-app`
- 备选：先改包再改名 → 否决为默认路径（增加双名窗口）；仅当构建/IDE 阻塞时才分两步。

### 2. 共享底座保持两层

- `common`：无业务工具与基建。
- `core`：项目间共享（如 `BaseEntity`），仅依赖 `common`。
- 备选：合并 `platform` → 已否决（与跨项目复用粒度冲突）。

### 3. Modulith 模块边界

- 业务 Application Module 从 `module-system` 起算；`common`/`core` 不标业务模块。
- 开放包：`*.api`；其余默认 `internal`（或等价封闭约定）。
- 结构测试挂在 `quickboot-app`（或 app 测试源码）调用 `ApplicationModules.verify()`。

### 4. 跨模块协作：显式 API

- 他域只依赖本域 `api` 中的接口与 DTO/命令/事件类型；实现类在 `internal`。
- 备选：事件优先 / 双轨 → 第一阶段不采用；可后加。

### 5. 首批 `system.api` 范围（最小可演进）

优先抽出只读查询门面，避免倒挖：

- 用户查询（按 id / username 等只读能力，供他域日后使用）
- 字典查询（与现有 `DictLookup` / 按类型取项能力对齐的门面）

不要求第一阶段把全部 CRUD Service 搬进 `api`。

### 6. Controller 位置

- 留在 `module-system` 的 `internal`；`app` 不承载业务 REST。

### 7. Spring Modulith 版本

- 通过与 Spring Boot 4.0.0 兼容的 BOM/依赖管理引入；具体版本号在实现时以官方兼容矩阵为准，记入父 POM `dependencyManagement`。

## Risks / Trade-offs

- [包搬家导致大 diff / IDE 索引] → 按模块分批提交；优先机械移动 + 再抽 api。
- [Modulith 与 Boot 4 版本不匹配] → 实现前核对兼容版本；校验测试失败则先钉版本再改结构。
- [误把业务代码留在 common] → tasks 含边界审计项；禁止 common/core 依赖 module。
- [重命名打断本地脚本] → 更新 README/AGENTS 中模块名提及处（仅本变更触及的文档）。

## Migration Plan

1. 父 POM 增加 Modulith 依赖管理；app（或现 web）增加空壳 `verify()` 测试。
2. 审计并修正 `common`/`core` 依赖与误放业务代码。
3. 重命名 `system`/`web` 目录与 artifact；整理 `api`/`internal`；声明 Application Module。
4. 实现首批 api 门面（internal 实现委托现有 Service）。
5. 调整扫描包与 MapperScan；全量编译 + `verify()` + 主路径冒烟。
6. 写入新域模板说明。

回滚：还原 POM 模块名与包结构；移除 Modulith 依赖与测试（业务行为无强制 schema 变更，回滚以 Git 为准）。

## Open Questions

- 无阻塞项。实现时确认 Modulith 精确版本号与首批 api 方法签名（对齐现有 Service，不扩大行为）。
