## Context

用户选择方案 B：以当前仓库已落地的 `SysOauthClient`（非 bak 旧版）为模板，对齐 **SysRole / SysMenu** 的 CRUD 写法，并补齐同步 Excel 导入导出。

现状摘要：

- `SysRole`：`sys/role` 下 page/add/update/remove 已与 OauthClient 接近；`add`/`update` 已显式赋值；缺 export/import 与权限按钮。
- `SysMenu`：路径为 `system/menu`，新增为 `POST /system/menu`（无 `/add`），删除仅单条 `POST /remove/{menuId}`；Service 用 `applyDefaults` 显式默认值；缺 export/import；前端非 `C7JsonTable`。
- 导入导出组件与模式已在 OauthClient 验证：`ExcelUtils` + `C7ExcelDownload` / `C7ExcelUpload` + 可选失败明细。

参考：`docs/superpowers/specs/2026-08-02-oauth-client-export-design.md`、`...-import-design.md`。

## Goals / Non-Goals

**Goals:**

1. Role/Menu Service 新增/修改字段赋值与默认值风格与 OauthClient/Role 一致（Menu 保持/收紧显式赋值，避免 `toEntity` 静默覆盖敏感字段）。
2. Menu Controller 路径与 Role/OauthClient 对齐：`POST .../add`、`GET .../remove/{id}`、`POST .../remove`（批量）。
3. Role/Menu 同步导出、同步导入（模板、updateSupport、失败明细）。
4. Flyway 权限 + 前端接线。

**Non-Goals:**

- 不改菜单树查询语义、排序拖拽、角色授权用户/菜单等扩展能力。
- 导入不重建角色-菜单、用户-角色关联。
- 不做异步导入导出中心。
- 不迁移 bak 旧 OauthClient 包结构。
- 不强制把菜单页改造成 `C7JsonTable`（可保留现树表 UI，仅加导入导出按钮）。

## Decisions

### 1. 参考实现：当前 `SysOauthClient`，不是 bak

- **选择**：以 `SysOauthClientController` / `SysOauthClientServiceImpl` 现契约为准。
- **备选**：严格抄 bak OauthClient（无分页/导入导出）— 与本期目标不符，否决。

### 2. Menu 路径对齐（BREAKING，前后端同发）

| 动作 | 现路径 | 目标路径 |
| --- | --- | --- |
| 新增 | `POST /system/menu` | `POST /system/menu/add` |
| 单删 | `POST /system/menu/remove/{id}` | `GET /system/menu/remove/{id}`（保留 POST 同路径亦可，优先与 Role 一致：GET 单删 + POST 批量） |
| 批删 | 无 | `POST /system/menu/remove` body=`List<Long>` |

- 列表仍用 `GET /system/menu/list`（树场景，不强制改 page）。
- **备选**：保留旧路径加别名 — 本期直接改齐，减少双路径负担。

### 3. 导出规则（对齐 OauthClient）

- 同步 xlsx；行数上限 **5000**。
- Role：有 `ids`（roleId 列表）→ 仅导出勾选；否则按 `roleName`/`roleKey`/`status` 条件。
- Menu：有 `ids`（menuId）→ 仅导出勾选；否则按 `menuName`/`status` 条件（扁平行，不含 children 嵌套列）。
- 0 行仍出表头。

### 4. 导入判重键

- **Role**：业务键 `roleKey`；`updateSupport=true` 时更新可写字段，保留 `roleId`；禁止更新/删除超级角色（`roleId=1`）的破坏性导入（更新允许改名称等，但不可改成删除；若导入行试图「覆盖」超级角色 key 冲突按既有唯一规则处理）。
- **Menu**：优先 `menuId`（模板可选列，导出带回）；无 `menuId` 时用 `(parentId, menuName, menuType)` 在同级判重。`updateSupport=false` 已存在则行失败。按钮类型 `F` 字段清空规则与单条 add 一致。
- **备选**：Menu 只用 path+parentId — path 可空（按钮），不如三元组稳。

### 5. 失败明细形态

- 与当前 OauthClient 导入一致：`ExcelResult` 或等价结果含 `failCount` + `errorFileBase64`（若项目已统一 `writeErrorFile()`，跟 OauthClient 现状走，前后端同一套 `C7ExcelUpload` 约定）。

### 6. 前端挂载

- Role：`C7JsonTable` 的 `exportFunction` / `importFunction` / `importTemplateFn`。
- Menu：工具栏增加「导入」「导出」按钮；导出可勾选行或当前筛选；导入对话框复用 `C7ExcelUpload`。

### 7. RBAC

- Role 父菜单 `2010`：新增 `system:role:export`、`system:role:import`。
- Menu 父菜单 `2020`：新增 `system:menu:export`、`system:menu:import`。
- 挂超级管理员角色，模式同 OauthClient V3/V4。

## Risks / Trade-offs

- [菜单路径 BREAKING] → 同 PR 改 `menu.js` + 全局搜旧 URL；编译/冒烟必测新增删除。
- [菜单树导入顺序] → 要求父先于子，或同批内按 parentId 拓扑；无法解析父节点的行记失败。
- [误导人批量改权限菜单] → 导入权限单独 `import`；超级角色保护与删除校验保持。
- [菜单页非 JsonTable] → 导出「勾选」依赖树表 selection；若无 selection 则仅按筛选条件导出。

## Migration Plan

1. 合并 Flyway 权限脚本后重启。
2. 前后端同发 Menu 路径变更。
3. 回滚：撤回权限菜单行 + 恢复旧 Controller 映射（低风险）。

## Open Questions

- 无（默认：Menu 路径直接对齐；导入判重键如上；菜单页不强制 JsonTable）。
