## ADDED Requirements

### Requirement: Wide-cut operlog capture on RestControllers
The system SHALL capture operation logs for public methods of classes annotated with `@RestController` when `qc.monitor.operlog.capture-enabled` is true. Capture MUST publish an event with request URI, HTTP method, client IP, login user id when present, duration, optional serialized params/result (truncated), and throwable if any. URI patterns in `ignore-url-patterns` MUST skip capture. `@IgnoreLogger(ALL)` on class or method MUST skip publishing. `@IgnoreLogger(PARAMS)` MUST omit request params. `@IgnoreLogger(RESULT)` MUST omit response body. Capture failures MUST NOT break the business request.

#### Scenario: RestController method is logged
- **WHEN** an authenticated user invokes a RestController endpoint that is not ignored
- **THEN** an operation log event is published and eventually persisted to `sys_oper_log`

#### Scenario: Ignore RESULT on export
- **WHEN** an export endpoint annotated with `@IgnoreLogger(RESULT)` succeeds
- **THEN** the persisted log omits `json_result` content for the file/response body

#### Scenario: Operlog controller not self-logged
- **WHEN** a user queries `/monitor/operlog/**`
- **THEN** no new operlog row is created solely from that query (class or URI ignore applies)

### Requirement: Operlog title from OpenAPI or OperLogMeta
The system SHALL resolve operlog title with priority: `@OperLogMeta.title` → class `@Tag` name plus method `@Operation.summary` → `ClassName.methodName`. Business type MUST use `@OperLogMeta.businessType` when set; otherwise infer from HTTP method and path/method name (export/import/add/update/remove/clean and related keywords).

#### Scenario: Title from Operation summary
- **WHEN** a controller method has `@Tag` and `@Operation(summary=...)` and no `@OperLogMeta`
- **THEN** the persisted `title` combines tag name and summary

### Requirement: Operlog management APIs
The system SHALL expose management under `/monitor/operlog`: page (`POST /monitor/operlog/page`), detail (`GET /monitor/operlog/{operId}`), batch remove (`POST /monitor/operlog/remove`), clean all (`POST /monitor/operlog/clean`), and sync export (`POST /monitor/operlog/export`). There MUST be no create/update APIs for oper logs. Page filters MUST support URI, title, operator name, business type, status, trace id, client operation id, client id, and time range as applicable.

#### Scenario: Page oper logs
- **WHEN** an authorized user posts page with filters
- **THEN** matching oper log rows are returned with total count

#### Scenario: Clean all oper logs
- **WHEN** an authorized user confirms clean
- **THEN** all oper log rows are removed
