# 文件管理

## 概述

对经 `FileTemplate` 上传的文件做 **自动登记**（`sys_file`），管理端提供列表、上传、预览、下载与删除。导入/导出编排产生的 Excel 亦通过 `sys_file` 关联（`import-source`、`import-error`、`export-result` 等 classify）。

| 项 | 值 |
|----|-----|
| Controller | `SysFileController` |
| 路径前缀 | `/system/file` |
| 前端 | `quick-ui/src/views/system/file/index.vue` |
| 菜单 | 系统监控 → 文件管理（`menu_id=2258`，`perms=system:file:list`） |
| 迁移 | `V50__sys_file_management.sql` |
| 设计稿 | 仓库 `docs/superpowers/specs/2026-06-02-file-management-design.md` |

## 数据模型（`sys_file`）

核心字段：`fileId`、`originalName`、`ext`、`sizeBytes`、`contentType`、`classify`、`relativePath`（唯一）、`uploaderUserId`、`uploaderUserName`、`uploadTime`、逻辑删除审计字段。

上传成功后由 `FileUploadHook` 写入登记，业务模块无需逐个改造。

## 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | `system:file:list` | 分页列表 |
| POST | `/upload/{classify}` | `system:file:upload` | multipart 上传，**classify 必填** |
| GET | `/view/{*relativePath}` | `system:file:view` | inline 预览流 |
| GET | `/download/{fileId}` | `system:file:download` | 附件下载 |
| POST | `/remove` | `system:file:remove` | 批量删除（逻辑删 + 删存储对象） |

预览路径在 `qc.security.web.anonymous-paths` 中可配置匿名例外；签名校验对 `/system/file/view/**` 有排除（见 `application.yml` `client-sign.exclude-paths`）。

## 文件分类（`qc.file.classifies`）

`application.yml` 中为每个 `classify` 配置大小、数量、扩展名、是否允许匿名上传。与导入导出相关的典型项：

| classify | 用途 |
|----------|------|
| `import-source` | 导入原始 Excel |
| `import-error` | 导入失败明细 xlsx |
| `export-result` | 异步导出结果 xlsx |

分类名 **不可含斜杠**（受 `FilePathSupport` 约束）。

## 与业务模块

- 用户头像等：业务 Service 调用 `SysFileService`，列表以 `fileId` 为主键操作。
- 通用上传能力仍见 [文件上传模块使用文档](../components/通用组件/文件上传模块使用文档)（`FileTemplate` API）。

## 相关文档

- [导入导出中心](./import-export-center)
- [文件上传模块设计](../../design/后端通用组件设计/文件上传模块设计)
