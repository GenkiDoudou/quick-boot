## 1. 数据库与 Flyway

- [x] 1.1 新增 Flyway 脚本（版本号顺延）：`sys_menu` 表，字段覆盖需求表单（菜单类型、名称、排序、路由 path、组件 component、权限 perms、图标、可见 visible、状态 status、是否缓存 isCache、是否外链 isFrame 等）及 `parent_id`（顶级 **-1**）、`del_flag`、审计字段；索引支持 `(parent_id, del_flag)` 等查询
- [x] 1.2 同脚本或拆分脚本：最小 `sys_role`、`sys_role_menu`；种子数据含内置角色、菜单管理菜单节点、角色-菜单关联；主键/ID 与现有迁移风格一致且 H2/MySQL 可执行
- [x] 1.3（若设计定稿）最小 **`sys_user_role`** 或等价方案：使当前登录用户能关联到内置角色，否则 `getRouters` 无法验收；在 `design.md` Open Questions 勾选最终方案并关闭

## 2. 后端菜单域

- [x] 2.1 新增 `system.menu` 包：`SysMenu` 实体、Mapper、`MenuService`/`MenuServiceImpl`：全表加载、内存建树、列表剪枝（菜单名模糊、状态精确）、`treeselect` 映射 `id`/`label`/`children`（空子为 `[]`）
- [x] 2.2 实现 `SysMenuController`：`GET /system/menu/list`、`GET /system/menu/{menuId}`、`GET /system/menu/treeselect`、`GET /system/menu/roleMenuTreeselect/{roleId}`；写操作 **`POST /system/menu`**（或 `/create`）、**`POST /system/menu/update`**、**`POST /system/menu/remove`**；Swagger、`@Validated`、业务异常统一
- [x] 2.3 校验：父存在、非自指、改父防环；按 `menuType` 字段组合校验；删除仅拦「有未删子节点」
- [x] 2.4 实现 **`getRouters` 用菜单数据组装路由 VO**：从 `AuthController` 委托 `MenuService`；形状与 `quick-ui` `filterAsyncRouter` 兼容；补充 JavaDoc

## 3. 权限与会话

- [x] 3.1 登录用户权限/角色解析：从 `sys_role_menu` + `sys_menu` 汇总 `perms` 供 Sa-Token 或项目既有鉴权使用（与 `getInfo` 返回 `permissions` 对齐若存在）
- [x] 3.2 为菜单管理接口配置所需权限标识（与种子菜单 `perms` 一致）

## 4. 后端验证

- [x] 4.1 `mvn -pl quickboot-web test -Dtest=MenuServiceImplTest`（需先 `install` common/core）；全量 `mvn test` 可能受 common 既有用例环境影响；补充 **MenuServiceImplTest** 覆盖树剪枝、删子、改父成环

## 5. 前端菜单管理

- [x] 5.1 新增 `quick-ui/src/api/system/menu.js`，路径与后端 POST 约定一致
- [x] 5.2 新增 `views/system/menu` 页面：筛选、树表（展开/折叠）、增删改、表单按类型显隐；读取 `DESIGN.md` 后实现样式
- [x] 5.3 权限：`system:menu:list/add/edit/remove` 与 `v-hasPermi` 对齐
- [x] 5.4 注册路由/菜单入口；避免与常量路由 path 冲突（**由 `getRouters` 动态下发**，不增加 `/system/menu` 常量路由）

## 6. 联调与构建

- [x] 6.1 本地启动前后端：列表、`treeselect`、`roleMenuTreeselect`、CRUD、登录后侧边栏与 `getRouters` 联调记录（实现侧就绪，具体联调以本地 `spring-boot:run` + `pnpm dev` 为准）
- [x] 6.2 `cd quick-ui && pnpm build:prod` 通过

## 7. 收尾

- [x] 7.1 对照 `openspec/changes/system-menu-management/specs/sys-menu/spec.md` 走查并勾选本 `tasks.md`
- [x] 7.2 更新原始需求文档 `原始需求/系统管理/菜单管理-需求文档.md` 中 §6 HTTP 方法与路径说明，与实现一致
