<template>
  <header class="wf-toolbar">
    <div class="wf-toolbar__left">
      <el-button link class="wf-toolbar__back" @click="$emit('back')">
        <el-icon><ArrowLeft /></el-icon>
        返回工作流列表
      </el-button>
      <span class="wf-toolbar__name">{{ name || '工作流设计' }}</span>
      <span class="wf-toolbar__status" :class="`wf-toolbar__status--${saveStatus}`">
        <span v-if="saveStatus === 'dirty'" class="wf-toolbar__dot" />
        {{ saveStatusLabel }}
      </span>
    </div>
    <div class="wf-toolbar__right">
      <el-button :loading="validating" @click="$emit('validate')" v-hasPermi="['workflow:edit']">
        校验
      </el-button>
      <el-button
        type="primary"
        :loading="running"
        @click="$emit('test-run')"
        v-hasPermi="['workflow:run']"
      >
        <el-icon><VideoPlay /></el-icon>
        测试运行
      </el-button>
      <el-button
        type="success"
        :loading="publishing"
        @click="$emit('publish')"
        v-hasPermi="['workflow:publish']"
      >
        发布
      </el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue'

defineOptions({ name: 'DesignToolbar' })

const props = defineProps({
  name: { type: String, default: '' },
  saveStatus: { type: String, default: 'saved' },
  validating: { type: Boolean, default: false },
  running: { type: Boolean, default: false },
  publishing: { type: Boolean, default: false }
})

defineEmits(['back', 'validate', 'test-run', 'publish'])

const saveStatusLabel = computed(() => {
  const map = { saved: '已保存', saving: '保存中…', dirty: '未保存' }
  return map[props.saveStatus] || props.saveStatus
})
</script>

<style scoped lang="scss">
.wf-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.wf-toolbar__left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.wf-toolbar__back {
  color: #606266;
  flex-shrink: 0;
}

.wf-toolbar__name {
  font-size: 15px;
  font-weight: 600;
  color: #0a2463;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wf-toolbar__status {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;

  &--dirty {
    color: #e6a23c;
  }

  &--saving {
    color: #409eff;
  }
}

.wf-toolbar__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e6a23c;
}

.wf-toolbar__right {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
