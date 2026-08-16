# quick-h5 角色管理 + 部门管理（一期）设计

日期：2026-08-15  
状态：待用户审阅  
范围：工作台「角色」「部门」入口 → H5 常用维护（对接现有 `/sys/role/*`、`/sys/dept/*`）  
前置：用户管理一期 `2026-08-15-quick-h5-user-mgmt-design.md`

## 1. 背景与目标

工作台 mock 已有「角色 / 部门」入口，目前仍为 toast。  
一期补齐与用户管理同档的**常用操作**，复用管理端接口，便于移动端运维。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 能力档位 | A：列表/树 + 新增编辑 + 角色启停 + 部门删除；不做菜单授权、分配用户、导入导出 |
| 部门上级 | 表单含上级部门选择；列表树形缩进展示 |
| 后端策略 | 直连现有 API，不新增 H5 BFF |
| OpenSpec | 单一 change：`quick-h5-role-dept` |
| UI/交互 | 列表页 + 独立表单页；风格对齐用户管理 |

## 3. 信息架构

### 3.1 入口

- 角色 → `/pages/system/role/index`
- 部门 → `/pages/system/dept/index`
- `workbenchMenus.ts` 配置 `path`；`workbench.vue` 沿用 path 跳转

### 3.2 角色

| 路径 | 职责 |
|------|------|
| `pages/system/role/index` | 搜角色名、分页、启停、新增/编辑入口 |
| `pages/system/role/form` | 名称、权限字符、排序、状态、备注 |

### 3.3 部门

| 路径 | 职责 |
|------|------|
| `pages/system/dept/index` | 树列表、按名称过滤、新增/编辑/删除 |
| `pages/system/dept/form` | 上级部门、名称、排序、负责人、电话、状态 |

## 4. 接口约定

### 4.1 角色 `src/api/system/role.ts`

| 方法 | HTTP |
|------|------|
| `pageRole` | `POST /sys/role/page`（已有，可复用） |
| `getRole` | `GET /sys/role/{roleId}` |
| `addRole` | `POST /sys/role/add` |
| `updateRole` | `POST /sys/role/update` |
| `changeRoleStatus` | `POST /sys/role/changeStatus` |

### 4.2 部门 `src/api/system/dept.ts`

| 方法 | HTTP |
|------|------|
| `listDept` | `GET /sys/dept/list` |
| `treeselectDept` | `GET /sys/dept/treeselect` |
| `getDept` | `GET /sys/dept/{id}` |
| `addDept` | `POST /sys/dept/add` |
| `updateDept` | `POST /sys/dept/update` |
| `delDept` | `GET /sys/dept/remove/{id}` |

统一走 `request()`（Bearer、业务码、401 跳登录）。

## 5. 交互与校验

### 5.1 角色

- 必填：`roleName`、`roleKey`；新增 `status` 默认 `'0'`
- `roleId === 1`：禁停用；编辑时权限字符只读
- 不做菜单树、分配用户

### 5.2 部门

- 列表：后端树 `children` → 前端缩进展示（含层级）
- 必填：`deptName`；`parentId` 可选（空=根）
- 删除前 `uni.showModal` 确认
- 上级选择：treeselect 数据扁平化为可选列表（禁用当前节点及其子孙，避免成环——能做则做，否则至少禁选自身）

### 5.3 错误

- toast 业务/网络错误；401 现有逻辑；403 展示文案

### 5.4 开发注意

- 修改 `pages.json` 后必须**重启** `pnpm dev:h5`（uni 不热更新路由表）

## 6. 文件清单

| 文件 | 动作 |
|------|------|
| `src/api/system/role.ts` | 扩展 |
| `src/api/system/dept.ts` | 新增 |
| `src/pages/system/role/index.vue` | 新增 |
| `src/pages/system/role/form.vue` | 新增 |
| `src/pages/system/dept/index.vue` | 新增 |
| `src/pages/system/dept/form.vue` | 新增 |
| `src/pages.json` | 注册 4 页 |
| `src/mock/workbenchMenus.ts` | 角色/部门补 path |

## 7. 验收标准

1. 工作台「角色」「部门」可进入真实页面  
2. 角色：搜索分页、新增、编辑、启停（超管角色除外）  
3. 部门：树展示、带上级新增/编辑、删除  
4. 数据与 quick-ui / 管理端一致  
5. 未登录跳转登录页  

## 8. 非目标与后续

- 本期不做：菜单权限、分配用户、导入导出、部门邮箱/备注（可选后补）
- 二期：角色菜单树勾选；工作台菜单改后端下发
