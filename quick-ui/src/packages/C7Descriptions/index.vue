<template>
  <el-descriptions v-bind="$attrs">
    <template v-if="$slots.title" #title>
      <slot name="title"/>
    </template>
    <template v-if="$slots.extra" #extra>
      <slot name="extra"/>
    </template>
    <el-descriptions-item
        v-for="(rawItem, idx) in items"
        :key="itemKey(rawItem, idx)"
        v-bind="pickDescItemProps(rawItem)"
    >
      <template v-if="useCustomSlot(rawItem)">
        <slot
            :name="rawItem.slotName"
            :row="rowRef"
            :value="cellValue(rawItem)"
            :item="rawItem"
        />
      </template>
      <template v-else-if="isTagColumn(rawItem)">
        <C7DictTag
            :model-value="cellValue(rawItem)"
            v-bind="pickDictTagProps(rawItem)"
        />
      </template>
      <template v-else-if="isImageColumn(rawItem)">
        <template v-if="isDisplayEmpty(cellValue(rawItem))">
          <span class="c7-descriptions__empty">{{ emptyDisplay(rawItem) }}</span>
        </template>
        <el-image
            v-else
            :src="imageSrc(rawItem)"
            v-bind="imageAttrsSafe(rawItem)"
        />
      </template>
      <template v-else-if="isLinkColumn(rawItem)">
        <template v-if="isDisplayEmpty(cellValue(rawItem))">
          <span class="c7-descriptions__empty">{{ emptyDisplay(rawItem) }}</span>
        </template>
        <a
            v-else
            :href="rawItem.linkHref"
            :target="rawItem.linkTarget || undefined"
            rel="noopener noreferrer"
        >{{ linkLabel(rawItem) }}</a>
      </template>
      <template v-else-if="isCopyColumn(rawItem)">
        <template v-if="isDisplayEmpty(cellValue(rawItem))">
          <span class="c7-descriptions__empty">{{ emptyDisplay(rawItem) }}</span>
        </template>
        <C7Copy
            v-else
            v-bind="copyPropsMerged(rawItem)"
        />
      </template>
      <template v-else>
        <span v-if="isDisplayEmpty(cellValue(rawItem))" class="c7-descriptions__empty">{{ emptyDisplay(rawItem) }}</span>
        <span v-else class="c7-descriptions__text">{{ defaultDisplay(rawItem) }}</span>
      </template>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup>
import {computed, shallowRef, useSlots} from 'vue'
import get from 'lodash/get'
import C7DictTag from '../C7DictTag/index.vue'
import C7Copy from '../C7Copy/index.vue'

defineOptions({name: 'C7Descriptions', inheritAttrs: false})

/**
 * C7 描述列表：在 **`ElDescriptions`** 上以 **`data` + `items`** 配置驱动详情单元格，内置 **`tag`（`C7DictTag`）**、**`image`**、**`link`**、**`copy`/`copyable`** 与文本列。
 *
 * **根透传**：除 **`data` / `items` / `defaultEmptyText`** 外，其余属性经 **`$attrs`** 传给 **`ElDescriptions`**（与 EP 文档一致）。
 *
 * **`row` 与 `data`**：具名插槽 **`item.slotName`** 的作用域 **`row`** 与父传入的 **`data` 为同一引用**。若 **`data` 为 `null`/`undefined`**，**`row` 亦为 `null`/`undefined`**；点路径取值结果为 **`undefined`**，**不抛错**。
 *
 * **展示空**（仅 **非 `tag`** 列）：**`value` 为 `null`、`undefined` 或 `''`** 时展示 **`item.emptyText ?? defaultEmptyText`**。**`[]`** 首期不视为空。**`tag` 列**不适用空文案，始终 **`C7DictTag`**。
 *
 * **`link` 展示空**：不渲染 **`<a>`**，仅展示上述空文案（避免空 **`href`**）。
 *
 * **`items` 与 `el-descriptions-item`**：仅将 **白名单字段**（**`label`、`span`、`width`…**）绑定到 **`el-descriptions-item`**；**`prop`、`columnType`、`formatter`** 等列逻辑字段不会透传到 EP 节点。
 */

const props = defineProps({
  /**
   * 详情数据对象；与插槽 **`row`** 同一引用。
   * 为 **`null`/`undefined`** 时 **`row`** 同值，**`lodash/get`** 得到 **`undefined`**。
   */
  data: {type: Object, default: null},
  /** 列配置数组 */
  items: {type: Array, default: () => []},
  /** 非 **`tag`** 列在「展示空」时的默认文案 */
  defaultEmptyText: {type: String, default: '暂无'}
})

const slots = useSlots()

/** 开发态未知 `columnType` 仅告警一次（每种类型字符串） */
const warnedColumnTypes = shallowRef(new Set())

/** 绑定到 **`el-descriptions-item`** 的字段白名单（与 Element Plus 对齐，camelCase） */
const DESC_ITEM_KEYS = [
  'label',
  'span',
  'width',
  'minWidth',
  'align',
  'labelAlign',
  'className',
  'labelClassName',
  'contentClassName',
  'rowSpan'
]

/** 透传给 **`C7DictTag`**（除 **`model-value`** 外） */
const DICT_TAG_KEYS = [
  'options',
  'separator',
  'showValue',
  'max',
  'collapse',
  'dictType',
  'size',
  'effect',
  'round'
]

const KNOWN_COLUMN_TYPES = new Set(['tag', 'image', 'link', 'copy', 'copyable'])

const rowRef = computed(() => props.data)

/**
 * @param {Record<string, *>} item
 * @returns {Record<string, *>}
 */
function pickDescItemProps(item) {
  const o = {}
  for (const k of DESC_ITEM_KEYS) {
    if (Object.prototype.hasOwnProperty.call(item, k) && item[k] !== undefined) {
      o[k] = item[k]
    }
  }
  return o
}

/**
 * @param {Record<string, *>} item
 * @returns {Record<string, *>}
 */
function pickDictTagProps(item) {
  const o = {}
  for (const k of DICT_TAG_KEYS) {
    if (Object.prototype.hasOwnProperty.call(item, k) && item[k] !== undefined) {
      o[k] = item[k]
    }
  }
  return o
}

/**
 * @param {Record<string, *>} item
 * @returns {unknown}
 */
function cellValue(item) {
  if (item.prop === undefined || item.prop === null || item.prop === '') {
    return undefined
  }
  return get(props.data, item.prop)
}

/**
 * @param {*} value
 * @returns {boolean}
 */
function isDisplayEmpty(value) {
  return value === null || value === undefined || value === ''
}

/**
 * @param {Record<string, *>} item
 * @returns {string}
 */
function emptyDisplay(item) {
  return item.emptyText != null && item.emptyText !== ''
      ? item.emptyText
      : props.defaultEmptyText
}

/**
 * @param {Record<string, *>} item
 * @returns {boolean}
 */
function useCustomSlot(item) {
  const name = item.slotName
  if (name === undefined || name === null || String(name).trim() === '') {
    return false
  }
  const fn = slots[name]
  return typeof fn === 'function'
}

/**
 * @param {Record<string, *>} item
 * @returns {boolean}
 */
function isTagColumn(item) {
  return item.columnType === 'tag'
}

/**
 * @param {Record<string, *>} item
 * @returns {boolean}
 */
function isImageColumn(item) {
  return item.columnType === 'image'
}

/**
 * @param {Record<string, *>} item
 * @returns {boolean}
 */
function isLinkColumn(item) {
  return item.columnType === 'link'
}

/**
 * @param {Record<string, *>} item
 * @returns {boolean}
 */
function isCopyColumn(item) {
  return item.columnType === 'copy' || item.columnType === 'copyable'
}

/**
 * @param {Record<string, *>} item
 * @returns {void}
 */
function warnUnknownColumnTypeIfAny(item) {
  const ct = item.columnType
  if (!import.meta.env.DEV || ct === undefined || ct === null || ct === '') {
    return
  }
  if (KNOWN_COLUMN_TYPES.has(ct)) {
    return
  }
  const s = new Set(warnedColumnTypes.value)
  if (s.has(ct)) {
    return
  }
  s.add(ct)
  warnedColumnTypes.value = s
  console.warn('[C7Descriptions] 未知 columnType，已按文本列降级:', ct)
}

/**
 * 文本列 / 未知 **`columnType`** 降级：先 **`formatter`**，再字符串化。
 *
 * @param {Record<string, *>} item
 * @returns {string}
 */
function defaultDisplay(item) {
  warnUnknownColumnTypeIfAny(item)
  const value = cellValue(item)
  const row = props.data
  if (typeof item.formatter === 'function') {
    const out = item.formatter(value, row, item)
    if (out !== undefined && out !== null) {
      return String(out)
    }
  }
  return String(value)
}

/**
 * @param {Record<string, *>} item
 * @returns {string}
 */
function linkLabel(item) {
  const value = cellValue(item)
  const row = props.data
  if (item.linkText !== undefined && item.linkText !== null && item.linkText !== '') {
    return String(item.linkText)
  }
  if (typeof item.formatter === 'function') {
    const out = item.formatter(value, row, item)
    if (out !== undefined && out !== null) {
      return String(out)
    }
  }
  return String(value)
}

/**
 * 复制串：**`copyProps.text`** 优先，其次 **`formatter`** 非空返回值，否则 **`String(value)`**（**`null`/`undefined`** → 空串）。
 *
 * @param {Record<string, *>} item
 * @returns {string}
 */
function resolveCopyText(item) {
  const cp = item.copyProps
  if (cp && Object.prototype.hasOwnProperty.call(cp, 'text') && cp.text !== undefined && cp.text !== null) {
    return String(cp.text)
  }
  const value = cellValue(item)
  const row = props.data
  if (typeof item.formatter === 'function') {
    const out = item.formatter(value, row, item)
    if (out !== undefined && out !== null) {
      return String(out)
    }
  }
  if (value === null || value === undefined) {
    return ''
  }
  return String(value)
}

/**
 * 合并 **`copyProps`** 与解析后的 **`text`**（后者覆盖 **`copyProps.text`**，保证与 **`resolveCopyText`** 优先级一致）。
 *
 * @param {Record<string, *>} item
 * @returns {Record<string, *>}
 */
function copyPropsMerged(item) {
  const text = resolveCopyText(item)
  const cp = item.copyProps
  if (cp && typeof cp === 'object') {
    return {...cp, text}
  }
  return {text}
}

/**
 * @param {Record<string, *>} item
 * @returns {Record<string, *>}
 */
function imageAttrsSafe(item) {
  const extra = item.imageAttrs
  if (!extra || typeof extra !== 'object') {
    return {}
  }
  const {src: _ignored, ...rest} = extra
  return rest
}

/**
 * @param {Record<string, *>} item
 * @returns {string}
 */
function imageSrc(item) {
  const v = cellValue(item)
  return v == null ? '' : String(v)
}

/**
 * @param {Record<string, *>} item
 * @param {number} idx
 * @returns {string|number}
 */
function itemKey(item, idx) {
  return item.prop != null ? String(item.prop) + ':' + idx : idx
}
</script>
