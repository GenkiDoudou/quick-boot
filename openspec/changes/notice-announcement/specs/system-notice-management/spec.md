## ADDED Requirements

### Requirement: 通知公告分页列表与筛选

系统 SHALL 提供通知公告分页查询能力，支持按公告标题（模糊）、公告类型（精确）、创建人（模糊，对应字段 `createBy`）筛选；列表默认按创建时间降序。成功响应 MUST 使用统一包装 `R`，且业务载荷为 `PageInfo<SysNoticeVo>`，包含 `records` 与 `total` 等分页字段。

#### Scenario: 按标题筛选并分页

- **WHEN** 客户端使用合法分页参数调用 `GET /system/notice/list` 且传入 `noticeTitle` 模糊条件
- **THEN** 系统返回 `R`，其 `data.records` 仅包含标题匹配的公告行，`data.total` 为总匹配条数

#### Scenario: 按创建人筛选

- **WHEN** 客户端调用 `GET /system/notice/list` 且传入 `createBy` 模糊条件
- **THEN** 系统返回的 `data.records` 中每条记录的创建人字段与筛选语义一致

### Requirement: 通知公告详情查询

系统 SHALL 提供按主键查询通知公告详情的能力，响应 MUST 包含 `noticeContent` 供编辑回显。

#### Scenario: 查询存在的公告

- **WHEN** 客户端调用 `GET /system/notice/{noticeId}` 且该 ID 对应记录存在
- **THEN** 系统返回 `R`，其 `data` 包含该公告全部可编辑字段含 `noticeContent`

#### Scenario: 查询不存在的公告

- **WHEN** 客户端调用 `GET /system/notice/{noticeId}` 且该 ID 不存在
- **THEN** 系统返回可识别的业务失败结果，并附带明确中文说明（不得使用 `IllegalArgumentException` 作为业务信号）

### Requirement: 通知公告新增

系统 SHALL 提供新增通知公告能力，接口为 `POST /system/notice/create`。请求体 MUST 接入 Jakarta Validation：`noticeTitle`、`noticeType` 为必填；`status` 若缺省则采用约定默认值（如 `0` 正常）。

#### Scenario: 新增成功

- **WHEN** 客户端提交满足校验且正文经消毒后合法的新增请求
- **THEN** 系统持久化新记录并返回成功响应，`create_by`/`create_time` 由当前用户上下文写入

### Requirement: 通知公告更新

系统 SHALL 提供更新通知公告能力，接口为 `POST /system/notice/update`。请求体 MUST 包含 `noticeId` 且接入 Update 分组校验；同一套富文本消毒与长度规则 MUST 适用于更新。

#### Scenario: 更新已存在记录

- **WHEN** 客户端提交合法更新请求且 `noticeId` 存在
- **THEN** 系统更新对应字段并返回成功响应，`update_by`/`update_time` 由当前用户上下文写入

#### Scenario: 更新不存在记录

- **WHEN** 客户端提交的 `noticeId` 不存在
- **THEN** 系统拒绝更新并返回明确中文业务错误

### Requirement: 通知公告删除

系统 SHALL 提供物理删除能力，接口为 `POST /system/notice/remove`，请求体为公告主键 ID 列表，支持批量。系统 MUST 拒绝空列表或非法 ID 集合（具体规则以实现阶段校验为准，须返回可读错误）。

#### Scenario: 批量删除多条

- **WHEN** 客户端提交包含多个合法 `noticeId` 的删除请求
- **THEN** 系统删除对应行并返回成功响应

#### Scenario: 删除后不可再查

- **WHEN** 删除成功后客户端再次请求同一 `noticeId` 详情
- **THEN** 系统表现为记录不存在（与「不存在」场景一致的业务结果）

### Requirement: 富文本 HTML 消毒与长度限制

系统 MUST 在持久化前对 `noticeContent` 执行白名单 HTML 消毒；消毒后内容 MUST 满足 UTF-8 逻辑下字符长度不超过 65535（与实现统一采用 `String` 字符计数）。若原始内容非空但消毒后为空（例如全部为非法标签），系统 MUST 拒绝保存并返回明确中文提示。

#### Scenario: 剔除危险标签

- **WHEN** 客户端提交包含可执行脚本或典型事件属性的 HTML
- **THEN** 持久化内容中不包含可执行脚本或未允许的危险构造

#### Scenario: 超长内容被拒绝

- **WHEN** 客户端提交消毒后长度超过 65535 字符的内容
- **THEN** 系统拒绝保存并返回明确中文提示

### Requirement: 字典类型与展示

系统 MUST 在初始化数据中提供字典类型 `sys_notice_type` 与 `sys_notice_status` 及其至少各两项字典数据（类型示例：1 通知、2 公告；状态示例：0 正常、1 关闭）。管理端列表与表单 MUST 能正确展示类型与状态的标签文本。

#### Scenario: 列表展示字典标签

- **WHEN** 用户打开通知公告列表页
- **THEN** 类型列与状态列展示为字典中文标签（或项目统一的字典渲染结果），而非裸编码

### Requirement: 对外接口文档与分层

Controller MUST 使用 `@Tag`、`@Operation`，关键参数使用 `@Parameter`。公开 Java 类型及 public 成员 MUST 具备 JavaDoc（简体中文为主，说明边界条件）。

#### Scenario: OpenAPI 可读性

- **WHEN** 开发者打开 Swagger/OpenAPI 文档中的通知公告接口分组
- **THEN** 能看到分组名称、各接口摘要及主要参数说明

### Requirement: 管理端权限控制

系统 MUST 对通知公告相关接口与页面操作施加权限控制，权限标识与需求一致：`system:notice:list`、`system:notice:add`、`system:notice:edit`、`system:notice:remove`（若项目统一以 `query` 替代 `list`，实现 MUST 在权限模型与文档中保持一致）。

#### Scenario: 无权限访问写接口

- **WHEN** 已认证用户缺少 `system:notice:add` 权限调用新增接口
- **THEN** 系统拒绝该请求并返回权限不足结果

### Requirement: 管理端页面交互与列表刷新

前端 SHALL 提供通知公告管理页面：查询区含标题、类型、创建人；表格列含公告编号（`noticeId`）、标题、类型、状态、创建人、创建时间；支持新增/编辑弹窗（富文本）、行内删除与批量删除。新增或编辑成功后 MUST 刷新列表数据以满足「列表及时更新」验收。

#### Scenario: 提交成功后刷新

- **WHEN** 用户在新增或编辑弹窗点击确定且提交成功
- **THEN** 弹窗关闭且列表重新加载展示最新数据
