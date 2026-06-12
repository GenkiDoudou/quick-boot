## Context

- 仓库为 Spring Boot 3.5.3 + MySQL + Sa-Token + Vue 3 管理端，已集成 Spring AI 与 **`quickboot-knowledge`**（知识库 CRUD、异步入库、语义检索、RAG 问答）。
- 已定稿设计见 `docs/superpowers/specs/2026-06-07-ai-workflow-design.md`；澄清结论：**1C、2D、3D、4A、5A、6A、7A**。
- 可复用模式：`BizExportHandlerRegistry` + Orchestrator（薄编排）、`IngestTaskAsyncExecutor`（异步入库）、`WebContentFetcher`（SSRF 防护）、C7JsonTable 列表页模板。

## Goals / Non-Goals

**Goals:**

- 交付可视化 DAG 工作流：Vue Flow 画布、JSON DSL 持久化、12 种节点 Handler。
- 三种运行模式：同步 Debug、异步 Run（任务表 + 轮询）、SSE 流式（`llm_delta` / `step_*` / `done`）。
- 草稿 / 发布版本；运行 Trace（`wf_run` + `wf_run_step`）。
- `knowledge-retrieval` 节点复用 `KnowledgeSearchService`；内置「默认 RAG」模板。
- `qc.workflow.enabled` 开关；Sa-Token 权限 `workflow:*`。

**Non-Goals:**

- Code 节点（P0–P1 均不做）。
- 替换或改造 `/knowledge/chat`。
- 对外 API Key / Bot 运行时 UI（P0 仅表字段与 `external-api.enabled=false` 预留）。
- 按部门 ACL、子工作流嵌套、图级循环节点、多模态节点。

## Decisions

### D1：模块边界 — 新增 `quickboot-workflow`

- **选择**：独立 Maven 模块，`quickboot-web` 依赖；workflow → knowledge **单向**依赖。
- **理由**：编排与知识库解耦；便于条件装配与独立测试。
- **备选**：放入 `quickboot-knowledge` — 拒绝，职责混杂。

### D2：编排模型 — DAG JSON DSL + Handler Registry

- **选择**：`graph_json` 存 nodes/edges；`WorkflowEngine` 拓扑排序 + `NodeHandlerRegistry` 调度；`TemplateRenderer` 解析 `{{nodeId.field}}`。
- **理由**：对齐 Dify/Coze；与 ExportOrchestrator 模式一致。
- **备选**：硬编码场景模板 — 拒绝，不满足 2D（12 节点）。

### D3：节点集 — 12 种（无 Code）

`start`, `answer`, `llm`, `knowledge-retrieval`, `if-else`, `template-transform`, `variable-assign`, `variable-aggregator`, `http-request`, `question-classifier`, `parameter-extractor`, `list-operator`。

- **question-classifier / parameter-extractor**：LLM + JSON 输出，非独立微调模型。
- **list-operator**：`filter`/`first`/`last`/`map-field`；不做图级 loop 节点。

### D4：三态运行

| 模式 | API | 说明 |
|------|-----|------|
| 同步 Debug | `POST /workflow/run/debug` | 阻塞至完成；返回完整 Trace |
| 异步 | `POST /workflow/run/async` | 返回 `runId`；`WorkflowRunAsyncExecutor` 后台执行 |
| SSE | `GET /workflow/run/stream?runId=` | `WorkflowStreamEmitter` 内存队列（P0 单实例） |

同步与异步共用 `WorkflowEngine.execute(runId)`。

### D5：版本与发布

- 草稿：`wf_workflow_version.is_draft=1`，`saveGraph` 更新。
- 发布：`publish` 生成新版本、更新 `published_version_id`；运行默认用已发布版本（Debug 可用草稿）。

### D6：前端 — @vue-flow/core

- **选择**：`@vue-flow/core` + background + controls；每种节点 `XxxNode.vue` + `XxxPanel.vue`。
- **页面**：列表 / 设计器 / 运行记录；遵循 `DESIGN.md` 与 `system/config/index.vue` 列表模板。

### D7：HTTP 节点安全

- SSRF 策略复用 knowledge 模块 `WebContentFetcher` 规则（内网拒绝、跳转复检、超时、大小限制）。
- Trace 落库前脱敏 Authorization/Cookie 等。

### D8：功能开关

```yaml
qc:
  workflow:
    enabled: true
    external-api:
      enabled: false
    sync-debug-timeout-ms: 60000
    async-timeout-ms: 600000
    max-concurrent-runs-per-user: 3
    http-request:
      enabled: true
      timeout-ms: 15000
      max-bytes: 5242880
```

### D9：表结构概要

- `wf_workflow`：主表 + `bot_enabled` / `external_api_enabled` 预留字段。
- `wf_workflow_version`：`graph_json`, `checksum`, `is_draft`。
- `wf_run` / `wf_run_step`：运行实例与逐步 Trace。
- `wf_api_key`：P0 建表不启用。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 12 节点 + 三态运行工作量大 | tasks 分 4 迭代；迭代 1 结束可演示线性 RAG |
| SSE 跨实例不可用 | P0 单实例内存 Emitter；P1 可选 Redis Pub/Sub |
| question-classifier 准确率依赖 LLM | 文档说明；支持 few-shot 示例配置 |
| HTTP 节点滥用 | `workflow:run` 权限 + 审计 |
| DSL 版本演进 | `version` 字段 + 迁移器 |
| workflow ↔ knowledge 循环依赖 | 严格单向依赖 |

## Migration Plan

1. Flyway 新增表与菜单权限；默认 `qc.workflow.enabled=false` 部署，验证通过后开启。
2. 无存量数据迁移；与 `/knowledge/chat` 并行上线。
3. 回滚：关闭 `qc.workflow.enabled`；不影响 knowledge 模块。

## Open Questions

- Flyway 版本号以实施时仓库最大版本 +1 为准。
- P1 是否引入 Redis 做 SSE 跨实例（当前 P0 不实现）。
