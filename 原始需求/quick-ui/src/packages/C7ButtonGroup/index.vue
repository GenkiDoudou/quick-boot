<template>
  <div class="c7-button-group" :class="[`spacing-${spacingClass}`, { 'is-responsive': responsive }]" :style="groupStyle">
    <!-- 数据驱动模式 -->
    <template v-if="buttons && buttons.length">
      <!-- 可见按钮 -->
      <template v-if="currentMode === 'inline'">
        <C7Button
          v-for="item in allDataButtons"
          :key="item.key"
          v-bind="getButtonProps(item)"
        />
      </template>
      <template v-else>
        <C7Button
          v-for="item in visibleDataButtons"
          :key="item.key"
          v-bind="getButtonProps(item)"
        />
        <!-- 折叠下拉 -->
        <el-dropdown
          v-if="hiddenDataButtons.length"
          :trigger="trigger"
          @command="handleDropdownCommand"
        >
          <slot name="dropdown-trigger" :count="hiddenDataButtons.length">
            <el-button
              :type="moreButtonType"
              :plain="moreButtonPlain"
              :size="size"
            >
              {{ moreText }}
              <el-icon class="el-icon--right"><component :is="moreIconComponent" /></el-icon>
            </el-button>
          </slot>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="item in hiddenDataButtons"
                :key="item.key"
                :command="item"
                :disabled="item.disabled"
              >
                <el-icon v-if="item.icon || getPresetIcon(item.btnType)">
                  <component :is="resolveIcon(item.icon || getPresetIcon(item.btnType))" />
                </el-icon>
                {{ item.label || getPresetLabel(item.btnType) }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </template>

    <!-- slot 模式（向后兼容）-->
    <template v-else>
      <template v-if="currentMode === 'inline'">
        <slot />
      </template>
      <template v-else>
        <!-- 可见 slot 按钮 -->
        <component
          :is="vnode"
          v-for="(vnode, i) in visibleSlotButtons"
          :key="i"
        />
        <!-- 折叠下拉 -->
        <el-dropdown
          v-if="hiddenSlotButtons.length"
          :trigger="trigger"
          @command="handleSlotCommand"
        >
          <slot name="dropdown-trigger" :count="hiddenSlotButtons.length">
            <el-button
              :type="moreButtonType"
              :plain="moreButtonPlain"
              :size="size"
            >
              {{ moreText }}
              <el-icon class="el-icon--right"><component :is="moreIconComponent" /></el-icon>
            </el-button>
          </slot>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="(vnode, i) in hiddenSlotButtons"
                :key="i"
                :command="i"
              >
                {{ getVnodeLabel(vnode) }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, useSlots } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

defineOptions({ name: 'C7ButtonGroup' })

// ── 预设配置表（与 C7Button 保持一致）──
const buttonConfigs = {
  add:      { icon: 'Plus',     label: '新增' },
  edit:     { icon: 'Edit',     label: '修改' },
  delete:   { icon: 'Delete',   label: '删除' },
  query:    { icon: 'Search',   label: '查询' },
  refresh:  { icon: 'Refresh',  label: '重置' },
  upload:   { icon: 'Upload',   label: '上传' },
  download: { icon: 'Download', label: '下载' },
  submit:   { icon: '',         label: '确定' },
  cancel:   { icon: '',         label: '取消' },
}

const spacingMap = {
  tight:  '4px',
  normal: '8px',
  loose:  '12px',
}

const props = defineProps({
  // 数据驱动
  buttons: { type: Array, default: null },
  // 布局
  mode:       { type: String,  default: 'auto' },   // 'auto' | 'inline' | 'dropdown'
  maxVisible: { type: Number,  default: 2 },
  spacing:    { type: [String, Number], default: 'normal' },
  size:       { type: String,  default: 'small' },
  // 响应式
  responsive: { type: Boolean, default: true },
  // 下拉配置
  trigger:          { type: String,  default: 'click' },
  moreText:         { type: String,  default: '更多' },
  moreIcon:         { type: String,  default: 'ArrowDown' },
  moreButtonType:   { type: String,  default: 'info' },
  moreButtonPlain:  { type: Boolean, default: true },
})

const emit = defineEmits(['before-command', 'after-command'])

const slots = useSlots()

// ── 强制刷新 slot（forceUpdate 用）──
const renderKey = ref(0)

// ── 暴露 forceUpdate ──
defineExpose({
  forceUpdate: () => { renderKey.value++ }
})

// ── 间距 ──
const groupStyle = computed(() => {
  const gap = typeof props.spacing === 'number'
    ? `${props.spacing}px`
    : spacingMap[props.spacing] ?? spacingMap.normal
  return { gap }
})

const spacingClass = computed(() => {
  return typeof props.spacing === 'string' ? props.spacing : 'custom'
})

// ── 当前模式 ──
const currentMode = computed(() => props.mode)

// ── 图标解析 ──
function resolveIcon(name) {
  if (!name) return null
  return ElementPlusIconsVue[name] || null
}

const moreIconComponent = computed(() => resolveIcon(props.moreIcon))

function getPresetIcon(btnType) {
  return buttonConfigs[btnType]?.icon || ''
}

function getPresetLabel(btnType) {
  return buttonConfigs[btnType]?.label || ''
}

// ── 数据驱动模式 ──
const activeDataButtons = computed(() => {
  if (!props.buttons) return []
  return props.buttons.filter(item => !item.hidden)
})

const allDataButtons = computed(() => activeDataButtons.value)

const visibleDataButtons = computed(() => {
  if (currentMode.value === 'dropdown') return []
  return activeDataButtons.value.slice(0, props.maxVisible)
})

const hiddenDataButtons = computed(() => {
  if (currentMode.value === 'inline') return []
  if (currentMode.value === 'dropdown') return activeDataButtons.value
  return activeDataButtons.value.slice(props.maxVisible)
})

function getButtonProps(item) {
  const { key, hidden, clickFunction, ...rest } = item
  return {
    size: props.size,
    ...rest,
    clickFunction,
  }
}

async function handleDropdownCommand(item) {
  emit('before-command', item)
  if (item.clickFunction) {
    await item.clickFunction()
  }
  emit('after-command', item)
}

// ── slot 模式 ──
const allSlotButtons = computed(() => {
  // 依赖 renderKey 触发重算
  void renderKey.value
  const defaultSlot = slots.default?.()
  if (!defaultSlot) return []
  // 展平并过滤出 C7Button vnode
  const result = []
  function walk(nodes) {
    for (const node of nodes) {
      if (!node) continue
      if (Array.isArray(node)) { walk(node); continue }
      const name = node.type?.name || node.type?.__name
      if (name === 'C7Button') {
        result.push(node)
      } else if (node.children?.length) {
        walk(node.children)
      }
    }
  }
  walk(defaultSlot)
  return result
})

const visibleSlotButtons = computed(() => {
  if (currentMode.value === 'dropdown') return []
  if (currentMode.value === 'inline') return allSlotButtons.value
  return allSlotButtons.value.slice(0, props.maxVisible)
})

const hiddenSlotButtons = computed(() => {
  if (currentMode.value === 'inline') return []
  if (currentMode.value === 'dropdown') return allSlotButtons.value
  return allSlotButtons.value.slice(props.maxVisible)
})

function getVnodeLabel(vnode) {
  const p = vnode.props || {}
  const btnType = p.btnType || p['btn-type']
  return p.label || getPresetLabel(btnType) || '操作'
}

async function handleSlotCommand(index) {
  const vnode = hiddenSlotButtons.value[index]
  if (!vnode) return
  const p = vnode.props || {}
  const fn = p.clickFunction || p['click-function']
  const item = { key: index, label: getVnodeLabel(vnode) }
  emit('before-command', item)
  if (typeof fn === 'function') {
    await fn()
  }
  emit('after-command', item)
}
</script>

<style scoped lang="scss">
.c7-button-group {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px; /* 默认值，由 groupStyle 覆盖 */

  &.is-responsive {
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      width: 100%;

      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>
