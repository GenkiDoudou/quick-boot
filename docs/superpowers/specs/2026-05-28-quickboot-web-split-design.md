## 背景与目标

将 `quickboot/quickboot-web` 拆分为两个业务子模块：

- `system` 模块：合并系统管理相关能力
- `tools` 模块：合并除 system 指定范围外的其余能力

最终 `quickboot-web` 模块仅作为 **Spring Boot 启动入口 + 依赖聚合**，不再承载具体业务包。

## 现状概览（关键点）

- `quickboot-web` 当前是 **可执行 Spring Boot 模块**（`spring-boot-maven-plugin` `repackage`）。
- 启动类：`io.github.genkidoudou.WebApplication`，根包为 `io.github.genkidoudou`，默认会扫描其子包。

## 拆分范围（已确认）

### system 模块包含

以下包从 `quickboot-web` 迁移到 `quickboot-system`：

- `io.github.genkidoudou.web.system/**`
- `io.github.genkidoudou.web.monitor.online/**`
- `io.github.genkidoudou.web.monitor.operlog/**`
- `io.github.genkidoudou.web.monitor.logininfor/**`

### tools 模块包含

除上述 `system` 范围以外，`quickboot-web` 现有的其余 Java 代码全部迁移到 `quickboot-tools`，其中明确包含：

- `io.github.genkidoudou.web.monitor.job/**`（选择 B：归 tools）
- `io.github.genkidoudou.web.tool/**`
- `io.github.genkidoudou.web.auth/**`
- `io.github.genkidoudou.web.report/**`
- `io.github.genkidoudou.web.config/**`
- 以及其它未在列表中点名的 `io.github.genkidoudou.web.*` 相关包

> 约束：迁移后 **包名保持不变**（仍是 `io.github.genkidoudou.web...`），仅移动代码所在 Maven 模块。

## Maven 模块设计（推荐方案 A，已确认 artifactId 命名）

新增两个同级模块：

- `quickboot/quickboot-system`（`artifactId=quickboot-system`）
- `quickboot/quickboot-tools`（`artifactId=quickboot-tools`）

调整聚合与依赖关系：

- `quickboot/pom.xml`：`<modules>` 新增 `quickboot-system`、`quickboot-tools`
- `quickboot-web/pom.xml`：
  - 移除直接承载业务代码的依赖（不做额外改造，只保证能跑起来）
  - 新增依赖 `quickboot-system` 与 `quickboot-tools`
  - 仍保留 `spring-boot-maven-plugin` 与 `WebApplication`

## Spring 扫描与运行方式

保持以下不变：

- `WebApplication` 仍在 `io.github.genkidoudou` 根包
- `quickboot-web` 依赖引入 `quickboot-system`、`quickboot-tools` 后，Spring Boot 默认组件扫描依旧覆盖 `io.github.genkidoudou.web...`
- `mvn -pl quickboot-web spring-boot:run` 等启动方式不变（如项目已有习惯）

## 资源与配置迁移策略

本次拆分优先只处理 Java 源码与 Maven 模块边界，资源文件按以下策略：

- `quickboot-web/src/main/resources` 默认继续保留在 `quickboot-web`（作为启动模块资源汇聚点）
- 若后续发现某些资源天然归属于 system/tools（例如模板、静态资源、自动配置元数据等），再按“最小改动”原则迁移到对应模块

## 成功标准（可验证）

- `quickboot` 父工程 `mvn -q -DskipTests clean package` 通过（或至少 `mvn -pl quickboot-web -am -DskipTests clean package` 通过）
- `quickboot-web` 仍可启动到 Spring Boot（不要求连库成功，但应完成上下文启动）
- 原有包名与对外 API 路径不因拆分发生变化（仅模块边界变化）

