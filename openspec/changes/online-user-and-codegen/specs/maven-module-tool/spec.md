## ADDED Requirements

### Requirement: Tool Maven module scaffold
系统 SHALL 提供独立 Maven 模块 `quickboot-module-tool`，并由父反应器与 `quickboot-app` 依赖组装。

#### Scenario: Module is part of reactor
- **WHEN** 构建父工程 `quickboot`
- **THEN** `quickboot-module-tool` 被列入 `<modules>` 且可被 `quickboot-app` 解析依赖

### Requirement: Modulith application module boundary
系统 SHALL 将包根 `io.github.genkidoudou.tool` 声明为 Modulith Application Module；对外仅通过 `api` 命名接口暴露类型（本期 `api` 可为空但包与注解须就绪）。

#### Scenario: Verify includes tool base package
- **WHEN** 运行 `ApplicationModules.verify()`（或等价 Modulith 结构测试）
- **THEN** 基包列表包含 `io.github.genkidoudou.tool` 且边界校验通过

### Requirement: No dependency on system internals
`quickboot-module-tool` SHALL NOT 依赖 `module-system` 的 `internal` 类型；默认不依赖 `module-system` 模块。

#### Scenario: Compile without system module
- **WHEN** 编译 `quickboot-module-tool`
- **THEN** 其 POM 不声明对 `quickboot-module-system` 的依赖，且源码不引用 `io.github.genkidoudou.system.internal`
