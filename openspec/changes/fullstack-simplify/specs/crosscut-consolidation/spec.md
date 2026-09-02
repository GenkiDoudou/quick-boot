## ADDED Requirements

### Requirement: OperLog persistence consolidation
Operation log capture in `quickboot-common` (annotation, aspect, event, desensitization) MUST remain free of `sys_oper_log` entity references. Persistence in `quickboot-module-system` MUST consolidate Assembler/MetaResolver/PersistSupport into a single `OperLogRecorder` (or equivalent) invoked by event listener. Monitor LogHub MUST query oper logs only via `system.api.OperLogMonitorQuery` facade, not system internal types.

#### Scenario: Oper log event persists after refactor
- **WHEN** a RestController method completes under capture-enabled config
- **THEN** a row appears in `sys_oper_log` via system listener calling consolidated recorder

### Requirement: SlowSQL chain consolidation
Slow SQL capture SPI MUST remain in common; Druid filter registration in app; persistence and REST management MUST reside in monitor module. App MUST NOT contain SlowSQL persist logic beyond filter bean registration.

#### Scenario: Slow SQL appears in monitor API
- **WHEN** a query exceeds configured threshold
- **THEN** a slow SQL row is queryable via monitor slow SQL POST page endpoint

### Requirement: Monitor module Maven boundary
`quickboot-module-monitor` MUST depend on `quickboot-module-system` and `quickboot-module-quartz` only through their public `api` packages. Source MUST NOT import `*.system.internal.*` or `*.quartz.internal.*` after migration.

#### Scenario: Modularity test passes monitor boundary
- **WHEN** ModularityTests run after crosscut migration
- **THEN** no monitor→system.internal dependency violations are reported

### Requirement: ExceptionReporter SPI
`quickboot-common` SHALL define `ExceptionReporter` interface. Monitor MAY provide implementation for LiteTrace reporting. `GlobalExceptionHandler` in app MUST depend on `ObjectProvider<ExceptionReporter>` only, not concrete monitor classes.

#### Scenario: Global exception without monitor class import
- **WHEN** app module is compiled after SPI introduction
- **THEN** `GlobalExceptionHandler` has no import of monitor internal reporter classes

### Requirement: File classify Vo deduplication
The system MUST NOT maintain duplicate `FileClassifyVo` in common and system. File classification APIs MUST use `system.internal.vo.SysFileClassifyVo` (or moved api DTO) exclusively.

#### Scenario: Single file classify Vo type
- **WHEN** file classify list API responds
- **THEN** JSON schema maps to one Vo type defined in system module
