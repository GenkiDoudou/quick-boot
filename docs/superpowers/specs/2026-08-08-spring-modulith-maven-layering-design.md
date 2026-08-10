# Spring Modulith + Maven 多模块分层设计

日期：2026-08-08  
状态：已定稿（OpenSpec change：`spring-modulith-maven-layering`）

## 1. 背景与目标

当前 `quickboot` 后端为技术向 Maven 分层：`common → core → system → web`，业务集中在 `quickboot-system`，尚未引入 Spring Modulith。

本设计目标：

- **为以后拆微服务做准备**：先在单体内用 Modulith 强制业务边界，再按需整模块外提。
- **Maven 模块与 Modulith Application Module 一对一**：一个业务域一个 Maven 模块、一个 Modulith 模块。
- **跨模块默认显式 API**：他域只依赖本域 `api` 包中的接口与契约类型。
- **第一阶段不拆 `system`**：现有系统能力仍作为单一业务模块；新业务域再按同一模板追加。

非目标：

- 第一阶段不把 `system` 细拆为 identity / permission 等多域。
- 不引入独立的 `*-api` Maven 模块（契约放在同模块的 `api` 包即可）。
- 不强制事件驱动（可后加 Modulith Application Events）。
- 不改前端与库表。
- **不合并** `common` 与 `core` 为单一 `platform`。

## 2. 决策摘要

| 议题 | 选择 |
|------|------|
| 引入目的 | 为拆微服务做准备 |
| Maven ↔ Modulith | 一对一 |
| 共享底座 | 保留 `common`（工具）+ `core`（项目间共享） |
| 跨模块协作 | 显式 API（接口 + DTO/命令/事件契约） |
| 第一阶段业务域 | 暂保留完整 `system` 一个模块 |
| 推荐方案 | 方案 A（修订版）：`common` + `core` + `module-system` + `app` |

## 3. 目标架构

### 3.1 逻辑视图（第一阶段）

```text
quickboot-app                 启动、组装、Modulith 结构校验
        │
        ▼
quickboot-module-system       Modulith 业务模块（api / internal）
        │
        ▼
quickboot-core                项目间共享（BaseEntity、项目级约定/抽象）
        │
        ▼
quickboot-common              独立工具模块（无业务能力）
```

依赖方向：`app → module-* → core → common`。禁止反向依赖；禁止 `common` / `core` 依赖任何 `module-*`。

### 3.2 模块职责

| Maven 模块 | 职责 | Modulith |
|------------|------|----------|
| `quickboot-common` | 独立工具：异常、校验组、Excel、缓存封装、安全基建等，**无业务表实体/业务 Service** | 非业务 Application Module |
| `quickboot-core` | 项目间共享：如 `BaseEntity`、可复用的项目级抽象；可被本仓库及其他项目依赖 | 非业务 Application Module |
| `quickboot-module-system` | 现有系统域：用户/角色/菜单/部门/字典/配置/日志/OAuth 客户端/登录等 | 一个 Application Module |
| `quickboot-app` | `@SpringBootApplication`、扫描与装配、CI 中跑模块校验；**不写业务 Controller** | 组装根 |

### 3.3 演进（第二阶段起）

新增业务域时复制模板，例如 `quickboot-module-order`：

- 新模块依赖 `core`（及传递的 `common`），以及所需他域的 **`api` 包**。
- 禁止依赖他域 `internal`。
- 拆微服务时：将该 Maven 模块（及对其 `api` 的依赖关系）整块外提，再把进程内调用换成 RPC/HTTP。

## 4. 业务模块内部结构

以 `quickboot-module-system` 为模板（包根示例：`io.github.genkidoudou.system`）：

```text
api/           对外公开：Facade/Query 接口、DTO、命令、事件契约
internal/      默认封闭：controller、service、mapper、entity、config、api 实现
package-info   @ApplicationModule 与开放包声明（开放 api）
```

约定：

- HTTP Controller 放在各业务模块的 `internal`（与现网 system 一致）。
- 第一阶段虽暂无第二业务域，仍把「可能被他域使用」的能力抽到 `api`（例如用户查询、字典查询），避免日后倒挖。
- 同模块 `internal` 内可自由协作。

## 5. 依赖与禁令

**允许**

- `app` → 任意 `module-*`、`core`、`common`
- `module-X` → `core` → `common`
- `module-X` → `module-Y.api`（编译期仅依赖公开类型）

**禁止**

- `module-X` → `module-Y.internal`
- `common` / `core` → 任意 `module-*`
- `common` ↔ `core` 反向（仅允许 `core → common`）
- 业务模块之间循环依赖
- 将业务实体 / Mapper / 业务 Service 放入 `common` 或 `core`

## 6. 从现状迁移

### 6.1 映射

| 现状 | 目标 |
|------|------|
| `quickboot-common` | 保留为独立工具模块（职责边界收紧：无业务） |
| `quickboot-core` | 保留为项目间共享模块 |
| `quickboot-system` | 演进为 `quickboot-module-system`（增加 `api` / `internal` 与 Modulith 元数据） |
| `quickboot-web` | 演进为 `quickboot-app` |

命名可一次改干净；推荐 artifactId / 目录与文档一致，避免长期双名。若短期必须兼容，允许先改包结构与 Modulith 元数据，再改模块名。

### 6.2 建议顺序

1. 引入 Spring Modulith 依赖与 `ApplicationModules.verify()` 测试骨架。
2. 梳理并收紧 `common` / `core` 边界（移出误放的业务代码，保证单向依赖）。
3. 将 `system` 整理为 `api` + `internal`，声明 Application Module。
4. `web` → `app`：调整 `scanBasePackages` / `MapperScan`；CI 加入结构测试。
5. 文档化「新域模板」（模块名、包名、`api` 约定、依赖清单）。

### 6.3 成功标准

- `ApplicationModules.of(...).verify()` 通过。
- Maven 依赖图符合第 5 节禁令。
- 登录、用户、字典等主路径回归可用。
- 新增业务域有可复制的书面模板。

## 7. 与备选方案的关系

曾对比三种方案：

- **A（采用，并修订）**：业务一对一模块 + 共享底座；原「合并 platform」已按反馈改为保留 `common` + `core`。
- **B**：每域再拆独立 `*-api` jar — 契约最硬但模块膨胀，第一阶段过重，不采用。
- **C**：少改名渐进 — 迁移最快但一对一叙事弱，不作为主叙事；实施时可借鉴其「先结构后改名」节奏。

## 8. 开放问题（实现计划阶段再细化）

- Spring Modulith 与当前 Spring Boot 4.0.0 的具体依赖版本对齐。
- `module-system` 第一批进入 `api` 的具体接口清单。
- artifact 是否立即从 `quickboot-system` / `quickboot-web` 重命名，或分两步。
