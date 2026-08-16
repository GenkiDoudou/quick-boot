## 1. Scaffold

- [x] 1.1 创建 `docs/demo/lite-frontend-rum-console.html`，CDN 引入 Vue 3、Element Plus、ECharts，页头标明「静态 Mock」
- [x] 1.2 实现壳层：侧栏/顶栏导航四段（数据总览 / 异常分析 / API 监控 / 告警规则）、只读 `appId`、时间范围选择

## 2. Mock 与工具

- [x] 2.1 编写集中 mock：KPI、趋势序列、错误 TOP 页、慢/失败 API、Issue 列表、原始事件、告警规则与触发记录
- [x] 2.2 实现错误率 / API 成功率色块与健康分计算（对齐设计阈值）

## 3. 四段页面

- [x] 3.1 数据总览：健康分、KPI、ECharts 趋势、三类 TOP；点击跳转并带筛选
- [x] 3.2 异常分析：Issue 表格 + 详情抽屉（stack、关联页、最近事件、简易趋势）
- [x] 3.3 API 监控：汇总趋势、耗时/失败 TOP Tab、选中行小趋势
- [x] 3.4 告警规则：表单编辑、本地保存提示、触发记录列表（不真实请求 Webhook）

## 4. 验收

- [x] 4.1 自检：无 `el-*` 自闭合；浏览器打开可切换四段并完成主要交互
- [x] 4.2 将本 tasks 对应项勾选为完成
