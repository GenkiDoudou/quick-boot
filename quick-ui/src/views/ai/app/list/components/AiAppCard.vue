<template>
  <div class="ai-app-card" :class="{ 'ai-app-card--draft': item.status !== 'published' }">
    <div class="ai-app-card__head">
      <div class="ai-app-card__icon" :class="`ai-app-card__icon--${iconType}`">
        <el-icon :size="22"><component :is="iconComponent" /></el-icon>
      </div>
      <div class="ai-app-card__title-wrap">
        <div class="ai-app-card__title" :title="item.name">{{ item.name }}</div>
      </div>
      <el-dropdown trigger="click" @command="onCommand">
        <el-button class="ai-app-card__more" link>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-if="item.status !== 'published'"
              command="design"
              v-hasPermi="['aiapp:edit']"
            >编排</el-dropdown-item>
            <el-dropdown-item command="chat" v-hasPermi="['aiapp:chat']">演示</el-dropdown-item>
            <el-dropdown-item
              v-if="item.status !== 'published'"
              command="publish"
              v-hasPermi="['aiapp:publish']"
            >发布</el-dropdown-item>
            <el-dropdown-item command="delete" divided v-hasPermi="['aiapp:remove']">
              <span class="ai-app-card__danger">删除</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <p class="ai-app-card__desc" :title="item.description || ''">
      {{ item.description || '暂无介绍' }}
    </p>

    <div class="ai-app-card__foot">
      <span class="ai-app-card__tag">
        <el-icon><component :is="typeIcon" /></el-icon>
        {{ typeLabel }}
      </span>
      <span class="ai-app-card__status" :class="`ai-app-card__status--${statusType}`" :title="statusTitle">
        <el-icon v-if="statusType === 'ok'"><CircleCheckFilled /></el-icon>
        <el-icon v-else><EditPen /></el-icon>
      </span>
      <span class="ai-app-card__meta">{{ statusText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  ChatLineRound,
  CircleCheckFilled,
  EditPen,
  MoreFilled,
  Share,
} from '@element-plus/icons-vue'

/**
 * AI 应用卡片：展示名称、介绍、类型与发布状态。
 */
const props = defineProps({
  /** @type {import('vue').PropType<Record<string, unknown>>} */
  item: { type: Object, required: true },
})

const emit = defineEmits(['design', 'chat', 'publish', 'delete'])

const typeLabel = computed(() => (
  props.item.appType === 'workflow' ? '高级编排' : '智能体'
))

const iconType = computed(() => (
  props.item.appType === 'workflow' ? 'workflow' : 'agent'
))

const iconComponent = computed(() => (
  props.item.appType === 'workflow' ? Share : ChatLineRound
))

const typeIcon = computed(() => iconComponent.value)

const statusType = computed(() => (
  props.item.status === 'published' ? 'ok' : 'draft'
))

const statusTitle = computed(() => (
  props.item.status === 'published' ? '已发布' : '草稿'
))

const statusText = computed(() => statusTitle.value)

function onCommand(cmd) {
  if (cmd === 'design') emit('design', props.item)
  else if (cmd === 'chat') emit('chat', props.item)
  else if (cmd === 'publish') emit('publish', props.item)
  else if (cmd === 'delete') emit('delete', props.item)
}
</script>

<style scoped>
.ai-app-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 168px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s, border-color 0.2s;
}

.ai-app-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.ai-app-card__head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.ai-app-card__icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.ai-app-card__icon--agent {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.ai-app-card__icon--workflow {
  background: linear-gradient(135deg, #e6a23c, #f3d19e);
}

.ai-app-card__title-wrap {
  flex: 1;
  min-width: 0;
}

.ai-app-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-app-card__more {
  flex-shrink: 0;
  padding: 4px;
  margin: -4px -4px 0 0;
}

.ai-app-card__desc {
  flex: 1;
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

.ai-app-card__foot {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 10px;
}

.ai-app-card__tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.ai-app-card__meta {
  margin-left: auto;
}

.ai-app-card__status {
  display: inline-flex;
  font-size: 16px;
}

.ai-app-card__status--ok {
  color: var(--el-color-success);
}

.ai-app-card__status--draft {
  color: var(--el-text-color-placeholder);
}

.ai-app-card__danger {
  color: var(--el-color-danger);
}
</style>
