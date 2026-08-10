## Why

当前后端按技术层（`common → core → system → web`）组织，业务边界无法在编译期/测试期强制约束，不利于按域整模块外提为微服务。需要在保留单体部署的前提下引入 Spring Modulith，并让 Maven 业务模块与 Modulith Application Module 一对一，为后续拆分打底。

参考设计：`docs/superpowers/specs/2026-08-08-spring-modulith-maven-layering-design.md`。

## What Changes

- 引入 Spring Modulith，在组装根上提供 `ApplicationModules.verify()` 结构测试。
- 业务模块采用 `api`（公开）/ `internal`（封闭）包约定；第一阶段将现有 `system` 演进为单一 Modulith 业务模块，并抽出首批跨域可用的 `api` 契约。
- Maven 布局对齐：`quickboot-system` → `quickboot-module-system`，`quickboot-web` → `quickboot-app`；**保留** `quickboot-common`（工具）与 `quickboot-core`（项目间共享），**不**合并为 `platform`。
- 收紧依赖方向：`app → module-* → core → common`；禁止共享层依赖业务模块、禁止跨域依赖 `internal`。
- 文档化「新业务域」复制模板（模块名、包名、`api` 约定、依赖清单）。
- **BREAKING（模块坐标/包路径）**：Maven artifactId 与部分包路径随重命名/分包调整；对外 HTTP API 路径与业务行为保持不变。

## Capabilities

### New Capabilities

- `modulith-boundaries`: Modulith Application Module 声明、`api`/`internal` 可见性、结构校验测试、system 首批公开 API 契约。
- `maven-module-layering`: Maven 多模块命名与依赖禁令、`common`/`core` 职责边界、app 组装根职责。

### Modified Capabilities

- （无；`openspec/specs/` 尚无既有能力需 delta。）

## Impact

- 后端：父 POM 模块列表与依赖管理；`system`/`web` 目录与 artifact 重命名；包结构重组；新增 Modulith 依赖与校验测试。
- 运行时：仍为单一可部署单体；登录/用户/字典等主路径行为不变。
- 前端 / 库表：不在本变更范围。
- 文档：新域模板说明（可放在 change 内或指向 design）。
