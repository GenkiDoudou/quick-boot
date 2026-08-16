# quick-h5 菜单与按钮权限可配置（一期）设计

日期：2026-08-16  
状态：已实现（OpenSpec `quick-h5-menu-perm`）  
范围：工作台菜单按角色下发；页面按钮按权限显隐  
前置：用户 / 角色 / 部门 H5 一期；后端已有 `/auth/me`（含 `permissions`/`roles`）、`sys_menu` + 角色菜单授权、`/getRouters`

## 1. 背景与目标

当前 H5：

- 工作台菜单来自 `mock/workbenchMenus.ts`，与角色无关；
- 登录只持久化 token/用户名，**未落库** `permissions`；
- 列表「新增 / 编辑 / 启停 / 删除」等对所有登录用户可见。

目标（一期）：

1. **菜单可配置化**：工作台入口由后台菜单 + 角色授权决定显隐与跳转；
2. **按钮可配置化**：页面操作按 `perms` 显隐，与 PC `v-hasPermi` 同一套权限字符。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 菜单数据源 | 复用 `sys_menu` + 角色授权，不新建 H5 菜单表 |
| 与 PC 关系 | 同一菜单树；一期用 **约定标记** 区分 H5 节点（见 §4），二期再加「端类型 / H5 path」管理端字段 |
| 按钮模型 | 对齐 PC：绑 `perms`（如 `system:user:add`），无权限则隐藏 |
| 一期边界 | 接 `/auth/me` 权限 + H5 工作台菜单接口；前端 `hasPermi`；不做通用「按钮文案/顺序配置台」 |
| 无权限交互 | 隐藏入口与按钮（不 toast、不灰态） |
| 后端闸门 | 接口仍靠 `@SaCheckPermission`；前端显隐仅体验层 |

## 3. 信息架构

```text
登录成功 / App 启动已登录
  → GET /auth/me
  → store.roles / store.permissions
  → GET /sys/menu/h5Workbench（或等价）
  → 工作台按分组渲染

业务页（用户/角色/部门…）
  → hasPermi(['system:xxx:add']) 控制新增/编辑/启停/删除等
```

### 3.1 工作台

- 数据：后台下发的分组 + 入口项（不再以 mock 为权威；可保留 mock 作离线兜底开关，默认关）。
- 点击：有 `path` 则 `uni.navigateTo`；无 path 不展示（或仅目录不展示为格子）。

### 3.2 按钮

| 页面 | 建议绑定 perms（与现网 PC 一致） |
|------|----------------------------------|
| 用户列表 | `system:user:add` / `edit` / `edit`（启停）/ `resetPwd` |
| 角色列表 | `system:role:add` / `edit` / `edit`（启停） |
| 部门列表 | `system:dept:add` / `edit` / `remove` |
| 表单保存 | 与对应 add/edit 一致 |

超管例外（如 `userId===1` 禁停用）**保留**，与权限指令叠加（有权限也不允许违例操作）。

## 4. 菜单约定（一期，无新表字段）

在**同一** `sys_menu` 树上维护 H5 入口，用约定区分 PC 动态路由节点：

| 约定 | 说明 |
|------|------|
| H5 菜单 `path` | 必须以 `/pages/` 开头，且为 uni 页面路径（如 `/pages/system/user/index`） |
| 分组 | `menu_type = M`（目录）→ 工作台分组标题 |
| 入口 | `menu_type = C`（菜单）且 path 符合上式 → 九宫格项 |
| 按钮 | `menu_type = F` → **不出现在工作台**，只贡献 `perms` 给 `/auth/me` |
| 排序 | `order_num`；隐藏/停用（`visible`/`status`）不返回 |
| 展示文案 | `menu_name`；可选：`icon` 首字或固定映射生成 `short`/`tone`（前端兜底） |

角色授权：与 PC 相同，在「角色 → 菜单」勾选 H5 目录/菜单/按钮即可。未勾选则该用户工作台不出现对应入口，也无对应按钮权限。

> 二期（可选）：表字段 `client_type`（`pc`/`h5`/`all`）或独立 `h5_path`，管理端可视化维护；一期不做 UI 改造强制项，可用 SQL/菜单管理手工录入符合约定的节点。

### 4.0 实际接口路径

后端菜单管理现网前缀为 **`/system/menu`**（非 `/sys/menu`）。一期工作台接口为：

`GET /system/menu/h5Workbench`

### 4.1 建议初始化菜单结构（示例）

```text
移动端工作台 (M)
├── 系统管理 (M)
│   ├── 用户 (C) path=/pages/system/user/index
│   ├── 部门 (C) path=/pages/system/dept/index
│   └── 角色 (C) path=/pages/system/role/index
│       └── （F）沿用已有 system:user:* / system:dept:* / system:role:* 按钮节点挂在对应 C 下或复用 PC 树已有 F
└── …（后续监控等）
```

说明：若 PC 树下已有 `system:user:add` 等 F 节点，**优先复用**，不必为 H5 再造一套 perms；H5 只需增加/挂载带 `/pages/` 的 C 节点供工作台展示。

## 5. 接口约定

### 5.1 已有：`GET /auth/me`

返回已含 `roles`、`permissions`。H5 登录与启动恢复会话后必须写入 store。

### 5.2 新增（推荐）：`GET /sys/menu/h5Workbench`

| 项 | 说明 |
|----|------|
| 鉴权 | 登录用户 |
| 逻辑 | 按当前用户角色取可见菜单 → 过滤 H5 约定节点 → 组装分组树 |
| 出参示例 | 见下 |

```json
{
  "code": 0,
  "data": [
    {
      "id": "115xxx",
      "title": "系统管理",
      "items": [
        {
          "id": "115yyy",
          "label": "用户",
          "path": "/pages/system/user/index",
          "icon": "user",
          "orderNum": 1
        }
      ]
    }
  ]
}
```

不采用直接复用 `/getRouters` 的原因：其 Map 结构面向 Vue Router（`component`/`name`/`meta`），与工作台九宫格契约不匹配；单独轻量接口更清晰。实现上可复用 `ISysPermissionService` / 菜单查询能力，避免平行造权限计算。

### 5.3 前端封装

| 文件 | 职责 |
|------|------|
| `stores/user.ts` | 增加 `roles`、`permissions`；login / 启动拉 me；logout 清空 |
| `utils/permission.ts` 或 `composables/usePermission.ts` | `hasPermi(codes)`、`hasRole(roles)`（`*:*:*` 超权与 PC 一致） |
| `api/system/menu.ts` | `fetchH5Workbench()` |
| `pages/workbench/workbench.vue` | 调接口渲染；去掉对 mock 的强依赖 |
| 业务页 | 操作按钮包 `v-if="hasPermi([...])"` |

uni-app 慎用「DOM remove」指令（与 PC `v-hasPermi` 实现细节不同），一期优先 **`v-if` + 工具函数**；若要做指令，仅作语法糖且基于 `v-if` 等价行为。

## 6. 交互与边界

- 工作台加载失败：toast + 空态；不回退为「全量 mock」（避免无权限用户看到假入口）。
- 会话恢复：`hydrateFromStorage` 后若有 token，须再 `fetchMe`（及可选拉工作台），否则 permissions 为空导致按钮全隐。
- 角色变更后：下次进工作台 / 重新登录生效即可；一期不做权限推送刷新。
- 页面未在 `pages.json` 注册：navigate 失败属配置问题；菜单 path 须与 `pages.json` 一致。

## 7. 文件清单（一期）

| 位置 | 动作 |
|------|------|
| `quick-h5/src/stores/user.ts` | 存 roles/permissions；拉 me |
| `quick-h5/src/utils/permission.ts`（或 composable） | hasPermi / hasRole |
| `quick-h5/src/api/system/menu.ts` | h5Workbench |
| `quick-h5/src/pages/workbench/workbench.vue` | 接 API |
| `quick-h5/src/pages/system/**` | 按钮按 perms 显隐 |
| `quickboot-module-system` | `h5Workbench` API + Service 组装 |
| Flyway/SQL（可选） | 初始化「移动端工作台」目录与用户/部门/角色 C 节点；F 复用已有 |
| `mock/workbenchMenus.ts` | 降级为可选 mock，非默认 |

## 8. 验收标准

1. 角色 A 仅勾选「用户」H5 菜单 → 工作台只见用户；角色 B 勾选用户+部门 → 两者都见。  
2. 无 `system:user:add` 时用户页无「新增」；有则可见。  
3. 超管 `*:*:*` 见全部已配置 H5 入口与按钮（仍受超管禁停用等业务规则约束）。  
4. 未登录 / 401 仍跳登录；403 接口仍 toast。  
5. PC `/getRouters` 与菜单管理行为不被破坏。

## 9. 非目标（一期不做）

- 管理端「端类型」表单字段与可视化双 path（二期）  
- 按钮文案/顺序后台可配  
- 小程序端独立菜单体系  
- 权限变更实时推送  
- 工作台图标库完整上传（可用 icon 字段或 short 兜底）

## 10. 风险与缓解

| 风险 | 缓解 |
|------|------|
| PC/H5 同树误配 path | 文档约定 + h5Workbench 严格过滤 `/pages/`；PC buildRouters 忽略非法 H5 path 或仅处理已知 component |
| 漏配 F 节点导致按钮全隐 | 初始化 SQL 挂齐常用 F；验收清单覆盖 |
| 仅存 token 未拉 me | App 启动有 token 强制 fetchMe |

## 11. 推荐实现顺序

1. Store + hasPermi + 业务页按钮（可先不改菜单，立即见效）  
2. 后端 h5Workbench + 初始化菜单数据  
3. 工作台接 API  
4. 冒烟：两角色交叉授权
