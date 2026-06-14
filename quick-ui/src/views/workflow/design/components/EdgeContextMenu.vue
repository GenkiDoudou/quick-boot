<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="wf-edge-menu-mask"
      @click="$emit('close')"
      @contextmenu.prevent="$emit('close')"
    />
    <div
      v-if="visible"
      class="wf-edge-menu"
      :style="{ left: `${x}px`, top: `${y}px` }"
      @click.stop
    >
      <button type="button" class="wf-edge-menu__item wf-edge-menu__item--danger" @click="$emit('delete')">
        <el-icon><Delete /></el-icon>
        <span>删除连线</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'

defineOptions({ name: 'EdgeContextMenu' })

defineProps({
  visible: { type: Boolean, default: false },
  x: { type: Number, default: 0 },
  y: { type: Number, default: 0 }
})

defineEmits(['close', 'delete'])
</script>

<style scoped lang="scss">
.wf-edge-menu-mask {
  position: fixed;
  inset: 0;
  z-index: 2999;
}

.wf-edge-menu {
  position: fixed;
  z-index: 3000;
  min-width: 132px;
  padding: 4px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(10, 36, 99, 0.12);
}

.wf-edge-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  cursor: pointer;
  text-align: left;

  &--danger {
    color: #f56c6c;

    &:hover {
      background: #fef0f0;
    }
  }
}
</style>
