# 项目概览

## 项目目标
`quickboot2` 是一个企业后台全栈项目仓库，包含：
- 后端服务与通用基础能力模块
- 前端管理端与可复用组件库
- 文档站点
- 基于 OpenSpec 的规范化变更流程

本仓库强调：
- 公共能力下沉与复用（`quickboot-common`）
- 规范驱动协作（`openspec/`）
- 前端统一组件模式（`quick-ui/src/packages`）

## 仓库结构
- `quickboot/`：Java 后端（Maven 多模块）
  - `quickboot-common/`：通用能力（统一响应、异常、安全、缓存、国际化、Servlet 工具、Excel 工具等）
  - `quickboot-core/`：核心配置与基础设施能力
  - `quickboot-web/`：Spring Boot Web 应用（controller/service/mapper/domain 等）
- `quick-ui/`：Vue 3 + Vite 前端管理端
- `docs/`：VitePress 文档站点
- `openspec/`：规范、变更提案与任务
- `原始需求/`：需求参考资料（不参与运行构建）

# 技术栈

## 后端
- Java 17
- Spring Boot 3.x
- MyBatis-Plus
- Jakarta Validation
- Sa-Token（认证鉴权）
- Hutool
- EasyExcel
- Caffeine / Redis（缓存后端可切换）
- MySQL 8
- Flyway（数据库迁移）

## 前端
- Vue 3
- Vite 5
- Element Plus
- Pinia
- Vue Router
- Axios
- Vitest（已具备）
- pnpm 9

## 文档与流程
- VitePress
- OpenSpec（规范驱动研发流程）

# 构建与运行

## 后端
```bash
cd quickboot
mvn clean install -DskipTests
mvn -pl quickboot-web spring-boot:run
```

## 前端
```bash
cd quick-ui
pnpm i
pnpm dev
pnpm build:prod
```

## 文档
```bash
cd docs
pnpm i
pnpm dev
```

# 代码与架构约定

## 通用约定
- 所有文本文件统一使用 UTF-8 编码（建议 UTF-8 无 BOM）。
- 优先遵循仓库已有规则：`AGENTS.md`、`.cursorrules`、`.cursor/rules/*`（若存在）。
- 避免在大依赖目录中做递归批量修改（如 `node_modules`、备份目录等）。

## 后端约定
- 分层保持清晰：`controller / service / mapper / domain(entity) / dto(bo/vo)`。
- 请求参数使用 `Bo`，返回对象使用 `Vo`。
- 校验注解放在 `Bo` 上。
- 新增/修改使用分组校验：
  - `AddGroup`、`UpdateGroup`
  - `@Validated(AddGroup.class)` / `@Validated(UpdateGroup.class)`。
- `Bo/Entity/Vo` 之间优先使用 `BeanUtil.copyProperties()` 做转换。
- 接口约定：
  - 修改/删除默认优先 `@PostMapping`（通过路径表达动作语义）
  - 补齐 OpenAPI 注解：`@Tag`、`@Operation`、关键参数 `@Parameter`。
- 业务失败抛项目自定义异常，不使用 `IllegalArgumentException` 作为业务异常。
- 响应遵循统一 `R` 结构（业务码驱动，通常 HTTP 200 + JSON 体 `code` 判定成败）。

## 前端约定
- API 模块放在 `quick-ui/src/api`。
- 通用组件放在 `quick-ui/src/components`。
- 业务增强组件库放在 `quick-ui/src/packages`。
- 新建或改造页面需遵循 `DESIGN.md`。
- 表格/查询页优先复用统一模式（如 `C7JsonTable` 体系），减少重复实现。

## Excel 约定
- Excel 公共能力统一沉淀在 `quickboot-common`。
- 优先复用 `EasyExcelSupport` 与通用 listener/result 抽象。
- 导入流程应提供可定位错误信息（行号、列、原因），必要时输出失败明细文件。

# 数据与配置约定
- 数据库结构变更使用 Flyway 管理。
- 环境差异通过 Spring Profile 配置管理。
- 缓存命名与 TTL 规则遵循 `quickboot-common` 的既有约定。

# OpenSpec 流程约定
- 变更目录使用：`openspec/changes/<change-id>/`。
- `proposal/design/tasks/spec` 需保持一致与同步。
- 优先小步、可评审、边界清晰的变更。
- 代码实现需与对应 spec/task 对齐，避免“实现与规范脱节”。

# 质量与验证
- 前端最少验证：`pnpm build:prod`。
- 后端优先模块级验证，再做全量构建/测试。
- 提交前检查代码引用依赖是否已在对应 `pom.xml` / `package.json` 声明。

# 协作补充
- 跨模块通用能力优先实现一次并下沉到 `quickboot-common`，业务模块按需复用。
- 优先保持与现有模块风格一致，避免在同一仓库混入多套实现范式。
- 重要行为变更需同步更新 OpenSpec 产物，确保规范与实现一致。
