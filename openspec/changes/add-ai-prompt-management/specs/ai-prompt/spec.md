## ADDED Requirements

### Requirement: 提示词元数据（ai_prompt）

系统 MUST 在 MySQL 持久化提示词模板（表 `ai_prompt`），至少包含：`prompt_id`、`code`（唯一）、`name`、`description`、`prompt_type`（`LLM`/`RAG`/`CLASSIFIER`/`EXTRACTOR`/`CUSTOM`）、`domain`、`category`、`tags`（JSON 数组）、`status`（`DRAFT`/`PUBLISHED`/`ARCHIVED`）、`current_version_id`、`current_version_no`、`optimize_model_id`、标准审计字段与 `deleted`。

#### Scenario: 新增 LLM 类型草稿成功
- **WHEN** 具备 `ai:prompt:add` 的用户提交唯一 `code`、`prompt_type=LLM` 与合法 `name`
- **THEN** 数据库新增 `status=DRAFT`、`deleted=0` 记录且返回 `promptId`

#### Scenario: code 唯一约束
- **WHEN** 用户提交已存在的 `code`
- **THEN** 请求 MUST 失败并返回可识别业务错误

#### Scenario: ARCHIVED 不出现在 options
- **WHEN** 用户请求 `GET /ai/prompt/options`
- **THEN** 响应 MUST 仅包含 `status=PUBLISHED` 且未删除的记录

### Requirement: 多段内容与变量

系统 MUST 将各内容段存于 `ai_prompt_content`（`prompt_id`、`version_id`、`section_key`、`content`）；变量存于 `ai_prompt_variable`（`var_key`、`var_type`、`required`、`description`、`sort`）。草稿内容 `version_id` MUST 为 `0`。

各 `prompt_type` 的段与必填约束：

| prompt_type | section_key | 必填 |
|-------------|-------------|------|
| LLM | systemPrompt, userPrompt | userPrompt |
| RAG | systemPrompt, userPromptTemplate | systemPrompt |
| CLASSIFIER | systemPrompt, instruction, categoriesHint | instruction |
| EXTRACTOR | systemPrompt, instruction, outputSchemaHint | instruction |
| CUSTOM | 用户自定义段 | 至少一段非空 |

#### Scenario: 保存草稿写入 content 与 variable
- **WHEN** 用户对 `status=DRAFT` 的模板提交各段内容与变量列表
- **THEN** 系统 MUST 以 `version_id=0` 覆盖写入 `ai_prompt_content` 与 `ai_prompt_variable`

#### Scenario: 发布前必填段校验
- **WHEN** 用户对 `prompt_type=LLM` 的草稿发布但 `userPrompt` 为空
- **THEN** 发布 MUST 失败并提示必填段缺失

### Requirement: 变量占位符校验

保存草稿、发布、优化采纳前，系统 MUST 提取所有内容段中 `{{...}}` 占位符的根键名，且每个根键名 MUST 存在于该模板已声明的 `var_key` 集合中；否则 MUST 阻断并返回 `undeclaredVars` 列表。

#### Scenario: 未声明变量阻断发布
- **WHEN** 草稿 `userPrompt` 含 `{{question}}` 但变量表未声明 `question`
- **THEN** `POST /publish` MUST 失败且响应包含 `undeclaredVars` 含 `question`

#### Scenario: 已声明变量允许保存
- **WHEN** 变量表声明 `question` 且内容引用 `{{question}}`
- **THEN** `POST /update` MUST 成功

### Requirement: 草稿发布与状态机

- `DRAFT` MUST 允许 `update` 内容与变量。
- `PUBLISHED` 与 `ARCHIVED` MUST 禁止直接 `update` 内容；修改须先 `createDraft` 或 `optimize/adopt`。
- `POST /publish` MUST：校验通过 → 自 `version_id=0` 生成 `ai_prompt_version` 快照 → 设置 `status=PUBLISHED` 并更新 `current_version_id`、`current_version_no`（从 1 递增）。
- `POST /createDraft` MUST 从当前已发布快照复制到 `version_id=0` 并将 `status=DRAFT`。
- `POST /archive` MUST 将 `PUBLISHED` 转为 `ARCHIVED`。

#### Scenario: 发布生成版本 1
- **WHEN** 用户对合法草稿调用 `POST /publish`
- **THEN** `status=PUBLISHED`、`current_version_no=1` 且 `ai_prompt_version` 新增 `version_no=1` 记录

#### Scenario: 已发布不可直接更新
- **WHEN** 用户对 `status=PUBLISHED` 的模板调用 `POST /update`
- **THEN** 请求 MUST 失败并提示先创建草稿

#### Scenario: 从已发布创建草稿
- **WHEN** 用户对 `status=PUBLISHED` 的模板调用 `POST /createDraft`
- **THEN** `status=DRAFT` 且 `version_id=0` 内容与已发布快照一致

### Requirement: 提示词管理 API

系统 SHALL 提供以下接口（前缀 `/ai/prompt`），修改/删除使用 `@PostMapping`：

| 路径 | 方法 | 权限 |
|------|------|------|
| `/list` | GET | `ai:prompt:list` |
| `/getInfo` | GET | `ai:prompt:query` |
| `/add` | POST | `ai:prompt:add` |
| `/update` | POST | `ai:prompt:edit` |
| `/remove` | POST | `ai:prompt:remove` |
| `/publish` | POST | `ai:prompt:edit` |
| `/archive` | POST | `ai:prompt:edit` |
| `/createDraft` | POST | `ai:prompt:edit` |
| `/versions` | GET | `ai:prompt:query` |
| `/version/getInfo` | GET | `ai:prompt:query` |
| `/version/diff` | GET | `ai:prompt:query` |
| `/optimize` | POST | `ai:prompt:optimize` |
| `/optimize/adopt` | POST | `ai:prompt:optimize` |
| `/ab/run` | POST | `ai:prompt:optimize` |
| `/ab/score` | POST | `ai:prompt:optimize` |
| `/options` | GET | `ai:prompt:list` |

`/remove` MUST 逻辑删除。Controller MUST 带 `@Tag`、`@Operation`；Bo MUST 使用 Jakarta Validation。

#### Scenario: 分页列表按类型筛选
- **WHEN** 用户请求 `/list?promptType=LLM`
- **THEN** 仅返回 `prompt_type=LLM` 的未删除记录

#### Scenario: 详情含草稿内容与变量
- **WHEN** 用户请求 `/getInfo?promptId={id}`
- **THEN** 响应 MUST 包含 `version_id=0` 的 sections 与 variables（若存在草稿）

### Requirement: 版本历史与 Diff

`GET /versions` MUST 返回指定 `promptId` 的版本列表（`version_no`、`source`、`change_summary`、`create_time`）。`GET /version/getInfo` MUST 返回 `snapshot_json`。`GET /version/diff` MUST 接受 `leftVersionId`、`rightVersionId`（`0` 表示当前草稿），返回段级与变量级结构化 diff。

#### Scenario: 两版本 Diff
- **WHEN** 用户请求 `/version/diff?leftVersionId=0&rightVersionId={v1}`
- **THEN** 响应 MUST 包含各 `section_key` 的 before/after 差异

### Requirement: AI 提示词优化

`POST /optimize` MUST 同步调用 Chat 模型（超时 60000ms），入参含 `promptId`、`optimizeGoal`、可选 `modelId`。模型解析顺序：`modelId` → 模板 `optimize_model_id` → 全局 `WORKFLOW_CHAT` → `CHAT` 默认。系统 MUST 将结果写入 `ai_prompt_optimize_session` 并返回 `sessionId`、`resultSnapshot`、可选 `warnings`（未声明变量）。

优化结果 JSON 解析失败时 MUST 标记 `status=FAILED` 并保留原始文本。

`POST /optimize/adopt` MUST 将成功 session 的 `result_snapshot` 写入 `version_id=0`；若原 `status=PUBLISHED` MUST 等效 createDraft；MUST NOT 自动发布。

#### Scenario: 优化成功返回 session
- **WHEN** Chat 模型返回合法 JSON 且变量校验通过
- **THEN** 响应 `sessionId` 非空且 `resultSnapshot.sections` 包含优化后各段

#### Scenario: 采纳写入草稿不自动发布
- **WHEN** 用户对成功 session 调用 `POST /optimize/adopt`
- **THEN** `version_id=0` 内容更新且 `status` 为 `DRAFT`（若原为 PUBLISHED 则已转草稿）

#### Scenario: qc.ai.enabled 关闭时优化不可用
- **WHEN** `qc.ai.enabled=false` 且用户调用 `POST /optimize`
- **THEN** 请求 MUST 失败并提示启用 AI 与配置 Chat 模型

#### Scenario: 优化超时
- **WHEN** Chat 调用超过 60000ms
- **THEN** 请求 MUST 失败且不部分写入 content

### Requirement: A/B 对比与评分

`POST /ab/run` MUST 接受 `promptId`、`variantAVersionId`、`variantBVersionId`（`0` 表示当前草稿快照）、`sampleInputJson`、`modelId`；用样例值替换一层 `{{key}}` 后并行或顺序调用 Chat（总超时 60000ms）；结果 MUST 写入 `ai_prompt_ab_run`。

`POST /ab/score` MUST 接受 `runId`、`scoreA`、`scoreB`（1–5）、`winner`（`A`/`B`/`TIE`）、可选 `remark` 并更新记录。

#### Scenario: A/B 运行返回双输出
- **WHEN** 用户提供合法样例变量与两版本 ID
- **THEN** 响应 MUST 包含 `runId`、`outputA`、`outputB`

#### Scenario: 提交评分落库
- **WHEN** 用户对已有 run 提交 `scoreA=4`、`scoreB=5`、`winner=B`
- **THEN** `ai_prompt_ab_run` 对应记录 MUST 更新评分字段

### Requirement: 菜单与权限

系统 MUST 在「AI 能力」（menu_id 2320）下新增「提示词管理」（menu_id 2330，路由 `prompt`，组件 `ai/prompt/index`，权限 `ai:prompt:list`，order_num=3）。按钮权限 MUST 包含：`ai:prompt:query`、`ai:prompt:add`、`ai:prompt:edit`、`ai:prompt:remove`、`ai:prompt:optimize`（menu_id 2331–2335）。

#### Scenario: 有权限用户可见菜单
- **WHEN** 用户具备 `ai:prompt:list` 且登录
- **THEN** 侧栏「AI 能力」下 MUST 显示「提示词管理」

#### Scenario: 无 optimize 权限不可调用优化
- **WHEN** 用户无 `ai:prompt:optimize` 调用 `POST /optimize`
- **THEN** 请求 MUST 被权限拦截拒绝

### Requirement: 提示词管理前端

系统 MUST 提供：

- 列表页 `views/ai/prompt/index.vue`：C7JsonTable，搜索 name/code/promptType/domain/category/status，行操作含编辑、发布（DRAFT）、停用（PUBLISHED）、删除。
- 编辑页 `views/ai/prompt/edit.vue`（或等价路由 `/ai/prompt/edit/:promptId?`），含 Tab：**内容** | **优化** | **版本** | **A/B**。
- API 封装 `api/ai/prompt.js`，风格对齐 `api/ai/model.js`。

内容 Tab MUST 按 `promptType` 动态渲染段编辑器与变量表，并展示 `{{var}}` 校验提示。优化 Tab MUST 支持优化目标、模型选择、同步优化 loading、Diff 展示与采纳。版本 Tab MUST 支持版本列表、快照查看与 Diff。A/B Tab MUST 支持版本选择、样例变量、双栏输出与评分。

#### Scenario: 列表页新增跳转编辑
- **WHEN** 用户点击列表「新增」
- **THEN** MUST 打开编辑页且处于 DRAFT 新建态

#### Scenario: 优化 Tab 展示 loading
- **WHEN** 用户点击「AI 优化」且请求未返回
- **THEN** 优化 Tab MUST 显示加载态直至完成或超时
