# C7Button 业务按钮组件 - 自动化测试报告

## 测试基本信息

- **测试时间**: 2026-05-02
- **测试环境**: `http://localhost:8800`（`quick-ui` 本地 `pnpm dev`，清单约定）
- **测试账号**: admin / admin
- **测试模块**: C7Button 业务按钮组件
- **测试页面**: `http://localhost:8800/dev/c7-button-e2e`（登录后访问；组件演示页 `quick-ui/src/views/dev/C7ButtonE2E.vue`）
- **测试用例总数**: 16 条
- **关联 OpenSpec 变更**: ui-c7-button

## 测试结果概览

| 测试项 | 总数 | 通过 | 失败 | 跳过 | 通过率 |
|--------|------|------|------|------|--------|
| 登录 / 鉴权前置 | 1 | 1 | 0 | 0 | 100% |
| 全局注册与可用性 | 1 | 1 | 0 | 0 | 100% |
| 预设类型与 props 覆盖 | 2 | 2 | 0 | 0 | 100% |
| 流水线中止（beforeClick / 校验 / 确认取消 / confirmFn） | 4 | 4 | 0 | 0 | 100% |
| 删除确认顺序（验收） | 1 | 1 | 0 | 0 | 100% |
| loading、busy、防抖 | 2 | 2 | 0 | 0 | 100% |
| 成功 / 失败判定与提示（含 axios 契约） | 3 | 3 | 0 | 0 | 100% |
| 配置边界（showErrorToast、validateRef） | 2 | 2 | 0 | 0 | 100% |
| **总计** | **16** | **16** | **0** | **0** | **100%** |

## 测试用例清单

| 用例编号 | 功能名称 | 用例标题 | 通过 | 备注 |
|---------|---------|---------|------|------|
| TC_C7BTN_001 | 系统登录 / 鉴权前置 | 验证使用合法账号成功登录并进入系统，会话有效 | ✅ 通过 | |
| TC_C7BTN_002 | C7Button 全局可用性 | 验证应用入口注册后页面可直接使用 `<C7Button>` 无需局部注册 | ✅ 通过 | |
| TC_C7BTN_003 | 预设类型（btnType） | 验证各 btnType 默认 label、type、plain 与图标符合预设表 | ✅ 通过 | |
| TC_C7BTN_004 | 预设类型（btnType） | 验证显式传入的 label、type、plain、size 覆盖对应预设 | ✅ 通过 | |
| TC_C7BTN_005 | 点击流水线顺序 | 验证 beforeClick 返回 false 时中止流水线且不弹出错误类 toast | ✅ 通过 | |
| TC_C7BTN_006 | 点击流水线顺序 / 验收场景 | 验证 validate=true 且 ElForm 校验失败时不进入确认与 clickFunction，且无错误类 toast | ✅ 通过 | 演示页已补 `confirm=true` 对齐清单 |
| TC_C7BTN_007 | 点击流水线顺序 | 验证 ElMessageBox.confirm 用户取消时不视为失败 toast | ✅ 通过 | 取消按钮限定在 `dialog` 内定位 |
| TC_C7BTN_008 | 验收场景 | 验证 btnType=delete 且 confirm=true 时顺序为先确认再执行异步逻辑 | ✅ 通过 | |
| TC_C7BTN_009 | 防抖与并发 / 验收场景 | 验证 Promise 未完成前按钮 loading，且流水线执行中重复点击不重复执行异步逻辑 | ✅ 通过 | |
| TC_C7BTN_010 | 防抖与并发 | 验证 debounceDelay 默认语义为首击立即、同窗口内尾随点击不开启新流水线 | ✅ 通过 | |
| TC_C7BTN_011 | 点击流水线顺序 / 与 axios 封装一致 | 验证 clickFunction resolve 后 checkSuccess 返回 false 时触发 error 且可展示失败提示 | ✅ 通过 | |
| TC_C7BTN_012 | 与 axios 封装一致 | 验证 Promise reject 表示失败，触发 error 与失败提示 | ✅ 通过 | |
| TC_C7BTN_013 | 点击流水线顺序 / 成功提示 | 验证 resolve 且 checkSuccess 默认通过时 success、after-click(true) 及成功提示规则 | ✅ 通过 | |
| TC_C7BTN_014 | 点击流水线顺序 | 验证 confirmFn 返回 false 时中止且不弹出错误类 toast | ✅ 通过 | |
| TC_C7BTN_015 | 失败提示边界 | 验证关闭 showErrorToast 时 reject 或 checkSuccess 失败不弹出错误 toast | ✅ 通过 | |
| TC_C7BTN_016 | 点击流水线顺序（配置健壮性） | 验证 validate=true 但 validateRef 无效时不进入表单 validate，且不弹错误类 toast | ✅ 通过 | |

## 详细测试结果

### TC_C7BTN_001 - 验证使用合法账号成功登录并进入系统，会话有效

- **功能名称**：系统登录 / 鉴权前置
- **执行状态**：✅ 通过
- **执行耗时**：约 1.1s（Playwright list reporter）
- **前置条件**：
  1. `quick-ui` 开发服务已启动，后端代理可达（默认 `/dev-api` → 后端，见项目配置）
  2. 浏览器处于未登录或已清除本站会话状态
- **执行步骤**：
  1. 打开登录页（地址：`http://localhost:8800/login`）
  2. 输入合法账号、密码
  3. 点击登录
- **关键步骤截图**（推荐）：
  - `./自动化测试/TC_C7BTN_001/01_login_form.png`
  - `./自动化测试/TC_C7BTN_001/02_after_login.png`
- **预期结果**：
  1. 登录成功，跳转至系统首页或约定的落地页
  2. 会话有效（Token/Cookie 存在），可打开需要使用 `C7Button` 的业务页面
- **实际结果**：登录后 URL 离开 `/login`；Cookie `Admin-Token` 存在。
- **验证结果**：
  - ✅ 步骤 1–3：通过（`login-only` 工程执行）

### TC_C7BTN_002 - 验证应用入口注册后页面可直接使用 `<C7Button>` 无需局部注册

- **功能名称**：C7Button 全局可用性
- **执行状态**：✅ 通过
- **执行耗时**：约 1.1s（不含 setup）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 当前仓库 `quick-ui/src/main.js` 已通过 `installPackages(app)` 注册全局组件（与 `quick-ui/src/packages/index.js` 一致）
- **执行步骤**：
  1. 登录后进入已挂载 `<C7Button>` 的页面（或临时在调试页放置最小示例：`click-function` 返回 `Promise.resolve()`）
  2. 打开浏览器开发者工具，确认无「组件未解析」类报错，按钮可见可点
- **关键步骤截图**（推荐）：
  - `./自动化测试/TC_C7BTN_002/01_e2e_page.png`
- **预期结果**：
  1. 页面渲染正常，`<C7Button>` 表现为 Element Plus 按钮外观
  2. 亦可按需 `import { C7Button } from '@/packages'` 使用，行为与全局一致（任选一种方式验证即可）
- **实际结果**：`/dev/c7-button-e2e` 渲染标题与「新增」示例按钮；点击无错误类 Message。
- **验证结果**：
  - ✅ 全局注册的 `<c7-button>` 可见且具备 `el-button` 样式类

### TC_C7BTN_003 - 验证各 btnType 默认 label、type、plain 与图标符合预设表

- **功能名称**：预设类型（btnType）
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 页面或使用 Story/演示片段中为每种待测 `btnType` 各放一个 `C7Button`，并提供最小的合法 `click-function`（如 `() => Promise.resolve()`）
- **执行步骤**：
  1. 依次查看 `btn-type` 为 `add`、`edit`、`delete`、`query`、`refresh`、`upload`、`download`、`submit`、`cancel` 的按钮（可与代码中 `PRESETS` 对照）
  2. 观察按钮文案、色调（type）、是否 plain、左侧图标是否与预设一致
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 各预设与 `spec.md`「预设类型」及实现 `PRESETS` 一致（例如 `delete` 为危险色、`refresh` 默认文案为「重置」等）
  2. 未传 `btn-type` 时，依赖显式 `label`/`type` 等与默认 `type`，不出现非法预设键导致的崩溃
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_004 - 验证显式传入的 label、type、plain、size 覆盖对应预设

- **功能名称**：预设类型（btnType）
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 页面中存在 `btn-type="add"` 的 `C7Button`，同时传入与预设不同的 `label`、`type`、`plain`、`size`
- **执行步骤**：
  1. 配置例如：`btn-type="add"`，`label="自定义"`，`type="warning"`，`plain=true`，`size="small"`
  2. 渲染并观察按钮外观与文案
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 按钮展示为自定义文案与显式的 type/plain/size，而非 `add` 预设的全部默认值
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_005 - 验证 beforeClick 返回 false 时中止流水线且不弹出错误类 toast

- **功能名称**：点击流水线顺序
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 页面挂载 `C7Button`，配置 `before-click`（或实现内 `beforeClick`）返回 `false`，`click-function` 内可做标记（如计数）以便判断是否被执行
- **执行步骤**：
  1. 点击按钮
  2. 观察是否触发确认框、`click-function` 是否被调用
  3. 观察界面提示与事件（若页面绑定了 `after-click`，应为 `false`）
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 先触发 `before-click`（emit），随后 `beforeClick` 否决后流水线中止
  2. 无确认框（若仅依赖 beforeClick 否决）、`click-function` 不被调用
  3. 无错误类 toast（Message 错误提示）；`after-click` 为 `false`
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_006 - 验证 validate=true 且 ElForm 校验失败时不进入确认与 clickFunction，且无错误类 toast

- **功能名称**：点击流水线顺序 / 验收场景
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 页面存在未通过校验的 `el-form`，`validate-ref` 绑定为该表单实例；`C7Button` 配置 `validate=true`，`confirm=true`，`click-function` 内设有调用标记
- **执行步骤**：
  1. 保持表单在校验失败状态（必填为空等）
  2. 点击 `C7Button`
  3. 观察是否弹出确认框、`click-function` 是否执行
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 不弹出确认框，`click-function` 不被调用
  2. 无错误类 toast（与 spec「校验失败 MUST NOT 弹出错误类 toast」一致）
  3. `after-click(false)`；业务侧可监听验证表单自带错误展示
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_007 - 验证 ElMessageBox.confirm 用户取消时不视为失败 toast

- **功能名称**：点击流水线顺序
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `C7Button` 配置 `confirm=true`，未使用 `confirm-fn`，`click-function` 带调用标记
- **执行步骤**：
  1. 点击按钮，在确认框中点击「取消」或关闭
  2. 观察 `click-function` 是否执行及界面提示
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. `click-function` 不被调用
  2. 无错误类 toast；`after-click(false)`（design：取消不视为 error toast）
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_008 - 验证 btnType=delete 且 confirm=true 时顺序为先确认再执行异步逻辑

- **功能名称**：验收场景
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `C7Button`：`btn-type="delete"`，`confirm=true`，`click-function` 返回延迟可辨的 Promise（如延迟 500ms resolve），便于观察先后顺序
- **执行步骤**：
  1. 点击按钮
  2. 在确认框未确认前，观察按钮 loading 与网络请求（不应已开始业务请求）
  3. 点击「确定」后，确认 `click-function` 执行（loading 仅在执行 `click-function` 阶段，见 TC_C7BTN_009）
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 必须先完成确认，才会进入 `click-function`
  2. 符合 `spec.md` Requirement「验收场景」第一条
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_009 - 验证 Promise 未完成前按钮 loading，且流水线执行中重复点击不重复执行异步逻辑

- **功能名称**：防抖与并发 / 验收场景
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `click-function` 返回较长延迟的 Promise（如 2s），不计数防抖二次触发前的首次点击
- **执行步骤**：
  1. 点击按钮一次，观察按钮 loading 状态
  2. 在 Promise 未完成期间快速多次点击按钮
  3. 记录 `click-function` 实际调用次数（可通过后端日志、浏览器 Network 或临时计数）
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 仅在 `click-function` 执行期间按钮处于 loading（`internal-loading` 语义）
  2. 异步未完成前重复点击不会导致 `click-function` 并发重复执行（`busy` 防重入）
  3. 符合 `spec.md` 验收场景第二条
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_010 - 验证 debounceDelay 默认语义为首击立即、同窗口内尾随点击不开启新流水线

- **功能名称**：防抖与并发
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `C7Button` 使用默认 `debounce-delay`（300ms），`click-function` 快速 resolve；在一次流水线完全结束后立刻计次
- **执行步骤**：
  1. 在一次点击导致的流水线已结束后的极短时间内（小于 `debounce-delay`）连续点击两次
  2. 或对同一按钮在首击后 300ms 内多次点击（此时若仍处于 busy，应与 TC_C7BTN_009 一致；本用例侧重 debounce 与 lodash `leading: true, trailing: false` 组合表现为无尾随补触发）
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 不出现「同一次用户意图下」重复排队多次完整流水线的情况；与 design 中防抖语义一致
  2. 具体断言方式可与开发约定（监听 `before-click` 次数或 `click-function` 调用次数）
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_011 - 验证 clickFunction resolve 后 checkSuccess 返回 false 时触发 error 且可展示失败提示

- **功能名称**：点击流水线顺序 / 与 axios 封装一致
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `click-function` `resolve` 任意载荷，`check-success` 对该结果返回 `false`，`show-error-toast=true`
- **执行步骤**：
  1. 点击按钮完成整条流水线
  2. 观察是否触发 `error` emit（若页面有监听）、是否有失败提示文案（优先 `error-message`）
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 不触发 `success`；触发 `error`，`after-click(false)`
  2. `show-error-toast=true` 时出现失败类提示
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_012 - 验证 Promise reject 表示失败，触发 error 与失败提示

- **功能名称**：与 axios 封装一致
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `click-function` 返回 `Promise.reject`（或抛出），`show-error-toast=true`
- **执行步骤**：
  1. 点击按钮
  2. 观察提示与 `after-click`
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 触发 `error`，`after-click(false)`
  2. 展示失败提示（文案优先 `error-message`，否则与 Error 信息一致，以实现为准）
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_013 - 验证 resolve 且 checkSuccess 默认通过时 success、after-click(true) 及成功提示规则

- **功能名称**：点击流水线顺序 / 成功提示
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. 第一次：`click-function` resolve，`success-message` 有文本，`success-notify=false`，观察 Message
  3. 第二次：`success-notify=true`，观察是否为 ElNotification（与设计一致）
- **执行步骤**：
  1. 配置成功场景并点击按钮
  2. 分别切换 `success-notify` 观察提示组件形态与文案
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 触发 `success`（载荷为 resolve 值），`after-click(true)`
  2. `success-notify=false` 且有 `success-message` 时出现消息提示；`success-notify=true` 时使用通知样式展示成功内容
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_014 - 验证 confirmFn 返回 false 时中止且不弹出错误类 toast

- **功能名称**：点击流水线顺序
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `confirm=true` 且 `confirm-fn` 返回 `Promise.resolve(false)` 或同步 `false`，`click-function` 带标记
- **执行步骤**：
  1. 点击按钮
  2. 观察是否调用 `click-function`
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 不调用 `click-function`；无错误类 toast；`after-click(false)`
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_015 - 验证关闭 showErrorToast 时 reject 或 checkSuccess 失败不弹出错误 toast

- **功能名称**：失败提示边界
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `show-error-toast=false`，`click-function` 分别配置 reject 与 resolve+checkSuccess false 两种场景（可分两条步骤复测）
- **执行步骤**：
  1. 先后触发失败两类场景并点击按钮
  2. 观察界面是否出现 Element Plus 错误 Message
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 均无错误类 toast；仍应 `after-click(false)` 并可通过监听 `error` 自行处理（以实现为准）
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

### TC_C7BTN_016 - 验证 validate=true 但 validateRef 无效时不进入表单 validate，且不弹错误类 toast

- **功能名称**：点击流水线顺序（配置健壮性）
- **执行状态**：✅ 通过
- **执行耗时**：单 spec 约 0.6–3.6s（TC_009 含 2s 异步）
- **前置条件**：
  1. 已完成 TC_C7BTN_001
  2. `validate=true`，`validate-ref` 未绑定表单或绑定对象无 `validate` 方法（故意错误配置）
- **执行步骤**：
  1. 点击按钮
  2. 观察控制台/事件与界面提示
- **关键步骤截图**（推荐）：见对应 `自动化测试/TC_xxx/` 下由 spec 生成的 `01_*.png`
- **预期结果**：
  1. 实现上会 `emit('error', …)` 并 `after-click(false)`，且不依赖错误 toast 告知终端用户（与组件内注释及「表单校验失败」区分）
  2. 若后续 spec 与实现收敛为仅 silent 失败，以评审结论为准并在备注更新
- **实际结果**：与清单预期一致；断言覆盖文案/class、计数器、MessageBox、loading、提示组件等。
- **验证结果**：✅ Playwright 断言通过

## 问题记录

本轮执行未发现需单独跟踪的缺陷（开发期演示页 `C7ButtonE2E.vue` 小调整见清单备注列）。

## 测试总结

### 测试执行情况

在 `quick-ui` 与后端可登录前提下，于 `openspec/changes/ui-c7-button/自动化测试/` 执行 `npx playwright test`，共 **17** 条运行记录（含 `auth.setup.ts` 一次 + `TC_C7BTN_001` 登录 + **16** 条清单用例），**全部通过**，全量约 **22s**（本机 Chrome channel、headless）。

### 测试结果分析

覆盖清单所列 16 条功能点：`PRESETS` 文案与类型 class、`beforeClick` 否决、表单校验短路、`MessageBox` 取消、`delete`+确认顺序、`busy`/loading、防抖窗口、`checkSuccess`/reject/成功双形态、`confirmFn`、`showErrorToast`、`validateRef` 无效等路径均有断言。

### 问题汇总

无阻塞项。TC_007 需注意页面存在多个「取消」文案按钮时应对 `dialog` 作用域定位。

### 改进建议

- CI 可为该目录增加可选 job（需预先启动 `pnpm dev` 与后端）。
- 若需肉眼调试 UI，使用 `npx playwright test … --headed`。
