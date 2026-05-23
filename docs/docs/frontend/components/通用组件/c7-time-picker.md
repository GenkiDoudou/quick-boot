# C7TimePicker 时间选择器

封装 `ElTimePicker`，支持 `rangeMerge` 将范围值合并为逗号分隔字符串。

**源码**：`quick-ui/src/packages/C7TimePicker/index.vue`

## 主要 Props

| 属性 | 说明 |
|------|------|
| `modelValue` | 单值或范围 |
| `rangeMerge` | 范围是否合并为字符串 |
| `mergeDelimiter` | 分隔符，默认 `,` |

其余属性透传 Element Plus（`is-range`、`format` 等）。

## 相关

- [C7DatePicker](./c7-date-picker)
