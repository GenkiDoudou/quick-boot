# AI 智能体应用 — 设计说明

**日期**：2026-06-15  
**状态**：已定稿（brainstorming 澄清结论）  
**依据**：`原始需求/智能体.md`（扣子低代码智能体 + 系统 AI 应用）  
**澄清结论**：1:B、2:B、3:A、4:B、5:C、6:C、7:A  
**关联**：`docs/superpowers/specs/2026-06-07-ai-workflow-design.md`、`add-ai-model-management`、`add-knowledge-rag`

---

## 1. 背景与目标

### 1.1 背景

项目已具备 **工作流引擎**（DAG 编排、Debug/Trace）、**知识库 RAG**、**AI 模型管理**，但缺少面向终端用户的 **AI 应用 / 智能体** 能力：应用列表、编排页、多轮对话、发布与嵌入。

需求文档包含两层诉求：

1. **扣子式低代码智能体**：人设提示词 + 技能（知识库、流程/插件）+ 预览调试 + 发布。
2. **系统 AI 应用**：原「简单配置」升级为智能体；保留「高级编排」；支持演示、嵌入、菜单挂载、多会话、联网搜索等。

### 1.2 目标（P0）

| 能力 | 说明 |
|------|------|
| 应用管理 | 列表 CRUD、草稿/发布、两种类型同期交付 |
| **智能体模式** | 三栏编排：人设提示词 / 技能 / 预览调试 |
| **关联工作流** | LLM Function Calling，模型自主选流程执行 |
| **知识库** | 注册为 Tool，对话中检索引用 |
| **变量记忆** | 应用级 key-value，注入 system prompt，会话内持久 |
| **高级编排** | 绑定单个已发布工作流，消息触发 DAG 运行 |
| **多会话** | 会话列表、用户隔离、历史轮数截断 |
| **联网搜索** | 千问模型聊天页可选开启 |
| **发布** | 管理端演示 + iframe/script 嵌入 + 系统菜单挂载 |

### 1.3 非目标（P0 不做）

- 长期记忆库（跨应用持久记忆）
- AI 绘画
- 飞书 / 微信 / 抖音等第三方渠道
- 扣子插件市场、背景图、AI 一键创建智能体
- 智能体商店
- 替换 `/knowledge/chat` 固定 RAG 链路

---

## 2. 已定稿产品决策

| 题号 | 选项 | 结论 |
|------|------|------|
| 1 | B | 智能体 + 关联工作流（AI 自主选流程） |
| 2 | B | 智能体 + 高级编排同期，共用应用列表与聊天壳 |
| 3 | A | LLM Function Calling / Tool Use |
| 4 | B | 仅会话内变量记忆（非长期记忆库） |
| 5 | C | 演示 + iframe/script 嵌入 + 系统菜单 |
| 6 | C | 多会话 + 千问联网搜索 |
| 7 | A | 扣子三栏编排 UI |

---

## 3. 方案对比与选型

| 方案 | 概要 | 优点 | 缺点 |
|------|------|------|------|
| **A（采用）** | 新建 `quickboot-ai-app` 模块 | 边界清晰；可复用 RAG 的 ToolCallback 模式 | 新模块 + 新表 |
| B | 在工作流模块内加应用壳 | 少模块 | 智能体与 DAG 语义混杂；三栏 UI 难映射 |
| C | 纯前端配置 + 薄 API | 上线快 | 无版本/发布/会话隔离 |

**采用方案 A。**

---

## 4. 总体架构

```text
┌─────────────────────────────────────────────────────────┐
│  quick-ui：应用列表 / 智能体三栏编排 / 高级编排 / 聊天 / 嵌入页   │
└───────────────────────────┬─────────────────────────────┘
                            │ REST + SSE
┌───────────────────────────▼─────────────────────────────┐
│  quickboot-ai-app（新）                                   │
│  · AiAppDefinitionService   应用 CRUD / 发布              │
│  · AiAppChatService         多轮对话 + Tool 循环          │
│  · AiAppSessionService      多会话 / 历史 / 变量记忆       │
│  · AiAppPublishService      嵌入 token / 菜单元数据        │
└─────┬──────────────────────┬────────────────────────────┘
      │                      │
      ▼                      ▼
 quickboot-workflow      quickboot-knowledge + quickboot-ai
 (WorkflowEngine)        (检索 Tool / ChatModel / 千问联网)
```

**依赖方向**：`ai-app` → `workflow`、`knowledge`、`ai`（单向）；不修改 knowledge RAG 对外契约。

**开关**：`qc.ai-app.enabled`（默认 `false`），关闭时不注册 Bean 与路由。

---

## 5. 数据模型

### 5.1 表 `ai_app`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | |
| `name` | VARCHAR | 应用名称 |
| `description` | VARCHAR | 功能介绍 |
| `icon` | VARCHAR | 图标 URL 或内置 key |
| `app_type` | VARCHAR | `agent` \| `workflow` |
| `status` | VARCHAR | `draft` \| `published` |
| `config_json` | JSON | 草稿配置 |
| `published_config_json` | JSON | 发布快照 |
| `del_flag` | TINYINT | 逻辑删除 |
| 审计字段 | | `create_by`, `create_time`, `update_by`, `update_time` |

### 5.2 表 `ai_app_session`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | |
| `app_id` | BIGINT | 关联应用 |
| `user_key` | VARCHAR | 登录 userId 或 embed 访客标识 |
| `title` | VARCHAR | 会话标题（首条消息摘要） |
| `variables_json` | JSON | 智能体变量记忆快照 |
| `create_time`, `update_time` | DATETIME | |

### 5.3 表 `ai_app_message`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | |
| `session_id` | BIGINT | |
| `role` | VARCHAR | `user` \| `assistant` \| `tool` |
| `content` | TEXT | |
| `metadata_json` | JSON | 工具调用、工作流 runId、引用等 |
| `create_time` | DATETIME | |

### 5.4 表 `ai_app_publish`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT PK | |
| `app_id` | BIGINT | |
| `embed_token` | VARCHAR | 嵌入访问令牌（唯一） |
| `allowed_origins` | VARCHAR | 域名白名单，逗号分隔 |
| `menu_path` | VARCHAR | 可选，系统菜单路由 |
| `menu_component` | VARCHAR | 可选，前端组件路径 |
| `enabled` | TINYINT | 是否启用嵌入 |
| 审计字段 | | |

### 5.5 智能体 `config_json`（`app_type=agent`）

```json
{
  "chatModelId": 1,
  "systemPrompt": "…",
  "openingMessage": "你好，我是夸夸机器人",
  "suggestedQuestions": ["夸夸我"],
  "quickCommands": [{ "label": "鼓励", "prompt": "给我鼓励" }],
  "kbIds": [101],
  "workflowBindings": [
    {
      "workflowId": 12,
      "toolName": "deep_research",
      "description": "深度研究分析流程"
    }
  ],
  "memoryVariables": [
    { "key": "user_name", "description": "用户姓名", "defaultValue": "" }
  ],
  "historyTurns": 10,
  "multiSession": true
}
```

### 5.6 高级编排 `config_json`（`app_type=workflow`）

```json
{
  "workflowId": 12,
  "openingMessage": "…",
  "suggestedQuestions": [],
  "multiSession": true
}
```

---

## 6. 智能体运行时

### 6.1 每轮消息流程

1. 校验应用已发布（演示/嵌入）或草稿（编排页预览）。
2. 加载 `published_config`（或草稿）、`variables_json`、最近 `historyTurns` 轮消息。
3. 组装 **system prompt**：
   - 用户 `systemPrompt`
   - 自动追加变量块：`{{key}}` 及用途（来自 `memoryVariables`）
   - 工具使用说明（知识库、关联流程）
4. 注册 **ToolCallback**：
   - `search_knowledge`：按 `kbIds` 调 `KnowledgeSearchService`
   - 每个 `workflowBindings` 一项：`toolName` + `description`（含工作流名称）
5. 若模型为千问且请求带 `webSearch=true` → 注入 DashScope 联网参数。
6. `ChatClient` **tool 循环**（上限 5 次）：
   - **workflow tool**：内部调用 `WorkflowEngine`（已发布版本），入参 `{ "query": "<string>" }` 映射到 `start` 节点第一个 input；取 `answer`/`end` 节点输出作为 tool result
   - **knowledge tool**：返回 topK 片段拼接文本
7. **变量抽取**：assistant 回复后，用轻量结构化 prompt 或规则从对话更新 `variables_json`（仅已声明 key）。
8. 持久化消息；SSE 流式返回（`delta` / `done` / `tool_call` 事件）。

### 6.2 流程 Tool 约定

| 项 | 规则 |
|----|------|
| `toolName` | 英文标识，应用内唯一 |
| `description` | 供模型语义匹配，建议含流程名称与场景 |
| 入参 schema | `{ "query": "string", "description": "用户问题或任务描述" }` |
| 失败 | tool result 返回错误摘要，由模型组织用户可见回复 |

### 6.3 知识库 Tool

- 名称固定：`search_knowledge`
- 入参：`{ "query": "string" }`
- 出参：检索片段文本 + citations 写入 `metadata_json` 供前端展示

---

## 7. 高级编排运行时

1. 用户消息 → `WfRunDebugBo` 等价内部调用：`workflowId` 来自配置，`usePublished=true`
2. `inputs`：`{ "query": userMessage }`（与 start 节点 inputs 对齐，缺省时引擎按 key 匹配）
3. 优先 **SSE**（复用 `WfStreamController` 事件模型）；超时回退同步
4. 会话记录 user/assistant 消息；metadata 含 `runId`、步骤摘要
5. 无 Tool Calling、无变量记忆（workflow 类型不启用 `memoryVariables`）

---

## 8. 前端页面

| 路由 | 说明 |
|------|------|
| `/ai/app/list` | C7JsonTable：创建、类型筛选、演示、发布 |
| `/ai/app/agent/:id` | **三栏编排**：左人设提示词 / 中技能 / 右预览 |
| `/ai/app/workflow/:id` | 高级编排：工作流选择 + 开场白 + 预览 |
| `/ai/app/chat/:appId` | 演示聊天：左会话列表 + 右对话；千问联网开关 |
| `/ai/embed/:token` | 匿名嵌入（无侧栏菜单） |

### 8.1 智能体三栏

| 区域 | 内容 |
|------|------|
| 左 | 人设与回复逻辑（systemPrompt）；接入提示词优化器入口 |
| 中 | 模型、知识库多选、关联流程表、变量表、开场白、预设问题、快捷指令 |
| 右 | 预览调试（SSE），展示 tool 调用与引用 |

### 8.2 发布弹窗

- 嵌入：`embed_token`、域名白名单、iframe/script 代码片段
- 菜单：可选写入 `menu_path` + 组件路径，由管理员确认挂载

---

## 9. API 概要

前缀 `/ai/app/**`；权限 `aiapp:list|query|edit|publish|chat`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 分页列表 |
| GET | `/getInfo` | 详情（含 config） |
| POST | `/add` | 创建 |
| POST | `/update` | 更新草稿 |
| POST | `/publish` | 发布（快照 `published_config_json`） |
| POST | `/chat/stream` | SSE 对话（需登录或 embed token） |
| GET | `/session/list` | 会话列表 |
| POST | `/session/add` | 新建会话 |
| GET | `/message/list` | 会话消息 |
| GET | `/publish/getEmbedInfo` | 嵌入配置（管理端） |
| POST | `/publish/saveEmbed` | 保存嵌入/菜单配置 |

**嵌入公开 API**：`/ai/embed/{token}/chat/stream`（校验 `allowed_origins` + token），会话 `user_key` 为访客 UUID（localStorage）。

---

## 10. 权限与安全

- 管理端：`Sa-Token` + `aiapp:*` 权限码
- 嵌入：`embed_token` 随机 32+ 字符；仅 **已发布** 应用可嵌入
- 工作流 Tool 调用：继承当前用户身份或 embed 访客的运行配额（复用 `WorkflowRunLimiter`）
- Trace/日志：消息 metadata 脱敏（不记录完整 API Key）

---

## 11. 测试用例

| ID | 场景 | 期望 |
|----|------|------|
| TC_AI_APP_001 | 创建智能体草稿 | 保存成功，status=draft |
| TC_AI_APP_002 | 三栏预览对话 | SSE 返回 assistant 文本 |
| TC_AI_APP_003 | 绑定知识库 | 触发 search_knowledge，回答含引用 |
| TC_AI_APP_004 | 绑定工作流 | 模型选择 workflow tool，返回流程输出 |
| TC_AI_APP_005 | 变量记忆 | 对话后 variables_json 更新，下轮 prompt 含值 |
| TC_AI_APP_006 | 高级编排应用 | 消息触发工作流，返回 end/answer 输出 |
| TC_AI_APP_007 | 多会话 | 新建会话互不影响历史 |
| TC_AI_APP_008 | 千问联网 | 勾选后请求带联网参数 |
| TC_AI_APP_009 | 发布嵌入 | iframe 页可对话；未发布拒绝 |
| TC_AI_APP_010 | 菜单挂载 | 配置 menu_path 后可从系统菜单进入 |

---

## 12. 实现分期建议

| 阶段 | 内容 |
|------|------|
| P0a | 模块骨架、表、应用 CRUD、智能体三栏（无 tool） |
| P0b | Tool Calling（知识库 + 工作流）、变量记忆 |
| P0c | 高级编排聊天、多会话 |
| P0d | 发布嵌入、菜单、千问联网 |

OpenSpec `tasks.md` 可按 P0a→P0d 组织。

---

## 13. 自检记录

| 检查项 | 结果 |
|--------|------|
| TBD / TODO 占位 | 无 |
| 与 workflow/knowledge 规范矛盾 | 无；单向依赖 |
| 范围可单 change 分阶段完成 | 是（tasks 分子阶段） |
| 歧义点 | 已明确：workflow 入参固定 `query`；变量仅 agent 类型；长期记忆 P1 |
