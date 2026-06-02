## 1. 数据库与权限种子

- [x] 1.1 新增 `sys_file` 表（含唯一约束与索引）并按 Flyway 规范落迁移脚本
- [x] 1.2 新增「系统管理 / 文件管理」菜单与按钮权限（list/upload/view/download/remove）并写入迁移脚本

## 2. 后端：文件登记 Hook（全系统上传自动登记）

- [x] 2.1 新增文件元数据 Entity/Mapper（`sys_file`）与基础写入能力
- [x] 2.2 实现 `FileUploadHook.afterUpload`：上传成功后写入 `sys_file`（包含上传人、时间、relativePath 唯一）
- [x] 2.3 按 `common-file-storage` delta 要求处理异常：登记失败时上传失败且不遗留存储对象（补齐单测/集成测试）

## 3. 后端：文件管理 API

- [x] 3.1 新增文件管理分页查询接口（默认过滤 deleted），返回字段满足 3A
- [x] 3.2 新增文件管理上传接口（`MultipartFile file` + 可选 `classify`），返回 `fileId/relativePath`
- [x] 3.3 新增预览接口：`GET /system/file/view/{fileId}` 返回 `url`
- [x] 3.4 新增下载接口：`GET /system/file/download/{fileId}` 返回附件并设置 `Content-Disposition` 文件名
- [x] 3.5 新增批量删除接口：逻辑删 + 同步删除存储对象（对象不存在视为成功）

## 4. 前端：文件管理页面与交互

- [x] 4.1 新增 `quick-ui` 文件管理 API 封装（list/upload/view/download/remove）
- [x] 4.2 新增页面 `views/system/file/index.vue`：基于 `C7JsonTable` 实现列表与操作列（预览/下载/删除）
- [x] 4.3 实现上传弹窗与上传后刷新列表
- [x] 4.4 实现预览交互 A：图片/视频弹窗预览，其他文件新窗口打开（关闭弹窗后视频停止）
- [x] 4.5 集成权限点：菜单与按钮的 `v-hasPermi` / permi 配置与后端对齐

## 5. 验证与回归

- [ ] 5.1 验证登记覆盖：业务侧调用 `FileTemplate.upload` 后 `sys_file` 必有记录且 `relativePath` 唯一
- [ ] 5.2 验证上传/预览/下载/删除全链路（本地存储与 MinIO 任选其一做最小验证）

