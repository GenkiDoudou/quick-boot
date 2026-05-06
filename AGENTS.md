# 项目协作指南（AGENTS.md）

本仓库是一个 **Spring Boot 3（后端）+ Vue 3 + Vite + Element Plus（前端）+ VitePress（文档）** 的全栈项目集合。请优先遵循仓库内已有规则文件：`.cursorrules` 与 `.cursor/rules/*`（若存在）。

## 目录结构（高层）

- `quickboot/`：后端（Maven 多模块）
  - `quickboot-common/`：通用能力（工具类、通用响应、异常、安全组件等）
  - `quickboot-core/`：核心配置/基础设施能力
  - `quickboot-web/`：Web 启动模块（Controller/Service/Mapper、Spring Boot 启动类、资源配置）
- `quick-ui/`：前端（Vue 3 + Vite，使用 `pnpm`）
- `docs/`：文档站点（VitePress）
- `原始需求/`：由分析/梳理产出的“原始需求”文档（不参与构建）


## 环境与依赖

- 后端：JDK **17+**、Maven **3.6+**、MySQL **8+**（或按实际配置）
- 前端/文档：Node.js（建议 **18/20**）、`pnpm`（`quick-ui/package.json` 标注 `pnpm@9.0.0`）

## 常用命令

### 后端（`quickboot/`）

```bash
cd quickboot
mvn clean install -DskipTests

# 启动 web 模块（端口默认 9991）
mvn -pl quickboot-web spring-boot:run
```

运行时注意：
- `quickboot/quickboot-web/src/main/resources/application.yml` 默认端口为 `9991`，并启用 Flyway（迁移目录：`classpath:db/migration`）。
- `quickboot/quickboot-web/src/main/resources/application-dev.yml` 中数据库连接信息使用 `ENC(...)`，启动时需要提供 `jasypt` 解密口令（项目内提示为 `-Djasypt.encryptor.password=密钥`）。
- **quickboot-common 缓存**：由 `spring.cache.type` 选择 `caffeine`（默认本地）或 `redis`；注解可使用 `@Cacheable(cacheNames="分区名#过期秒数")`，未写 `#ttl` 或解析失败时默认 TTL 为 3600 秒；`@CacheEvict`、`@CachePut` 的 `cacheNames` 必须与 `@Cacheable` **完全一致**（含 `#过期秒数`）。应用模块需在启动类（或配置类）上启用 `@EnableCaching`。实现位于 `io.github.genkidoudou.common.cache`，通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置。
- **quickboot-common 国际化**：词条由 Spring `MessageSource` 提供，常用配置为 `spring.messages.basename=i18n/messages`（对应 `classpath:i18n/messages*.properties`）。业务侧使用 `io.github.genkidoudou.common.i18n.I18nUtil` 按错误码与占位参数取文案；须在 **Spring 容器就绪后**调用（内部通过 Hutool `SpringUtil` 获取 `MessageSource`）。默认语言为当前线程 `LocaleContextHolder.getLocale()`；浏览器/网关语言切换依赖 Web 层 `LocaleResolver` 等与 `LocaleContextHolder` 的配合，**`I18nUtil` 不负责完整切换链路**。
- **quickboot-common 统一响应与分页契约**：业务约定 **HTTP 200**，成败看 JSON 体字段 `code`（与 `io.github.genkidoudou.common.api.R` 对齐）；`traceId` 从 SLF4J MDC（键名与 `logback`/`TraceIds` 一致）填充。Controller 可使用 `PageRequest` / `PageInfo` 作为分页入参/出参，Service 内转为 MyBatis-Plus `Page` 并可用 `PageInfo.from(IPage)` 回填；**`size` 默认 10、最小 1**。全局异常到 `R` 的映射不在本包，由异常模块承担（见 OpenSpec `common-response-paging`）。
- **quickboot-common Servlet 写出错误 JSON**：Filter / 防火墙等可在未进入 Controller 时调用 `io.github.genkidoudou.common.servlet.ServletUtils#writeResponse`，固定 **HTTP 200** + `R.error` 形态 JSON；`MessageSource` 词条键与业务码一致，为 **`String.valueOf(业务码)`**（如 `40301`）。须在 **Locale 已写入 `LocaleContextHolder`** 之后调用（通过 Filter `@Order` 与 `LocaleResolver` 等保证），否则文案可能始终为默认语言。

### 前端（`quick-ui/`）

```bash
cd quick-ui
pnpm i
pnpm dev
```

约定与运行时信息：
- Vite dev server 默认端口：`8800`（见 `quick-ui/vite.config.js`）。
- 开发代理：`/dev-api -> http://localhost:9991`（会 rewrite 去掉 `/dev-api` 前缀）。
- 环境变量：`.env.development` / `.env.production` 控制 `VITE_APP_BASE_API` 等。

### 文档（`docs/`）

```bash
cd docs
pnpm i
pnpm dev
```

## 代码与改动约定（面向协作/维护）

- **避免误扫/误改大目录**：不要对 `**/node_modules/**`、`bak/` 做递归检索或批量改动；在 PowerShell 下递归遍历可能触发长路径/缺失目录报错。
- **后端分层**：尽量保持 `controller / service / mapper / entity / dto / vo` 分层清晰（`quickboot-web/src/main/java/...` 已按此组织）。
- **前端组织**：API 放在 `quick-ui/src/api/`，通用组件放在 `quick-ui/src/components/`；业务增强组件库位于 `quick-ui/src/packages/`，并通过 `quick-ui/src/packages/index.js` 统一导出/注册。
- **文档同步**：若修改 `quickboot-common/` 的通用能力，优先同步对应文档（具体规范参考 `.cursorrules` 与 `.cursor/rules/*`）。

### 生成代码时的依赖与工具类（AI / 脚手架 / 批量产出）

在**新建或生成**代码需要通用工具能力时，**先以当前工程已声明的依赖为准**，再考虑手写实现或新加依赖：

- **后端（Maven）**：查阅目标模块及父工程 `quickboot/pom.xml`、各子模块 `pom.xml` 中已有 **artifact**；优先使用已引入能力，例如 **Lombok**（样板代码缩减）、**Hutool**（字符串/日期/集合/IO/加解密等工具）、**Spring / MyBatis-Plus** 及 **`quickboot-common` 已提供的工具类**（如 `I18nUtil`、`ServletUtils` 等）。避免在已有等价 API 时重复造轮子；确需新增依赖时须在变更说明中写明理由并同步修改 POM。
- **前端（pnpm）**：以 `quick-ui/package.json`（及 workspace 内实际解析）为准，优先复用已有库（如 Element Plus、axios 封装、常用工具库等），避免静默增加功能重复的包。

### 生成前后端代码时的注释要求（AI / 脚手架 / 批量产出）

在**新建或生成**后端（Java）与前端（Vue/TS）代码时，应附带**可维护向的详细注释**，便于后续协作与评审，而非仅输出「能跑」的裸代码。

**原则**：面向「数月后仍能理解约束与边界」的读者；避免复述标识符的废话注释，也**不得**留下对外 API、组件契约仅靠猜的「黑盒」实现。

**最低覆盖**（生成物应达到）：

- **后端**：新建的 **public 类型**及其 **public/protected 成员**均具备 **JavaDoc**（`@param`、`@return`、`@throws` 等齐全；无返回值或简单场景也应有简短语义说明）；对包外可见、承担跨层或框架集成职责的类型，同样建议类级 JavaDoc。
- **前端**：**默认导出的 Vue 组件**、**composable**、模块级 **工具函数**均具备 **`/** ... */` JSDoc**（说明职责、入参/返回值、`props`/`emit` 契约、副作用与调用前提）。
- **行内补充**：非显而易见的分支、状态机、**事务边界**（含传播/回滚预期）、与外部系统/缓存/消息的约定，用简短段落或行内注释写明；复杂表达式、**魔数**、时间/金额等单位须注明含义与来源。

**建议在非琐碎业务下进一步写清**：

- **后端**：Entity/DTO 字段与库表列或上下游接口字段的对应（名称不一致时必写）；枚举/状态取值含义；缓存或重试策略的假设；并发、线程安全、**null 可空性**等不直观处点明。
- **前端**：重要 `props` 的合法取值、默认值与「未传」行为；`emit` 事件名与载荷形状；依赖路由参数、全局状态、权限、异步数据就绪的逻辑须注明前置条件；与后端 DTO 的字段映射或转换规则（camelCase、字典码等）。
- **统一口径**：注释语言以**简体中文**为主，专有名词、API 名称、配置键可保留英文；着重解释「为什么」、不变量与误用后果。

## 提交/验证建议（尽量局部、可复现）

- 后端：优先跑模块级命令（例如 `mvn -pl quickboot-web test`），再视情况跑全量。
- 前端：至少 `pnpm build:prod` 做一次打包校验（当前未配置 lint/test 脚本时以构建为主）。
- **依赖与生成物自检**：提交或评审前，核对改动所涉 **Maven 模块 `pom.xml`** 与 **`quick-ui/package.json`**（及实际用到的 workspace 包），确保代码中使用的工具类、注解与三方 API **与已声明依赖一致**；若新增坐标或 npm 依赖，须已在对应构建文件中落库，并在变更说明或 PR 中简要说明用途，避免「代码已引用、构建未声明」。

