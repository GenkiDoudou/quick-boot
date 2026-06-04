# 全链路监控页 HTML 原型

浏览器直接打开对应 `.html`，无需 Node 构建。

| 文件 | 版本 | 表现形式 |
|------|------|----------|
| [monitor-trace-chain-prototype-v2-story.html](./monitor-trace-chain-prototype-v2-story.html) | **v2（已定稿保存）** | ⓪ 页面跳转表 + ① 按 pageVisitId 行为明细 + ② HTTP/日志/SQL；胶囊概览含多页 |
| [monitor-trace-chain-prototype-v3-network.html](./monitor-trace-chain-prototype-v3-network.html) | **v3** | 页面跳转独立表 + Network 行（跳转/行为/后端分段）+ 瀑布条 |
| [monitor-trace-chain-prototype.html](./monitor-trace-chain-prototype.html) | 与 v2 同步 | 默认入口 |

**共用 mock 链路：** 工作台 → 用户管理（pageVisit）→ 搜索/列表 → 点击修改 → GET 详情 → POST 保存（慢 SQL）→ 刷新列表。

设计依据：`.cursor/plans/全链路监控页设计_c430faa5.plan.md`
