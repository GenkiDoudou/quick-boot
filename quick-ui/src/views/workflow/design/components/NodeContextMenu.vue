<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="wf-node-menu-mask"
      @click="$emit('close')"
      @contextmenu.prevent="$emit('close')"
    />
    <div
      v-if="visible"
      class="wf-node-menu"
      :style="{ left: `${x}px`, top: `${y}px` }"
      @click.stop
    >
      <button type="button" class="wf-node-menu__item" @click="$emit('rename')">
        <el-icon><EditPen /></el-icon>
        <span>重命名</span>
      </button>
      <button
        type="button"
        class="wf-node-menu__item"
        :class="{ 'wf-node-menu__item--disabled': !copyable }"
        :disabled="!copyable"
        @click="onCopy"
      >
        <el-icon><CopyDocument /></el-icon>
        <span>复制节点</span>
      </button>
      <div class="wf-node-menu__divider" />
      <button
        type="button"
        class="wf-node-menu__item wf-node-menu__item--danger"
        :class="{ 'wf-node-menu__item--disabled': !deletable }"
        :disabled="!deletable"
        @click="onDelete"
      >
        <el-icon><Delete /></el-icon>
        <span>删除节点</span>
      </button>
      <div v-if="!copyable || !deletable" class="wf-node-menu__tip">
        <template v-if="!copyable && !deletable">输入节点不可复制或删除</template>
        <template v-else-if="!copyable">输入节点不可复制</template>
        <template v-else>输入节点不可删除</template>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { CopyDocument, Delete, EditPen } from '@element-plus/icons-vue'

defineOptions({ name: 'NodeContextMenu' })

defineProps({
  visible: { type: Boolean, default: false },
  x: { type: Number, default: 0 },
  y: { type: Number, default: 0 },
  copyable: { type: Boolean, default: true },
  deletable: { type: Boolean, default: true }
})

const emit = defineEmits(['close', 'rename', 'copy', 'delete'])

function onCopy() {
  emit('copy')
}

function onDelete() {
  emit('delete')
}
</script>

<style scoped lang="scss">
.wf-node-menu-mask {
  position: fixed;
  inset: 0;
  z-index: 2999;
}

.wf-node-menu {
  position: fixed;
  z-index: 3000;
  min-width: 152px;
  padding: 4px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(10, 36, 99, 0.12);
}

.wf-node-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  color: #303133;
  cursor: pointer;
  text-align: left;

  &:hover:not(:disabled) {
    background: #f5f7fa;
  }

  &--danger {
    color: #f56c6c;

    &:hover:not(:disabled) {
      background: #fef0f0;
    }
  }

  &--disabled {
    color: #c0c4cc;
    cursor: not-allowed;
  }
}

.wf-node-menu__divider {
  height: 1px;
  margin: 4px 8px;
  background: #ebeef5;
}

.wf-node-menu__tip {
  padding: 4px 12px 6px;
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
}
</style>
