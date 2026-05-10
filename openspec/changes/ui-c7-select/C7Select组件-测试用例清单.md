# C7Select（业务下拉）-测试用例清单

## 测试环境信息

- **测试登录地址**：`http://localhost:8800/login`（以 `quick-ui` 本地 `pnpm dev` 默认端口为准，见 `vite.config.js`）
- **测试账号**： admin/admin

## 模块信息

- **模块名称**：C7Select（业务下拉封装）
- **模块缩写**：C7SEL
- **模块页面地址**：`/dev/c7-select-e2e`（Dev 演示；路由见 `quick-ui/src/router/index.js`）
- **前端组件**：`quick-ui/src/packages/C7Select/index.vue`
- **后端接口路径**：不适用（`fetchData` 由业务注入；本清单以 Dev 页 mock 与网络面板为主）
- **测试用例总数**：16

---

## TC_C7SEL_001 - 合法账号登录并进入可测状态

**功能名称**：系统登录

**用例标题**：验证使用合法账号成功登录，会话有效，可继续访问业务或 Dev 路由

**前置条件**：

1. 未登录或已清除本地会话（Cookie/LocalStorage 按项目实际存储）
2. `quick-ui` 开发服务已启动（如 `pnpm dev`）

**测试步骤**：

1. 浏览器打开登录页（地址见上文「测试登录地址」）
2. 输入合法账号、密码
3. 点击登录

**预期结果**：

1. 登录成功，跳转至首页或系统约定的落地页
2. 会话有效（后续访问需鉴权的页面不再被重定向到登录页，或 Dev 路由在贵司路由守卫策略下可访问）

---

## TC_C7SEL_002 - Dev 演示页可访问且 C7Select 渲染

**功能名称**：C7Select / 环境就绪

**用例标题**：登录后访问 C7Select Dev 页，页面标题与各分区可见

**前置条件**：

1. 已完成 **TC_C7SEL_001** 或具备访问 `/dev/c7-select-e2e` 的同等会话（若路由为白名单可注明）
2. 前端路由已注册 `/dev/c7-select-e2e`（见 `quick-ui/src/router/index.js`）

**测试步骤**：

1. 在地址栏访问 `/dev/c7-select-e2e`
2. 观察页面标题与「静态 / autoLoad / remote / separator / reload」等分区标题

**预期结果**：

1. 页面加载无白屏、无未捕获控制台错误（与当前构建版本一致）
2. 存在 `data-testid="c7-select-title"` 的标题文案「C7Select Dev」
3. 各 `c7-select` 实例占位符、下拉可点开（Element Plus 控件可见）

---

## TC_C7SEL_003 - 静态 dataList 选择与 v-model 同步

**功能名称**：静态数据来源

**用例标题**：使用 `data-list` 静态选项完成单选，父级 `v-model` 与展示一致

**前置条件**：

1. 已进入 **TC_C7SEL_002** 所述 Dev 页
2. 定位「静态 dataList」分区（`data-testid="tc-static"`）

**测试步骤**：

1. 打开 `data-testid="c7-sel-static"` 的下拉
2. 选择「香蕉」或「苹果」任一项
3. 查看 `data-testid="c7-sel-static-model"` 展示

**预期结果**：

1. 选中项在下拉中显示正确
2. 下方 `pre` 展示的 `v-model` 与所选 `value` 一致（与 `ElSelect` 单选行为一致）

---

## TC_C7SEL_004 - 静态 clearable 清空

**功能名称**：静态数据来源

**用例标题**：开启 `clearable` 时可清空选择，`v-model` 回到空值语义

**前置条件**：

1. 同 **TC_C7SEL_003** 分区
2. 已选中一项

**测试步骤**：

1. 点击清空图标（Element Plus 清除）
2. 观察 `c7-sel-static-model`

**预期结果**：

1. 选择被清空，展示与 `v-model` 空值语义一致（单选一般为 `null`/`''` 等，以当前实现为准）

---

## TC_C7SEL_005 - autoLoad 非 remote 挂载后自动拉取

**功能名称**：autoLoad（非 remote）

**用例标题**：`auto-load=true` 且非 `remote` 时，挂载后自动出现异步选项

**前置条件**：

1. Dev 页「autoLoad（非 remote）」分区（`data-testid="tc-autoload"`）

**测试步骤**：

1. 刷新页面后无需手动展开下拉（或展开一次）
2. 打开 `data-testid="c7-sel-autoload"` 的下拉
3. 观察选项是否包含 mock 文案「异步项1」「异步项2」（以 `C7SelectE2E.vue` 中 `fetchAutoList` 为准）

**预期结果**：

1. 在首次交互前或展开时可见由 `fetchData` 返回的选项（与 `result-key="list"` 解析一致）
2. 浏览器网络或 mock 可观察到挂载后触发过一次 **无 `query` 键** 的 `fetchData` 调用（若以 Dev mock 为准，可在代码断点或临时日志验证）

---

## TC_C7SEL_006 - remote 首次展开全量请求不含 query

**功能名称**：remote 与 query 语义

**用例标题**：`remote=true` 时，首次展开下拉触发的 `fetchData` 合并参数中 **不得包含 `query` 键**

**前置条件**：

1. Dev 页「remote 搜索」分区（`data-testid="tc-remote"`）
2. 具备在 `fetchData` 内打断点、打日志或使用代理抓包的能力

**测试步骤**：

1. 刷新页面，**不要**先在输入框输入关键字
2. 点击 `data-testid="c7-sel-remote"` 展开下拉
3. 记录本次 `fetchData` 的入参对象键名

**预期结果**：

1. 首次展开触发的 `fetchData` 入参 **不包含** `query` 属性（与 `spec.md`「全量 = 不带 query」一致）
2. 下拉选项为 mock 全量城市列表（上海/北京/南京等）

---

## TC_C7SEL_007 - remote 输入关键字后请求含 query 且列表更新

**功能名称**：remote 与 query 语义

**用例标题**：在可过滤的 remote 下拉中输入关键字，`fetchData` 带 `query` 且选项被过滤

**前置条件**：

1. 同 **TC_C7SEL_006** 分区
2. 已完成首次全量加载（已展开过一次）

**测试步骤**：

1. 在 `c7-sel-remote` 的输入区输入「北」或「南」（与 mock 过滤逻辑一致）
2. 等待防抖时间（实现默认约 **300ms**）后观察选项
3. 查看 `fetchData` 入参是否包含 `query`

**预期结果**：

1. `fetchData` 入参包含 **`query`**，且值为当前输入关键字
2. 下拉选项与 mock 过滤结果一致（如仅「北京」等）
3. 若快速连续输入，最终展示与 **最后一次** 成功返回一致（last-write-wins，见 **TC_C7SEL_008**）

---

## TC_C7SEL_008 - remote 防抖与 last-write-wins（观测）

**功能名称**：竞态与防抖（remote）

**用例标题**：快速连续输入时，选项不被较早的慢请求覆盖

**前置条件**：

1. 可对 `fetchRemoteList` 人为加入随机延迟的临时改造，或在后端接口制造快慢差异（**测试后还原代码**）

**测试步骤**：

1. 在 remote 下拉中快速输入多段字符，使多次请求交叠
2. 观察最终选项与最后一次输入语义是否一致

**预期结果**：

1. 可见选项与 **最后一次完成且仍有效的** 请求结果一致，不出现「先显示新结果又被旧结果覆盖」的错误终态

---

## TC_C7SEL_009 - 多选 + separator 对外逗号字符串与空选择

**功能名称**：多选、separator 与 v-model 形态

**用例标题**：`multiple` + `separator` 时，父级 `v-model` 为逗号分隔字符串；清空后为约定空串

**前置条件**：

1. Dev 页「多选 + separator」分区（`data-testid="tc-separator"`）

**测试步骤**：

1. 查看初始 `data-testid="c7-sel-sep-model"`（预置含缺 option 的 value，见页面说明）
2. 增删选中项（勾选 a/b 等）
3. 清空全部选中
4. 观察 `c7-sel-sep-model`

**预期结果**：

1. 非空多选时，`pre` 中展示为 **逗号分隔字符串**（无多余首尾逗号）
2. 清空后对外为空字符串 **`''`**（与实现 JSDoc 固定策略一致；若实现改为 `null` 则以 JSDoc 为准并更新本用例）

---

## TC_C7SEL_010 - 多选无 separator 对外数组

**功能名称**：多选、separator 与 v-model 形态

**用例标题**：`multiple=true` 且未启用 `separator` 时，`v-model` / `change` 为数组

**前置条件**：

1. 需在业务页或 **临时父组件** 中挂载：`<c7-select v-model="arr" multiple :data-list="opts" />`（当前 `C7SelectE2E.vue` 未单独分区时可本项标注「待补充专用分区」）

**测试步骤**：

1. 选择两个选项
2. 在父组件打印或绑定展示 `v-model` 类型与内容

**预期结果**：

1. `v-model` 为 **数组**，元素顺序与选择行为符合 `ElSelect` 多选约定

---

## TC_C7SEL_011 - 逗号字符串回显与缺 option 的 value 保留

**功能名称**：与 options 对不齐的 value（保留策略）

**用例标题**：外部 `v-model` 为含不存在项的逗号串时，不静默删除「孤儿」value

**前置条件**：

1. Dev 页 `tc-separator` 分区预置 `a,x,b` 类场景（见页面文案）

**测试步骤**：

1. 打开 `data-testid="c7-sel-sep"` 的下拉，观察已选 tag
2. 确认选项列表仅有 a、b 等有限项
3. 查看 `c7-sel-sep-model` 是否仍包含 `x` 段

**预期结果**：

1. 模型字符串 **仍包含** 无对应 `el-option` 的 value（如 `x`），**不得**被组件静默改写为仅 `a,b`
2. 展示上允许出现仅有 value、无 label 的 tag（与 Element Plus 行为一致）

---

## TC_C7SEL_012 - reloadOnClear 清空后无 query 再次加载

**功能名称**：remote 与 query 语义 / reloadOnClear

**用例标题**：`remote=true` 且 `reload-on-clear=true` 时，用户清空已选后再次触发 **无 `query`** 的加载

**前置条件**：

1. 当前 Dev 页 **未挂载** `reload-on-clear` 示例时：在业务页或临时 Story 中配置 `reload-on-clear="true"` 与 `remote`、`fetch-data`（见备注）

**测试步骤**：

1. 展开 remote 下拉并完成一次全量加载，再选择一项
2. 点击清除，清空已选
3. 记录清空后触发的 `fetchData` 入参

**预期结果**：

1. 清空后触发一次加载，且入参 **不包含 `query` 键**（与 `spec.md` 默认一致）

---

## TC_C7SEL_013 - expose reload() 手动重拉

**功能名称**：对外暴露

**用例标题**：通过组件 ref 调用 `reload()` 后选项按当前 remote 状态刷新

**前置条件**：

1. Dev 页「reload 暴露」分区（`data-testid="tc-reload"`）

**测试步骤**：

1. 展开 `data-testid="c7-sel-reload"` 加载选项，可任选一项或保持空
2. 点击 `data-testid="c7-sel-reload-btn"` 触发 `reload()`
3. 观察选项是否仍为 mock 全量或可过滤状态（与 `C7Select` 实现 `reload` 语义一致）

**预期结果**：

1. 无控制台报错
2. `fetchData` 再次被调用，下拉选项与 mock 返回一致

---

## TC_C7SEL_014 - fetchData reject 时保留已有选项与 loading 回落

**功能名称**：异步数据来源与解析链

**用例标题**：`fetchData` Promise reject 时，不静默清空已有选项；`loading-change` 最终为 false

**前置条件**：

1. 临时父组件：`fetchData` 第一次 resolve 出列表，第二次点击「重试」时 reject（或 Dev 页扩展按钮，**测完删除**）

**测试步骤**：

1. 先成功加载选项并记住列表
2. 触发第二次会 reject 的加载
3. 观察选项列表与 `loading-change` 事件（可在父组件 `@loading-change` 打日志）

**预期结果**：

1. reject 后 **仍保留** 上一次成功时的选项（与 `design`/实现「失败不静默清空」一致）
2. `loading-change` 从 true 回到 false，无永久 loading

---

## TC_C7SEL_015 - resultKey 与 dataFormatter 解析链

**功能名称**：异步数据来源与解析链

**用例标题**：从 `response.data` 经 `result-key` 取数组，再经 `data-formatter` 整形后展示

**前置条件**：

1. `fetchData` 返回形如 `{ data: { list: [...] } }`，配置 `result-key="list"`
2. `data-formatter` 对列表做映射（可在临时页验证）

**测试步骤**：

1. 挂载带 `result-key` 与 `data-formatter` 的 `C7Select`，触发加载
2. 核对下拉 label/value 与 formatter 输出一致

**预期结果**：

1. 展示数据与 `resultKey` + `dataFormatter` 链一致；`response` 顶层非 `data` 的字段不被误当作列表根（与 `spec.md` 一致）

---

## TC_C7SEL_016 - 插槽 prefix / empty 透传至 ElSelect

**功能名称**：插槽透传

**用例标题**：使用 `#prefix`、`#empty` 时，在页面上可见且行为符合 Element Plus

**前置条件**：

1. 临时父组件为 `C7Select` 提供 `#prefix` 与 `#empty` 插槽内容（图标或文案）

**测试步骤**：

1. 打开无选项或过滤后无结果的下拉，查看 empty 插槽
2. 查看选择框前缀区域是否渲染 prefix 插槽

**预期结果**：

1. `prefix` / `empty` 内容可见，与 `ElSelect` 插槽行为一致（`option`/`label` 可按需另补用例）

---

## 测试用例统计

### 按功能分类统计

| 功能模块 | 测试用例数量 |
|---------|------------|
| 登录 / 会话 | 1 |
| 环境与 Dev 页 | 1 |
| 静态 dataList / clearable | 2 |
| autoLoad（非 remote） | 1 |
| remote / query / reloadOnClear | 3 |
| 竞态与防抖（remote） | 1 |
| 多选、separator、value 保留 | 3 |
| expose reload / loading / 失败形态 | 2 |
| resultKey / dataFormatter | 1 |
| 插槽透传 | 1 |
| **总计** | **16** |

### 按测试类型分类统计

| 测试类型 | 测试用例数量 |
|---------|------------|
| 正向流程（主流程） | 11 |
| 边界 / 观测类（防抖、LWW、空值） | 2 |
| 异常 / 失败恢复 | 1 |
| UI / 插槽 | 1 |
| 前置 / 环境（登录、入口） | 1 |
| **总计** | **16** |

---

## 备注

1. **数据来源**：OpenSpec 变更 **`ui-c7-select`**；主要工件：`proposal.md`、`design.md`、`tasks.md`、`specs/ui-c7-select/spec.md`；实现代码：`quick-ui/src/packages/C7Select/index.vue`、`quick-ui/src/views/dev/C7SelectE2E.vue`。
2. **`dataList` 与 `options` 同时存在**：规格要求 **`dataList` 优先**；当前 Dev 页未单独演示双 prop 同页，建议在临时父组件中增加 **TC_C7SEL_017** 类用例或扩展 Dev 页后再归档计数（本清单未单列以免与「总数 16」冲突时误计，可后续增补为第 17 条）。
3. **`reloadOnClear` / `separator=false` 多选 / `dataFormatter`**：部分步骤依赖 **临时页或 Dev 页扩展**；当前以 `C7SelectE2E.vue` 已有分区为准，缺项已在用例前置条件中写明。
4. **spec 与 design**：以可验收的 **`spec.md`** 场景为主；`reloadOnClear` 是否重置最后一次 `query` 以组件 JSDoc 与代码注释为最终依据。
5. **登录地址与账号**：以实际部署与项目文档为准，本清单仅给本地开发占位。
