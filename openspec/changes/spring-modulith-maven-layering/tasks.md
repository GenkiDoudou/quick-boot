## 1. Modulith 依赖与校验骨架

- [x] 1.1 在父 POM `dependencyManagement` 中引入与 Spring Boot 4.0.0 兼容的 Spring Modulith 版本
- [x] 1.2 在组装模块（现 `quickboot-web`，后续改名为 app）测试源码中增加 `ApplicationModules.verify()` 骨架测试并确保可编译运行
- [x] 1.3 确认 Modulith 相关测试依赖（如 `spring-modulith-starter-test`）仅用于测试范围

## 2. 共享模块边界审计

- [x] 2.1 审计 `quickboot-common`：移出或标注误放的业务实体/Mapper/业务 Service；保持无业务工具职责
- [x] 2.2 审计 `quickboot-core`：确认仅含项目间共享抽象（如 BaseEntity）；POM 仅依赖 `common`
- [x] 2.3 确认 `common`/`core` 均不依赖 `system`/未来 `module-*`

## 3. Maven 模块重命名与反应器

- [x] 3.1 将 `quickboot-system` 目录与 artifactId 重命名为 `quickboot-module-system`，更新父 POM `<modules>` 与引用方依赖
- [x] 3.2 将 `quickboot-web` 目录与 artifactId 重命名为 `quickboot-app`，更新父 POM 与启动相关配置
- [x] 3.3 全反应器 `mvn -q -DskipTests compile`（或等价）通过

## 4. System Modulith 包结构

- [x] 4.1 在 `module-system` 建立 `api` 与 `internal` 包树，并将现有 controller/service/mapper/entity/config 迁入 `internal`
- [x] 4.2 添加 Modulith `package-info`（或等价元数据），声明 Application Module 且仅开放 `api`
- [x] 4.3 调整 `quickboot-app` 的 `scanBasePackages` / `MapperScan` 指向迁移后的包路径
- [x] 4.4 修复因搬家产生的 import/资源路径问题，保证模块可启动编译

## 5. 首批 system.api 门面

- [x] 5.1 在 `api` 中定义用户只读查询 Facade/接口与必要 DTO，`internal` 实现委托现有用户查询能力
- [x] 5.2 在 `api` 中定义字典查询 Facade/接口（对齐现有 DictLookup/按类型查询），`internal` 实现委托现有字典能力
- [x] 5.3 确认 HTTP 路径与对外 JSON 契约未因抽取 api 而改变

## 6. 结构校验与回归

- [x] 6.1 使 Modulith `verify()` 测试在目标布局下通过
- [x] 6.2 用依赖检查或评审确认无 `module`→`internal` 跨模块引用、无 shared→business 依赖
- [x] 6.3 冒烟：登录、用户管理关键读路径、字典相关主路径可用

## 7. 新域模板文档

- [x] 7.1 编写新业务域模板（`quickboot-module-<domain>`、`api`/`internal`、依赖 `core`/`common` 与他域 `api` only）
- [x] 7.2 在 change 文档或仓库约定位置交叉引用权威设计 `docs/superpowers/specs/2026-08-08-spring-modulith-maven-layering-design.md`
- [x] 7.3 更新本仓库中提及旧模块名 `quickboot-system`/`quickboot-web` 的必要说明（如 AGENTS 技术栈摘要中的模块列表，若存在过时表述）
