## Context

- 原始需求与已定稿 Superpowers 设计见 `docs/superpowers/specs/2026-05-08-dept-management-design.md`；本变更的 OpenSpec **proposal** 已界定范围与 **BREAKING**（`list` 树形 `data`）。
- 后端当前仅有占位迁移与 `AuthController`；前端存在 `quick-ui/src/api/system/dept.js`，尚无部门管理页面实体逻辑。
- 统一响应为 `R`（`code`/`msg`/`data`），前端 axios 拦截器 resolve 整段 `res.data`，列表页应读取 **`data`** 作为树数据。

## Goals / Non-Goals

**Goals:**

- Flyway 创建 **`sys_dept`**，字段与主键策略与项目现有 MyBatis-Plus / 数据库方言约定一致（H2 dev + MySQL prod 可执行）。
- 实现与设计一致的 HTTP 接口，并与 **`dept.js`** 路径对齐；`list` 返回嵌套树及剪枝筛选语义；`treeselect` 供表单树选。
- 前端部门管理单页：筛选 + 树表 + 增删改查/查看；字典 **`sys_normal_disable`**；删除展示后端 `msg`。

**Non-Goals:**

- 本变更**不**建 **`sys_user`**，**不**在删除接口中校验「部门下仍有用户」（后续独立变更在 `DELETE` 中增加计数校验即可）。
- 不在此变更中归档入主 spec 或做跨模块大重构。

## Decisions

| 决策 | 选项 | 理由 |
|------|------|------|
| 列表载荷形态 | **嵌套树**（根数组 + `children`） | 已定稿；表格直接 `tree-props`，避免前端重复建树逻辑。 |
| `children` 空节点 | **空数组 `[]`** | 与 Element Plus 表格树习惯一致，减少 null 分支。 |
| `treeselect` 与列表筛选解耦 | **首版 `treeselect` 全量树、忽略 list 的筛选 query** | 避免下拉父级时受列表搜索条件影响导致「选不到父节点」；与设计 §4.2 一致。 |
| 顶级 `parent_id` | **`-1`** | 与原始需求及已定稿设计一致。 |
| 删除校验 | **仅子部门存在则拒绝** | 用户表未就绪，避免伪实现。 |
| 权限 | 与 `system/user` 类似 **`v-hasPermi` + 路由 meta** | 保持 quick-ui 既有模式；具体标识在 **tasks** 列清单。 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 与若依习惯「扁平 list」不一致，其它代码误用 `listDept` 当扁平 | proposal 已标 **BREAKING**；实现阶段全仓检索 `listDept`/`/system/dept/list` 并仅在新页面按树解析。 |
| 剪枝树算法边界复杂 | 单测覆盖「仅命中叶子」「多根」「无匹配空数组」；与设计条文对照验收。 |
| Flyway 与 H2/MySQL 类型差异 | 迁移脚本在本地双跑（`mvn` + 配置 profile）；必要时使用两方言兼容类型。 |

## Migration Plan

1. 部署时执行 Flyway 新版本，仅新增 `sys_dept`（**无**数据回填硬性要求；可选种子根部门便于演示）。
2. 回滚：在开发环境使用 Flyway repair/undo 策略按团队规范执行；生产回滚需独立 DDL 评估（本表为空时可直接 `DROP`）。

## Open Questions

- **`del_flag` 取值**：若仓库后续用户模块采用若依惯例（`0` 正常 `2` 删除），本表应对齐；实现前核对是否已有枚举常量，无则在代码层集中常量并写 JavaDoc。
