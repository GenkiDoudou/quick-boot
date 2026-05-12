## MODIFIED Requirements

### Requirement: 统一响应体 R
系统 SHALL 在 `quickboot-common` 提供泛型响应类型 `R<T>`，序列化为 JSON 时包含字段：`code`、`msg`、`data`、`traceId`、`timestamp`。系统 SHALL 提供工厂方法：`ok()`、`ok(String msg)`、`ok(T data)`、`ok(String msg, T data)`，以及 `error()`、`error(String msg)`、`error(int code, String msg)`、`error(int code, String msg, T data)`。系统 SHALL 提供 `boolean isSuccess()`（当且仅当 `code == 200`）与 `boolean isError()`（`code != 200`）。

在导入类接口场景中，系统 SHALL 允许 `data` 承载结构化导入结果对象，并统一支持以下字段语义：`total`、`successCount`、`failCount`、`failRows`，以及失败明细文件信息（如 `errorFileName`、`errorFileBase64` 或与其等价的可下载载体）。

#### Scenario: 导入接口返回结构化结果
- **WHEN** 导入接口执行完成且通过 `R.ok(importResult)` 返回
- **THEN** `data` 中包含 `total`、`successCount`、`failCount`、`failRows` 等导入统计字段

#### Scenario: 导入存在失败明细文件
- **WHEN** 导入过程中存在失败记录且系统生成失败明细
- **THEN** `data` 中包含失败明细文件信息字段，供前端下载或展示

#### Scenario: 非导入接口保持兼容
- **WHEN** 非导入类接口继续使用既有 `R<T>` 返回模式
- **THEN** 既有 `R` 语义与判定逻辑保持不变
