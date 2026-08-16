## 1. API 与路由入口

- [x] 1.1 新增 `quick-h5/src/api/system/user.ts`（page / get / add / update / changeStatus / resetPwd）
- [x] 1.2 `pages.json` 注册 `pages/system/user/index`、`pages/system/user/form`
- [x] 1.3 `workbenchMenus.ts` 为用户项配置 `path: /pages/system/user/index`
- [x] 1.4 `workbench.vue`：有 `path` 则 `navigateTo`，否则保留 toast

## 2. 列表页

- [x] 2.1 实现 `pages/system/user/index.vue`：搜索、下拉刷新、上拉分页、卡片列表
- [x] 2.2 列表操作：跳转编辑、启停（超管禁停）、重置密码弹层
- [x] 2.3 `onShow` 刷新列表；错误 toast；401 走现有 http 逻辑

## 3. 表单页

- [x] 3.1 实现 `pages/system/user/form.vue`：新增/编辑精简字段校验与提交（新增含角色多选）
- [x] 3.2 编辑回填 `getUser`；更新带回详情 `roleIds`、不传 `deptId`；成功 `navigateBack`

## 4. 验收

- [x] 4.1 代码已就绪：工作台入口、列表分页、表单、启停、重置密码（请本地登录后冒烟对照管理端）
