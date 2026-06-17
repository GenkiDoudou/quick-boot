## ADDED Requirements

### Requirement: AI 应用列表页

系统 MUST 提供 `/ai/app/list` 管理页，使用 C7JsonTable 模板，支持：

- 分页、按名称搜索、按 `app_type` 筛选
- 创建空白应用（选择智能体 / 高级编排）
- 行操作：编辑、演示、发布、删除

列表 MUST 展示名称、类型、状态、更新时间。

#### Scenario: 创建智能体

- **WHEN** 用户点击创建并选择「智能体」
- **THEN** 跳转 `/ai/app/agent/:id` 编排页

### Requirement: 智能体三栏编排页

`/ai/app/agent/:id` MUST 采用三栏布局：

| 栏 | 内容 |
|----|------|
| 左 | 人设与回复逻辑（`systemPrompt`）；提供提示词优化入口 |
| 中 | 技能：模型、知识库多选、关联流程表、变量表、开场白、预设问题、快捷指令 |
| 右 | 预览调试（SSE 聊天） |

关联流程表 MUST 支持配置 `workflowId`、`toolName`、`description`。

变量表 MUST 支持 `key`、`description`、`defaultValue`。

保存 MUST 调用 `/ai/app/update`；预览 MUST 使用当前草稿配置。

#### Scenario: 关联流程配置

- **WHEN** 用户添加流程 binding 并保存
- **THEN** 刷新后 binding 回显正确

### Requirement: 高级编排配置页

`/ai/app/workflow/:id` MUST 提供：

- 工作流选择器（已存在工作流列表）
- 开场白、预设问题
- 右侧预览调试（触发工作流运行）

MUST NOT 展示智能体专属技能（变量、知识库 Tool、多流程 binding）。

#### Scenario: 选择工作流

- **WHEN** 用户选择工作流并保存
- **THEN** `config_json.workflowId` 持久化

### Requirement: 演示聊天页

`/ai/app/chat/:appId` MUST 提供：

- 左侧会话列表（`multiSession=true` 时）
- 右侧消息区 + 输入框
- 开场白与预设问题/快捷指令展示
- 千问模型时显示「联网搜索」开关

MUST 支持 SSE 流式渲染；tool 调用时展示简要状态；知识库回答展示 citations（若有）。

#### Scenario: 多会话切换

- **WHEN** 用户切换会话
- **THEN** 消息区加载对应 session 历史

### Requirement: 发布弹窗

应用列表或编排页 MUST 提供发布入口，包含：

- 发布记录备注
- 嵌入：`embed_token` 展示、域名白名单编辑、iframe/script 代码片段复制
- 菜单：`menu_path`、组件路径（可选）

未发布应用 MUST 禁用嵌入代码复制或标注「需先发布」。

#### Scenario: 复制嵌入代码

- **WHEN** 应用已发布且用户打开发布弹窗
- **THEN** 可复制含 `embed_token` 的 script/iframe 示例

### Requirement: 嵌入页

`/ai/embed/:token` MUST 为独立布局（无管理端侧栏），包含：

- 聊天 UI（同演示页核心）
- 从 URL token 解析应用；localStorage 维护访客 `user_key`

#### Scenario: 嵌入页对话

- **WHEN** 合法 token 下用户发送消息
- **THEN** SSE 流式返回且会话持久化

### Requirement: 前端 API 封装

`quick-ui/src/api/ai/app/` MUST 封装列表、详情、保存、发布、会话、消息、SSE 聊天、嵌入配置接口；JSDoc 说明路径与主要字段。

#### Scenario: 构建通过

- **WHEN** 执行 `pnpm build:prod`
- **THEN** 无编译错误
