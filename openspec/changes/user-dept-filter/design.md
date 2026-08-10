## Context

用户与部门已独立落地：部门有完整 CRUD；用户表单可绑 `deptId`，分页/导出对 `deptId` 为精确 `eq`。产品要求用户页搜索按部门筛，且含下级。权威产品设计见 `docs/superpowers/specs/2026-08-08-user-dept-filter-design.md`。

约束：`sys_dept` 仅有 `parentId`（无 `ancestors`）；`C7JsonTable` 搜索已支持 `type: 'slot'`；`SysUserServiceImpl` 已注入 `SysDeptMapper`。

## Goals / Non-Goals

**Goals:**

- 用户页搜索「归属部门」树选，清空/重置后不加部门条件。
- `page` 与导出在传入 `deptId` 时查询本部门及全部子孙下用户，语义一致。
- 改动面最小：用户页 + `SysUserServiceImpl`。

**Non-Goals:**

- 左树右表、合并部门页、`ancestors`、数据权限、新 API/权限码、改 `C7JsonTable` 内置类型。

## Decisions

1. **前端用搜索 slot + `el-tree-select`，不扩展 C7JsonTable**  
   - 备选：公共组件加 `tree-select` 类型 → 本页单次使用，slot 足够。

2. **后端内存展开子部门 ID 后 `IN`，不加 `ancestors`**  
   - 备选：DDL + 路径查询 → 对本需求过重。  
   - 实现：`resolveDeptIdsIncludingChildren(deptId)`，`page` 与 `listForExport` 共用；拉全表 `deptId/parentId` 建树后 BFS/DFS。  
   - 停用子部门 ID 仍纳入；未知 `deptId` → `IN (自身)`，结果可为空。

3. **契约仍传单个 `deptId`**  
   - 备选：客户端传 `deptIds[]` → 增加前后端约定成本，无收益。

## Risks / Trade-offs

- [语义 BREAKING] 原 `eq` 变为含下级 → 此前搜索未暴露 `deptId`，影响面小；文档标明。  
- [部门树很大时每次筛选扫全表部门] → 组织规模通常可控；后续可加缓存/`ancestors`。  
- [列表与导出逻辑分叉] → 强制共用解析方法。

## Migration Plan

- 无 DDL；发版即生效。  
- 回滚：恢复 `eq` 并去掉前端搜索项即可。

## Open Questions

- 无（产品决策已定稿）。
