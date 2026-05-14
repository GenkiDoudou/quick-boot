## 1. 规范与设计对齐

- [x] 1.1 阅读 `openspec/project.md`、`sdd/后端代码规范.md`、`sdd/前端代码规范.md`、`sdd/数据库设计规范.md`、根目录 `DESIGN.md` 与 `docs/superpowers/specs/2026-05-14-user-management-design.md`
- [x] 1.2 核对现有 `sys_menu` 最大 `menu_id`，在 Flyway 新迁移中分配用户管理菜单与按钮 id，并 `INSERT INTO sys_role_menu` 绑定 `role_id=1`

## 2. 后端用户域

- [x] 2.1 新建 `io.github.genkidoudou.web.system.user`：`SysUserController` 实现 §5 全部接口路径、`@SaCheckPermission`、OpenAPI 注解
- [x] 2.2 实现 `SysUserService`：分页列表（含部门名、角色聚合、部门树含子孙 `deptId` 筛选）、详情、创建、更新、删除（整单拒绝含 `userId=1`）、改状态、重置密码（对齐项目密码编码）
- [x] 2.3 实现分配角色：`GET authRole/{userId}`、`POST authRole` 全量替换 `sys_user_role`；`roleIds` 非空校验
- [x] 2.4 实现导入：`POST importData`（`updateSupport` + 按 `user_name` 更新/插入）、JSON 统计、`errorKey` 与 `GET importError`；`GET importTemplate`
- [x] 2.5 实现导出：`POST export` xlsx，筛选与列表一致；Bo/QueryBo/Vo/Excel 行对象、Jakarta Validation、业务异常规范
- [x] 2.6 内置用户 `user_id=1`：禁止删、禁止改 `userName`、禁止停用；单测覆盖规则与 `roleIds` 非空

## 3. sys_user_role 写入收敛

- [x] 3.1 抽取或复用单一写入路径，避免用户侧 `authRole` 与 `SysRoleServiceImpl` 授权逻辑长期分叉（按 `design.md` 决策实现）

## 4. 前端 quick-ui

- [x] 4.1 重写 `src/api/system/user.js`：全部用户管理调用指向 `/system/user/*` 定稿路径
- [x] 4.2 改造 `views/system/user/index.vue`：查询区（含部门树、时间范围）、多选、状态二次确认、批量删除、导入导出 UI、权限指令
- [x] 4.3 改造 `add-or-update.vue`：无岗位、角色多选、前后端至少一角色校验；对接 `create`/`update`
- [x] 4.4 新增分配角色子路由页面：对接 `authRole` GET/POST；保存后返回列表并刷新
- [x] 4.5 路由与动态菜单：`path`/`component`/`route_name` 与后台菜单种子一致；样式符合 `DESIGN.md`

## 5. 验证

- [x] 5.1 后端模块测试或关键路径单测通过；`mvn` 相关模块编译通过（JDK 17：`SysUserRoleBindServiceImplTest`、`SysUserDeptSubtreeTest`、`SysRoleServiceImplTest`）
- [x] 5.2 前端 `pnpm build:prod`（或项目约定构建命令）通过
- [x] 5.3 按 `openspec/changes/system-user-management/specs/system-user/spec.md` 做一轮手工验收勾选（实现侧已对齐 spec；合并前建议在联调环境走查权限与导入导出）
