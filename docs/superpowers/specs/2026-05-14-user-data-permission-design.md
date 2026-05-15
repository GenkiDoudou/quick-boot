# 用户管理数据权限设计（DataPermissionInterceptor + 角色 dataScope）

## 1. 背景与目标

系统已具备角色 **`data_scope`**（1 全部、2 自定义、3 本部门、4 本部门及以下、5 仅本人）及 **`sys_role_dept`**（自定义部门绑定）。用户列表当前仅按请求参数中的部门树筛选，**未**按登录者角色数据权限自动收紧。

**目标**：在用户管理模块内，对所有**读库**访问 `sys_user` 的路径统一施加与角色配置一致的数据范围；实现方式以 **MyBatis-Plus `DataPermissionInterceptor`** 为主，语义与表结构对齐 **若依 RuoYi-Vue3**（后端 `@DataScope` / 角色 `data_scope` 的常见行为），用拦截器统一注入 SQL 条件，避免业务层重复拼条件。

> **说明**：本仓库当前未检出 `原始需求/RuoYi-Vue3` 目录；实现时以外部若依工程为对照，核对 `data_scope` 取值与「多角色 OR 合并」行为即可。

## 2. 已确认的决策摘要

| 项 | 选择 | 说明 |
|----|------|------|
| 多角色合并 | **A：取最宽（并集）** | 任一角色为「全部」则整体不追加部门/本人限制；否则各角色产生的可见集合 **OR** 合并。 |
| 作用范围 | **B：用户模块内所有读 `sys_user` 的接口** | 含分页、详情、导出、分配角色页加载的用户主数据等；凡通过 Mapper 读 `sys_user` 单表或主表为 `sys_user` 的查询均需纳入。 |
| 超级管理员 | **B：不绕过数据权限** | 与业务账号一致，仍受角色 `data_scope` 约束（与若依默认「admin 看全部」不同，以本需求为准）。 |
| 部门必填 | **不允许没有部门** | 业务规则：`sys_user.dept_id` 必填；创建/修改/导入等写入路径须校验；存量或登录用户若缺失部门则按统一错误策略处理（见 §7）。 |
| 与前端部门筛选 | **A：AND** | 数据权限为**基线**；列表/导出请求中的 **`deptId`（含子孙）** 在基线结果上进一步 **AND** 收窄。 |

## 3. 与若依（RuoYi）的对照与映射

| 若依常见做法 | 本项目映射 |
|--------------|------------|
| 角色 `data_scope` 五种取值 | 沿用现有 `SysRole.dataScope` 与 `sys_role_dept`。 |
| 切面 / `@DataScope` 改 SQL 或拼 `WHERE` | 使用 **`DataPermissionInterceptor` + `DataPermissionHandler`（或 `MultiDataPermissionHandler`）** 在编译期/运行期为带注解的 Mapper 方法追加片段。 |
| 自定义部门：`sys_user` 与 `sys_role` / `sys_role_dept` 联查或子查询 | Handler 内查询当前用户关联角色的部门集合，生成 `dept_id IN (...)` 或子查询；保持与若依「自定义 = 角色绑定部门列表」一致。 |
| 多角色 OR | 与决策 **§2 多角色 A** 一致。 |

## 4. 范围：用户模块内需受控的读路径（初版清单）

以下凡最终落到 **`SysUserMapper` / `BaseMapper<SysUser>` 的 `select*`**（含 `selectById`、`selectOne`、`selectList`、`selectPage`、`selectCount` 等）均应命中数据权限（通过注解键或 Mapper 自定义方法统一控制，避免遗漏）：

- 分页列表 `page`
- 详情 `get`
- 导出 `export`
- 分配角色页 `authRoleInfo` 中对目标用户的读取
- 修改前加载 `update`、状态/密码/删除等路径中的 **`selectById` / `selectOne`**（读他人用户以判断存在性时，同样必须落在数据权限内，否则存在越权）

**明确不在本设计「数据权限 SQL 注入」内、但需业务校验的**：仅读 **`sys_role` / `sys_dept` / `sys_user_role`** 等辅助表时，仍按原有权限注解；若某接口仅改关系不读 `sys_user`，可不在此表上加注解。跨表「列表展示」若未来出现 `sys_user` JOIN 他表，需单独约定表别名与占位符。

## 5. 数据语义（与现有字段一致）

- **1 全部**：不追加数据权限条件（或等价恒真）。
- **2 自定义**：`dept_id IN (当前用户所拥有角色中，所有 `data_scope=2` 角色在 `sys_role_dept` 中的部门 id 并集)`。
- **3 本部门**：`dept_id = 当前登录用户 dept_id`。
- **4 本部门及以下**：`dept_id IN (以当前用户部门为根的子树)`；部门树算法与现有 `SysUserServiceImpl` 中按 `deptId` 筛选的子孙集合逻辑保持一致或可抽取为共享组件供 Handler 调用。
- **5 仅本人**：`user_id = 当前登录用户 id`。

**多角色（并集）**：对每个角色分别计算上述集合，再 **OR**；若其中任一角色为 **1 全部**，整体不再追加限制。

## 6. 技术方案要点

### 6.1 拦截器注册

- 在现有 `MybatisPlusInterceptor` Bean 中 **增加** `DataPermissionInterceptor`，**顺序置于 `PaginationInnerInterceptor` 之前**，避免先分页再过滤导致页内数据错误。
- Db 方言与现网一致（H2 MySQL 模式 / 生产 MySQL）；数据权限片段使用与分页方言兼容的写法。

### 6.2 Handler 职责

- 从 **登录上下文**（Sa-Token：`StpUtil` / 既有封装）读取 **`userId`、`deptId`**。
- 查询当前用户关联角色及 `data_scope`；对 `data_scope=2` 批量读取 `sys_role_dept`。
- 合并得到 SQL 片段（或 `Expression`），与 `@DataPermission` 中配置的占位符对应；**表名/列名**与 `sys_user` 实体一致（注意 MP 生成 SQL 时主表别名，统一为文档中约定写法，如主表无别名则用 `sys_user.dept_id`）。

### 6.3 Mapper 标注策略

- 优先在 **`SysUserMapper`** 上为「受控读」定义显式方法（或在 XML 中统一 `WHERE` 占位）并加 **`@DataPermission`**，避免仅依赖 `BaseMapper` 默认方法导致注解无法挂载。
- 对仍使用 `Wrappers.lambdaQuery()` 的代码路径，评估改为调用带注解的 Mapper 方法，或在 Service 层改为走统一封装查询，**禁止**出现绕过 Mapper、直连 `JdbcTemplate` 读用户列表的旁路（除非同样加等价条件）。

### 6.4 与列表查询条件 AND（决策 §2-5）

- Service 层在构建 `LambdaQueryWrapper` 时保留现有用户名、状态、`deptId` 树等条件。
- 数据权限由拦截器追加为 **同一 `WHERE` 的 AND 组**；前端传入的 `deptId` 不得放宽数据权限基线。

## 7. 部门必填（`dept_id`）

- **写入**：`create` / `update` / `import` 等路径 **`dept_id` 非空校验**；非法则 `WarningException` 等统一业务错误。
- **读取**：登录用户若缺失 `dept_id`，对依赖部门的 `data_scope`（2/3/4）无法计算合法集合时，约定为 **可见集合为空**（如 `1 = 0`）或统一返回「用户未分配部门」类错误；实现阶段二选一并在本文件定稿句中写死一种（推荐：**业务错误 + 禁止进入用户管理写操作**，列表只返回空集易误导，可结合产品选择）。

## 8. 超级管理员不绕过（决策 §2-3）

- 不根据「是否 admin 角色」跳过 `DataPermissionInterceptor`。
- 若需超级管理员具备「全部数据」，应通过为其分配 **`data_scope=1`** 的角色实现，而非代码特判。

## 9. 测试建议

- **Handler 单测**：固定用户、多角色、`sys_role_dept`、部门树，断言生成片段或解析后的 id 集合。
- **接口层**：分页 / 详情 / 导出 / 分配角色页，使用不同 `data_scope` 账号请求，断言无越权 `userId`。
- **回归**：`deptId` 查询参数与数据权限 **AND** 后条数单调不增。

## 10. 风险与后续

- **性能**：每次查询加载角色与部门树可能增加开销；可对「当前用户数据权限片段」做 **请求级缓存**（ThreadLocal 或短期缓存 key=userId）。
- **扩展**：部门管理、岗位等若需同类数据权限，可复用 Handler 内核，按表配置不同 `@DataPermission` 键。

## 11. 自检

- 无 TBD 占位；与决策表无矛盾。
- 范围 B 已落到具体 Mapper 读路径原则。
- 与若依差异点（超级管理员、仓库无 RuoYi 目录）已写明。

---

**请审阅本文档**；确认无修改后，再进入 **writing-plans** 产出实现任务清单与拆分步骤。
