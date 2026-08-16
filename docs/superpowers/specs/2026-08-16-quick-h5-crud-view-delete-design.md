# quick-h5 列表查看与删除补齐

日期：2026-08-16  
状态：已批准（OpenSpec `quick-h5-crud-view-delete`）  
范围：`quick-h5/src/pages/system/**`、`pages/monitor/**`  
参照：现网 remove / get 接口；已有 operlog / jobLog / slowSql 详情页

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 查看形态 | B：复用现有 `form`，增加 `mode=view`（只读、隐藏保存） |
| 删除范围 | B：用户、角色、参数、客户端、定时任务统一确认弹窗 + `*:remove`（参数已有删除则核对） |
| 查看覆盖 | B：system + monitor 列表均提供查看入口；已有 `detail.vue` 保留现链路 |
| 查看权限 | A：`hasPermi([*:query, *:list])`（OR） |

## 2. 目标与非目标

**目标**

- 五类模块删除体验一致：二次确认、权限码、关键禁删规则。
- 业务列表可进入只读查看；表单页以 `mode=view` 禁用编辑。
- 无表单的监控/文件等补轻量只读页；已有详情页不强制改造。

**非目标**

- 批量多选删除、回收站、新后端权限码。
- 定时任务 Cron 编辑。
- 把已有 `detail.vue` 全部改成 `mode=view`。

## 3. 删除

| 模块 | H5 行为 | 权限 | 禁删 |
|------|---------|------|------|
| 用户 | 列表「删除」→ confirm → remove API | `system:user:remove` | `userId=1` |
| 角色 | 同上 | `system:role:remove` | `roleId=1` |
| 参数 | 已有：确认文案与内置禁删核对 | `system:config:remove` | `configType=1` |
| OAuth 客户端 | 新增删除 | `system:oauthClient:remove` | — |
| 定时任务 | 新增删除 | `monitor:job:remove` | — |

- API：对接现网 `POST .../remove` 或既有 GET remove；`api/` 缺封装则补齐。
- 成功后刷新列表（`load(true)` / 等价）。

## 4. 查看（`mode=view`）

### 4.1 有 form 的模块

用户、角色、部门、参数、字典类型/数据、OAuth 客户端、文件分类：

- 列表增加「查看」→ `form?...&mode=view`
- `onLoad` 解析 `mode`；`isView` 时：
  - 导航栏标题「查看…」
  - 输入 / 芯片 / picker 全部 `disabled`
  - 隐藏保存；可选「返回」
  - 仍调用现有 get 详情接口

### 4.2 已有 detail 的模块

操作日志、调度日志、慢 SQL：保留现有「详情」→ `detail.vue`，本期不改。

### 4.3 无 form 需补只读页

| 模块 | 方案 |
|------|------|
| 定时任务 | 新建 `job/form.vue`（或 `detail.vue`）：只读展示名称/组/Cron/调用目标/状态；列表可「查看」；无 Cron 编辑 |
| 登录日志 | 新建只读页（字段：用户/IP/地点/浏览器/状态/时间/消息） |
| 在线用户 | 新建只读页（用户/部门/IP/浏览器/登录时间等）；强退仍在列表 |
| 文件 | 新建只读页展示元数据；预览/下载动作可保留在列表或详情内 |

权限：查看按钮 `hasPermi(['模块:query','模块:list'])`。

## 5. 验收标准

1. 用户/角色/客户端/任务：有 remove 权限时可删；禁删对象有提示且不请求。  
2. 参数：内置仍不可删；非内置确认后可删。  
3. 上述 CRUD 列表：有 query/list 时可进 `mode=view`，无法保存。  
4. system/monitor 其余列表均有查看入口（detail 或只读 form）。  
5. 无新增 PUT/DELETE；无 Cron 编辑。

## 6. 风险

| 风险 | 缓解 |
|------|------|
| form 改造漏禁某控件 | 以 `isView` 统一绑 disabled；自测每页 |
| 权限码与 PC 不一致 | 对齐现网 `*:query/list/remove` |
| 任务删除影响调度 | 沿用后端校验；前端仅确认 |

## 7. 推荐实现顺序

1. API 补 remove（用户/角色/客户端/任务）  
2. 五模块列表删除  
3. 已有 form 加 `mode=view` + 列表「查看」  
4. 补 job / logininfor / online / file 只读页  
5. 冒烟 §5  

---

请审阅。回复「设计可以」后开始实现（或 `/openspec-propose`）；若要砍某只读页或改禁删规则，直接点名。
