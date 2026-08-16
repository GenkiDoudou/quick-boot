# quickboot 编码约定

> **定位**：本仓库**编码 / 库表 / 分层 / 命名**的事实来源。  
> **优先级**：写代码、改 DDL、改前端页面时以本文与 `.cursor/rules`、现网实现为准。  
> **协作流程**（排障两阶段、Karpathy §5、brainstorming 澄清格式等）以 `AGENTS.md` 为准；本文不重复。  
> **非目标**：不引入未落地的硬门槛（强制 TDD 覆盖率、WCAG AA、深色科技感主题等）。

写代码前须通读本文相关章节与同域现网实现；勿把规范正文复制进 `AGENTS.md`。

---

## 0. 生成代码优先复用（强制）

生成或改造**前端 / 后端**功能时，**先检索并优先使用仓库已有能力**，禁止平行再造：

**前端**
- 列表/表单/弹窗/Excel：优先 `src/packages/`（C7*，如 `C7JsonTable`、`C7Dialog`、`C7ExcelDownload`）与同域 `views/` 已有页模板。
- 通用 UI：优先 `src/components/`、Element Plus；工具优先 `src/utils/`、`src/plugins/`、`src/directive/`。
- HTTP：只走 `src/api/` + 统一 `utils/request`（或项目既有封装），禁止页面裸调 axios/fetch。

**后端**
- 通用能力优先 `quickboot-common`（如 `R` / `PageInfo` / `ExcelUtils` / 校验分组 / 异常体系 / 缓存封装）。
- 实体基类、项目级抽象优先 `quickboot-core`（如 `BaseEntity`）。
- 持久化优先 MyBatis-Plus + `BaseBaseMapper`；对照同域已有 Controller/Service 契约再写新接口。

若现有组件/工具**明显不满足**需求，可新增，但须在实现说明中写清「已检索过什么、为何不能复用」。

---

## 0.1 生成代码注释（强制）

Agent 或人工**生成 / 实质性改写**业务代码时，须同步写好**详细中文注释**（协作层要求见 `AGENTS.md`「生成代码注释」）。目标：后人无需猜意图即可维护。

### 原则

- 注释说明 **职责、为什么、边界与约束**；禁止只把标识符翻译成中文的废话注释。
- 默认 **简体中文**；与 OpenAPI / JavaDoc 标签并存时，摘要与字段说明用中文。
- **本次新增或改写**的公开 API、复杂分支必须有注释；未改动的既有代码不要为「凑注释」整文件重写。

### 后端（`quickboot`）

- **类**：类级 JavaDoc 说明模块职责与所属域。
- **方法**：public/protected 须 JavaDoc（用途、`@param`、`@return`、重要副作用/事务/鉴权前提）；Controller 还须与 `@Operation` 语义一致。
- **字段**：Entity / VO / DTO 字段注明业务含义；字典列注明类型编码（如 `状态(sys_normal_disable)`）。
- **复杂逻辑**：非显而易见的分支、批量、缓存、并发、自定义 SQL 意图须行内或块注释。

### 前端（`quick-ui` / `quick-h5`）

- **页面 / 组件**：文件头或 `script` 顶部简要说明页面职责与关键交互。
- **composable / 工具函数**：说明入参、返回值、适用场景与副作用（如是否写 storage、是否绑生命周期）。
- **复杂逻辑**：`watch` / `computed` / 多步请求编排写清触发条件与边界；权限/超管例外等业务规则须标明。
- **类型与常量**：非字面可读的枚举值、魔法数旁注明含义（优先具名常量 + 注释）。

### 豁免

与 `AGENTS.md` 相同：琐碎无行为变更、纯配置/文案/样式、或用户明确要求少写注释时，可从简。

---

## 1. 文件编码（强制）

- 新建或修改任何文本文件时，**一律使用 UTF-8 无 BOM**。
- 严禁 GBK/ANSI/UTF-16 等会导致跨环境乱码的编码。
- 严禁在源码文件头写入 BOM（`\ufeff`）；若编译报 `非法字符: '\ufeff'`，须先移除 BOM。
- 若发现历史乱码或编码不一致，优先转为 UTF-8 无 BOM 后再改。
- PowerShell 写文件时优先无 BOM（如 `new UTF8Encoding($false)`）。

---

## 2. 技术栈与目录锚点

### 2.1 前端（`quick-ui`）

- Vue 3 + Vite + Element Plus + Pinia + Axios；样式 Sass/SCSS；包管理 pnpm。
- 语言：**现以 JS 为主**；新代码可逐步 TypeScript；禁止擅自引入第二套 UI 框架（如 Naive UI）。
- 视觉以根目录 `DESIGN.md` 为准（Element Plus 主色体系），不另起深色/发光主题。
- 口头需求与 `DESIGN.md` 冲突时，默认以 `DESIGN.md` 为准，并向用户确认例外。

推荐目录：

```text
quick-ui/src/
├── api/            # 按业务域封装接口（禁止页面裸调 axios/fetch）
├── assets/
├── components/     # 通用组件
├── packages/       # 业务级封装组件（如 C7*）
├── layout/
├── views/          # 页面：views/{模块}/.../index.vue
├── store/
├── utils/
├── plugins/
├── directive/
├── router/
└── main.js
```

### 2.2 后端（`quickboot`）

- Java 17、Spring Boot 4.0.0、MyBatis-Plus、Lombok、springdoc OpenAPI。
- Maven 模块：`quickboot-common` / `quickboot-core` / `quickboot-module-*` / `quickboot-app`。

---

## 3. Maven / Modulith 分层

依赖方向：`app → module-* → core → common`。禁止反向依赖；禁止 `common` / `core` 依赖任何 `module-*`。

| Maven 模块 | 职责 | Modulith |
|---|---|---|
| `quickboot-common` | 独立工具：异常、校验、Excel、缓存封装等；**无业务表实体 / 业务 Service** | 非业务 Application Module |
| `quickboot-core` | 项目间共享（如 `BaseEntity`）；仅依赖 `common` | 非业务 Application Module |
| `quickboot-module-system` | 系统域：用户/角色/菜单/部门/字典/配置/日志/OAuth 等 | 一个 Application Module |
| `quickboot-app` | 启动组装、模块校验；**不写业务 Controller** | 组装根 |

业务模块包结构（模板：`openspec/changes/spring-modulith-maven-layering/new-domain-module-template.md`）：

```text
io.github.genkidoudou.<domain>/
  api/           对外：Facade、DTO、命令、事件契约
  internal/      封闭：controller、service、mapper、entity、config、api 实现、vo/utils
  package-info   @ApplicationModule；开放 api
```

- 保持 `controller / service / mapper` / `entity / dto / vo` 分层；业务模块内再按 `api`（公开）与 `internal`（封闭）划分。
- 允许：`module-X` → `module-Y` 的 **`api` 公开类型**。
- 禁止：依赖他域 `internal`；业务实体/Mapper/Service 放入 `common`/`core`；业务模块循环依赖。

---

## 4. 数据库约定（强制）

### 4.1 布尔与是否类字段

- **禁止**在数据库列、持久化实体（`entity`）以及与表一一映射的 VO 中使用 `boolean` / `Boolean` / `TINYINT(1)` 表达是否类语义。
- 统一用 **`String` + 库表 `CHAR(1)`**，取值与 `status` / `del_flag` 一致：
  - **`0`** = 否 / 关闭 / 禁用（默认）
  - **`1`** = 是 / 开启 / 启用
- 业务判断写 `"1".equals(field)`；入库前将入参归一化为 `"0"` / `"1"`。
- **不在本规范内**：配置类、运行时 DTO、与库无关的 API 契约中的 `boolean`（如开关配置）可保留。
- 须同时遵守 `.cursor/rules/no-boolean-db-entity.mdc`（若存在）。

### 4.2 命名与审计

- 表名：业务前缀 + snake_case（如 `sys_user`），非强制英语复数。
- 字段：snake_case；审计字段对齐 `BaseEntity`：`create_by` / `create_time` / `update_by` / `update_time`。
- 软删除：标准使用 `del_flag` + MyBatis-Plus `@TableLogic`。
- 主键：业务表常用雪花 / `ASSIGN_ID` 对应的数值主键（如 `user_id`），非强制全局 `id` UUID。

### 4.3 字典字段（强制）

当列存储的是**系统字典键值**（非自由文本）时：

1. **列注释格式**：`描述(字典类型)`，括号内为 `sys_dict_type.dict_type` 编码。  
   - 示例：`性别(sys_user_sex)`、`状态(sys_normal_disable)`。
2. **字典类型选型**  
   - **优先复用**已有全局/域内字典（如 `sys_normal_disable`）。  
   - 无合适可复用类型时，新建字典类型，编码约定为 **`表名_字段名`**（均为 snake_case），如 `sys_user.sex` → `sys_user_sex`。
3. **取值落库**：列内存字典 **value**（键值），不存 label；是否类仍遵守 §4.1 的 `0`/`1`。
4. **迁移**：新建字典类型/数据须在同一变更或明确前置迁移中落库（`sys_dict_type` / `sys_dict_data`），并与列注释中的类型编码一致。

### 4.4 设计与迁移

- 迁移文件头部用块注释说明：变更目的、影响范围、依赖的前置版本或表。
- 表与列须有业务注释；枚举/状态类字段在注释中写明取值含义；字典列按 §4.3。
- 非显而易见的索引须注释查询场景或性能目的；复杂 `ALTER` 须说明数据迁移或回填策略。

---

## 5. 后端编码约定

新建或生成后端代码前：通读本节，并对照同域已有实现；优先复用见 **§0**。

### 5.1 实体与 MyBatis-Plus

1. 实体与表一一对应；类名取表意名，**不加 `Entity` 后缀**（如 `SysUser`）。
2. 使用 Lombok（如 `@Data`）；业务实体可继承 `io.github.genkidoudou.core.entity.BaseEntity`。
3. 主键：`@TableId(value = "...", type = IdType.ASSIGN_ID)`（现网默认）；`ASSIGN_UUID` 仅作明确例外。
4. `@TableField`：**仅在需要时**显式配置（自动填充、逻辑删除、非默认列名/策略等）；**禁止**「每个字段必须写 `@TableField(value)`」。
5. 请求 DTO 以 `DTO`（或项目既有命名）结尾；响应 VO 以 `VO` 结尾；与表映射的 VO 同样遵守布尔 `CHAR(1)` 约定。

日期时间：优先 `LocalDateTime`；全局 Jackson 与现网一致。DTO 若需局部格式再用 `@JsonFormat` / `@DateTimeFormat`，时区与项目配置一致（常见 `GMT+8`）。

### 5.2 异常

- 可预期业务失败：`io.github.genkidoudou.common.exception.WarningException`（配合错误码 / i18n）。
- 不可预期或严重错误：`ErrorException` 等项目异常体系。
- **禁止**用 `IllegalArgumentException` 作为主要业务失败信号。
- 不使用外来名 `WarnException` / `ResponseMessage`。

### 5.3 持久层（Mapper）

```java
import org.apache.ibatis.annotations.Mapper;
import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;

@Mapper
public interface SysRoleMapper extends BaseBaseMapper<SysRole> {
}
```

- 优先 MyBatis-Plus 内置 CRUD / `LambdaQueryWrapper` / 分页；仅在 MP 不足时写自定义 SQL。
- XML 文件名与 Mapper 接口名一致；存放路径以项目配置为准。

### 5.4 Controller

- 只做 HTTP 入参、鉴权注解、调用 Service、返回统一体；**禁止**把业务逻辑堆在 Controller。
- 统一返回 `io.github.genkidoudou.common.api.R<T>`（**保留泛型**）。
- 分页数据使用项目已有 `PageInfo`（或等价结构）放入 `R` 的 `data`。
- 必须使用 springdoc：`@Tag`、`@Operation`；关键参数加 `@Parameter`。
- 参数接入 Jakarta Validation；禁止用 `JSONObject` / 无类型 `Map` 作为对外入参或返回值。
- 路径段与 JSON 字段统一 **camelCase**（如 `sys/config`、`monitor/operlog`）。
- 修改/删除语义默认 `@PostMapping`（除非项目已有统一 REST 约定）；默认不使用 `@PutMapping` / `@DeleteMapping`。
- **public** 类型及 public/protected 成员须具备标准 JavaDoc；Controller 说明用途、鉴权前提与副作用。
- 异常由全局处理器处理；Controller 一般不吞业务异常。

示例风格（示意）：

```java
@Tag(name = "参数配置")
@RestController
@RequestMapping("sys/config")
public class SysConfigController {

  @Operation(summary = "分页查询")
  @GetMapping("/list")
  public R<PageInfo<...>> list(...) { ... }

  @Operation(summary = "新增参数")
  @PostMapping
  public R<Void> add(@Valid @RequestBody ... dto) { ... }
}
```

### 5.5 Service

- 按方法需要加事务（写操作默认审视 `@Transactional`）。
- 返回业务对象 / 分页结构，**不要**在 Service 直接组装 HTTP 专用包装（由 Controller 包 `R`）。
- Service/Mapper 注释非显而易见的事务、批量、缓存与并发约束。

### 5.6 Excel 与字典字段（生成/改造时强制）

对 §4.3 认定的字典列，若该业务提供 **Excel 导出和/或导入**：

1. 在对应 **导出 VO** / **导入 `*ImportRow`** 的该字段上标注  
   `@ExcelDictFormat(dictType = "<字典类型>")`  
   （类型编码与库表注释括号内一致，如 `sys_normal_disable`）。
2. **优先 `dictType`** 走系统字典；仅当确无系统字典、且选项极少且稳定时，可用内联  
   `@ExcelDictFormat(dictText = {"0=男", "1=女"})`（与字典设计一致：`dictType` 优先于 `dictText`）。
3. 字段 Java 类型以 **`String`** 为准（存字典 value）；导入模板若开启列约束，下拉选项为 **label**（见 Excel 导入模板约束设计）。
4. 导入/导出统一走 `ExcelUtils`；勿在业务代码手写 value↔label 映射平行实现。
5. 实体 / 与表映射 VO 的 JavaDoc 或字段注释宜标明字典类型，便于前后端对齐。

示意：

```java
@ExcelProperty("状态")
@ExcelDictFormat(dictType = "sys_normal_disable")
private String status;

@ExcelProperty("性别")
@ExcelDictFormat(dictText = {"0=男", "1=女", "2=未知"}) // 仅无系统字典时的例外
private String sex;
```

---

## 6. 前端编码约定

新建或改造页面/组件前：通读根目录 `DESIGN.md` 与本节，并对照同域已有页；优先复用见 **§0**。

### 6.1 结构与调用

- 接口一律走 `src/api/`（或项目约定的 `services/`）；统一错误与拦截在现有 `utils/request`（或项目封装）中处理。
- 通用组件：`src/components/`；业务级封装优先对照 `src/packages/`（如 C7*）与同域已有页。
- 页面：`src/views/{模块}/...`，常见入口 `index.vue`。
- 列表页对照同域已有页与统一表格/模板，避免单页自造布局。
- 路由可在 `router/index.js` 或后端菜单配置；**前端路由 path 不必与后端 `@RequestMapping` 逐字相同**。
- 类型定义放在模块旁或 `types/` / `typings/`。

### 6.2 组件、命名与导入

- 单文件组件 `.vue`，Composition API + `<script setup>`；复杂状态抽 composable。
- Props 使用 `defineProps<T>()`（TS 场景）。
- 命名：组件/类型 PascalCase；composable `useXxx`；常量 `UPPER_SNAKE_CASE`；CSS 类名 kebab-case；函数/变量 camelCase。
- 导入顺序：框架核心 → UI 库 → 第三方工具 → 路径别名 `@/` → 相对路径 → 样式文件（最后）。
- 列表渲染须有 `key`；禁止静态 UI 滥用内联 style（遵循项目样式方案）；可写明确类型时禁止 `any`。
- 注释使用中文，写清「为什么」与边界；复杂 `watch`/`computed` 说明触发条件。
- 异步须有 try/catch 或 `.catch()`；对用户提示友好，禁止泄漏密钥；API 错误尽量在统一请求封装中处理。

### 6.3 页面生成规范

- 生成页面时：列表**不要**展示业务主键 id 字段；**要**展示创建时间，格式 `yyyy-MM-dd HH:mm:ss`。

### 6.4 字典字段（生成/改造时强制）

对 §4.3 认定的字典列，前端须走项目字典能力，**禁止**页面内写死 options 平行实现（除非确认无系统字典且与后端 `dictText` 例外一致）：

1. **取数**：`import { useDict } from '@/utils/dict'`，按字典类型拉取，例如  
   `const { sys_normal_disable } = useDict('sys_normal_disable')`  
   （类型编码与库表注释 / 后端 `@ExcelDictFormat(dictType)` 一致）。
2. **列表 / 详情回显**：优先 `C7DictTag`（`:model-value` + `:options`）；  
   在 `C7JsonTable` 列配置中可用 `columnType: 'tag'`（或项目约定的 tag 列）并传入 `options` / `dictList`。
3. **查询条件 / 表单**：`el-select`（或 C7 搜索列 `type: 'select'`）的 `options` 绑定 `useDict` 返回值（注意 `.value`）。
4. **存取语义**：接口与表单绑定字典 **value**；界面展示 **label**（由 `C7DictTag` / 下拉完成），禁止把 label 当主键回写。
5. 多字典类型一次 `useDict('type_a', 'type_b')` 即可；勿重复请求同类型。

示意（对齐现网 monitor 页）：

```js
import { useDict } from '@/utils/dict'
const { sys_oper_status } = useDict('sys_oper_status')
// 列：<C7DictTag :model-value="row.status" :options="sys_oper_status" />
// 搜索：{ prop: 'status', type: 'select', options: sys_oper_status.value || [] }
```

### 6.5 测试

- 已有 Vitest 的模块保持可跑；**不强制**仓库级 80% 覆盖率门槛（若日后引入 CI 门槛，先写入 `AGENTS.md` 协作约束后再执行）。

---

## 7. Git 提交（编码侧）

- Conventional Commits：`type(scope): description`
- 类型：feat / fix / docs / style / refactor / test / chore
- 信息清晰说明「为什么」；只提交自己改动的文件（多 Agent 协作约束见 `AGENTS.md`）。

