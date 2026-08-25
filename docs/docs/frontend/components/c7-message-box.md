# C7MessageBox

## 用途

命令式 MessageBox / 确认框封装（非 Vue SFC）。

源码：`quick-ui/src/packages/C7MessageBox/index.js`

## 公开 API

| 名称 | 类型 | 说明 |
| --- | --- | --- |
| setMessageBoxDefaults | function | 见源码 |
| mergeMessageBoxOptions | function | 见源码 |
| mapMessageBoxResolve | function | 见源码 |
| mapMessageBoxReject | function | 见源码 |
| splitTitleAndOptions | function | 见源码 |
| c7Confirm | function | 见源码 |
| c7Alert | function | 见源码 |
| c7Prompt | function | 见源码 |
| c7DangerConfirm | function | 见源码 |
| c7Loading | function | 见源码 |


## 示例

```js
import { c7Confirm } from '@/packages/C7MessageBox'
await c7Confirm('确认操作？')
```
