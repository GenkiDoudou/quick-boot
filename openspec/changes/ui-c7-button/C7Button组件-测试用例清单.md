# C7Button组件-测试用例清单

## 测试环境信息

- **测试登录地址**：`http://localhost:8800/login`（以 `quick-ui` 本地 `pnpm dev` 为准，见 `vite.config.js`）
- **测试账号**：admin/admin

## 模块信息

- **模块名称**：C7Button 业务按钮组件
- **模块缩写**：C7BTN
- **模块页面地址**：无独立路由；在挂载了 `<C7Button>` 的任意业务页验证（tasks 曾示例 `quick-ui/src/views/system/user/index.vue`，以仓库实际引用为准；若无示例可在临时演示页挂载）
- **前端组件**：`quick-ui/src/packages/C7Button/index.vue`
- **后端接口路径**：无固定路径；`clickFunction` 内调用的接口由业务决定
- **测试用例总数**：16

---

## TC_C7BTN_001 - 合法账号登录并具备后续页面访问能力

**功能名称**：系统登录 / 鉴权前置

**用例标题**：验证使用合法账号成功登录并进入系统，会话有效

**前置条件**：

1. `quick-ui` 开发服务已启动，后端代理可达（默认 `/dev-api` → 后端，见项目配置）
2. 浏览器处于未登录或已清除本站会话状态

**测试步骤**：

1. 打开登录页（地址：`http://localhost:8800/login`）
2. 输入合法账号、密码
3. 点击登录

**预期结果**：

1. 登录成功，跳转至系统首页或约定的落地页
2. 会话有效（Token/Cookie 存在），可打开需要使用 `C7Button` 的业务页面

---

## TC_C7BTN_002 - 全局注册后模板可直接使用 C7Button

**功能名称**：C7Button 全局可用性

**用例标题**：验证应用入口注册后页面可直接使用 `<C7Button>` 无需局部注册

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 当前仓库 `quick-ui/src/main.js` 已通过 `installPackages(app)` 注册全局组件（与 `quick-ui/src/packages/index.js` 一致）

**测试步骤**：

1. 登录后进入已挂载 `<C7Button>` 的页面（或临时在调试页放置最小示例：`click-function` 返回 `Promise.resolve()`）
2. 打开浏览器开发者工具，确认无「组件未解析」类报错，按钮可见可点

**预期结果**：

1. 页面渲染正常，`<C7Button>` 表现为 Element Plus 按钮外观
2. 亦可按需 `import { C7Button } from '@/packages'` 使用，行为与全局一致（任选一种方式验证即可）

---

## TC_C7BTN_003 - btnType 预设的文案、类型与图标

**功能名称**：预设类型（btnType）

**用例标题**：验证各 btnType 默认 label、type、plain 与图标符合预设表

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 页面或使用 Story/演示片段中为每种待测 `btnType` 各放一个 `C7Button`，并提供最小的合法 `click-function`（如 `() => Promise.resolve()`）

**测试步骤**：

1. 依次查看 `btn-type` 为 `add`、`edit`、`delete`、`query`、`refresh`、`upload`、`download`、`submit`、`cancel` 的按钮（可与代码中 `PRESETS` 对照）
2. 观察按钮文案、色调（type）、是否 plain、左侧图标是否与预设一致

**预期结果**：

1. 各预设与 `spec.md`「预设类型」及实现 `PRESETS` 一致（例如 `delete` 为危险色、`refresh` 默认文案为「重置」等）
2. 未传 `btn-type` 时，依赖显式 `label`/`type` 等与默认 `type`，不出现非法预设键导致的崩溃

---

## TC_C7BTN_004 - 显式 props 覆盖预设字段

**功能名称**：预设类型（btnType）

**用例标题**：验证显式传入的 label、type、plain、size 覆盖对应预设

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 页面中存在 `btn-type="add"` 的 `C7Button`，同时传入与预设不同的 `label`、`type`、`plain`、`size`

**测试步骤**：

1. 配置例如：`btn-type="add"`，`label="自定义"`，`type="warning"`，`plain=true`，`size="small"`
2. 渲染并观察按钮外观与文案

**预期结果**：

1. 按钮展示为自定义文案与显式的 type/plain/size，而非 `add` 预设的全部默认值

---

## TC_C7BTN_005 - beforeClick 否决中止流水线且无错误类提示

**功能名称**：点击流水线顺序

**用例标题**：验证 beforeClick 返回 false 时中止流水线且不弹出错误类 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 页面挂载 `C7Button`，配置 `before-click`（或实现内 `beforeClick`）返回 `false`，`click-function` 内可做标记（如计数）以便判断是否被执行

**测试步骤**：

1. 点击按钮
2. 观察是否触发确认框、`click-function` 是否被调用
3. 观察界面提示与事件（若页面绑定了 `after-click`，应为 `false`）

**预期结果**：

1. 先触发 `before-click`（emit），随后 `beforeClick` 否决后流水线中止
2. 无确认框（若仅依赖 beforeClick 否决）、`click-function` 不被调用
3. 无错误类 toast（Message 错误提示）；`after-click` 为 `false`

---

## TC_C7BTN_006 - 表单校验失败时不进入确认与 clickFunction

**功能名称**：点击流水线顺序 / 验收场景

**用例标题**：验证 validate=true 且 ElForm 校验失败时不进入确认与 clickFunction，且无错误类 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 页面存在未通过校验的 `el-form`，`validate-ref` 绑定为该表单实例；`C7Button` 配置 `validate=true`，`confirm=true`，`click-function` 内设有调用标记

**测试步骤**：

1. 保持表单在校验失败状态（必填为空等）
2. 点击 `C7Button`
3. 观察是否弹出确认框、`click-function` 是否执行

**预期结果**：

1. 不弹出确认框，`click-function` 不被调用
2. 无错误类 toast（与 spec「校验失败 MUST NOT 弹出错误类 toast」一致）
3. `after-click(false)`；业务侧可监听验证表单自带错误展示

---

## TC_C7BTN_007 - 确认框用户取消中止流水线且无错误类提示

**功能名称**：点击流水线顺序

**用例标题**：验证 ElMessageBox.confirm 用户取消时不视为失败 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `C7Button` 配置 `confirm=true`，未使用 `confirm-fn`，`click-function` 带调用标记

**测试步骤**：

1. 点击按钮，在确认框中点击「取消」或关闭
2. 观察 `click-function` 是否执行及界面提示

**预期结果**：

1. `click-function` 不被调用
2. 无错误类 toast；`after-click(false)`（design：取消不视为 error toast）

---

## TC_C7BTN_008 - delete 预设且 confirm 时先确认再执行 clickFunction

**功能名称**：验收场景

**用例标题**：验证 btnType=delete 且 confirm=true 时顺序为先确认再执行异步逻辑

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `C7Button`：`btn-type="delete"`，`confirm=true`，`click-function` 返回延迟可辨的 Promise（如延迟 500ms resolve），便于观察先后顺序

**测试步骤**：

1. 点击按钮
2. 在确认框未确认前，观察按钮 loading 与网络请求（不应已开始业务请求）
3. 点击「确定」后，确认 `click-function` 执行（loading 仅在执行 `click-function` 阶段，见 TC_C7BTN_009）

**预期结果**：

1. 必须先完成确认，才会进入 `click-function`
2. 符合 `spec.md` Requirement「验收场景」第一条

---

## TC_C7BTN_009 - clickFunction 执行期 loading 且 busy 阻止重入

**功能名称**：防抖与并发 / 验收场景

**用例标题**：验证 Promise 未完成前按钮 loading，且流水线执行中重复点击不重复执行异步逻辑

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `click-function` 返回较长延迟的 Promise（如 2s），不计数防抖二次触发前的首次点击

**测试步骤**：

1. 点击按钮一次，观察按钮 loading 状态
2. 在 Promise 未完成期间快速多次点击按钮
3. 记录 `click-function` 实际调用次数（可通过后端日志、浏览器 Network 或临时计数）

**预期结果**：

1. 仅在 `click-function` 执行期间按钮处于 loading（`internal-loading` 语义）
2. 异步未完成前重复点击不会导致 `click-function` 并发重复执行（`busy` 防重入）
3. 符合 `spec.md` 验收场景第二条

---

## TC_C7BTN_010 - 防抖 leading 语义下同窗口尾随点击不重复排队

**功能名称**：防抖与并发

**用例标题**：验证 debounceDelay 默认语义为首击立即、同窗口内尾随点击不开启新流水线

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `C7Button` 使用默认 `debounce-delay`（300ms），`click-function` 快速 resolve；在一次流水线完全结束后立刻计次

**测试步骤**：

1. 在一次点击导致的流水线已结束后的极短时间内（小于 `debounce-delay`）连续点击两次
2. 或对同一按钮在首击后 300ms 内多次点击（此时若仍处于 busy，应与 TC_C7BTN_009 一致；本用例侧重 debounce 与 lodash `leading: true, trailing: false` 组合表现为无尾随补触发）

**预期结果**：

1. 不出现「同一次用户意图下」重复排队多次完整流水线的情况；与 design 中防抖语义一致
2. 具体断言方式可与开发约定（监听 `before-click` 次数或 `click-function` 调用次数）

---

## TC_C7BTN_011 - checkSuccess 为 false 时走失败分支与错误提示

**功能名称**：点击流水线顺序 / 与 axios 封装一致

**用例标题**：验证 clickFunction resolve 后 checkSuccess 返回 false 时触发 error 且可展示失败提示

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `click-function` `resolve` 任意载荷，`check-success` 对该结果返回 `false`，`show-error-toast=true`

**测试步骤**：

1. 点击按钮完成整条流水线
2. 观察是否触发 `error` emit（若页面有监听）、是否有失败提示文案（优先 `error-message`）

**预期结果**：

1. 不触发 `success`；触发 `error`，`after-click(false)`
2. `show-error-toast=true` 时出现失败类提示

---

## TC_C7BTN_012 - clickFunction reject 时失败提示与 error

**功能名称**：与 axios 封装一致

**用例标题**：验证 Promise reject 表示失败，触发 error 与失败提示

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `click-function` 返回 `Promise.reject`（或抛出），`show-error-toast=true`

**测试步骤**：

1. 点击按钮
2. 观察提示与 `after-click`

**预期结果**：

1. 触发 `error`，`after-click(false)`
2. 展示失败提示（文案优先 `error-message`，否则与 Error 信息一致，以实现为准）

---

## TC_C7BTN_013 - 成功路径 success emit 与 successMessage/successNotify

**功能名称**：点击流水线顺序 / 成功提示

**用例标题**：验证 resolve 且 checkSuccess 默认通过时 success、after-click(true) 及成功提示规则

**前置条件**：

1. 已完成 TC_C7BTN_001
2. 第一次：`click-function` resolve，`success-message` 有文本，`success-notify=false`，观察 Message
3. 第二次：`success-notify=true`，观察是否为 ElNotification（与设计一致）

**测试步骤**：

1. 配置成功场景并点击按钮
2. 分别切换 `success-notify` 观察提示组件形态与文案

**预期结果**：

1. 触发 `success`（载荷为 resolve 值），`after-click(true)`
2. `success-notify=false` 且有 `success-message` 时出现消息提示；`success-notify=true` 时使用通知样式展示成功内容

---

## TC_C7BTN_014 - confirmFn 返回 false 时中止且无错误 toast

**功能名称**：点击流水线顺序

**用例标题**：验证 confirmFn 返回 false 时中止且不弹出错误类 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `confirm=true` 且 `confirm-fn` 返回 `Promise.resolve(false)` 或同步 `false`，`click-function` 带标记

**测试步骤**：

1. 点击按钮
2. 观察是否调用 `click-function`

**预期结果**：

1. 不调用 `click-function`；无错误类 toast；`after-click(false)`

---

## TC_C7BTN_015 - showErrorToast=false 时失败不弹出错误 toast

**功能名称**：失败提示边界

**用例标题**：验证关闭 showErrorToast 时 reject 或 checkSuccess 失败不弹出错误 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `show-error-toast=false`，`click-function` 分别配置 reject 与 resolve+checkSuccess false 两种场景（可分两条步骤复测）

**测试步骤**：

1. 先后触发失败两类场景并点击按钮
2. 观察界面是否出现 Element Plus 错误 Message

**预期结果**：

1. 均无错误类 toast；仍应 `after-click(false)` 并可通过监听 `error` 自行处理（以实现为准）

---

## TC_C7BTN_016 - validateRef 无效配置时的行为与表单校验失败区分

**功能名称**：点击流水线顺序（配置健壮性）

**用例标题**：验证 validate=true 但 validateRef 无效时不进入表单 validate，且不弹错误类 toast

**前置条件**：

1. 已完成 TC_C7BTN_001
2. `validate=true`，`validate-ref` 未绑定表单或绑定对象无 `validate` 方法（故意错误配置）

**测试步骤**：

1. 点击按钮
2. 观察控制台/事件与界面提示

**预期结果**：

1. 实现上会 `emit('error', …)` 并 `after-click(false)`，且不依赖错误 toast 告知终端用户（与组件内注释及「表单校验失败」区分）
2. 若后续 spec 与实现收敛为仅 silent 失败，以评审结论为准并在备注更新

---

## 测试用例统计

### 按功能分类统计

| 功能模块 | 测试用例数量 |
|---------|------------|
| 登录 / 鉴权前置 | 1 |
| 全局注册与可用性 | 1 |
| 预设类型与 props 覆盖 | 2 |
| 流水线中止（beforeClick / 校验 / 确认取消 / confirmFn） | 4 |
| 删除确认顺序（验收） | 1 |
| loading、busy、防抖 | 2 |
| 成功 / 失败判定与提示（含 axios 契约） | 3 |
| 配置边界（showErrorToast、validateRef） | 2 |
| **总计** | **16** |

### 按测试类型分类统计

| 测试类型 | 测试用例数量 |
|---------|------------|
| 正向流程（主流程 / 成功路径） | 8 |
| 异常流程（否决、校验失败、取消、reject、业务失败、无效 validateRef） | 7 |
| 边界（关闭失败 toast） | 1 |
| **总计** | **16** |

---

## 备注

1. **数据来源**：OpenSpec 变更 `ui-c7-button`；依据 `proposal.md`、`design.md`、`specs/ui-c7-button/spec.md`、`tasks.md` 推导；与 `quick-ui/src/packages/C7Button/index.vue` 行为不一致时以实现为准并迭代本清单。
2. **原始需求**：`原始需求/前端/C7按钮.md`（提案引用）。
3. **事件契约**：以 emit `before-click`、`success`、`error`、`after-click(success)` 为准；勿在外层对 `C7Button` 使用 `@click`（design）。
4. **示例页面**：tasks 提及 `views/system/user/index.vue` 替换示例，当前仓库检索可能无 `<C7Button>` 引用时，可在任意页面临时挂载最小示例完成 UI 验证。
5. **spec 与实现**：表单校验失败路径实现为 `catch` 后仅 `after-click(false)`；`validateRef` 无效时实现额外 `emit('error')`。若归档前统一口径，请同步更新 spec 或组件二者之一。
