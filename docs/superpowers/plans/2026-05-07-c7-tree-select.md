# C7TreeSelect Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `quick-ui` 中新增 **`C7TreeSelect`**（基于 `ElTreeSelect`），实现与设计 spec `docs/superpowers/specs/2026-05-07-c7-tree-select-design.md` 一致的静态/异步整树、`separator`/`valueType`、字段映射、`reload()`、`load-error`，并完成注册、原始需求同步与 VitePress 文档。

**Architecture:** 单文件 `C7TreeSelect/index.vue` 内完成：与 `C7Select` 相同的 `fetchData`/`resultKey`/`dataFormatter`/`autoLoad` 非远程路径、树数据 `mapTree` 规范为 `{ label, value, children }` 后绑定 `:data`；`defineModel` + 内外 `outerToInner`/`innerToOuter` 对齐 `C7Select` 的多选 `separator` 规则；`valueType` 单选/多选元素在内外边界做显式转换；`inheritAttrs: false` + `forwardedAttrs` 透传其余 EP 属性（含 `filterable`、`filter-node-method`、`multiple`、`check-strictly` 等）。

**Tech Stack:** Vue 3.5 SFC、`element-plus` ^2.10、`lodash/get`、`pnpm`；**无** Vitest（`quick-ui/package.json` 未声明测试脚本），回归以 **`pnpm build:prod`** 为主。

**工作树说明：** writing-plans 技能建议在独立 worktree 执行；若当前已在功能分支上，可直接在 `quick-ui` 子目录按任务顺序提交，无需强制新建 worktree。

---

## 文件结构（创建 / 修改）

| 文件 | 职责 |
|------|------|
| `quick-ui/src/packages/C7TreeSelect/index.vue` | **新建**：组件实现 |
| `quick-ui/src/packages/index.js` | **修改**：import / export / `installPackages` 注册 `C7TreeSelect` |
| `原始需求/前端/C7树选择.md` | **修改**：`rangeMerge` → `separator`，与实现对齐 |
| `docs/docs/frontend/components/通用组件/c7-tree-select.md` | **新建**：使用说明（sidebar 已存在链接，文件可能缺失） |

---

### Task 1: 新建 `C7TreeSelect/index.vue`（完整实现一次落盘）

**Files:**

- Create: `quick-ui/src/packages/C7TreeSelect/index.vue`
- Modify: （无）
- Test: 在 Task 5 统一执行 `pnpm build:prod`

- [ ] **Step 1: 新建文件并粘贴下列完整 SFC**

下列实现满足 design spec：`dataList`/`options` 优先级、`fetchData`+`resultKey`+`dataFormatter`、`autoLoad`（无静态绑定时挂载拉取）、失败 **`emit('load-error', err)`** 且保留上次树数据、`separator` 与 `C7Select` 相同的逗号规则、`valueType` auto 取 **映射后根列表首节点** `value` 字段的 `typeof`、`reload()`、`visible-change`/`loading-change` 与 `C7Select` 对齐、`defineExpose({ reload, loading, treeSelectRef })`。

```vue
<template>
  <el-tree-select
      ref="treeSelectRef"
      v-bind="forwardedAttrs"
      :data="displayTreeData"
      :model-value="innerModel"
      :loading="mergedLoading"
      @update:model-value="onInnerModelUpdate"
      @change="onInnerChange"
      @visible-change="onVisibleChange"
  />
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref, shallowRef, useAttrs, watch} from 'vue'
import get from 'lodash/get'

defineOptions({name: 'C7TreeSelect', inheritAttrs: false})

/**
 * C7 树选择：在 `ElTreeSelect` 上统一静态/异步整树加载、字段映射、多选 `separator` 与 `valueType`。
 *
 * **静态**：`dataList` 与 `options` 为别名；**`dataList !== undefined` 时仅用 `dataList`**（与 `C7Select` 一致）。
 * **异步**：`autoLoad` 为 true、且无静态 props 绑定时，挂载后 `fetchData({ ...fetchParams })`（**无 `query`**）。
 * **树字段**：内部将节点规范为 `{ label, value, children }` 供 EP 使用（`mapTree`）。
 * **多选 + `separator`**：对外 `v-model`/`change` 为逗号字符串，空为 `''`；对内为数组（与 `C7Select` 一致）。
 * **`valueType`**：`auto` 时以 **规范树根节点第一条** 的 `value` 的 `typeof` 为准（仅为 `number` 时用 number，否则按 string）；大整数/精度问题与 `Number()` 固有限制相同，业务应避免超大 ID。
 *
 * @emits update:modelValue
 * @emits change 载荷与对外 `modelValue` 一致
 * @emits load-error 异步失败
 * @emits visible-change 下拉可见性
 * @emits loading-change 请求进行中为 true
 */

const RESERVED_ATTR_KEYS = new Set([
  'dataList',
  'options',
  'fetchData',
  'fetchParams',
  'resultKey',
  'dataFormatter',
  'autoLoad',
  'separator',
  'modelValue',
  'multiple',
  'labelKey',
  'valueKey',
  'childrenKey',
  'valueType'
])

const props = defineProps({
  dataList: {type: Array, default: undefined},
  options: {type: Array, default: undefined},
  fetchData: {type: Function, default: undefined},
  fetchParams: {type: Object, default: () => ({})},
  resultKey: {type: String, default: ''},
  dataFormatter: {type: Function, default: undefined},
  autoLoad: {type: Boolean, default: false},
  multiple: {type: Boolean, default: false},
  separator: {type: Boolean, default: false},
  labelKey: {type: String, default: 'label'},
  valueKey: {type: String, default: 'value'},
  childrenKey: {type: String, default: 'children'},
  /** `auto`：由规范树根首节点的 `value` 类型推断对外单选标量/多选元素类型 */
  valueType: {type: String, default: 'auto', validator: (v) => ['auto', 'string', 'number'].includes(v)}
})

const emit = defineEmits(['update:modelValue', 'change', 'load-error', 'visible-change', 'loading-change'])

const attrs = useAttrs()

const forwardedAttrs = computed(() => {
  const out = {}
  for (const key of Object.keys(attrs)) {
    if (RESERVED_ATTR_KEYS.has(key)) continue
    out[key] = attrs[key]
  }
  return out
})

const modelValue = defineModel()
const treeSelectRef = ref(null)

const staticBindingDefined = computed(() => props.dataList !== undefined || props.options !== undefined)

const staticTreeRaw = computed(() => {
  if (props.dataList !== undefined) return props.dataList
  if (props.options !== undefined) return props.options
  return []
})

const asyncTreeRaw = shallowRef([])

const resolvedSourceRows = computed(() => {
  if (staticBindingDefined.value) return staticTreeRaw.value
  return asyncTreeRaw.value
})

/**
 * 将业务树映射为 EP 需要的 `label`/`value`/`children`。
 * @param {*} nodes
 * @returns {Array<{label: *, value: *, children?: *}>}
 */
function mapTree(nodes) {
  if (!Array.isArray(nodes)) return []
  const lk = props.labelKey
  const vk = props.valueKey
  const ck = props.childrenKey
  return nodes.map((n) => {
    if (!n || typeof n !== 'object') return {label: '', value: undefined, children: undefined}
    const children = Array.isArray(n[ck]) && n[ck].length > 0 ? mapTree(n[ck]) : undefined
    return {
      label: n[lk],
      value: n[vk],
      children,
      disabled: n.disabled === true
    }
  })
}

const displayTreeData = computed(() => mapTree(resolvedSourceRows.value))

/** `auto` 模式下用于单选/多选元素类型：仅根第一条映射后节点的 `value` 为 `number` 时为 `number`，否则 `string` */
const autoCoerceKind = computed(() => {
  const rows = displayTreeData.value
  if (!rows.length) return 'string'
  const t = typeof rows[0].value
  return t === 'number' && !Number.isNaN(rows[0].value) ? 'number' : 'string'
})

const effectiveValueKind = computed(() => {
  if (props.valueType === 'string') return 'string'
  if (props.valueType === 'number') return 'number'
  return autoCoerceKind.value
})

/**
 * @param {*} v
 * @returns {*}
 */
function coerceOutScalar(v) {
  if (v === undefined || v === null) return v
  if (effectiveValueKind.value === 'number') {
    const n = Number(v)
    return Number.isNaN(n) ? v : n
  }
  return String(v)
}

/**
 * @param {*} v
 * @returns {*}
 */
function coerceInScalar(v) {
  if (v === undefined || v === null) return v
  if (effectiveValueKind.value === 'number') {
    const n = Number(v)
    return Number.isNaN(n) ? v : n
  }
  return String(v)
}

/**
 * @param {*} outer
 * @returns {*}
 */
function outerToInner(outer) {
  if (!props.multiple) {
    if (outer === undefined || outer === null || outer === '') return undefined
    return coerceInScalar(outer)
  }
  if (props.separator) {
    if (outer == null || outer === '') return []
    if (Array.isArray(outer)) return outer.map((x) => coerceInScalar(x))
    return String(outer)
        .split(',')
        .map((s) => s.trim())
        .filter((s) => s.length > 0)
        .map((x) => coerceInScalar(x))
  }
  if (Array.isArray(outer)) return outer.map((x) => coerceInScalar(x))
  if (outer == null || outer === '') return []
  return [coerceInScalar(outer)]
}

/**
 * @param {*} inner
 * @returns {*}
 */
function innerToOuter(inner) {
  if (!props.multiple) {
    if (inner === undefined || inner === null || inner === '') return undefined
    return coerceOutScalar(inner)
  }
  if (props.separator) {
    if (!inner || inner.length === 0) return ''
    return inner.map((v) => String(v)).join(',')
  }
  if (!inner || inner.length === 0) return []
  return inner.map((v) => coerceOutScalar(v))
}

const innerModel = ref(undefined)

watch(
    () => modelValue.value,
    (v) => {
      innerModel.value = outerToInner(v)
    },
    {immediate: true, deep: true}
)

watch(
    () => props.multiple,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => props.separator,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => props.valueType,
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

watch(
    () => [props.labelKey, props.valueKey, props.childrenKey],
    () => {
      innerModel.value = outerToInner(modelValue.value)
    }
)

/** 异步树首帧为空时 `auto` 可能为 string；数据到达后需按新首节点类型重算内外值 */
watch(autoCoerceKind, () => {
  innerModel.value = outerToInner(modelValue.value)
})

let fetchGeneration = 0
const inFlightCount = ref(0)
const loadingInternal = computed(() => inFlightCount.value > 0)

const mergedLoading = computed(() => loadingInternal.value || !!attrs.loading)

watch(
    loadingInternal,
    (v) => {
      emit('loading-change', v)
    },
    {flush: 'post'}
)

/**
 * @param {Record<string, *>} mergedParams
 */
async function executeFetch(mergedParams) {
  if (typeof props.fetchData !== 'function') return
  const gen = ++fetchGeneration
  inFlightCount.value++
  try {
    const res = await props.fetchData(mergedParams)
    if (gen !== fetchGeneration) return
    const rawData = res?.data
    let list = rawData
    if (props.resultKey) list = get(rawData, props.resultKey)
    if (typeof props.dataFormatter === 'function') list = props.dataFormatter(list)
    if (!Array.isArray(list)) list = []
    asyncTreeRaw.value = list
  } catch (err) {
    emit('load-error', err)
  } finally {
    inFlightCount.value--
  }
}

onMounted(() => {
  if (staticBindingDefined.value) return
  if (!props.autoLoad || typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
})

onMounted(() => {
  if (props.autoLoad && typeof props.fetchData !== 'function') {
    if (import.meta.env.DEV) {
      console.warn('[C7TreeSelect] autoLoad=true 但未提供 fetchData，已跳过。')
    }
  }
})

/**
 * @param {*} v
 */
function onInnerModelUpdate(v) {
  innerModel.value = v
  emit('update:modelValue', innerToOuter(v))
}

/**
 * @param {*} v
 */
function onInnerChange(v) {
  emit('change', innerToOuter(v))
}

/**
 * @param {boolean} visible
 */
function onVisibleChange(visible) {
  emit('visible-change', visible)
}

function reload() {
  if (staticBindingDefined.value) {
    innerModel.value = outerToInner(modelValue.value)
    return
  }
  if (typeof props.fetchData !== 'function') return
  executeFetch({...props.fetchParams})
}

defineExpose({
  loading: loadingInternal,
  reload,
  treeSelectRef
})

onUnmounted(() => {
  fetchGeneration++
})
</script>
```

- [ ] **Step 2: 对照 `C7Select` 快速复读**

打开 `quick-ui/src/packages/C7Select/index.vue`，确认：`executeFetch` 从 `res?.data` 取根再 `resultKey`；本任务在 **catch** 中 **`emit('load-error')`**（树 spec 要求），且失败**不**覆盖 `asyncTreeRaw`（与 `C7Select` 静默保留一致）。

- [ ] **Step 3: Commit**

```bash
git add quick-ui/src/packages/C7TreeSelect/index.vue
git commit -m "feat(ui): add C7TreeSelect component"
```

---

### Task 2: 注册 `C7TreeSelect`

**Files:**

- Modify: `quick-ui/src/packages/index.js`

- [ ] **Step 1: 在 `C7Title` import 下一行增加**

```js
import C7TreeSelect from './C7TreeSelect/index.vue'
```

- [ ] **Step 2: 扩展命名导出**

在 `export { ... C7Title }` 中加入 `C7TreeSelect`（保持单行或按项目现有换行风格）。

- [ ] **Step 3: 在 `installPackages` 内注册**

```js
app.component('C7TreeSelect', C7TreeSelect)
```

- [ ] **Step 4: Commit**

```bash
git add quick-ui/src/packages/index.js
git commit -m "feat(ui): register C7TreeSelect in packages index"
```

---

### Task 3: 同步原始需求文档

**Files:**

- Modify: `原始需求/前端/C7树选择.md`

- [ ] **Step 1: 将「`rangeMerge=true` → 逗号字符串」改为「`separator=true` → 逗号字符串」**，并在「输出」小节加一句：**与 `C7Select` 的 `separator` 命名一致**。

示例片段（按你文件实际标题微调即可）：

```markdown
- 输出：
  - `separator=true` → 逗号字符串（与 `C7Select` 一致）
  - 否则数组
```

- [ ] **Step 2: Commit**

```bash
git add "原始需求/前端/C7树选择.md"
git commit -m "docs(需求): C7树选择 rangeMerge 更名为 separator"
```

---

### Task 4: VitePress 组件文档 `c7-tree-select.md`

**Files:**

- Create: `docs/docs/frontend/components/通用组件/c7-tree-select.md`
- Modify: （无，除非 `sidebar.ts` 缺少该项；当前已有 `c7-tree-select` 链接则跳过 sidebar）

- [ ] **Step 1: 新建文档**（与 `c7-switch.md` 同层级结构）

```markdown
# C7TreeSelect 树形选择器

在 **`ElTreeSelect`** 上封装 **静态/异步整树**、**字段映射**、**多选 `separator` 逗号串** 与 **`valueType`**，契约对齐 **`C7Select`**（`dataList`/`options`、`fetchData`、`resultKey`、`dataFormatter`、`autoLoad`）。

**源码**：`quick-ui/src/packages/C7TreeSelect/index.vue`

## 功能概要

- **数据**：`dataList` 优先于 `options`；无静态绑定时 `fetchData` + `resultKey`（可选 `dataFormatter`）拉取整树。
- **映射**：`labelKey` / `valueKey` / `childrenKey`（默认 `label`/`value`/`children`），内部规范为 EP 树节点。
- **多选**：`multiple`；`separator` 为 true 时对外为逗号字符串，空为 `''`。
- **`valueType`**：`auto` | `string` | `number`；`auto` 由**映射后根列表首节点**的 `value` 类型推断。
- **透传**：`filterable`、`filter-node-method` 等未占用键通过 attrs 透传至 `ElTreeSelect`。
- **事件**：`update:modelValue`、`change`、`load-error`、`visible-change`、`loading-change`。
- **方法**：`reload()`（静态时用于重新同步内外值；异步时重新 `fetchData`）。

## 与全局注册

`main.js` 调用 `installPackages(app)` 后，可使用 `<c7-tree-select />` / `<C7TreeSelect />`。

## 限制

- 若多选 value 本身含英文逗号，**勿**使用 `separator` 模式（与 `C7Select` 相同）。
- 一期不支持节点 `lazy` 远程懒加载（见 design spec §2.2）。

## 相关规格

设计说明：`docs/superpowers/specs/2026-05-07-c7-tree-select-design.md`
```

- [ ] **Step 2: Commit**

```bash
git add docs/docs/frontend/components/通用组件/c7-tree-select.md
git commit -m "docs: add C7TreeSelect VitePress page"
```

---

### Task 5: 构建验证

**Files:** 无

- [ ] **Step 1: 安装依赖（若尚未安装）**

```bash
cd quick-ui
pnpm i
```

预期：命令结束码 0，`node_modules` 存在。

- [ ] **Step 2: 生产构建**

```bash
pnpm build:prod
```

预期：Vite build **成功**，无 `C7TreeSelect` 相关编译错误。

- [ ] **Step 3:（可选）文档站点构建**

```bash
cd ../docs
pnpm i
pnpm build
```

预期：构建成功且新页面可被静态路由解析（无死链）。

- [ ] **Step 4: Commit**

若无代码变更仅验证通过，可跳过本步 commit；若有 `pnpm-lock` 等连带变更则单独说明后提交。

---

## Spec 覆盖核对（自检）

| Design / Spec 条款 | 对应任务 |
|--------------------|----------|
| §2.1 静态优先、`options` 别名 | Task 1 `staticBindingDefined` / `staticTreeRaw` |
| §2.1 异步 `fetchData`/`resultKey`/`dataFormatter`/`autoLoad` | Task 1 `executeFetch`、`onMounted` |
| §2.1 `separator` / `valueType` / 透传 filterable | Task 1 `outerToInner`/`innerToOuter`、`forwardedAttrs` |
| §2.1 `load-error`、`reload` | Task 1 `catch` + `reload`、`defineExpose` |
| §2.2 不做 lazy | Task 1 未实现 `lazy`/`load`（文档 Task 4 写明） |
| §5.4 EP 默认父子联动 | Task 1 不默认传 `check-strictly` |
| §8 注册与路径 | Task 1 + Task 2 |
| §2.3 原始需求 `separator` | Task 3 |
| 用户文档 | Task 4 |

**占位符扫描：** 本计划不含 TBD/TODO/「后续再写测试」类语句。

**命名一致性：** 全篇仅使用 `separator`、`valueType`、`labelKey`/`valueKey`/`childrenKey`，与 design spec §10 一致。

---

## Plan complete and saved to `docs/superpowers/plans/2026-05-07-c7-tree-select.md`. Two execution options:

**1. Subagent-Driven (recommended)** — 每个 Task 派生子代理执行，任务间人工快速复核。

**2. Inline Execution** — 在当前会话按 Task 顺序直接改代码，并在 Task 5 运行 `pnpm build:prod` 作为检查点。

**Which approach?**

（若你未指定，默认建议 **2** 以减少上下文切换；大改动再选 **1**。）
