# 登录日志与登录失败锁定设计文档

## 1. 背景与目标

在 `quickboot` + `quick-ui` 中实现《登录日志需求文档》所述能力：登录行为可查、可排序、可筛选、单条/批量删除、清空、导出、按用户名解锁；并与**登录失败计数 + 临时锁定**联动（方案乙），满足「解锁有实际效果」的验收预期。

路径与权限以需求文档为真源：`/monitor/logininfor/*`、`monitor:logininfor:*`。

## 2. 已确认决策摘要

| 项 | 决策 |
|----|------|
| API 前缀与权限 | `/monitor/logininfor`，`monitor:logininfor:list/remove/unlock/export` |
| HTTP 动词 | 与仓库约定一致：删除/清空用 **POST**；列表 **GET**；解锁 **GET**；导出 **POST**（与需求文档字面 `DELETE` 不一致，以本设计为准） |
| 表字段语义 | 对齐若依：`status` + `msg`（描述），列表「状态」「描述」直接映射 |
| 登录写日志 | `/login` 成功与失败均写 `sys_logininfor`；写库异常不得阻断登录主流程 |
| 锁定与解锁 | 失败达阈值锁定；成功登录清零失败计数；解锁接口清除该用户锁定相关缓存条目 |
| 缓存实现 | **仅通过 Spring `CacheManager` / `org.springframework.cache.Cache` 读写**，禁止业务代码注入 `RedisTemplate`、`StringRedisTemplate` 或 `RedisConnectionFactory` 操作锁定数据 |
| 锁定策略参数 | 落在 **参数设置（`sys_config`）**，且均为 **系统内置（`config_type=1`）**，**不允许删除**；与现有 `SysConfigServiceImpl` 规则一致 |

## 3. 范围与非范围

### 3.1 范围

- 表 `sys_logininfor` 与 Flyway 迁移。
- 监控模块：登录日志分页查询、导出、删除（含批量）、清空、解锁。
- 登录链路：写访问日志；失败计数与锁定；成功清零。
- `sys_config` 种子：登录锁定相关内置参数。
- `quick-ui`：API、路由、列表页、权限、菜单（与现有监控/系统管理风格一致）。

### 3.2 非范围（本期不做）

- 登出访问日志（旧栈若有，可二期对齐）。
- 直接暴露 Redis 命令或 `RedisTemplate` 给业务层。

## 4. 数据模型

### 4.1 `sys_logininfor`

建议字段（与需求列表一致，命名与 MySQL 习惯及现有实体风格对齐）：

| 字段 | 说明 |
|------|------|
| `info_id` | 主键 |
| `user_id` | 可空；成功登录时可写入 |
| `user_name` | 用户名 |
| `ipaddr` | 登录地址（IP） |
| `login_location` | 登录地点（解析结果，可空） |
| `browser` | 浏览器 |
| `os` | 操作系统 |
| `status` | 状态（与字典一致，如 `0`/`1` 成功失败，具体字典类型在实现阶段与 `sys_dict_data` 对齐） |
| `msg` | 描述（失败原因、成功简述等） |
| `login_time` | 访问时间，默认查询按此倒序，索引 |

### 4.2 参数设置（`sys_config`）内置项

以下记录通过 **Flyway** 插入，`config_type = '1'`（系统内置），`del_flag = '0'`。

| config_key | config_name（示例） | config_value（默认示例） | 说明 |
|------------|---------------------|--------------------------|------|
| `qc.login.fail-lock-enabled` | 登录失败锁定开关 | `true` | `true`/`false`；为 `false` 时不做计数与锁定，解锁接口仍为幂等成功 |
| `qc.login.max-retry` | 登录失败锁定阈值 | `5` | 连续失败达到该次数后拒绝登录并写入锁定状态至缓存 |
| `qc.login.lock-minutes` | 登录锁定时长（分钟） | `30` | 锁定持续时间，与缓存内 `lockUntil` 一致 |

**内置约束**（沿用现有参数模块）：

- 不可删除；编辑时仅允许修改 `config_value`（及允许的展示字段），禁止改 `config_key`、`config_name`、`config_type` 的篡改绕过（由 `SysConfigServiceImpl.update` 已强制）。

**备注**：若需「滑动窗口」与「固定窗口」语义，实现阶段在计划中二选一并写死，本设计不强制窗口算法，仅要求行为可测、与 `lock-minutes` 一致。

## 5. 登录失败锁定与 CacheManager

### 5.1 抽象与依赖

- 新增组件（命名以代码为准，例如 `LoginSecurityCacheService` 或 `LoginLockService`）：
  - **仅依赖** `org.springframework.cache.CacheManager`。
  - 通过 `cacheManager.getCache(<cacheName>)` 取得 `Cache`，使用 `get` / `put` / `evict` 维护条目。
- **禁止**：在业务类中注入 `RedisTemplate`、`StringRedisTemplate`、`RedisConnectionFactory` 或调用 Jedis/Lettuce API 存储锁定状态。

### 5.2 缓存分区命名

与项目既有约定一致，使用 **带 TTL 后缀的缓存名**（参见 `DynamicTtlRedisCacheManager`、`SysConfigServiceImpl` 中 `sys-config#3600`）。

- 建议固定一个逻辑分区，例如：`qc-login-fail#604800`（TTL 取「远大于单次锁定最大合理值」的安全上限，如 7 天；**实际锁定结束时间以条目内字段为准**，避免单缓存 TTL 与 `lock-minutes` 不一致导致提前失效时逻辑仍正确）。
- 缓存 **key**：规范化后的 `userName`（与 `AuthLoginService` 中 `trim` 后一致，大小写策略与登录查询一致）。
- 缓存 **value**：可序列化对象（如失败次数、锁定截止时间戳），由 `ObjectMapper` 序列化（与现有 Redis Cache value 序列化方式兼容）。

### 5.3 与 `sys_config` 的配合

- 阈值与时长：`SysConfigService.getConfigValueByKey` 读取（已带 Spring Cache，避免每次打库）。
- `fail-lock-enabled` 为 `false` 时：不递增失败计数、不检查锁定；**解锁**仍执行 `evict`，幂等。

### 5.4 登录流程中的调用点（逻辑顺序）

1. 解析用户名（trim）。
2. 若启用锁定：检查缓存中是否处于锁定窗口内，若是则抛出业务异常（中文提示），并可选写一条失败日志（避免刷日志可合并策略，实现计划定稿）。
3. 执行现有验证码逻辑（若开启）。
4. 调用 `authenticate`；若抛凭据类失败：递增失败计数，若达阈值则写入锁定至缓存，并写失败日志。
5. 成功：`StpUtil.login` 前或后清零该用户失败缓存条目；写成功日志。

### 5.5 解锁接口

- `GET /monitor/logininfor/unlock/{userName}`：对规范化用户名执行 **`Cache.evict`**（及与锁定相关的全部派生 key，若实现中有多 key 则一并清除）；返回统一成功文案（满足需求验收「解锁后有明确成功提示」）。

## 6. 后端：登录日志模块

### 6.1 分层

- 建议包路径：`io.github.genkidoudou.web.monitor.logininfor`（若项目将「监控」挂在 `web.system` 下，则与现有目录对齐，**二选一保持单一职责包**）。
- Controller / Service / Mapper / Entity / QueryBo / Vo 分层与现有 `SysRoleController` 等一致。

### 6.2 接口（对外契约）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/monitor/logininfor/list` | `monitor:logininfor:list` | 分页 + 条件 + 排序 |
| POST | `/monitor/logininfor/export` | `monitor:logininfor:export` | Excel，筛选条件与列表一致 |
| POST | `/monitor/logininfor/remove` | `monitor:logininfor:remove` | 批量 `infoIds`（body 或约定参数，与现网批量删除风格一致） |
| POST | `/monitor/logininfor/clean` | `monitor:logininfor:remove` | 清空 |
| GET | `/monitor/logininfor/unlock/{userName}` | `monitor:logininfor:unlock` | 解锁 |

- 注解：`@Tag`、`@Operation`、`@Parameter`、`@SaCheckPermission`。
- 校验：Jakarta Validation；业务失败使用项目自定义异常，**禁止** `IllegalArgumentException` 作为业务信号。

### 6.3 列表查询

- 条件：登录地址（IP）、用户名、状态、登录时间区间。
- 排序：默认 `login_time` 降序；支持按用户名、访问时间排序（参数名与现有分页 BO 对齐）。

## 7. 前端（`quick-ui`）

- 新增 `api/monitor/logininfor` 对接上述接口。
- 列表页：查询、表格列、多选删除、清空、导出、解锁（对选中行用户名去重后调用解锁）。
- 状态列：字典与 `status` 字段一致（`useDict` 或项目等价方案）。
- 权限：`v-hasPermi` 或项目等价物，与需求四点一致。
- 菜单：Flyway 增加「登录日志」菜单及管理员角色授权（menu_id 与现有迁移错开）。

参数设置页：无需单独改页面逻辑；内置项随种子出现，删除按钮侧已由后端禁止 + 前端可按 `configType` 禁用（与参数设置设计一致）。

## 8. 测试与验收

- 列表筛选、排序、删除、清空、导出非空。
- 连续密码错误达阈值后登录拒绝；解锁后立即可登录（在启用锁定时）。
- 关闭 `qc.login.fail-lock-enabled` 后不计数，解锁仍成功提示。
- 内置参数不可删除；修改 `config_value` 后行为变化符合新值（注意缓存刷新：参数更新已 `@CacheEvict` 全量清 `sys-config#3600`，行为应即时生效于下一次读取）。

## 9. 后续步骤

实现前在单独会话中按 brainstorming 收尾流程执行 **writing-plans**，生成可执行任务清单后再编码。

---

**文档版本**：2026-05-16  
**关联需求**：`原始需求/系统管理/登录日志-需求文档.md`  
**关联规范**：`docs/superpowers/specs/2026-05-13-parameter-settings-design.md`（内置参数规则）、`AGENTS.md`（POST 删除语义、异常与 OpenAPI 注解）
