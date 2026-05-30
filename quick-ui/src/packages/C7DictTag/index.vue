<template>
  <div class="c7-dict-tag" v-bind="$attrs">
    <template v-if="isEmpty">
      <span class="c7-dict-tag__dash">-</span>
    </template>
    <template v-else>
      <template v-for="(cell, idx) in cells" :key="idx">
        <span v-if="cell.kind === 'dash'" class="c7-dict-tag__dash">-</span>
        <el-tag
            v-else-if="cell.kind === 'unmatched'"
            type="info"
            :size="size"
            :effect="effect"
            :round="round"
        >{{ cell.text }}</el-tag>
        <el-tag
            v-else-if="cell.kind === 'dict'"
            :type="cell.tagType"
            :class="cell.tagClass"
            :size="size"
            :effect="effect"
            :round="round"
        >{{ cell.label }}</el-tag>
        <span
            v-else-if="cell.kind === 'plain'"
            :class="cell.tagClass"
        >{{ cell.label }}</span>
        <!-- +N：collapse 时 ElTooltip；否则 ElPopover 可点 -->
        <el-tooltip
            v-else-if="cell.kind === 'more' && collapse"
            placement="top"
        >
          <template #content>
            <div
                v-for="(line, li) in cell.overflowLabels"
                :key="'tt-' + li"
                class="c7-dict-tag__tooltip-line"
            >{{ line }}</div>
          </template>
          <el-tag
              class="c7-dict-tag__more"
              type="info"
              :size="size"
              :effect="effect"
              :round="round"
          >+{{ cell.n }}</el-tag>
        </el-tooltip>
        <el-popover
            v-else-if="cell.kind === 'more' && !collapse"
            placement="bottom"
            :width="240"
            trigger="click"
        >
          <template #reference>
            <el-tag
                class="c7-dict-tag__more c7-dict-tag__more--clickable"
                type="info"
                :size="size"
                :effect="effect"
                :round="round"
            >+{{ cell.n }}</el-tag>
          </template>
          <div class="c7-dict-tag__overflow-list">
            <div v-for="(line, li) in cell.overflowLabels" :key="li" class="c7-dict-tag__overflow-line">{{ line }}</div>
          </div>
        </el-popover>
      </template>
    </template>
  </div>
</template>

<script setup>
import {computed} from 'vue'

defineOptions({name: 'C7DictTag', inheritAttrs: false})

/**
 * C7 字典标签：只读将 **`modelValue`**（单值 / 多值 / 逗号串）按 **`options`** 匹配为 **`ElTag`** 列表。
 *
 * **解析**：**`null`/`undefined`** 与解析后无非空原子 → 展示 **`-`**；**`number`** 为单原子；**`array`** 按序不去重；**`string`** 若 **包含 `separator`** 则按分隔符拆分（各段 **trim**、去空），否则整段为单原子。
 *
 * **匹配**：对每个原子 **`val`**，取 **`options`** 中 **首个** 满足 **`String(opt.value) === String(val)`** 的项的 **`label`**。
 *
 * **`mk` 与 `max`**：仅对 **已匹配** 原子自 **1** 递增编号 **`mk`**。当 **`max > 0`** 时：**`mk <= max`** 渲染字典色 tag；**`mk === max + 1`** 在该位渲染 **`+N`**（**`N = 已匹配总数 − max`**）；**`mk > max + 1`** 不占位。**`max`** 为 **`0`/负数/`undefined`** 时不折叠。
 *
 * **未匹配**：**`showValue=true`** → **`type=info`** 展示 **`String(val)`**；**`showValue=false`** → 该位 **`-`**。
 *
 * **`dictType`**：当字典项未配置 **`listClass`/`elTagType`** 时的 **`ElTag.type`** 兜底；已配置时以字典项为准。
 *
 * **`+N`**：**`collapse=true`** 时 **`ElTooltip`** 展示溢出 **`label`** 列表；**`collapse=false`** 时 **`ElPopover` + `click`** 展示同序列表。
 *
 * **注意**：本组件 **不** **`emit('update:modelValue')`**，仅展示。
 */

const EP_TYPES = new Set(['primary', 'success', 'info', 'warning', 'danger'])

const props = defineProps({
  /** 当前值：单值、数组或含分隔符的字符串；只读展示 */
  modelValue: {type: [String, Number, Array], default: undefined},
  /** 字典行：`{ label, value }`；匹配首项 */
  options: {type: Array, default: () => []},
  /** 逗号串拆分时使用的分隔符，默认 **`,`**；仅当 **string** 形态 **包含** 该子串时拆分 */
  separator: {type: String, default: ','},
  /** 未匹配时是否以 **info** tag 展示原始原子值；为 **false** 时该位为 **`-`** */
  showValue: {type: Boolean, default: false},
  /** 大于 **0** 时启用「仅前 **max** 个已匹配 tag + **`+N`**」 */
  max: {type: Number, default: undefined},
  /**
   * 为 **true** 时 **`+N`** 使用 **`ElTooltip`**（hover 展示溢出 **label**）；
   * 为 **false** 时 **`+N`** 使用 **`ElPopover`（click）** 展示溢出列表。
   */
  collapse: {type: Boolean, default: false},
  /** 业务字典类型名，用于映射 **`ElTag.type`**；未知时 **`primary`** */
  dictType: {type: String, default: ''},
  /** 透传 **`ElTag.size`** */
  size: {type: String, default: undefined},
  /** 透传 **`ElTag.effect`** */
  effect: {type: String, default: undefined},
  /** 透传 **`ElTag.round`** */
  round: {type: Boolean, default: undefined}
})

/**
 * @param {string} dictType
 * @returns {'primary'|'success'|'info'|'warning'|'danger'}
 */
function resolveDictTagType(dictType) {
  if (dictType === undefined || dictType === null || dictType === '') {
    return 'primary'
  }
  const dt = String(dictType).trim().toLowerCase()
  if (EP_TYPES.has(dt)) {
    return /** @type {'primary'|'success'|'info'|'warning'|'danger'} */ (dt)
  }
  if (dt.includes('danger') || dt.includes('error') || dt === 'fail') {
    return 'danger'
  }
  if (dt.includes('warning') || dt === 'warn') {
    return 'warning'
  }
  if (dt.includes('success') || dt === 'ok' || dt.includes('pass') || dt.includes('normal')) {
    return 'success'
  }
  if (dt.includes('info') || dt.includes('tip') || dt.includes('secondary')) {
    return 'info'
  }
  return 'primary'
}

const dictTagType = computed(() => resolveDictTagType(props.dictType))

/**
 * 字典项是否纯文本展示（RuoYi：listClass=default 且 cssClass 为空）。
 * @param {Record<string, *>|null} opt
 */
function isPlainDictOption(opt) {
  if (!opt) {
    return true
  }
  const t = opt.elTagType ?? opt.listClass ?? ''
  const c = opt.elTagClass ?? opt.cssClass ?? ''
  return (t === '' || t === 'default' || t == null) && (c === '' || c == null)
}

/**
 * 优先取字典项 listClass/elTagType，否则用 dictType 兜底。
 * @param {Record<string, *>|null} opt
 * @returns {'primary'|'success'|'info'|'warning'|'danger'|undefined}
 */
function resolveOptionTagType(opt) {
  const raw = String(opt?.elTagType ?? opt?.listClass ?? '').trim().toLowerCase()
  if (raw && raw !== 'default' && EP_TYPES.has(raw)) {
    return /** @type {'primary'|'success'|'info'|'warning'|'danger'} */ (raw)
  }
  return dictTagType.value === 'primary' ? undefined : dictTagType.value
}

/**
 * @param {Record<string, *>|null} opt
 * @returns {string}
 */
function resolveOptionTagClass(opt) {
  const c = opt?.elTagClass ?? opt?.cssClass
  return c ? String(c) : ''
}

/**
 * @param {*} raw
 * @returns {Array<string|number>}
 */
function parseAtoms(modelValue, separator) {
  if (modelValue === null || modelValue === undefined) {
    return []
  }
  const sep = separator == null ? ',' : String(separator)
  if (Array.isArray(modelValue)) {
    const out = []
    for (const v of modelValue) {
      if (v === null || v === undefined) {
        continue
      }
      if (typeof v === 'number' && !Number.isNaN(v)) {
        out.push(v)
      } else {
        const s = String(v).trim()
        if (s !== '') {
          out.push(s)
        }
      }
    }
    return out
  }
  if (typeof modelValue === 'number' && !Number.isNaN(modelValue)) {
    return [modelValue]
  }
  if (typeof modelValue === 'string') {
    const t = modelValue.trim()
    if (t === '') {
      return []
    }
    if (sep !== '' && t.includes(sep)) {
      return t.split(sep).map((p) => p.trim()).filter((p) => p !== '')
    }
    return [t]
  }
  return []
}

/**
 * @param {Array<Record<string, *>>} options
 * @param {string|number} val
 * @returns {Record<string, *>|null}
 */
function findOption(options, val) {
  const s = String(val)
  for (const opt of options) {
    if (opt != null && Object.prototype.hasOwnProperty.call(opt, 'value') && String(opt.value) === s) {
      return opt
    }
  }
  return null
}

const atoms = computed(() => parseAtoms(props.modelValue, props.separator))

const isEmpty = computed(() => atoms.value.length === 0)

const maxLimitActive = computed(() => {
  const m = props.max
  if (m === undefined || m === null) {
    return false
  }
  const n = Number(m)
  return Number.isFinite(n) && n > 0
})

const rows = computed(() => {
  const opts = Array.isArray(props.options) ? props.options : []
  let mk = 0
  /** @type {Array<{ raw: string|number, matched: boolean, opt: Record<string, *>|null, mk?: number }>} */
  const list = []
  for (const raw of atoms.value) {
    const opt = findOption(opts, raw)
    if (opt) {
      mk += 1
      list.push({raw, matched: true, opt, mk})
    } else {
      list.push({raw, matched: false, opt: null})
    }
  }
  return list
})

const totalMatched = computed(() => rows.value.filter((r) => r.matched).length)

const overflowLabels = computed(() =>
  rows.value.filter((r) => r.matched && maxLimitActive.value && /** @type {number} */ (r.mk) > Number(props.max)).map((r) => String(r.opt.label))
)

const cells = computed(() => {
  /** @type {Array<Record<string, *>>} */
  const out = []
  const maxN = maxLimitActive.value ? Number(props.max) : 0

  for (const r of rows.value) {
    if (!r.matched) {
      if (props.showValue) {
        out.push({kind: 'unmatched', text: String(r.raw)})
      } else {
        out.push({kind: 'dash'})
      }
      continue
    }
    const mk = /** @type {number} */ (r.mk)
    const label = String(r.opt.label)
    const tagClass = resolveOptionTagClass(r.opt)
    if (isPlainDictOption(r.opt)) {
      if (!maxLimitActive.value) {
        out.push({ kind: 'plain', label, tagClass })
        continue
      }
      if (mk <= maxN) {
        out.push({ kind: 'plain', label, tagClass })
      } else if (mk === maxN + 1) {
        const n = totalMatched.value - maxN
        out.push({ kind: 'more', n, overflowLabels: overflowLabels.value.slice() })
      }
      continue
    }
    const tagType = resolveOptionTagType(r.opt)
    if (!maxLimitActive.value) {
      out.push({ kind: 'dict', label, tagType, tagClass })
      continue
    }
    if (mk <= maxN) {
      out.push({ kind: 'dict', label, tagType, tagClass })
    } else if (mk === maxN + 1) {
      const n = totalMatched.value - maxN
      out.push({
        kind: 'more',
        n,
        overflowLabels: overflowLabels.value.slice()
      })
    }
  }
  return out
})
</script>

<style scoped>
.c7-dict-tag {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.c7-dict-tag__dash {
  color: var(--el-text-color-secondary);
  user-select: none;
}

.c7-dict-tag__more--clickable {
  cursor: pointer;
}

.c7-dict-tag__overflow-list {
  max-height: 220px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.5;
}

.c7-dict-tag__overflow-line + .c7-dict-tag__overflow-line {
  margin-top: 4px;
}

.c7-dict-tag__tooltip-line + .c7-dict-tag__tooltip-line {
  margin-top: 4px;
}
</style>
