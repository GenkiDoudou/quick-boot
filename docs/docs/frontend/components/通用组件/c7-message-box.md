# C7MessageBox 消息弹窗工具

在 **`ElMessageBox` / `ElLoading`** 之上提供 **函数式** API：**全局默认 options（浅合并）**、**统一 `Promise` 结构化返回值**（用户取消/关闭 **不 reject**）、**`asyncConfirm` 自动按钮 loading** 与 **`c7Loading`**。

**源码**：`quick-ui/src/packages/C7MessageBox/index.js`  
**OpenSpec**：`openspec/changes/ui-c7-messagebox/specs/ui-c7-messagebox/spec.md`

## 与全局注册

本模块 **无 `.vue`**，**不**通过 `installPackages` 注册。请在业务中 **按需 import**：

```js
import {
  setMessageBoxDefaults,
  c7Confirm,
  c7Alert,
  c7Prompt,
  c7DangerConfirm,
  c7Loading,
} from '@/packages'
```

## 应用入口（可选）

在 `main.js` / `main.ts` 中统一默认文案或行为：

```js
import { setMessageBoxDefaults } from '@/packages'

setMessageBoxDefaults({
  confirmButtonText: '确定',
  cancelButtonText: '取消',
  closeOnClickModal: false,
})
```

## API 摘要

| 符号 | 说明 |
|------|------|
| **`setMessageBoxDefaults(config)`** | 浅合并写入模块级默认 options；与单次调用合并时 **单次优先**。 |
| **`c7Confirm(message, title?, options?)`** | 确认/取消；支持 **`options.asyncConfirm`**、**`errorNotify`**、**`asyncConfirmLoadingText`**（默认 **`处理中...`**）。 |
| **`c7Alert(message, title?, options?)`** | 仅确定。 |
| **`c7Prompt(message, title?, options?)`** | 输入框；校验 **仅透传** EP 字段（如 **`inputValidator`**）。 |
| **`c7DangerConfirm(message, title?, options?)`** | 危险确认：预设 **`type: 'warning'`** 与 **`confirmButtonClass: 'el-button--danger'`**（可被入参覆盖）。 |
| **`c7Loading(text?, options?)`** | **`ElLoading.service`**，返回 **`{ close() }`**。 |

第二参 **`title`** 可与 Element Plus 一致：若传入 **对象**，则视为 **options**（即 **`c7Confirm(message, options)`**）。

## 统一返回值

所有对话框类函数均 **resolve** 为：

```ts
{ action: 'confirm' | 'cancel' | 'close'; value?: string }
```

- **`action`** 与当前 **Element Plus** 版本下 **`MessageBoxData.action`** 语义对齐。
- **`value`**：主要在 **`c7Prompt`** 成功时出现。

## `asyncConfirm` 示例

点击确定后 **await** 异步任务；成功自动关窗；失败 **保持弹窗**，由 **`errorNotify`** 自行决定是否 **`ElMessage`**：

```js
import { c7Confirm } from '@/packages'

const { action } = await c7Confirm('确认提交该单据？', '提示', {
  asyncConfirm: async () => {
    await api.submit()
  },
  errorNotify: (err) => {
    console.error(err)
  },
  asyncConfirmLoadingText: '提交中...',
})

if (action === 'confirm') {
  // 用户完成确认且异步成功
}
```

## `c7Loading` 示例

```js
import { c7Loading } from '@/packages'

const { close } = c7Loading('加载中...', { lock: true })
try {
  await fetchData()
} finally {
  close()
}
```

## 相关规格

- 设计说明：`docs/superpowers/specs/2026-05-08-c7-messagebox-design.md`
- 原始需求：`原始需求/前端/C7消息弹窗工具.md`
