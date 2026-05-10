<template>
  <template v-for="(col, idx) in renderColumns" :key="columnRowKey(col, idx)">
    <el-table-column
        v-bind="columnPropsMerged(col)"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :fixed="col.fixed"
        :align="col.align"
        :header-align="col.headerAlign"
        :sortable="col.sortable"
        :show-overflow-tooltip="col.showOverflowTooltip"
        :formatter="col.__et === 'text' && col.formatter ? col.formatter : undefined"
    >
      <template v-if="col.prop && $slots[`header-${col.prop}`]" #header="headerScope">
        <slot :name="`header-${col.prop}`" v-bind="headerScope"/>
      </template>

      <!-- 存在 formatter 时交给 EP，禁止同时使用 #default（与「不在 formatter 路径套 emptyText」一致） -->
      <template v-if="!(col.__et === 'text' && col.formatter)" #default="scope">
        <span v-if="col.__et === 'text'">{{ textCellDisplay(scope.row, col, scope.column, scope.$index) }}</span>
        <C7DictTag
            v-else-if="col.__et === 'tag'"
            :model-value="scope.row[col.prop]"
            :options="(col.options ?? col.dictList) ?? []"
            v-bind="dictTagPassthrough(col)"
        />
        <C7Preview
            v-else-if="col.__et === 'image'"
            :urls="imageUrls(scope.row, col)"
            cover-type="none"
            v-bind="previewPassthrough(col)"
        />
        <template v-else-if="col.__et === 'link'">
          <template v-if="linkHrefResolved(scope.row, col, scope.column, scope.$index)">
            <a
                :href="linkHrefResolved(scope.row, col, scope.column, scope.$index)"
                :target="col.linkTarget || undefined"
            >{{ linkTextResolved(scope.row, col, scope.column, scope.$index) }}</a>
          </template>
          <span v-else>{{ EMPTY_PLACEHOLDER }}</span>
        </template>
        <template v-else-if="col.__et === 'slot'">
          <slot
              v-if="$slots[slotColumnName(col)]"
              :name="slotColumnName(col)"
              v-bind="scope"
          />
          <span v-else>{{ EMPTY_PLACEHOLDER }}</span>
        </template>
      </template>
    </el-table-column>
  </template>
</template>

<script setup>
import {computed} from 'vue'
import C7DictTag from '../C7DictTag/index.vue'
import C7Preview from '../C7Preview/index.vue'

defineOptions({name: 'C7JsonTableColumn', inheritAttrs: false})

/** 与规范、设计文档一致的单元格空占位（无 formatter 的 text / 空链接 / 缺 slot 等） */
const EMPTY_PLACEHOLDER = '-'

const KNOWN_TYPES = new Set(['text', 'tag', 'image', 'link', 'slot'])

const COLUMN_PROPS_EXCLUDE = new Set([
  'prop', 'label', 'width', 'minWidth', 'fixed', 'align', 'headerAlign', 'sortable',
  'showOverflowTooltip', 'props', 'visible', 'order', 'columnType', 'emptyText',
  'formatter', 'options', 'dictList', 'slotName', 'linkHref', 'linkText', 'linkTarget',
])

const DICT_TAG_EXCLUDE = new Set([
  ...COLUMN_PROPS_EXCLUDE,
  'options', 'dictList',
])

const PREVIEW_EXCLUDE = new Set([
  ...COLUMN_PROPS_EXCLUDE,
])

const props = defineProps({
  /**
   * 列描述数组（必填）。非数组时本组件不渲染列，开发环境 `console.warn`。
   * @type {Record<string, unknown>[]}
   */
  columns: {type: Array, required: true},
  /**
   * 表级默认空文案：用于 **`columnType==='text'`** 且无 **`formatter`** 时，
   * 当列上未配置 **`emptyText`** 且单元格值为 **`null` / `undefined` / ''`** 的展示兜底（链式：`列 emptyText` → 本 prop → **`'-'`**）。
   */
  emptyText: {type: String, default: undefined},
})

function warnDev(message) {
  if (import.meta.env.DEV) {
    console.warn(`[C7JsonTableColumn] ${message}`)
  }
}

/**
 * 合并 **`col.props`**：显式 **`prop` / `label` / …** 以模板上的绑定为准（写在 **`v-bind` 之后**），覆盖同名字段。
 *
 * @param {Record<string, unknown>} col
 * @returns {Record<string, unknown>}
 */
function columnPropsMerged(col) {
  const raw = col.props
  return raw && typeof raw === 'object' && !Array.isArray(raw) ? {...raw} : {}
}

/**
 * @param {Record<string, unknown>} col
 * @returns {Record<string, unknown>}
 */
function dictTagPassthrough(col) {
  /** @type {Record<string, unknown>} */
  const out = {}
  for (const key of Object.keys(col)) {
    if (!DICT_TAG_EXCLUDE.has(key)) {
      out[key] = col[key]
    }
  }
  return out
}

/**
 * @param {Record<string, unknown>} col
 * @returns {Record<string, unknown>}
 */
function previewPassthrough(col) {
  /** @type {Record<string, unknown>} */
  const out = {}
  for (const key of Object.keys(col)) {
    if (!PREVIEW_EXCLUDE.has(key)) {
      out[key] = col[key]
    }
  }
  delete out.coverType
  delete out.urls
  return out
}

/**
 * @param {Record<string, unknown>} col
 * @param {number} idx
 */
function columnRowKey(col, idx) {
  const p = col.prop
  const s = col.slotName
  if (p != null && String(p) !== '') {
    return `p:${p}`
  }
  if (s != null && String(s) !== '') {
    return `s:${s}`
  }
  return `i:${idx}`
}

/**
 * @param {Record<string, unknown>} col
 */
function slotColumnName(col) {
  return String(col.slotName || col.prop || '')
}

/**
 * @param {unknown[]} list
 */
function sortColumnsStable(list) {
  const decorated = list.map((c, inputIndex) => ({c, inputIndex}))
  const hasOrder = (c) => c.order != null && c.order !== '' && !Number.isNaN(Number(c.order))
  const withO = decorated.filter((x) => hasOrder(x.c))
  const withoutO = decorated.filter((x) => !hasOrder(x.c))
  withO.sort((a, b) => {
    const na = Number(a.c.order)
    const nb = Number(b.c.order)
    if (na !== nb) {
      return na - nb
    }
    return a.inputIndex - b.inputIndex
  })
  return [...withO, ...withoutO].map((x) => x.c)
}

const renderColumns = computed(() => {
  const cols = props.columns
  if (!Array.isArray(cols)) {
    warnDev('columns 须为数组，当前未渲染任何列')
    return []
  }
  const visible = cols.filter((c) => c && typeof c === 'object' && c.visible !== false)
  const sorted = sortColumnsStable(visible)
  const unknownWarned = new WeakMap()

  /**
   * @param {Record<string, unknown>} col
   * @returns {'text'|'tag'|'image'|'link'|'slot'}
   */
  function resolveEffectiveType(col) {
    const raw = col.columnType
    const t = raw == null || raw === '' ? 'text' : String(raw)
    if (!KNOWN_TYPES.has(t)) {
      if (!unknownWarned.has(col)) {
        unknownWarned.set(col, true)
        warnDev(`未知 columnType「${t}」，已按 text 处理`)
      }
      return 'text'
    }
    return /** @type {'text'|'tag'|'image'|'link'|'slot'} */ (t)
  }

  /**
   * @param {Record<string, unknown>} col
   * @param {'text'|'tag'|'image'|'link'|'slot'} et
   */
  function isRenderableColumn(col, et) {
    if ((et === 'text' || et === 'tag' || et === 'image' || et === 'link') && !col.prop) {
      warnDev(`columnType=${et} 但缺少 prop，已跳过该列`)
      return false
    }
    if (et === 'slot' && !col.slotName && !col.prop) {
      warnDev('columnType=slot 但 slotName 与 prop 均无效，已跳过该列')
      return false
    }
    return true
  }

  /** @type {Record<string, unknown>[]} */
  const out = []
  for (const col of sorted) {
    const et = resolveEffectiveType(col)
    if (!isRenderableColumn(col, et)) {
      continue
    }
    out.push({...col, __et: et})
  }
  return out
})

/**
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 * @param {unknown} column
 * @param {number} index
 */
function cellValue(row, col, column, index) {
  if (!col.prop) {
    return undefined
  }
  return row[col.prop]
}

/**
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 * @param {unknown} column
 * @param {number} index
 */
function textCellDisplay(row, col, column, index) {
  const v = cellValue(row, col, column, index)
  if (v === null || v === undefined || v === '') {
    const colEmpty = col.emptyText
    if (colEmpty != null && colEmpty !== '') {
      return String(colEmpty)
    }
    if (props.emptyText != null && props.emptyText !== '') {
      return props.emptyText
    }
    return EMPTY_PLACEHOLDER
  }
  return String(v)
}

/**
 * @param {unknown} v
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 * @param {unknown} column
 * @param {number} index
 */
function resolveMaybeFn(v, row, col, column, index) {
  if (typeof v === 'function') {
    return v(row, column, cellValue(row, col, column, index), index)
  }
  return v
}

/**
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 * @param {unknown} column
 * @param {number} index
 */
function linkHrefResolved(row, col, column, index) {
  const href = resolveMaybeFn(col.linkHref, row, col, column, index)
  if (href === null || href === undefined || href === '') {
    return ''
  }
  return String(href)
}

/**
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 * @param {unknown} column
 * @param {number} index
 */
function linkTextResolved(row, col, column, index) {
  const t = resolveMaybeFn(col.linkText, row, col, column, index)
  if (t === null || t === undefined || t === '') {
    const h = linkHrefResolved(row, col, column, index)
    return h || EMPTY_PLACEHOLDER
  }
  return String(t)
}

/**
 * @param {Record<string, unknown>} row
 * @param {Record<string, unknown>} col
 */
function imageUrls(row, col) {
  const v = row[col.prop]
  if (v === null || v === undefined) {
    return ''
  }
  return String(v)
}
</script>
