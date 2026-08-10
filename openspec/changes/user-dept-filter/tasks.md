## 1. Backend — dept ID expand + query

- [x] 1.1 在 `SysUserServiceImpl` 增加 `resolveDeptIdsIncludingChildren(Long deptId)`：查部门 `deptId/parentId`，收集自身及全部子孙（含停用节点）
- [x] 1.2 将 `page` 中 `deptId` 的 `eq` 改为对展开 ID 列表的 `in`；`deptId == null` 不加条件
- [x] 1.3 `listForExport` 同样改用该方法，保证与分页范围一致
- [x] 1.4（建议）为展开逻辑补充单测：根含全部、叶子仅自身、未知 id 仅含自身

## 2. Frontend — user search

- [x] 2.1 `user/index.vue`：`defaultSearch` / `searchColumns` 增加 `deptId`（`type: 'slot'`，标签「归属部门」）
- [x] 2.2 搜索 slot 挂载 `el-tree-select`（`deptTree`、`check-strictly`、`clearable`），绑定 `form-data.deptId`
- [x] 2.3 确认重置清空 `deptId`；表单归属部门与部门管理页未误改

## 3. Verification

- [ ] 3.1 手工：叶子部门 / 含下级 / 清空重置；带部门条件导出与列表一致
- [x] 3.2 对照 `specs/sys-user-dept-filter/spec.md` 场景勾选通过（代码与单测覆盖；UI 冒烟见 3.1）
