## ADDED Requirements

### Requirement: 导入场景文件分类

系统 SHALL 在 `qc.file.classifies` 中支持导入专用分类：

- **`import/source`**：用户上传的原始 Excel（`.xls`/`.xlsx`），`limitExt` MUST 包含 xls、xlsx，`limitSize` MUST 不小于业务导入上限（建议与 `spring.servlet.multipart.max-file-size` 对齐或文档说明）。
- **`import/error`**：导入失败明细 Excel，后缀限制与 `import/source` 一致。

通过 `FileTemplate.upload(file, "import/source")` 或 `FileAccessService.upload` 上传的文件 MUST 经 `FileUploadHook` 登记至 `sys_file`（与 `system-file-management` 一致）。

#### Scenario: 上传原始导入文件

- **WHEN** 编排器上传导入源文件且 classify 为 `import/source`
- **THEN** 存储 MUST 成功且 `sys_file` MUST 产生对应记录

#### Scenario: 上传失败明细文件

- **WHEN** 编排器上传失败明细 xlsx 且 classify 为 `import/error`
- **THEN** 存储 MUST 成功且返回的 `fileId` 可供任务表 `error_file_id` 引用
