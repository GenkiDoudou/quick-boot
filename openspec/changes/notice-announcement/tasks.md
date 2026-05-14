## 1. 数据库与字典种子

- [x] 1.1 新增 Flyway 脚本：创建 `sys_notice` 表（字段与 `design.md` / 超级能力设计文档一致，主键 `notice_id`，`notice_content` 使用 LONGTEXT）。
- [x] 1.2 在同一或后续 Flyway 脚本中插入 `sys_notice_type`、`sys_notice_status` 字典类型及字典项，主键使用约定数值段（如自 `800001` 起）并写 SQL 注释，避免与现有数据冲突。

## 2. 后端模块

- [x] 2.1 在 `quickboot-web` 下新增 `system.notice` 包：实体、Mapper、`SysNoticeQueryBo`/`SysNoticeBo`/`SysNoticeVo`、Service 接口与实现。
- [x] 2.2 实现 `GET /system/notice/list`：分页参数、`PageInfo` 出参、按 `noticeTitle`/`noticeType`/`createBy` 条件查询。
- [x] 2.3 实现 `GET /system/notice/{noticeId}` 详情（含 `noticeContent`），不存在时返回约定业务异常与中文提示。
- [x] 2.4 实现 `POST /system/notice/create` 与 `POST /system/notice/update`：Jakarta Validation（Add/Update 分组）、审计字段写入、更新时校验 ID 存在。
- [x] 2.5 实现 `POST /system/notice/remove`：批量物理删除，拒绝空列表与非法输入。
- [x] 2.6 在 Service 中集成 HTML 白名单消毒（OWASP Sanitizer 或项目等价方案），并实现消毒后长度 ≤ 65535 校验及「消毒后为空」拒绝逻辑。
- [x] 2.7 Controller 补齐 `@Tag`、`@Operation`、`@Parameter`；业务失败统一使用项目自定义异常，禁止 `IllegalArgumentException`。
- [x] 2.8 为通知接口配置 Spring Security 权限表达式，与 `system:notice:*` 标识对齐。

## 3. 前端模块

- [x] 3.1 新增 `quick-ui/src/api/system/notice.js`，封装列表、详情、创建、更新、删除接口路径与方法。
- [x] 3.2 新增系统管理「通知公告」页面：使用 `C7JsonTable`，`listFunction` 直连列表接口，`rows-key`/`total-key` 与 `PageInfo` 嵌套于 `R.data` 的路径一致。
- [x] 3.3 实现查询项（标题、类型、创建人）与表格列（编号、标题、类型、状态、创建人、创建时间）；类型/状态使用字典 `sys_notice_type`、`sys_notice_status`。
- [x] 3.4 实现新增/编辑弹窗：标题与类型必填校验；富文本编辑；提交成功后关闭弹窗并 `refreshData()`。
- [x] 3.5 实现行内删除与批量删除，调用 `remove` 接口并二次确认；接入 `v-hasPermi` 与路由/菜单。
- [x] 3.6 实现前阅读并遵守仓库根目录 `DESIGN.md` 与 `sdd/前端代码规范.md`，默认导出组件补充 JSDoc。

## 4. 验证与文档

- [x] 4.1 后端：补充或执行 Service/Controller 层关键用例（消毒、超长、不存在 ID、空删除列表）。
- [x] 4.2 前端：执行 `pnpm build:prod`（或项目约定构建命令）确保无编译错误。
- [x] 4.3 前后端联调：分页、筛选、增删改、字典展示、权限拒绝场景。
- [x] 4.4 若需与原始需求文档对齐，更新 `原始需求/系统管理/通知公告-需求文档.md` 中 HTTP 方法说明为 POST 子路径，并引用 OpenSpec 变更目录。
