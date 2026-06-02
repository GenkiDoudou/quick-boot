## Why

当前仓库已具备通用文件存储能力（`quickboot-common` 的 `FileTemplate`，支持本地/MinIO 上传与下载），但缺少面向管理端的统一「文件管理」能力，导致无法集中查看系统所有上传文件、无法统一提供预览/下载/删除入口，也缺少上传审计信息（上传人、时间等）。

## What Changes

- 新增「文件管理」能力：全系统上传文件自动登记，并在管理端提供上传、列表、预览、下载、删除功能。
- 新增文件元数据表（如 `sys_file`）：记录上传文件的原始名称、大小、扩展名、上传人、上传时间、相对路径等，并支持逻辑删除审计。
- 在 `FileTemplate` 上传流程中引入登记 Hook：上传成功后落库登记，确保“存储所有上传文件”。
- 新增后端文件管理 API：分页查询、上传、预览 URL 获取、下载、批量删除。
- 新增前端「系统管理 / 文件管理」页面：基于 `C7JsonTable` 展示与操作（上传/预览/下载/删除）。

## Capabilities

### New Capabilities

- `system-file-management`: 系统文件管理（上传文件元数据登记 + 管理端上传/预览/下载/删除 + 权限菜单）

### Modified Capabilities

- `common-file-storage`: 上传成功后增加“登记 Hook”的可观察行为（上传成功必须登记，登记失败视为上传失败的业务约束）

## Impact

- **后端**：
  - `quickboot-common`：新增/调整文件上传 Hook（依赖登录态读取上传人信息）。
  - `quickboot-system`/`quickboot-web`：新增文件管理模块（Controller/Service/Mapper/DTO/VO）与权限。
  - **数据库**：新增 `sys_file` 表与相关索引；新增菜单/权限种子数据（按仓库 Flyway 迁移规范）。
- **前端**：`quick-ui` 新增文件管理页面与对应 API 封装；使用弹窗预览图片/视频，其它文件新窗口打开。
- **配置/部署**：复用现有 `qc.file.*` 文件存储配置（本地或 MinIO）；不引入新的外部依赖。

