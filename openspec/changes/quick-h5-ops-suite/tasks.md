## 1. 批次 1 — API 与基础设施

- [x] 1.1 新增 `api/system/config.ts`：page / get / add / update / remove / refresh（对齐 PC `/sys/config`）
- [x] 1.2 新增 `api/system/dictType.ts` 与 `dictData.ts`（或合并 `dictAdmin.ts`）：类型与数据 CRUD + refresh
- [x] 1.3 新增 `api/system/oauthClient.ts`：page / get / add / update（含 status）
- [x] 1.4 新增 `api/system/fileClassify.ts` 与 `api/system/file.ts`（含 listFileClassifies、upload、preview/download/delete）
- [x] 1.5 如有 GET `pageNum/pageSize` 与 POST page 混用，在 api 层统一适配为列表页可用的 rows/total

## 2. 批次 1 — 系统域页面

- [x] 2.1 参数：`pages/system/config/index.vue` + `form.vue`；搜索分页；增改删；刷新缓存；`hasPermi`
- [x] 2.2 字典类型：`pages/system/dict/type/index.vue` + form；进入数据列表带 `dictType`
- [x] 2.3 字典数据：`pages/system/dict/data/index.vue` + form；CRUD；无导入导出
- [x] 2.4 客户端：`pages/system/oauthClient/index.vue` + form/detail；secret 默认掩码可揭示；启停
- [x] 2.5 文件分类：`pages/system/fileClassify/index.vue` + form；编辑时 `classify` 只读
- [x] 2.6 文件：`pages/system/file/index.vue`；筛选列表；上传必选分类；预览/下载/删除
- [x] 2.7 `pages.json` 注册上述页面；easycom 沿用现有 `Qb*`

## 3. 批次 1 — 菜单种子与冒烟

- [x] 3.1 Flyway：在移动端工作台下增加系统域 C 节点（config/dict/oauthClient/file/fileClassify）；F 复用 PC perms；绑定 admin
- [x] 3.2 人工冒烟：admin 工作台可见入口；参数/字典/客户端/分类/文件主流程；无权限按钮隐藏（需重启后端跑 Flyway + 重启 `pnpm dev:h5` 后人工验证）
- [x] 3.3 提醒：改 `pages.json` 后重启 `pnpm dev:h5`

## 4. 批次 2 — Monitor API

- [x] 4.1 新增 `api/monitor/job.ts`：list / changeStatus / run（不接 add/edit Cron）
- [x] 4.2 新增 `api/monitor/jobLog.ts`：list / get / remove / clean
- [x] 4.3 新增 `api/monitor/logininfor.ts`：page / remove / clean / unlock
- [x] 4.4 新增 `api/monitor/operlog.ts`：page / get / remove / clean
- [x] 4.5 新增 `api/monitor/online.ts`：list / forceLogout
- [x] 4.6 新增 `api/monitor/slowSql.ts`：list / get / remove

## 5. 批次 2 — 监控域页面

- [x] 5.1 定时任务：`pages/monitor/job/index.vue`；启停、执行一次、跳转调度日志；无 Cron 编辑
- [x] 5.2 调度日志：`pages/monitor/jobLog/index.vue` + detail；删除/清空按权限
- [x] 5.3 登录日志：`pages/monitor/logininfor/index.vue`；删/清空/解锁
- [x] 5.4 操作日志：`pages/monitor/operlog/index.vue` + detail；删/清空
- [x] 5.5 在线用户：`pages/monitor/online/index.vue`；强退二次确认
- [x] 5.6 慢 SQL：`pages/monitor/slowSql/index.vue` + detail；删除
- [x] 5.7 `pages.json` 注册监控页

## 6. 批次 2 — 菜单种子与总验收

- [x] 6.1 Flyway：增加监控域 M + C 节点（job/jobLog/logininfor/operlog/online/slowSql）；F 复用 PC；绑定 admin
- [x] 6.2 冒烟：任务启停/执行与日志联动；各日志查询详情；在线强退；慢 SQL 详情（代码已接；需人工冒烟）
- [x] 6.3 确认未提供导入导出与 H5 Cron 编辑；勾选 tasks 完成项
- [x] 6.4 更新产品设计文档状态为已实现（可选，与归档一并）
