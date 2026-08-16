# quick-h5 JSON 卡片列表（对齐 C7 字段配置思路）

日期：2026-08-16  
状态：已实现（OpenSpec `quick-h5-json-card-list`）  
范围：通用 `QbJsonCardList` + 用户管理列表样板  
非目标：完整搬迁 C7JsonTable（搜索表单、导入导出、批量删、listFunction 内置）

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 配置范围 | 仅卡片字段（cardColumns） |
| 落地 | 通用组件 + 用户管理先接 |
| 渲染 | text / dictTag / slot |
| 操作区 | 页面 slot 手写（权限逻辑不变） |

## 2. 目标

用类似 C7 `tableColumns` 的 JSON 描述卡片 meta 区字段，减少用户列表里手写 `qb-row` / `qb-kv` 重复结构；其它列表后续可复用。

## 3. 组件 API（草案）

组件路径：`quick-h5/src/components/qb/QbJsonCardList.vue`（或拆 `QbJsonCardFields` 只渲染字段区）。

推荐拆两层，更贴现有结构：

| 组件 | 职责 |
|------|------|
| `QbJsonCardFields` | 只渲染 `columns` → `qb-row` + `qb-col` + `qb-kv` |
| 用户页 | 继续用 `QbListCard` + `QbSearchBar` + actions slot；meta 槽内挂 `QbJsonCardFields` |

### 3.1 column 配置项

```ts
type QbCardColumn = {
  prop: string           // 行数据字段
  label: string          // 展示标签
  span?: number          // 24 栅格：6|8|12|16|24，默认 12
  kv?: 'row' | 'stack'   // 标签值同行或上下，默认 row
  type?: 'text' | 'dict' | 'slot'  // 默认 text
  dictType?: string      // type=dict 时用 useDict（或由页面传入 options）
  options?: DictOption[] // type=dict 时可直接传选项
  slotName?: string      // type=slot 时插槽名，默认 prop
  emptyText?: string     // 空值展示，默认 '—'
  show?: boolean | ((row) => boolean)  // 可选显隐；一期可只支持布尔或简单 v-if 由页面过滤
}
```

一期最小实现：`prop` / `label` / `span` / `kv` / `type` / `options`（dict）/ `slot`；`show` 可用 `v-if` 等价：列上 `showWhen: 'email'`（有值才显示）或页面 computed 过滤。

推荐一期 `show`：支持 `showIfProp`（属性有值才渲染），覆盖邮箱场景。

### 3.2 模板用法（用户页）

```vue
<QbListCard :title="..." :subtitle="...">
  <template #status>...</template>
  <template #meta>
    <view class="user-meta">
      <QbJsonCardFields :row="row" :columns="cardColumns" />
    </view>
  </template>
  <template #actions>...</template>
</QbListCard>
```

```ts
const cardColumns = computed(() => [
  { prop: 'deptName', label: '部门', span: 12, kv: 'row' },
  { prop: 'phonenumber', label: '手机', span: 12, kv: 'row' },
  { prop: 'roleNames', label: '角色', span: 24, kv: 'stack' },
  { prop: 'email', label: '邮箱', span: 24, kv: 'row', showIfProp: true },
])
```

状态仍放在 `#status`（`QbDictTag`），不塞进 columns（与现交互一致）。

## 4. 与 C7 的对应关系

| C7JsonTable | H5 一期 |
|-------------|--------|
| `tableColumns[].prop/label` | 同 |
| `minWidth` / `fixed` | 忽略，用 `span` |
| `columnType: 'tag'` | `type: 'dict'` + options |
| `columnType: 'slot'` | `type: 'slot'` |
| `searchColumns` / `listFunction` | 不做 |
| 工具栏增删导入导出 | 不做 |

## 5. 文件清单

| 文件 | 动作 |
|------|------|
| `components/qb/QbJsonCardFields.vue` | 新增 |
| `pages/system/user/index.vue` | 改用 columns 配置 |
| `common/style.scss` | 一般不改（复用 qb-row/col/kv） |

## 6. 验收

1. 用户列表视觉与现网接近（部门/手机同行半宽，角色 stack 整行，邮箱有值才显示且同行）
2. 改 `cardColumns` 的 span/kv/顺序即可调布局，无需改模板结构
3. 操作按钮与权限行为不变

## 7. 后续（非本期）

- 搜索列 JSON、`listFunction` 封装成 `QbJsonListPage`
- `type: dict` 内联 `useDict(dictType)`
- 推广到角色/部门/运维列表

---

请审阅。回复「设计可以」后开始实现；若要改组件名或 columns 字段名，直接说。
