# C7ButtonGroup 按钮组组件-测试用例清单

## 测试环境信息

- **测试登录地址**：`http://localhost:8800/login`（以实际 `quick-ui` 开发环境为准；若 Dev 路由在未登录态不可达，则须先登录）
- **测试账号**：待补充（使用项目分配的合法账号/密码）

## 模块信息

- **模块名称**：C7ButtonGroup 按钮组组件
- **模块缩写**：C7BGP
- **模块页面地址**：`/dev/c7-button-e2e`（与 `C7Button` 共用 Dev/E2E 页中的 **`tc-grp-data-auto`**、**`tc-grp-slot-auto`** 分区）
- **前端组件**：`quick-ui/src/packages/C7ButtonGroup/index.vue`；依赖 `quick-ui/src/packages/C7Button/index.vue`
- **后端接口路径**：不适用（纯前端组件）
- **测试用例总数**：16

---

## TC_C7BGP_001 - 验证已登录或可鉴权状态下可进入组件验证入口

**功能名称**：系统登录 / 会话就绪

**用例标题**：验证使用合法会话访问 Dev E2E 页面前提成立

**前置条件**：

1. `quick-ui` 开发服务已启动（默认端口以 `vite.config.js` 为准，常见为 **8800**）。
2. 浏览器未登录或会话已失效时，需准备合法测试账号（见上「测试账号」）。

**测试步骤**：

1. 打开登录页（地址见「测试登录地址」）。
2. 使用合法账号、密码完成登录（若当前工程允许匿名访问 Dev 路由，则跳过本步并在备注中记录环境差异）。
3. 在地址栏访问：`http://localhost:8800/dev/c7-button-e2e`（基础路径以部署为准）。

**预期结果**：

1. 登录成功（若需要登录），跳转至系统内可达页面且会话有效。
2. Dev E2E 页面可访问，页面标题区域可见 **`data-testid="e2e-title"`** 对应文案（「C7Button E2E」）。
3. 页面中存在 **`data-testid="tc-grp-data-auto"`** 与 **`data-testid="tc-grp-slot-auto"`** 分区。

---

## TC_C7BGP_002 - Dev 页加载与全局组件注册可用性

**功能名称**：C7ButtonGroup 全局可用性

**用例标题**：验证页面加载后 C7ButtonGroup 演示区渲染无报错

**前置条件**：

1. 已完成 **TC_C7BGP_001** 或可等价访问 `/dev/c7-button-e2e`。

**测试步骤**：

1. 打开 `/dev/c7-button-e2e`。
2. 打开浏览器开发者工具 Console，刷新页面。
3. 滚动至「C7ButtonGroup」相关小节（**`tc-grp-data-auto`**、**`tc-grp-slot-auto`**）。

**预期结果**：

1. Console 无未捕获的运行时错误（与 `C7ButtonGroup` / `C7Button` 渲染相关）。
2. **`data-testid="c7-grp-data-auto"`** 与 **`data-testid="c7-grp-slot-auto"`** 对应的组件实例存在于 DOM 中。
3. 全局组件名 **`C7ButtonGroup`** 可通过模板 **`c7-button-group`** 正常使用（与页面呈现一致）。

---

## TC_C7BGP_003 - 数据模式 mode=auto 且 maxVisible=2 时折叠布局

**功能名称**：布局模式（auto）

**用例标题**：第三个非 hidden 按钮出现在「更多」下拉内

**前置条件**：

1. 已进入 **`tc-grp-data-auto`** 分区（**`data-testid="tc-grp-data-auto"`**）。
2. 当前演示配置为 **`mode="auto"`**、**`maxVisible=2`**，数据项不少于 3 个可见按钮（与实现及 Dev 页一致）。

**测试步骤**：

1. 观察主区域内可见 **`C7Button`** 数量（应为 2）。
2. 观察是否存在「更多」类触发按钮并可展开下拉。
3. 展开「更多」，确认第三个业务按钮位于下拉面板内（仍为 **`C7Button`** 形态，而非纯文案菜单项替代整条流水线）。

**预期结果**：

1. 外露区最多 **2** 个按钮。
2. 第 **3** 个按钮仅在「更多」展开后可见或可触达。
3. 下拉内为真实 **`C7Button`**（可与外露按钮同样出现 loading/禁用等表现）。

---

## TC_C7BGP_004 - 折叠项二次确认与 clickFunction 完整流水线

**功能名称**：命令路径与 C7Button 等价性

**用例标题**：「更多」内带 confirm 的按钮先确认再执行 clickFunction

**前置条件**：

1. 与 **TC_C7BGP_003** 相同演示区；折叠项配置 **`confirm=true`** 与确认文案（Dev 页示例为删除类折叠项）。

**测试步骤**：

1. 展开「更多」，点击折叠区内触发删除/确认的 **`C7Button`**。
2. 在 **`ElMessageBox.confirm`** 中点击「取消」。
3. 再次点击同一按钮，在确认框点击「确定」。
4. 观察 **`data-testid="grp-click-exec-count"`**（或等价计数展示）是否递增。

**预期结果**：

1. 步骤 2：不出现 **`clickFunction`** 成功执行的可观测副作用（计数不增加）；无异常未处理报错。
2. 步骤 3～4：确认通过后执行 **`clickFunction`**，计数 **`grp-click-exec-count`** 较步骤 1 前增加。
3. 流程中外露项与折叠项确认行为与单独使用 **`C7Button`** 时一致（loading 出现在 **`clickFunction`** 阶段）。

---

## TC_C7BGP_005 - 组级 before-command 与 after-command 载荷

**功能名称**：组级事件与按钮级事件并存

**用例标题**：点击数据模式子按钮时组级计数递增且顺序合理

**前置条件**：

1. **`tc-grp-data-auto`** 分区已挂载 **`@before-command` / `@after-command`** 计数展示（**`grp-before-cmd-count`**、**`grp-after-cmd-count`**）。

**测试步骤**：

1. 记录 **`grp-before-cmd-count`**、**`grp-after-cmd-count`** 初始值。
2. 依次点击一个外露按钮、一个「更多」内按钮（均需完成各自流水线，含确认项则先确认）。
3. 再次读取两项计数。

**预期结果**：

1. 每次有效命令结束后：**`before-command`** 触发次数 **`+1`**，**`after-command`** 触发次数 **`+1`**。
2. **`after-command`** 在对应 **`C7Button`** **`after-click`** 之后发生（可通过在测试脚本中加日志或 Playwright 监听顺序断言；手工场景以「计数均在点击周期结束后更新」为最低验收）。
3. 数据模式 **`item`** 含 **`key/index/raw`** 等与规格一致的字段（自动化时断言载荷形状）。

---

## TC_C7BGP_006 - 插槽模式 mode=auto 折叠与点击一致性

**功能名称**：插槽模式

**用例标题**：三个 `<C7Button />` 时第三项进入「更多」且点击生效

**前置条件**：

1. 已进入 **`tc-grp-slot-auto`**（**`data-testid="tc-grp-slot-auto"`**）。
2. 默认展示 **3** 个槽按钮（与 Dev 页 **`grp-slotThirdVisible=true`** 一致）。

**测试步骤**：

1. 确认外露 **`C7Button`** 为 **2** 个（槽1、槽2）。
2. 展开「更多」，点击槽3对应按钮。
3. 观察 **`data-testid="grp-slot-count"`**。

**预期结果**：

1. 折叠分区与 **TC_C7BGP_003** 语义一致（第三项在「更多」）。
2. 点击槽3后 **`grp-slot-count`** 递增。
3. **`after-command`** 的 **`item.slotIndex`** 与规格一致（自动化断言；手工可跳过字段明细）。

---

## TC_C7BGP_007 - 动态插槽与 forceUpdate 重算分区

**功能名称**：forceUpdate

**用例标题**：切换第三个槽按钮显示后分区正确刷新

**前置条件**：

1. **`tc-grp-slot-auto`** 提供「切换第三个按钮」操作（**`data-testid="grp-slot-toggle-third"`**）且在切换后调用 **`forceUpdate()`**（与实现一致）。

**测试步骤**：

1. 初始为 **3** 个槽按钮时记录「更多」内是否有槽3。
2. 点击「切换第三个按钮」隐藏第三项，观察外露区与「更多」是否仅剩 **2** 项且无多余「更多」（仅 **2** 项时通常不出现折叠）。
3. 再次点击恢复第三项，确认槽3回到「更多」内。

**预期结果**：

1. 步骤 2：布局与 **`maxVisible=2`** 一致（无错误折叠或空白触发器异常）。
2. 步骤 3：第三项恢复后重新进入「更多」，点击仍可增加 **`grp-slot-count`**。

---

## TC_C7BGP_008 - mode=inline 全平铺无「更多」

**功能名称**：布局模式（inline）

**用例标题**：inline 模式下可见按钮全部外露

**前置条件**：

1. 若 Dev 页未提供 inline 示例：在本地临时页面或使用 Vue SFC  playground 挂载 **`C7ButtonGroup`**，`mode="inline"`，**`buttons`** 不少于 3 条非 hidden（或插槽 **3** 个 **`C7Button`**）。

**测试步骤**：

1. 渲染上述配置。
2. 观察是否出现「更多」下拉触发器。
3. 确认所有可见按钮均在同一主区域内平铺（允许换行）。

**预期结果**：

1. 不出现「更多」下拉（或等价折叠触发器）。
2. 所有非 **`hidden`** 按钮均可直接点击，无需展开下拉。

---

## TC_C7BGP_009 - mode=dropdown 全部按钮在下拉内

**功能名称**：布局模式（dropdown）

**用例标题**：dropdown 模式下主区仅保留触发器，业务按钮均在面板内

**前置条件**：

1. 临时挂载：**`mode="dropdown"`**，可见按钮不少于 **2** 个（数据或插槽）。

**测试步骤**：

1. 观察主按钮区是否仅「更多」触发按钮（或等价）。
2. 展开下拉，确认全部业务 **`C7Button`** 位于面板内。
3. 任选一按钮执行一次完整点击（含可选 **`confirm`**）。

**预期结果**：

1. 主区域不外露独立业务 **`C7Button`**（与规格「全部置于下拉」一致）。
2. 点击仍走 **`C7Button`** 流水线（确认、loading、提示）。

---

## TC_C7BGP_010 - hidden 不占 maxVisible 名额且不展示

**功能名称**：数据驱动模式

**用例标题**：hidden 项不参与折叠计数且不渲染

**前置条件**：

1. 临时挂载数据模式：**`mode="auto"`**、**`maxVisible=2`**，**`buttons`** 配置示例：项1 可见、项2 **`hidden: true`**、项3、项4 可见（共 **3** 个非 hidden）。

**测试步骤**：

1. 统计外露按钮文案/顺序。
2. 展开「更多」，统计下拉内按钮数量。
3. 确认 DOM 中不存在 **`hidden`** 项对应按钮。

**预期结果**：

1. **`hidden`** 项不占用 **`maxVisible`** 的两个外露名额（外露应为非 hidden 顺序中的前 **2** 个）。
2. **`hidden`** 项不出现在「更多」内。
3. 与 **`disabled`** 区分：**`hidden`** 为不渲染；**`disabled`** 仍为渲染但不可点（见 **TC_C7BGP_014** 可选组合）。

---

## TC_C7BGP_011 - maxVisible 非法值降级策略

**功能名称**：布局模式（auto）边界

**用例标题**：maxVisible 为 0、负数或非数字时按不小于 1 处理

**前置条件**：

1. 临时挂载：`mode="auto"`，依次传入 **`maxVisible`** 为 **`0`**、**`-1`**、**`NaN`**（或字符串非法），**`buttons`** 至少 **2** 条可见。

**测试步骤**：

1. 每种非法值下观察外露按钮数量（应视为 **1** 或与代码注释一致的降级值）。
2. 确认无控制台报错或未捕获异常。

**预期结果**：

1. 降级策略与组件注释/static 规格一致（规格：**不小于 1 的整数**）。
2. 页面不崩溃，折叠逻辑仍可读。

---

## TC_C7BGP_012 - spacing、size、responsive 钩子

**功能名称**：样式与响应式钩子

**用例标题**：spacing 生效；size 透传；responsive 根 class 存在

**前置条件**：

1. 临时挂载或扩展 Dev 页：`spacing` 分别试 **`sm`/`md`/`lg`** 与数字 **`12`**；**`size="small"`**；**`responsive`** 为 **`true`**。

**测试步骤**：

1. 检查根元素是否包含 **`c7-button-group--responsive`**（与实现 JSDoc 一致）。
2. 比对不同 **`spacing`** 下列表式按钮区间距变化（目测或计算 computed style **`gap`**）。
3. 观察子 **`C7Button`** 尺寸是否随 **`size`** 变化。

**预期结果**：

1. **`responsive===true`** 时根节点具备约定 class。
2. **`spacing`** 改变时间距变化可观察。
3. 子按钮 **`size`** 与 Element Plus / **`C7Button`** 约定一致。

---

## TC_C7BGP_013 - 「更多」触发器配置（moreText、trigger）

**功能名称**：「更多」触发器配置

**用例标题**：moreText 展示正确；trigger 切换 hover/click 行为符合 Element Plus

**前置条件**：

1. 临时挂载：`showMoreDropdown` 为真的场景（**`auto`** 溢出或 **`dropdown`**）；传入 **`moreText="更多操作"`**；**`trigger`** 分别为 **`click`** 与 **`hover`**。

**测试步骤**：

1. 确认触发按钮文案为 **`更多操作`**。
2. **`trigger="click"`**：单击展开，移开不自动展开。
3. **`trigger="hover"`**：悬停展开，移开收起（与 EP 默认行为一致）。

**预期结果**：

1. 文案与 props 一致。
2. 下拉展开方式符合 **`el-dropdown`** 对 **`trigger`** 的定义。
3. **`moreButtonType` / `moreButtonPlain` / `moreIcon`** 变更时触发按钮样式同步更新（可与 **TC_C7BGP_012** 合并抽检）。

---

## TC_C7BGP_014 - 插槽内非 C7Button 节点被忽略

**功能名称**：插槽模式（边界）

**用例标题**：默认插槽中混入文本或其它组件时不参与折叠列表

**前置条件**：

1. 临时挂载 **`C7ButtonGroup`**：`mode="auto"`，**`maxVisible=2`**，默认插槽内顺序为：**`<span>说明文字</span>`**、**`<C7Button/>`** ×3。

**测试步骤**：

1. 统计参与折叠的 **`C7Button`** 数量（应为 **3**，不被 span 占位）。
2. 确认「说明文字」不出现在组内按钮区（可能被 Vue 置于默认插槽相邻位置；以 **仅 3 个 C7Button 参与布局** 为准）。

**预期结果**：

1. 非 **`C7Button`** 节点不参与 **`maxVisible`** 计数。
2. 三个 **`C7Button`** 折叠规则与纯三按钮场景一致。

---

## TC_C7BGP_015 - beforePipeline 顺序：before-command 早于 before-click

**功能名称**：组级事件与按钮级事件并存（顺序）

**用例标题**：监听子按钮 before-click 与组 before-command 时顺序为组先于按钮

**前置条件**：

1. 临时挂载：在某一 **`C7Button`** 上增加 **`@before-click`** 日志或计数；在 **`C7ButtonGroup`** 上 **`@before-command`** 日志或计数。

**测试步骤**：

1. 点击该按钮一次。
2. 比对日志顺序：应先 **`before-command`**，后 **`before-click`**。

**预期结果**：

1. 顺序符合规格推荐链：**`before-command` → `before-click` → … → `after-click` → `after-command`**。
2. 与 **`C7Button.beforePipeline`** 设计一致。

---

## TC_C7BGP_016 - 确认取消后 after-command 中 success 为 false

**功能名称**：组级 after-command 成功与否语义

**用例标题**：用户在 MessageBox 取消确认时不增加业务计数且 after-command.success=false

**前置条件**：

1. 使用带 **`confirm=true`** 的折叠项（或外露项），并在父层监听 **`@after-command`**（自动化断言载荷）。

**测试步骤**：

1. 触发按钮弹出确认框。
2. 点击「取消」。
3. 记录 **`after-command`** 载荷 **`success`** 与业务副作用计数。

**预期结果**：

1. **`success === false`**（或等价布尔）。
2. **`clickFunction`** 未执行（业务计数不变）。
3. **`after-command`** 仍触发（与 **`after-click(false)`** 对齐）。

---

## 测试用例统计

### 按功能分类统计

| 功能模块 | 测试用例数量 |
|---------|------------|
| 登录 / 会话就绪 | 1 |
| Dev 页加载与全局注册 | 1 |
| 布局模式（auto / inline / dropdown） | 3 |
| 数据驱动（hidden、maxVisible 降级） | 2 |
| 插槽模式（折叠、forceUpdate、非 C7Button） | 3 |
| 流水线 / 组级事件 / 确认链 / emit 顺序 | 4 |
| 样式与「更多」触发器 | 2 |
| **总计** | **16** |

### 按测试类型分类统计

| 测试类型 | 测试用例数量 |
|---------|------------|
| 正向流程（主流程） | 11 |
| 异常 / 否定路径（确认取消、after-command 失败语义） | 2 |
| 边界（hidden、非法 maxVisible、非 C7Button 插槽） | 3 |
| **总计** | **16** |

---

## 备注

1. **数据来源**：OpenSpec 变更 **`ui-c7-button-group`** 的 `proposal.md`、`design.md`、`specs/ui-c7-button-group/spec.md`、`tasks.md`；与实现对齐路径见「模块信息」。
2. **Dev 页覆盖**：当前仓库在 **`quick-ui/src/views/dev/C7ButtonE2E.vue`** 中已实现 **`tc-grp-data-auto`**、**`tc-grp-slot-auto`**；**TC_C7BGP_008、009、010～015** 中部分步骤需临时挂载或后续 Playwright 用例补充，已在用例内标明。
3. **关联变更**：单按钮行为以 **`ui-c7-button`** / **`C7Button`** 规格为准；本组件不新增后端接口。
4. **已知限制**：插槽收集仅遍历 **顶层与 Fragment 内** 直连 **`C7Button`**（见组件 JSDoc）；嵌套在其它组件内部的 **`C7Button`** 不在本清单正向覆盖范围内（应使用数据模式或调整模板结构）。
5. **自动化**：可与 **`openspec/changes/ui-c7-button/自动化测试`** 目录风格对齐新增 Playwright 用例；登录步骤复用既有 **`auth.setup.ts`** 模式（若存在）。
