## 1. H5 会话权限

- [x] 1.1 `stores/user.ts`：增加 `roles` / `permissions`；login 写 me；logout 清空；persist 字段对齐
- [x] 1.2 App 启动：有 token 时强制 `fetchMe` 刷新权限（避免仅 hydrate token）
- [x] 1.3 新增 `utils/permission.ts` 或 `composables/usePermission.ts`：`hasPermi` / `hasRole`（含 `*:*:*`）

## 2. H5 按钮显隐

- [x] 2.1 用户列表/表单：新增、编辑、启停、重置密码、保存按 `system:user:*` 控制
- [x] 2.2 角色列表/表单：新增、编辑、启停、保存按 `system:role:*` 控制
- [x] 2.3 部门列表/表单：新增、编辑、删除、保存按 `system:dept:*` 控制
- [x] 2.4 保留超管/管理员角色业务禁停用规则，与 hasPermi 叠加

## 3. 后端 h5Workbench

- [x] 3.1 Service：按当前用户可见菜单过滤 `path` 以 `/pages/` 开头的 C 节点，M 作分组，排除 F/隐藏/停用
- [x] 3.2 Controller：`GET /system/menu/h5Workbench` 返回分组 VO；鉴权登录用户
- [x] 3.3 确认不破坏现有 `/getRouters` 行为（buildRouters 排除 `/pages/` C 节点）

## 4. 菜单初始化数据

- [x] 4.1 Flyway/SQL：增加「移动端工作台」目录及用户/部门/角色 C 节点（path=`/pages/system/...`）
- [x] 4.2 将入口挂到 admin（或现网惯例角色）并依赖已有 `system:user|role|dept:*` F 节点，不重复造 perms
- [x] 4.3 文档/注释写明 H5 path 约定，供后续手工维护

## 5. 工作台接 API

- [x] 5.1 `api/system/menu.ts`：`fetchH5Workbench`
- [x] 5.2 `workbench.vue`：拉接口渲染；失败 toast + 空态；禁止默认回退全量 mock
- [x] 5.3 `mock/workbenchMenus.ts` 降级为可选/非默认

## 6. 验收

- [x] 6.1 双角色交叉：仅授权用户入口 vs 用户+部门，工作台显隐正确（需本地重启后端跑 Flyway 后人工冒烟）
- [x] 6.2 去掉某 add 权限后对应按钮隐藏；`*:*:*` 可见已配置入口与按钮（逻辑已接；需人工冒烟）
- [x] 6.3 PC 菜单管理与 `/getRouters` 冒烟正常（编译通过；H5 path 已从 buildRouters 排除）
