## MODIFIED Requirements

### Requirement: FileUploadHook 与顺序

系统 SHALL 定义 `FileUploadHook` 接口，包含 `beforeUpload`、`afterUpload`、`onError`（方法签名与上下文对象以实现为准，但语义必须覆盖：上传前、成功后、失败回调）。系统 SHALL 支持多个 Bean 实现，并使用 Spring `@Order`（或等价排序）依次调用。`beforeUpload` 若拒绝上传，SHALL **抛出异常**（不设并行返回码路径）。`onError` SHALL 在上传失败且钩子链已执行到的范围内被调用。

系统 MUST 将 `afterUpload` 的异常视为上传失败：当任一钩子在 `afterUpload` 阶段抛出异常时，`upload(...)` MUST 以失败结束，并 MUST 确保存储端最终不存在该次上传产生的对象（不得遗留“上传失败但对象存在”的孤儿对象），随后 SHALL 调用 `onError` 回调（对已执行过的钩子链按实现约定范围调用，行为需与单测一致）。

#### Scenario: beforeUpload 否决
- **WHEN** 某钩子 `beforeUpload` 决定拒绝
- **THEN** 抛出异常且不产生存储对象

#### Scenario: afterUpload 失败应回滚对象
- **WHEN** 上传已成功写入存储且某钩子 `afterUpload` 抛出异常
- **THEN** `upload(...)` 返回失败，且存储端不存在该 `relativePath` 对应对象

