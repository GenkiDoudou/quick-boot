## 1. API 与入口

- [x] 1.1 扩展 `src/api/system/role.ts`（get / add / update / changeStatus）
- [x] 1.2 新增 `src/api/system/dept.ts`（list / treeselect / get / add / update / del）
- [x] 1.3 `pages.json` 注册 role/dept 的 index、form 共 4 页
- [x] 1.4 `workbenchMenus.ts` 为角色、部门配置 path

## 2. 角色页面

- [x] 2.1 实现 `pages/system/role/index.vue`（搜索、分页、启停、跳转表单）
- [x] 2.2 实现 `pages/system/role/form.vue`（增改字段与 roleId=1 限制）
- [x] 2.3 `onShow` 刷新；错误 toast；401 走现有 http

## 3. 部门页面

- [x] 3.1 实现 `pages/system/dept/index.vue`（树缩进、过滤、删除确认）
- [x] 3.2 实现 `pages/system/dept/form.vue`（上级选择、精简字段、禁选自身）
- [x] 3.3 保存成功 `navigateBack`；列表刷新

## 4. 验收

- [x] 4.1 重启 `pnpm dev:h5` 后冒烟：工作台入口、角色增改启停、部门树增改删
