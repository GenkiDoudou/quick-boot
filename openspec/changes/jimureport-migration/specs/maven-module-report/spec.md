## ADDED Requirements

### Requirement: Report Maven module scaffold
系统 SHALL 提供独立 Maven 模块 `quickboot-module-report`，并由父反应器与 `quickboot-app` 依赖组装。

#### Scenario: Module is part of reactor
- **WHEN** 构建父工程 `quickboot`
- **THEN** `quickboot-module-report` 被列入 `<modules>`，且可被 `quickboot-app` 解析依赖

### Requirement: Modulith application module boundary
系统 SHALL 将包根 `io.github.genkidoudou.report` 声明为 Modulith Application Module，仅通过 `api` 命名接口对外暴露契约类型。

#### Scenario: Verify includes report base package
- **WHEN** 运行 `ApplicationModules.verify()`（或等价 Modulith 结构测试）
- **THEN** 基包列表包含 `io.github.genkidoudou.report` 且边界校验通过

### Requirement: No dependency on system internals
`quickboot-module-report` SHALL NOT 依赖 `module-system` 的 `internal` 类型；其 POM SHALL NOT 声明对 `quickboot-module-system` 的依赖。

#### Scenario: Compile without system module
- **WHEN** 编译 `quickboot-module-report`
- **THEN** 源码不引用 `io.github.genkidoudou.system.internal`，且模块可独立编译

### Requirement: Jimu starter dependencies
模块 SHALL 引入 JimuReport / JimuBI / ECharts 的 Boot4 starter（版本分别为 2.5.0 / 2.5.0 / 2.3.0），并由父 POM 管理版本与 Jeecg 仓库（或等价 install 脚本）。

#### Scenario: Dependency resolves
- **WHEN** 解析 `quickboot-module-report` 依赖树
- **THEN** 包含上述三个 starter 且无提交 AI API Key 配置

### Requirement: Auth bridge implemented in app
系统 SHALL 在 `report.api` 定义 `JimuAuthBridge`，并在 `quickboot-app` 提供实现以对接 system 侧用户/角色/权限/字典能力。

#### Scenario: Bridge wiring
- **WHEN** `qc.jimu.enabled=true` 且应用启动
- **THEN** 存在唯一的 `JimuAuthBridge` Bean，且实现类位于 `quickboot-app`（非 report.internal）
