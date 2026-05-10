# C7Pagination（业务分页）-测试用例清单

## 测试环境信息

- **测试登录地址**：`http://localhost:8800/login`（以 `quick-ui` 本地 `pnpm dev` 默认端口为准，见 `quick-ui/vite.config.js`）
- **测试账号**：`admin` / `admin`（与仓库既有 C7 Dev 用例约定一致；若环境不同请替换）

## 模块信息

- **模块名称**：C7Pagination（业务分页封装）
- **模块缩写**：C7PGN
- **模块页面地址**：`/dev/c7-pagination-e2e`（Dev 演示；路由见 `quick-ui/src/router/index.js`）
- **前端组件**：`quick-ui/src/packages/C7Pagination/index.vue`
- **后端接口路径**：不适用（本组件不封装列表请求；与 `PageRequest` 的映射由业务页在 `@change` 中处理）
- **测试用例总数**：9

---

## TC_C7PGN_001 - 合法账号登录并进入可测状态

**功能名称**：系统登录

**用例标题**：验证使用合法账号成功登录，会话有效，可继续访问 Dev 路由

**前置条件**：

1. 未登录或已清除本地会话（Cookie / LocalStorage 按项目实际存储）
2. `quick-ui` 开发服务已启动（如 `pnpm dev`）

**测试步骤**：

1. 浏览器打开登录页（地址见上文「测试登录地址」）
2. 输入合法账号、密码
3. 点击登录

**预期结果**：

1. 登录成功，跳转至首页或系统约定的落地页
2. 会话有效（后续访问需鉴权的页面不再被重定向到登录页；若路由守卫将 `/dev/*` 设为白名单，则至少保证 Dev 页可访问策略与项目一致）

---

## TC_C7PGN_002 - C7Pagination Dev 页可访问且双区块渲染

**功能名称**：C7Pagination / 环境就绪

**用例标题**：登录后访问 Dev 页，标题与「autoReset 默认」「autoReset=false」两节可见

**前置条件**：

1. 已完成 **TC_C7PGN_001** 或具备访问 `/dev/c7-pagination-e2e` 的同等会话
2. 前端路由已注册 `/dev/c7-pagination-e2e`（见 `quick-ui/src/router/index.js`）

**测试步骤**：

1. 在地址栏访问 `http://localhost:8800/dev/c7-pagination-e2e`（或域名 + 该 path）
2. 观察页面主标题与两个 `section` 区域标题

**预期结果**：

1. 页面加载无白屏、无与本次变更相关的未捕获控制台错误
2. 存在 `data-testid="c7-pagination-title"`，文案为「C7Pagination Dev」
3. 可见 `data-testid="tc-autoreset"` 与 `data-testid="tc-no-autoreset"` 两个区块说明与分页控件

---

## TC_C7PGN_003 - autoReset 默认：切换 pageSize 回第 1 页且 change 单次为 (1, newSize)

**功能名称**：autoReset 与统一 change

**用例标题**：在非第 1 页切换每页条数后，当前页为 1，且 `change` 仅出现一条最终态 `(1, newPageSize)`

**前置条件**：

1. 已进入 **TC_C7PGN_002** 所述 Dev 页
2. 定位「autoReset（默认）」区块（`data-testid="tc-autoreset"`），确认文案展示当前 `currentPage` 初始为 **3**（与 `C7PaginationE2E.vue` 实现一致）

**测试步骤**：

1. 在第一个分页器的「每页条数」下拉中，将条数从 **10** 改为 **20**（或改为 **50**，与初始不同即可）
2. 观察同区块内「currentPage=…, pageSize=…」文案
3. 观察 `data-testid="c7-pg-change-log"` 中最新一条 `change` 记录

**预期结果**：

1. 切换完成后 **currentPage 为 1**，**pageSize** 为所选新值
2. **`change` 日志仅新增一条**，且内容为 **`change(1, <新条数>)`** 形态（与 `openspec/changes/ui-c7-pagination/specs/ui-c7-pagination/spec.md` 验收场景一致）
3. **不出现**连续两条分别为「旧页 + 新条数」与「1 + 新条数」的中间态双发

---

## TC_C7PGN_004 - 仅翻页：点击页码后状态与 change 最终态一致

**功能名称**：双绑与 change（翻页路径）

**用例标题**：在 `autoReset` 默认区块通过页码切换页码，`currentPage` 与 `change` 日志一致且无多余重复

**前置条件**：

1. 同 **TC_C7PGN_003** 后，当前 **currentPage 为 1**（或手动将条数调回 10 后仍为第 1 页亦可）
2. 总条数足够多页（Dev 页 `total=233`，`pageSize=10` 时页数大于 1）

**测试步骤**：

1. 点击分页器上的页码 **2**（或「下一页」直至到第 2 页）
2. 观察同区块 `currentPage` 文案与 `c7-pg-change-log` 最新记录

**预期结果**：

1. 页面展示 **currentPage 为 2**（或与所点页码一致）
2. **`change` 日志新增一条**，载荷为 **`(2, 当前 pageSize)`**（或与当前条数一致），**不因单次点击产生两条连续相同/矛盾的 change**

---

## TC_C7PGN_005 - 上一页 / 下一页按钮与 prev-click / next-click 语义

**功能名称**：分页导航

**用例标题**：使用上一页、下一页按钮切换页码，界面与 v-model 展示正确

**前置条件**：

1. 第一个分页区 `currentPage` 为 **2**（可通过 **TC_C7PGN_004** 到达）

**测试步骤**：

1. 点击「上一页」一次
2. 再点击「下一页」一次

**预期结果**：

1. 点击上一页后 **currentPage 为 1**
2. 点击下一页后 **currentPage 为 2**
3. 若业务侧需验收 **`prev-click` / `next-click`**：可在父组件临时增加监听并在控制台输出（本 Dev 页未强制展示；事件契约见 `design.md` / 组件 `emit` 列表）

---

## TC_C7PGN_006 - Jumper 跳转到合法页码

**功能名称**：layout 透传与 ElPagination 行为

**用例标题**：使用首区块 layout 中的「前往」输入框跳转到合法页码

**前置条件**：

1. 第一个分页区 layout 含 **jumper**（见 `C7PaginationE2E.vue`：`layout="total, sizes, prev, pager, next, jumper"`）

**测试步骤**：

1. 在 jumper 输入框中输入 **5**（确保不超过当前 `total` 与 `pageSize` 下的总页数；233/10 时最大页为 24，**5** 合法）
2. 按回车或触发 Element Plus 约定的确认方式

**预期结果**：

1. 当前页跳转至 **5**（或与输入一致且被 EP 接受的页码）
2. 页面 `currentPage` 展示与分页器高亮一致

---

## TC_C7PGN_007 - autoReset=false：切换条数不强制回到第 1 页

**功能名称**：autoReset 关闭

**用例标题**：在第二区块将每页条数从一种改为另一种，当前页**不为**强制 1，而与 `ElPagination` 钳制规则一致

**前置条件**：

1. 定位「autoReset=false」区块（`data-testid="tc-no-autoreset"`）
2. 初始实现中 **currentPage 为 4**、`pageSize` 为 **10**、`total` 为 **100**（以 `C7PaginationE2E.vue` 为准）

**测试步骤**：

1. 记录当前展示的 **currentPage**
2. 将该区块「每页条数」从 **10** 改为 **5**（总页数变化，可能触发 EP 内部钳制）
3. 观察 **currentPage** 是否被强制变为 **1**

**预期结果**：

1. **currentPage 不为**因本组件 **`autoReset`** 而**必然**变为 **1**（允许为 EP 钳制后的合法页，如仍可能为 4 或变为不超过新总页数的页码，以当前 EP 行为为准）
2. 与 `spec.md` 中「**`autoReset` 为 false 时不强制置 1**」一致

---

## TC_C7PGN_008 - total 与 layout 中文案展示

**功能名称**：透传 total / layout

**用例标题**：首区块展示总条数等与 `total`、`layout` 一致

**前置条件**：

1. 第一个分页区可见

**测试步骤**：

1. 观察分页区域「共 xxx 条」或当前 Element Plus 语言包下的总条数文案

**预期结果**：

1. 总条数展示与 Dev 页配置的 **`total=233`** 一致（若国际化文案不同，以可见数字 **233** 为准）

---

## TC_C7PGN_009 - 连续多次切换条数：每次 change 仍为单次 (1, newSize)

**功能名称**：autoReset 与 change 稳定性

**用例标题**：在同一默认区块连续两次修改 `pageSize`，每次均只增加一条 `change(1, …)` 日志

**前置条件**：

1. 第一个分页区 **currentPage 为 1**（可先执行 **TC_C7PGN_003** 或手动回到第 1 页）

**测试步骤**：

1. 将条数 **10 → 20**，观察 `c7-pg-change-log` 新增行数与内容
2. 再将条数 **20 → 50**，再次观察日志

**预期结果**：

1. 每次操作后 **仅新增一行** `change` 记录，且均为 **`change(1, <本次新条数>)`**
2. 两次操作之间 **不出现**同一操作下的双行 `change`

---

## 测试用例统计

### 按功能分类统计

| 功能模块 | 测试用例数量 |
|---------|------------|
| 系统登录 / 鉴权前置 | 1 |
| Dev 页加载与环境 | 1 |
| autoReset 与 change | 3 |
| 翻页与导航（页码 / 上一下一 / jumper） | 3 |
| autoReset=false | 1 |
| **总计** | **9** |

### 按测试类型分类统计

| 测试类型 | 测试用例数量 |
|---------|------------|
| 正向流程（主流程 / 验收） | 7 |
| 边界与组合（连续操作、钳制语义） | 2 |
| **总计** | **9** |

---

## 备注

1. **数据来源**：本清单由 OpenSpec 变更 **`ui-c7-pagination`** 的 `proposal.md`、`design.md`、`tasks.md`、`specs/ui-c7-pagination/spec.md` 推导；与实现不一致时以代码为准并迭代本清单。
2. **数据准备**：默认依赖 `C7PaginationE2E.vue` 中写死的 `total` / 初始 `page` / `size`；若修改 Dev 页数据，请同步更新 **TC_C7PGN_003 / 007 / 008** 中的预期数字与合法页范围。
3. **已知限制**：**`hide-on-single-page` / `disabled` / `small` / `pager-count` 等** 若未在 Dev 页覆盖，可在业务列表页补测或扩展 Dev 页后再增用例；**`current-change` / `size-change` / `prev-click` / `next-click`** 的精确载荷可在父组件临时监听验证（本清单以可见 UI 与 `change` 日志为主）。
4. **双变更仓库**：当前 `openspec list` 中另有已完成的 **`ui-c7-select`**；本文件**仅**覆盖 **`ui-c7-pagination`**。
