<template>
  <div class="wf-add-bar">
    <el-popover
      v-model:visible="visible"
      placement="top"
      :width="420"
      trigger="click"
      popper-class="wf-add-bar-popper"
    >
      <template #reference>
        <button type="button" class="wf-add-bar__trigger" @click.stop>
          <el-icon :size="18"><Plus /></el-icon>
          <span>添加节点</span>
        </button>
      </template>
      <NodePalette variant="popup" :container-kind="containerKind" @pick="onPick" />
    </el-popover>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import NodePalette from './NodePalette.vue'

defineOptions({ name: 'CanvasNodeAddBar' })

const props = defineProps({
  /** 当前容器上下文：null | loop | batch */
  containerKind: { type: String, default: null }
})

const emit = defineEmits(['add-node'])

const visible = ref(false)

/**
 * 从节点库选择类型后添加到画布中心。
 * @param {string} type
 */
function onPick(type) {
  if (!type) return
  emit('add-node', type)
  visible.value = false
}
</script>

<style scoped lang="scss">
.wf-add-bar {
  position: absolute;
  left: 50%;
  bottom: 20px;
  transform: translateX(-50%);
  z-index: 12;
  pointer-events: none;
}

.wf-add-bar__trigger {
  pointer-events: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: 999px;
  background: #fff;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(10, 36, 99, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease, transform 0.15s ease;

  &:hover {
    box-shadow: 0 6px 20px rgba(10, 36, 99, 0.16), 0 0 0 1px rgba(64, 158, 255, 0.25);
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}
</style>

<style lang="scss">
.wf-add-bar-popper {
  padding: 0 !important;
  border-radius: 12px !important;
  overflow: hidden;
}
</style>
