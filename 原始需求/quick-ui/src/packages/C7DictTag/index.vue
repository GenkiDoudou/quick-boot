<template>
  <span class="c7-dict-tag">
    <!-- 正常匹配项：最多显示 max 个 -->
    <template v-for="(opt, idx) in visibleItems" :key="idx">
      <!-- tag 模式 -->
      <el-tag
        v-if="opt.elTagType !== 'text'"
        :type="normalizeType(opt.elTagType)"
        :class="opt.elTagClass"
        :size="size"
        :effect="effect"
        :round="round"
        disable-transitions
      >
        {{ opt.label }}
      </el-tag>
      <!-- 纯文本模式 -->
      <span v-else :class="opt.elTagClass" class="c7-dict-tag__text">
        {{ opt.label }}
      </span>
    </template>

    <!-- 超出 max 时的折叠显示 -->
    <template v-if="overflowCount > 0">
      <!-- collapse 模式：tooltip 展示所有超出项 -->
      <el-tooltip
        v-if="collapse"
        :content="overflowTooltip"
        placement="top"
      >
        <el-tag :size="size" :effect="effect" type="info" disable-transitions>
          +{{ overflowCount }}
        </el-tag>
      </el-tooltip>
      <!-- 非 collapse 模式：仅显示 +N 标签 -->
      <el-tag v-else :size="size" type="info" disable-transitions>
        +{{ overflowCount }}
      </el-tag>
    </template>

    <!-- 未匹配的原始值（showValue=true 时显示） -->
    <template v-if="showValue && unmatchItems.length">
      <el-tag
        v-for="(val, idx) in unmatchItems"
        :key="'unmatch-' + idx"
        :size="size"
        type="info"
        :effect="effect"
        disable-transitions
      >
        {{ val }}
      </el-tag>
    </template>

    <!-- 值为空时的兜底 -->
    <span v-if="!values.length" class="c7-dict-tag__empty">-</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

defineOptions({ name: 'C7DictTag' })

const props = defineProps({
  /** 字典选项列表 */
  options: {
    type: Array,
    default: () => []
  },
  /** 字段值：数字、字符串、逗号分隔字符串或数组 */
  modelValue: {
    type: [Number, String, Array],
    default: undefined
  },
  /** 未匹配时是否显示原始值 */
  showValue: {
    type: Boolean,
    default: true
  },
  /** 字符串分隔符，默认逗号 */
  separator: {
    type: String,
    default: ','
  },
  /** 标签尺寸 */
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  },
  /** 最多显示的标签数量，超出部分折叠（0 = 不限制） */
  max: {
    type: Number,
    default: 0
  },
  /** 超出 max 时是否用 tooltip 展示所有值 */
  collapse: {
    type: Boolean,
    default: false
  },
  /** el-tag effect */
  effect: {
    type: String,
    default: 'light',
    validator: (v) => ['dark', 'light', 'plain'].includes(v)
  },
  /** 圆角标签 */
  round: {
    type: Boolean,
    default: false
  }
})

/**
 * 将多格式输入统一转为字符串数组
 * 支持：数字、字符串、逗号分隔字符串、数组
 */
const values = computed(() => {
  const mv = props.modelValue
  if (mv === null || mv === undefined || mv === '') return []
  if (Array.isArray(mv)) return mv.map(String)
  return String(mv).split(props.separator).filter(Boolean)
})

/**
 * 匹配到字典项的结果列表
 */
const matchedItems = computed(() => {
  if (!values.value.length || !props.options.length) return []
  return values.value
    .map((val) => props.options.find((opt) => String(opt.value) === val))
    .filter(Boolean)
})

/**
 * 未匹配到字典项的原始值列表（纯计算，无副作用）
 */
const unmatchItems = computed(() => {
  if (!values.value.length || !props.options.length) return values.value
  return values.value.filter(
    (val) => !props.options.some((opt) => String(opt.value) === val)
  )
})

/**
 * 实际显示的匹配项（受 max 限制）
 */
const visibleItems = computed(() => {
  if (!props.max || props.max <= 0) return matchedItems.value
  return matchedItems.value.slice(0, props.max)
})

/**
 * 超出 max 的数量
 */
const overflowCount = computed(() => {
  if (!props.max || props.max <= 0) return 0
  return Math.max(0, matchedItems.value.length - props.max)
})

/**
 * tooltip 展示超出部分的标签文本
 */
const overflowTooltip = computed(() => {
  if (!props.max || props.max <= 0) return ''
  return matchedItems.value
    .slice(props.max)
    .map((opt) => opt.label)
    .join('、')
})

/**
 * 规范化 elTagType：
 * - 空字符串 / undefined → '' (Element Plus 默认)
 * - 'primary' → 'primary'（EP 新版已支持）
 * - 其他透传
 */
function normalizeType(elTagType) {
  if (!elTagType) return ''
  return elTagType
}
</script>

<style scoped>
.c7-dict-tag {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  vertical-align: middle;
}

.c7-dict-tag__text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

.c7-dict-tag__empty {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
