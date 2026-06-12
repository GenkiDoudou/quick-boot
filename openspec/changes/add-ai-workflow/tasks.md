## 1. 模块与依赖



- [x] 1.1 在父 `pom.xml` 注册 `quickboot-workflow` 子模块

- [x] 1.2 创建 `quickboot-workflow` 模块骨架（config/controller/service/mapper/domain/dto/engine/handler/stream/async）

- [x] 1.3 `quickboot-web` 引入 `quickboot-workflow`；确认 workflow → knowledge 单向依赖

- [x] 1.4 验证 `mvn -pl quickboot-web -am clean compile -DskipTests` 通过



## 2. 配置与基础设施



- [x] 2.1 新增 `WorkflowProperties`（`qc.workflow.*`）与 `@ConditionalOnProperty(enabled=true)` 自动配置

- [x] 2.2 在 `application.yml` / `application-dev.yml` 增加 workflow 配置示例（含 http-request、stream、external-api 预留）

- [x] 2.3 实现 `WorkflowAiGuard`（或复用 knowledge 模式检测 ChatModel 可用性）



## 3. 数据库与权限种子



- [x] 3.1 Flyway 迁移：创建 `wf_workflow`、`wf_workflow_version`、`wf_run`、`wf_run_step` 表（含列注释与索引）

- [x] 3.2 Flyway 迁移：创建 `wf_api_key` 表（P0 不启用逻辑）

- [x] 3.3 Flyway 迁移：插入「工作流管理」菜单（列表/设计器/运行记录）及 `workflow:*` 按钮权限



## 4. 领域模型与 DSL 基础



- [x] 4.1 实现 Entity/Mapper：`WfWorkflow`、`WfWorkflowVersion`、`WfRun`、`WfRunStep`

- [x] 4.2 实现 Bo/Vo/QueryBo 及 graph DTO（nodes/edges）

- [x] 4.3 实现 `WorkflowGraphValidator`（start/answer 唯一、DAG、可达性、sourceHandle）

- [x] 4.4 实现 `TemplateRenderer`（`{{nodeId.field}}` / `{{sys.*}}` / `{{inputs.key}}`）及单元测试



## 5. 执行引擎核心



- [x] 5.1 定义 `NodeHandler` 接口与 `NodeHandlerRegistry`

- [x] 5.2 实现 `WorkflowContext` 与 `WorkflowEngine`（拓扑排序、分支路由、失败短路）

- [x] 5.3 实现 StartHandler、AnswerHandler

- [x] 5.4 实现 `WorkflowDefinitionService`（CRUD、saveGraph、validateGraph、publish、版本管理）



## 6. AI 与知识库节点



- [x] 6.1 实现 KnowledgeRetrievalHandler（调用 `KnowledgeSearchService`，输出 chunks/citations/contextText）

- [x] 6.2 实现 LlmHandler（ChatClient；支持 streaming 标志与 WorkflowStreamEmitter 集成）

- [x] 6.3 实现 QuestionClassifierHandler（LLM + JSON 分类输出）

- [x] 6.4 实现 ParameterExtractorHandler（LLM + JSON schema 约束）



## 7. 逻辑、变量与 HTTP 节点



- [x] 7.1 实现 IfElseHandler（条件运算符集）

- [x] 7.2 实现 TemplateTransformHandler、VariableAssignHandler、VariableAggregatorHandler

- [x] 7.3 实现 ListOperatorHandler（filter/first/last/map-field）

- [x] 7.4 实现 HttpRequestHandler（SSRF 防护对齐 WebContentFetcher）及单元测试



## 8. 运行态：同步、异步、SSE



- [x] 8.1 实现 `WfWorkflowController`（定义 CRUD、saveGraph、publish、template/list）

- [x] 8.2 实现同步 Debug：`POST /workflow/run/debug` + Trace 组装

- [x] 8.3 实现 `WorkflowRunAsyncExecutor` + `POST /workflow/run/async` + `GET /workflow/run/getInfo|list`

- [x] 8.4 实现 `WorkflowStreamEmitter` + `WfStreamController`（SSE：step_start/llm_delta/step_end/done/error/heartbeat）

- [x] 8.5 实现 Trace 脱敏与并发限制（`max-concurrent-runs-per-user`）



## 9. 内置模板



- [x] 9.1 实现「默认 RAG 工作流」内置模板（start → knowledge-retrieval → llm → answer）

- [x] 9.2 提供 `GET /workflow/template/list` 与从模板创建工作流能力



## 10. 前端：依赖与 API



- [x] 10.1 在 `quick-ui` 添加 `@vue-flow/core`、`@vue-flow/background`、`@vue-flow/controls` 依赖

- [x] 10.2 实现 `api/workflow/` 封装（定义 CRUD、run/debug、run/async、run/stream、run 查询）



## 11. 前端：列表与运行记录



- [x] 11.1 实现 `views/workflow/list/index.vue`（C7JsonTable，参照 system/config）

- [x] 11.2 实现 `views/workflow/run/index.vue`（运行记录列表 + 详情 Trace 时间线）



## 12. 前端：Vue Flow 设计器



- [x] 12.1 实现设计器主框架（节点面板、画布、属性面板、工具栏）

- [x] 12.2 实现 12 种节点 `XxxNode.vue` + `XxxPanel.vue` 并注册 nodeTypes

- [x] 12.3 实现 graph 与 DSL 双向转换、saveGraph、validateGraph

- [x] 12.4 实现 Debug 运行（节点高亮 + Trace 抽屉）

- [x] 12.5 实现异步运行 + EventSource SSE 流式展示区



## 13. 联调与验证



- [x] 13.1 配置路由与菜单权限联调

- [x] 13.2 验证默认 RAG 模板 Debug/SSE 运行与 knowledge 跨库隔离

- [x] 13.3 验证 `qc.workflow.enabled=false` 时无 `/workflow/**` 端点

- [x] 13.4 验证 `/knowledge/chat` 行为与改造前一致

- [x] 13.5 前端 `pnpm build:prod` 与后端模块测试通过

