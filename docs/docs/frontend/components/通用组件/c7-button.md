# C7Button 按钮

封装 `ElButton` 的**业务流水线**：校验 → 确认 → 异步 `clickFunction` → 成功提示 → 回调。

**源码**：`quick-ui/src/packages/C7Button/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `btnType` | 预设：决定默认文案、图标、`type`、`plain` |
| `clickFunction` | 异步业务函数；失败应 `reject` |
| `checkSuccess` | 可选，区分业务 code |
| `confirmMessage` | 非空时先 `MessageBox.confirm` |
| `disabled` / `size` | 同 Element Plus |

## 示例

```vue
<C7Button
  btn-type="save"
  :click-function="() => updateConfig(form)"
  v-hasPermi="['system:config:edit']"
/>
```

默认插槽可覆盖按钮文字。未传 `clickFunction` 时 dev 环境会告警。

## 相关

- [C7ButtonGroup](./c7-button-group)
- [C7MessageBox](./c7-message-box)
