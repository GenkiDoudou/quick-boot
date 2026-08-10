## ADDED Requirements

### Requirement: Monitor Maven module scaffold
系统 SHALL 提供独立 Maven 模块 `quickboot-module-monitor`，并由父反应器与 `quickboot-app` 依赖组装。

#### Scenario: Module is part of reactor
- **WHEN** 构建父工程 `quickboot`
- **THEN** `quickboot-module-monitor` 被列入 `<modules>` 且可被 `quickboot-app` 解析依赖

### Requirement: Modulith application module boundary
系统 SHALL 将包根 `io.github.genkidoudou.monitor` 声明为 Modulith Application Module，仅通过 `api` 命名接口对外暴露类型，并允许依赖 `system::api`。

#### Scenario: Verify includes monitor base package
- **WHEN** 运行 `ApplicationModules.verify()`（或等价 Modulith 结构测试）
- **THEN** 基包列表包含 `io.github.genkidoudou.monitor` 且边界校验通过

### Requirement: No dependency on system internals
`quickboot-module-monitor` SHALL NOT 引用 `io.github.genkidoudou.system.internal`；跨域协作 MUST 仅通过 `system.api` 公开类型。

#### Scenario: Compile without system internal imports
- **WHEN** 编译 `quickboot-module-monitor`
- **THEN** 源码不存在对 `io.github.genkidoudou.system.internal` 的 import，且对 system 的依赖仅用于其 `api` 契约
