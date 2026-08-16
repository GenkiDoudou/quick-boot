# quick-h5 运维套件（系统 + 监控）一期设计

日期：2026-08-16  
状态：已实现（OpenSpec `quick-h5-ops-suite`；人工冒烟待本地验证）  
范围：在已落地用户/角色/部门/菜单权限之上，下沉参数、文件、分类、客户端、字典、定时任务、调度日志、登录/操作日志、在线用户、慢 SQL  
前置：`2026-08-15-quick-h5-user-mgmt-design.md`、`2026-08-15-quick-h5-role-dept-design.md`、`2026-08-16-quick-h5-menu-perm-design.md`

## 1. 背景与目标

PC（quick-ui）已具备上述能力；H5 工作台可按角色下发菜单，但除用户/角色/部门外尚无对应页面。  
一期目标：移动端运维常用操作，**直连现有后端 API**，交互与视觉对齐已有 H5 CRUD（`QbSearchBar` / `QbListCard` / `usePagedList` / `hasPermi`）。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 能力档位 | A：列表搜索分页 + 常用写操作；不做导入导出、不对齐 PC 全字段 |
| 交付节奏 | 一份总设计；实现分两批：先系统域，再监控域 |
| 菜单权限 | `path=/pages/...` + Flyway 挂 H5 菜单 + `hasPermi` |
| OpenSpec | 单一 change：`quick-h5-ops-suite`（实现前再 `/openspec-propose`） |
| 文件 | 列表 + 预览/下载 + 删除；上传需选分类 |
| 定时任务 | 列表 + 启停/执行一次 + 可进调度日志；不做移动端 Cron 编辑 |
| 日志类 | 列表 + 详情（操作/调度/慢 SQL）；登录日志可删/清空/解锁（有权限时）；不做导出 |

## 3. 分批交付

### 批次 1 — 系统域

| 模块 | H5 路径前缀 | 一期能力 |
|------|-------------|----------|
| 参数设置 | `/pages/system/config/` | 分页搜索、增改删、刷新缓存 |
| 字典管理 | `/pages/system/dict/` | 类型列表 + 进数据列表；类型/数据增改删；刷新缓存 |
| 客户端 | `/pages/system/oauthClient/` | 分页、增改、启停；详情可看 secret（注意脱敏展示） |
| 文件分类 | `/pages/system/fileClassify/` | 列表、增改删；`classify` 键创建后只读 |
| 文件管理 | `/pages/system/file/` | 列表筛选、上传（选分类）、预览/下载、删除 |

### 批次 2 — 监控域

| 模块 | H5 路径前缀 | 一期能力 |
|------|-------------|----------|
| 定时任务 | `/pages/monitor/job/` | 列表、启停、执行一次；入口进调度日志 |
| 调度日志 | `/pages/monitor/jobLog/` | 列表、详情、删除/清空（有权限） |
| 登录日志 | `/pages/monitor/logininfor/` | 列表、删除/清空/解锁 |
| 操作日志 | `/pages/monitor/operlog/` | 列表、详情、删除/清空 |
| 在线用户 | `/pages/monitor/online/` | 列表、强退 |
| 慢 SQL | `/pages/monitor/slowSql/` | 列表、详情、删除 |

两批均可复用同一套页面骨架；批次 2 依赖批次 1 的约定不变。

## 4. 统一约定

### 4.1 交互与组件

- 列表：`usePagedList` 或模块自有 list API（若后端为 GET `pageNum/pageSize`，封装适配层转成统一分页状态）
- UI：`QbSearchBar` / `QbListCard` / `QbListFooter` / `QbDictTag` / `QbStatusChips` / `QbForm*` 样式类
- 表单：独立 `form.vue` 或中心弹层；复杂详情（操作日志、慢 SQL）用独立 `detail.vue` 或半屏 popup
- 权限：所有写操作 `v-if="hasPermi([...])"`；与 PC 权限字符一致
- 错误：`toastErr`；401 现有 http 逻辑

### 4.2 后端策略

- **不新增 H5 BFF**；路径与 quick-ui 一致（注意前缀混用：`/sys/*` vs `/system/*` vs `/monitor/*`，以现网为准）
- 分页两种形态并存，H5 `api` 层各自封装，页面只消费统一结果

### 4.3 菜单（Flyway）

在「移动端工作台」下增加目录与 C 节点，例如：

```text
移动端工作台 (已有 9000)
├── 系统管理 (9001…)
│   ├── 参数 / 字典 / 客户端 / 文件 / 文件分类 …
└── 系统监控 (新建 M)
    ├── 定时任务 / 调度日志 / 登录日志 / 操作日志 / 在线用户 / 慢 SQL …
```

- `path` 必须以 `/pages/` 开头  
- 按钮 F **复用 PC 已有 perms**，不重复造权限字符  
- 绑定 `role_id=1`（admin）；其它角色由管理端勾选  

### 4.4 pages.json

每模块注册 `index`（及需要的 `form`/`detail`）；改完须**重启** `pnpm dev:h5`。

## 5. 模块要点与 API（摘要）

### 5.1 参数 `sys/config`

| 能力 | API |
|------|-----|
| 分页 | `POST /sys/config/page` |
| 详情/增改删 | `GET/POST .../add|update|remove` |
| 刷新缓存 | 现网 refresh 接口（与 PC 一致） |

字段精简：`configName`、`configKey`、`configValue`、`configType`、`remark`。内置参数禁删规则对齐 PC。

### 5.2 字典

- 类型：`/sys/dict/type/*` 分页 CRUD + refresh  
- 数据：`/sys/dict/data/*`；从类型行进入，带 `dictType` query  
- 列表展示 label/value/status；不做 Excel

### 5.3 OAuth 客户端 `/sys/oauthclient`

- 精简：`clientId`、`clientName`、`status`、超时等常用项；secret 仅详情展示一次/可复制  
- 不做导入导出

### 5.4 文件分类 `/system/fileClassify`

- 列表 + 表单；`classify` 新增可填、编辑只读  
- 字段：名称、扩展名、大小限制、状态等与上传强相关的子集

### 5.5 文件 `/system/file`

- 列表：`GET /system/file/list`  
- 上传：`POST /system/file/upload/{classify}`（uni.chooseFile / chooseImage）  
- 预览/下载/删除：对齐 PC 鉴权 URL  
- 分类选项：`listFileClassifies`（common）

### 5.6 定时任务 `/monitor/job`

- 列表 + `changeStatus` + `run`  
- **不提供** Cron/调用目标编辑表单（避免 H5 误配）；新增/修改整单留给 PC  
- 行操作「日志」→ `/pages/monitor/jobLog/index?jobName=`

### 5.7 调度 / 登录 / 操作日志

- 调度：`/monitor/jobLog/*`  
- 登录：`POST /monitor/logininfor/page` + remove/clean/unlock  
- 操作：`POST /monitor/operlog/page` + get/remove/clean  
- 详情页展示关键字段（IP、耗时、报文摘要等），长文本可折叠

### 5.8 在线用户 `/monitor/online`

- 列表 + `forceLogout`（二次确认）

### 5.9 慢 SQL `/monitor/slowSql`

- 列表 + 详情（SQL 文本）+ 删除  

## 6. 文件清单（按批）

### 批次 1

| 区域 | 内容 |
|------|------|
| `api/system/*` | config、dict、oauthClient、file、fileClassify |
| `pages/system/{config,dict,oauthClient,file,fileClassify}/` | index + form（dict 含 type/data） |
| `pages.json` | 注册页 |
| Flyway | H5 系统域菜单节点 |

### 批次 2

| 区域 | 内容 |
|------|------|
| `api/monitor/*` | job、jobLog、logininfor、operlog、online、slowSql |
| `pages/monitor/{job,jobLog,logininfor,operlog,online,slowSql}/` | index + detail（按需） |
| Flyway | H5 监控域菜单节点 |

## 7. 验收标准

1. admin 登录后工作台可见已挂菜单；无权限角色不见入口/按钮  
2. 批次 1：参数/字典/客户端/分类/文件主流程可走通，数据与 PC 一致  
3. 批次 2：任务启停/执行、各日志查询与详情、在线强退、慢 SQL 详情可走通  
4. 未做导入导出；定时任务无 Cron 编辑  
5. 改 `pages.json` 后重启 H5 开发服务  

## 8. 非目标

- 积木 BI / 代码生成 / 前端 RUM 控制台完整下沉  
- 菜单「端类型」表字段（仍用 `/pages/` 约定）  
- 按钮文案后台可配  
- 与 PC 100% 字段/交互对齐  

## 9. 风险

| 风险 | 缓解 |
|------|------|
| 分页协议不统一 | api 层适配，页面统一 loading/finished |
| 文件上传在各端差异 | 优先 H5；小程序另测 chooseMessageFile |
| 客户端 secret 泄露 | 详情默认掩码，点击「显示」再拉/展示 |
| 一次改动面过大 | 严格按批次合并；每批可单独冒烟 |

## 10. 推荐实现顺序

1. OpenSpec `quick-h5-ops-suite`（proposal/design/specs/tasks）  
2. 批次 1 实现 → 冒烟  
3. 批次 2 实现 → 冒烟  
4. 归档 change  

---

审阅通过后请回复「设计可以」或指出修改点；通过后执行 `/openspec-propose` 再 `/opsx:apply`（或说明直接按设计分批实现）。
