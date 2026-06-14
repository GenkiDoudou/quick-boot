## Context

QuickBoot 已在工作流 LLM 节点（`LlmForm`）、RAG（`RagService`）、问题分类、参数抽取等场景使用提示词，但内容分散在节点表单或后端硬编码，无统一台账。`add-ai-model-management` 已实现 `quickboot-ai` 模块与 `AiModelRegistry`，并将「Prompt 模板管理」明确为非目标。现于 **AI 能力** 菜单域新增独立提示词库，变量语法对齐工作流 `{{var}}`，二期再打通业务引用。

**已定稿决策**（见 `docs/superpowers/specs/2026-06-14-ai-prompt-management-design.md`）：方案 A — 在 `quickboot-ai` 扩展 `prompt` 子域；MVP 独立 CRUD + 库内优化闭环；同步优化 60s；编辑页 Tab（内容 | 优化 | 版本 | A/B）。

## Goals / Non-Goals

**Goals:**

- 提示词模板 CRUD：名称、编码、`promptType`、业务域、分类、标签
- 按 `promptType` 多段内容（LLM/RAG/CLASSIFIER/EXTRACTOR/CUSTOM）
- 变量声明表 + 编辑/发布时 `{{var}}` 根键名校验
- 状态机：DRAFT / PUBLISHED / ARCHIVED；发布生成版本快照
- 版本历史、两版本 Diff、AI 优化（同步 60s）、采纳、A/B 运行与人工评分
- 菜单「提示词管理」`/ai/prompt`；权限 `ai:prompt:*`
- 优化/A/B 经 `AiModelResolver` 解析 Chat 模型

**Non-Goals:**

- 工作流「从模板插入」、RAG 绑定、运行时模板渲染
- Few-shot 独立段、Token 统计、异步优化队列
- 跨租户/部门隔离（P0 全局可见）
- 工作流插入时变量与节点参数一致性校验（二期）

## Decisions

### 1. 模块边界：扩展 `quickboot-ai`（方案 A）

**选择**：在现有 `quickboot-ai` 新增 `prompt` 包，不新建 Maven 模块。

**备选**：`quickboot-prompt` 独立模块 — MVP 过重，Flyway/依赖链冗余，不采用。

**备选**：`sys_config` JSON — 无法支撑版本/A/B/变量校验，不采用。

### 2. 内容存储：主表 + 内容段表 + 变量表

**选择**：`ai_prompt` 元数据；`ai_prompt_content`（`version_id=0` 为草稿）；`ai_prompt_variable` 同行版本维度；发布时复制到 `ai_prompt_version.snapshot_json`。

**理由**：避免按 `promptType` 宽表多列 NULL；版本与草稿隔离清晰。

### 3. 变量校验

**选择**：提取各段 `{{...}}` 根键名（对齐 `useUpstreamVariables.js` 的 `extractTemplateRootKeys` 逻辑），保存/发布/采纳前须 ⊆ 已声明 `var_key`。

**实现**：后端 `AiPromptVariableValidator` 统一校验；前端编辑 Tab 实时提示（复用或移植工作流校验函数）。

### 4. AI 优化与 A/B

**选择**：同步 HTTP 调用，`timeout=60000ms`；优化 meta-prompt 服务端内置，要求模型输出 JSON `{"sections":{...},"changeSummary":"..."}`。

**模型解析**：`optimize_model_id` → 全局 `WORKFLOW_CHAT` → `CHAT` 默认；`qc.ai.enabled=false` 时 `/optimize`、`/ab/run` 抛业务异常，CRUD 不受影响。

**A/B**：`CompletableFuture` 并行两次 Chat；样例变量仅一层 `{{key}}` 替换；按 `promptType` 拼接有效 prompt（如 LLM：system + user）。

### 5. 版本 Diff

**选择**：`GET /version/diff` 返回结构化段级 before/after + 变量列表 diff；前端可选用 `diff` 库展示行级差异。

### 6. 菜单与 Flyway

- Flyway **V66**（V65 已占用）：6 张表 + menu 2330–2335
- 更新 menu 2320 `remark` 为「AI 大模型、MCP 与提示词管理」

### 7. 前端结构

- 列表：`views/ai/prompt/index.vue`（C7JsonTable，对齐 `ai/model/index.vue`）
- 编辑：`views/ai/prompt/edit.vue`，路由 `/ai/prompt/edit/:promptId?`
- Tab：内容 | 优化 | 版本 | A/B
- API：`api/ai/prompt.js`

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 优化模型返回非 JSON | session 标记 FAILED，前端展示原文；不自动采纳 |
| 60s 同步阻塞 UI | 前端全页 loading + 超时提示；二期可加异步 |
| `CUSTOM` 类型段结构复杂 | MVP 用 JSON 数组配置 key/label；校验至少一段非空 |
| 大段 TEXT 性能 | 分页列表不返回 content；详情按需加载 |
| 与二期工作流接入字段不一致 | `promptType` 段 key 与工作流 `nodeMeta` 对齐，文档注明映射表 |

## Migration Plan

1. 部署 Flyway V66（仅增表与菜单，无破坏性 ALTER）
2. 部署 `quickboot-ai` prompt 包与前端页面
3. 管理员分配 `ai:prompt:*` 角色权限
4. 配置至少一个可用 Chat 模型后使用优化/A/B
5. 回滚：隐藏菜单 2330；`qc.ai.enabled` 不影响已存数据

## Open Questions

（无。brainstorming 已定稿，本期硬编码优化超时 60000ms，不新增 `qc.ai.prompt.*` 配置项。）
