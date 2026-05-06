<template>
  <div
      class="c7-button-group"
      :class="{ 'c7-button-group--responsive': responsive }"
  >
    <div class="c7-button-group__row" :style="rowFlexStyle">
      <div
          v-if="mainSectionCount > 0"
          class="c7-button-group__main"
          :style="mainFlexStyle"
      >
        <template v-if="isDataMode">
          <c7-button
              v-for="ent in dataMain"
              :key="dataEntryKey(ent)"
              v-bind="rowToButtonProps(ent.row)"
              :before-pipeline="makeDataBeforePipeline(ent)"
              @after-click="(success) => onDataAfterClick(ent, success)"
          />
        </template>
        <VNodeList v-else :nodes="slotMainNodes"/>
      </div>

      <el-dropdown
          v-if="showMoreDropdown"
          class="c7-button-group__more"
          :trigger="trigger"
          teleported
      >
        <el-button
            :type="moreButtonType"
            :plain="moreButtonPlain"
            :size="size || undefined"
            :icon="moreIcon"
        >
          {{ moreText }}
        </el-button>
        <template #dropdown>
          <el-dropdown-menu class="c7-button-group__dropdown-menu">
            <div class="c7-button-group__dropdown-body" :style="dropdownBodyStyle">
              <template v-if="isDataMode">
                <c7-button
                    v-for="ent in dataOverflow"
                    :key="dataEntryKey(ent)"
                    v-bind="rowToButtonProps(ent.row)"
                    :before-pipeline="makeDataBeforePipeline(ent)"
                    @after-click="(success) => onDataAfterClick(ent, success)"
                />
              </template>
              <VNodeList v-else :nodes="slotOverflowNodes"/>
            </div>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
/**
 * C7 按钮组：在 {@link C7Button} 之上提供 inline / dropdown / auto 布局与「更多」折叠。
 *
 * - **数据模式**：非空 {@code buttons} 时按行配置渲染 {@code C7Button}；{@code hidden} 不占 {@code maxVisible} 名额。
 * - **插槽模式**：{@code buttons} 为空时解析默认插槽内 **直接子级 / Fragment 内** 的 {@code C7Button} VNode（其它节点忽略）；动态插槽结构变化后可调用 {@link forceUpdate}。
 *
 * 组级事件 {@code before-command} / {@code after-command} 与单按钮 {@code before-click} / {@code after-click} 等 **并存**；
 * 顺序为 {@code before-command} → {@code before-click} → … → {@code after-click} → {@code after-command}（依赖 {@code C7Button.beforePipeline}）。
 *
 * @emits before-command 进入子按钮流水线之前，载荷为 data 项描述或 {@code { slotIndex }}
 * @emits after-command 子按钮流水线结束后，载荷为 {@code { item, success }}
 */
import {
  cloneVNode,
  computed,
  defineComponent,
  defineExpose,
  Fragment,
  nextTick,
  ref,
  useSlots
} from 'vue'
import C7Button from '../C7Button/index.vue'

const props = defineProps({
  /** 非空时启用数据驱动；为空时使用默认插槽中的 C7Button */
  buttons: {
    type: Array,
    default: () => []
  },
  /** inline：全平铺；dropdown：全部进「更多」；auto：最多外露 maxVisible 个 */
  mode: {
    type: String,
    default: 'auto',
    validator: (v) => ['inline', 'dropdown', 'auto'].includes(v)
  },
  /** 仅 mode=auto 时生效；非法或小于 1 时按 1 处理 */
  maxVisible: {
    type: Number,
    default: 2
  },
  /** 间距：预设 sm/md/lg、数字像素或带 px 的字符串 */
  spacing: {
    type: [String, Number],
    default: 'md'
  },
  /** 透传到子 C7Button（可被单项覆盖） */
  size: {
    type: String,
    default: ''
  },
  /** 为 true 时根节点增加 {@code c7-button-group--responsive}，供业务侧写媒体查询 */
  responsive: {
    type: Boolean,
    default: false
  },
  moreText: {
    type: String,
    default: '更多'
  },
  /** Element Plus 图标组件或兼容 ElIcon 的图标对象 */
  moreIcon: {
    type: [Object, String],
    default: null
  },
  moreButtonType: {
    type: String,
    default: 'default'
  },
  moreButtonPlain: {
    type: Boolean,
    default: false
  },
  /** 同 ElDropdown：hover | click | contextmenu */
  trigger: {
    type: String,
    default: 'click'
  }
})

const emit = defineEmits(['before-command', 'after-command'])

const slots = useSlots()

/** 渲染 VNode 数组（用于插槽克隆节点） */
const VNodeList = defineComponent({
  name: 'C7ButtonGroupVNodeList',
  props: {
    nodes: {
      type: Array,
      default: () => []
    }
  },
  setup(p) {
    return () => p.nodes
  }
})

const slotTick = ref(0)

const gapCss = computed(() => {
  const presets = {sm: '4px', md: '8px', lg: '12px'}
  const s = props.spacing
  if (s === undefined || s === null || s === '') {
    return presets.md
  }
  if (typeof s === 'number' && !Number.isNaN(s)) {
    return `${s}px`
  }
  if (typeof s === 'string' && presets[s]) {
    return presets[s]
  }
  if (typeof s === 'string' && /^\d+(\.\d+)?$/.test(s)) {
    return `${s}px`
  }
  if (typeof s === 'string') {
    return s
  }
  return presets.md
})

const rowFlexStyle = computed(() => ({
  display: 'flex',
  flexWrap: 'wrap',
  alignItems: 'center',
  gap: gapCss.value
}))

const mainFlexStyle = computed(() => ({
  display: 'flex',
  flexWrap: 'wrap',
  alignItems: 'center',
  gap: gapCss.value
}))

const dropdownBodyStyle = computed(() => ({
  display: 'flex',
  flexDirection: 'column',
  gap: gapCss.value,
  padding: '8px',
  minWidth: '120px'
}))

const isDataMode = computed(() => Array.isArray(props.buttons) && props.buttons.length > 0)

const effectiveMaxVisible = computed(() => {
  const n = Number(props.maxVisible)
  if (!Number.isFinite(n) || n < 1) {
    return 1
  }
  return Math.floor(n)
})

const dataVisibleEntries = computed(() => {
  if (!isDataMode.value) {
    return []
  }
  return props.buttons
      .map((row, idx) => ({row, idx}))
      .filter(({row}) => !row.hidden)
})

const dataMain = computed(() => {
  const entries = dataVisibleEntries.value
  const n = entries.length
  const mv = effectiveMaxVisible.value
  if (props.mode === 'inline') {
    return entries
  }
  if (props.mode === 'dropdown') {
    return []
  }
  return entries.slice(0, Math.min(mv, n))
})

const dataOverflow = computed(() => {
  const entries = dataVisibleEntries.value
  const n = entries.length
  const mv = effectiveMaxVisible.value
  if (props.mode === 'inline') {
    return []
  }
  if (props.mode === 'dropdown') {
    return entries
  }
  return entries.slice(mv)
})

/**
 * 从插槽收集 C7Button VNode（仅遍历顶层与 Fragment 子级；嵌套在其它组件内的按钮不会被收集）。
 *
 * @param {import('vue').VNode[]|undefined} vnodes
 * @returns {import('vue').VNode[]}
 */
function collectC7ButtonVnodes(vnodes) {
  const out = []
  if (!vnodes || !vnodes.length) {
    return out
  }
  const walk = (nodes) => {
    for (const node of nodes) {
      if (!node || typeof node !== 'object') {
        continue
      }
      if (node.type === Fragment) {
        walk(node.children || [])
        continue
      }
      if (node.type === C7Button) {
        out.push(node)
        continue
      }
    }
  }
  walk(vnodes)
  return out
}

const slotSourceVnodes = computed(() => {
  void slotTick.value
  if (isDataMode.value) {
    return []
  }
  return collectC7ButtonVnodes(slots.default?.() || [])
})

/**
 * @param {import('vue').VNode} vnode
 * @param {number} slotIndex
 */
function wrapSlotVnode(vnode, slotIndex) {
  const mergedSize = props.size || vnode.props?.size || ''
  return cloneVNode(vnode, {
    size: mergedSize,
    beforePipeline: async () => {
      emit('before-command', {slotIndex})
    },
    onAfterClick: (success) => {
      emit('after-command', {item: {slotIndex}, success})
    }
  })
}

const slotMainNodes = computed(() => {
  const list = slotSourceVnodes.value
  const n = list.length
  const mv = effectiveMaxVisible.value
  if (props.mode === 'inline') {
    return list.map((vn, i) => wrapSlotVnode(vn, i))
  }
  if (props.mode === 'dropdown') {
    return []
  }
  return list.slice(0, Math.min(mv, n)).map((vn, i) => wrapSlotVnode(vn, i))
})

const slotOverflowNodes = computed(() => {
  const list = slotSourceVnodes.value
  const n = list.length
  const mv = effectiveMaxVisible.value
  if (props.mode === 'inline') {
    return []
  }
  if (props.mode === 'dropdown') {
    return list.map((vn, i) => wrapSlotVnode(vn, i))
  }
  return list.slice(mv).map((vn, j) => wrapSlotVnode(vn, mv + j))
})

const showMoreDropdown = computed(() => {
  if (isDataMode.value) {
    const total = dataVisibleEntries.value.length
    if (total === 0) {
      return false
    }
    if (props.mode === 'dropdown') {
      return true
    }
    return dataOverflow.value.length > 0
  }
  const total = slotSourceVnodes.value.length
  if (total === 0) {
    return false
  }
  if (props.mode === 'dropdown') {
    return true
  }
  return slotOverflowNodes.value.length > 0
})

const mainSectionCount = computed(() => {
  if (isDataMode.value) {
    return dataMain.value.length
  }
  return slotMainNodes.value.length
})

/**
 * @param {{ row: Record<string, unknown>, idx: number }} ent
 */
function dataEntryKey(ent) {
  const k = ent.row.key
  return k !== undefined && k !== null ? String(k) : `idx-${ent.idx}`
}

/**
 * @param {Record<string, unknown>} row
 */
function rowToButtonProps(row) {
  const {hidden, key, ...rest} = row
  const sz = props.size || rest.size || ''
  return {...rest, size: sz}
}

/**
 * @param {{ row: Record<string, unknown>, idx: number }} ent
 */
function dataCommandItem(ent) {
  return {
    key: ent.row.key !== undefined && ent.row.key !== null ? ent.row.key : ent.idx,
    index: ent.idx,
    raw: ent.row
  }
}

/**
 * @param {{ row: Record<string, unknown>, idx: number }} ent
 */
function makeDataBeforePipeline(ent) {
  return async () => {
    emit('before-command', dataCommandItem(ent))
  }
}

/**
 * @param {{ row: Record<string, unknown>, idx: number }} ent
 * @param {boolean} success
 */
function onDataAfterClick(ent, success) {
  emit('after-command', {item: dataCommandItem(ent), success})
}

function forceUpdate() {
  nextTick(() => {
    slotTick.value++
  })
}

defineExpose({
  /**
   * 插槽内 C7Button 增删或顺序变化后调用，用于重新收集 VNode 并计算折叠分区（普通响应式更新通常已足够，本方法覆盖边缘情况）。
   */
  forceUpdate
})
</script>

<style scoped>
.c7-button-group__dropdown-menu {
  padding: 0;
}
</style>
