## Why

当前仓库缺少可用的文件存储与管理能力：`quick-ui` 已有部分 `C7Upload`/`C7Preview` 与 API 封装，但后端无文件模块，分类不可配置，管理端也无法统一管理已上传文件。需要从 `bak` 迁移本地上传能力，并落地可配置的文件分类与文件管理。

## What Changes

- 在 `quickboot-common` 迁入本地 `FileTemplate` 存储（本期不做 MinIO）。
- 新增表 `sys_file_classify`：管理端可配置后缀、大小、数量、压缩开关（仅落库不压缩）、匿名、启停。
- 新增表 `sys_file`：仅「文件管理」页上传时显式登记；业务通用上传不进列表。
- 新增 API：`/file/**`（通用上传/分类查询/预览）、`/system/fileClassify/**`、`/system/file/**`。
- `quick-ui`：校准 `C7Upload`/`C7Preview`；新增文件分类管理页与文件管理页；菜单与权限种子。
- 分类规则改由 DB 提供；`qc.file.*` 仅保留本地存储基础配置（不再用 YAML 维护 classifies）。

## Capabilities

### New Capabilities

- `file-storage`: 本地文件存储、按分类校验上传、通用上传/预览接口。
- `file-classify`: 文件分类 CRUD 与启用规则查询（供上传组件与校验）。
- `file-management`: 管理端文件列表、登记上传、预览、下载、删除。
- `quick-ui-file`: 管理端分类/文件页面与 `C7Upload`/`C7Preview` 对齐。

### Modified Capabilities

- （无；仓库主 specs 中尚无对应能力。）

## Impact

- 后端：`quickboot-common`（存储）、`quickboot-module-system`（分类/文件）、`quickboot-app`（Flyway 表/菜单/权限、`qc.file` 配置）。
- 前端：`quick-ui` packages、views、api、动态菜单权限。
- 参考：`docs/superpowers/specs/2026-08-12-file-management-design.md`、`bak` 既有实现。
- 非目标：MinIO、真实压缩、分片续传、业务上传自动登记。
