# C7MessageBox 手工验收清单

对应 **`openspec/changes/ui-c7-messagebox/specs/ui-c7-messagebox/spec.md`** 中 Scenario 要点；实现完成后按序勾选。

## 模块与导出

- [ ] 自 **`@/packages`**（或 **`quick-ui/src/packages/index.js`**）可 **import** **`c7Confirm`** 等，**不**依赖 **`installPackages`** 注册组件。

## 默认与合并

- [ ] 调用 **`setMessageBoxDefaults({ confirmButtonText: '知道了' })`** 后，**`c7Alert`** 未覆盖时确定钮为「知道了」。
- [ ] 同场景下单次 **`c7Alert(..., { confirmButtonText: '好的' })`** 覆盖为「好的」。

## 取消 / 关闭不抛异常

- [ ] **`await c7Confirm('?', '提示')`** 点 **取消**：得到 **`resolve`**，**`action`** 为 **`cancel`** 或 **`close`**（与 EP 行为一致），控制台 **无** Unhandled rejection。

## `asyncConfirm`

- [ ] 配置 **`asyncConfirm`** 返回 **延迟 resolve** 的 **Promise**：点确定后确定钮进入 **loading**（及可选文案「处理中...」）；**resolve** 后弹窗关闭，**`action === 'confirm'`**。
- [ ] **`asyncConfirm`** **reject/throw** 且传入 **`errorNotify`**：**errorNotify** 被调用，弹窗 **仍打开**，可再次点确定或取消。
- [ ] 失败且 **未**传 **`errorNotify`**：开发构建下控制台有 **error** 日志（或可见失败信息），弹窗仍保持打开。

## `c7Prompt`

- [ ] 输入 **`hello`** 并确认：**`action === 'confirm'`** 且 **`value === 'hello'`**。
- [ ] 点取消：**resolve** + **`cancel`/`close`**，无未处理异常。

## `c7DangerConfirm`

- [ ] 弹窗确认钮为 **危险** 样式，**`type`** 为警告语义（与设计一致）。
- [ ] 取消路径仍为 **resolve** 结构化结果。

## `c7Loading`

- [ ] 调用返回的 **`close()`** 后 loading 消失。

## 与 `beforeClose` 同传（可选）

- [ ] 同时传入 **`asyncConfirm`** 与 EP **`beforeClose`**：异步成功后弹窗能关闭；若业务 **`beforeClose`** 未调用 **`done()`** 导致无法关闭，属业务误用（文档已说明由用户钩子负责 **`done`**）。
