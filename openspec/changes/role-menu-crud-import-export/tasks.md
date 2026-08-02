## 1. Menu CRUD 对齐

- [x] 1.1 调整 `SysMenuController`：`POST /add`、`GET remove/{id}`、`POST /remove` 批量删除；保留 list/treeselect/detail/update/updateSort
- [x] 1.2 扩展 `ISysMenuService`/`SysMenuServiceImpl.remove` 支持批量；确认 add/update 显式赋值与按钮类型规范化
- [x] 1.3 同步修改 `quick-ui/src/api/system/menu.js` 与菜单页调用路径

## 2. 角色导入导出（后端）

- [x] 2.1 增加 Role 导出列/导入行 VO（Excel 注解），`export`/`importExcel`/`importTemplate` 服务方法（`roleKey` 判重，对齐 OauthClient）
- [x] 2.2 `SysRoleController` 增加 `/export`、`/import/template`、`/import`
- [x] 2.3 Flyway：`system:role:export`、`system:role:import` 按钮权限并挂超管

## 3. 菜单导入导出（后端）

- [x] 3.1 增加 Menu 导出/导入行 VO；`export`/`importExcel`/`importTemplate`（`menuId` 或 `(parentId,menuName,menuType)` 判重）
- [x] 3.2 `SysMenuController` 增加 `/export`、`/import/template`、`/import`
- [x] 3.3 Flyway：`system:menu:export`、`system:menu:import` 按钮权限并挂超管

## 4. 前端接线

- [x] 4.1 `role.js` + `views/system/role/index.vue`：挂载 `C7JsonTable` 导入导出（权限字符、模板/上传/下载）
- [x] 4.2 `menu.js` + `views/system/menu/index.vue`：工具栏导入导出（复用 `C7ExcelUpload`/`C7ExcelDownload` 或等价实现）

## 5. 验证

- [x] 5.1 `mvn -pl quickboot-system -am compile` 通过
- [ ] 5.2 手工冒烟：Role/Menu 增删改、导出勾选/条件、导入新增/更新/失败明细下载
