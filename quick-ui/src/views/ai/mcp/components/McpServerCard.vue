<template>
  <div
    class="mcp-server-card"
    :class="{ 'mcp-server-card--disabled': item.status !== 0 }"
    role="button"
    tabindex="0"
    @click="emit('open', item)"
    @keyup.enter="emit('open', item)"
  >
    <div class="mcp-server-card__head">
      <div class="mcp-server-card__icon" :class="`mcp-server-card__icon--${iconType}`">
        <el-icon :size="22"><component :is="iconComponent" /></el-icon>
      </div>
      <div class="mcp-server-card__title-wrap">
        <div class="mcp-server-card__title" :title="item.name">{{ item.name }}</div>
      </div>
      <el-dropdown trigger="click" @command="onCommand" @click.stop>
        <el-button class="mcp-server-card__more" link @click.stop>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="test" v-hasPermi="['ai:mcp:test']">测试连接</el-dropdown-item>
            <el-dropdown-item command="edit" v-hasPermi="['ai:mcp:edit']">修改</el-dropdown-item>
            <el-dropdown-item command="delete" divided v-hasPermi="['ai:mcp:remove']">
              <span class="mcp-server-card__danger">删除</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <p class="mcp-server-card__desc" :title="item.description || ''">
      {{ item.description || '暂无描述' }}
    </p>

    <div class="mcp-server-card__foot">
      <span class="mcp-server-card__tag">
        <el-icon><Link /></el-icon>
        {{ transportLabel }}
      </span>
      <span class="mcp-server-card__status" :class="`mcp-server-card__status--${statusType}`" :title="statusTitle">
        <el-icon v-if="statusType === 'ok'"><CircleCheckFilled /></el-icon>
        <el-icon v-else-if="statusType === 'fail'"><CircleCloseFilled /></el-icon>
        <el-icon v-else><RemoveFilled /></el-icon>
      </span>
      <span class="mcp-server-card__tools" :class="{ 'mcp-server-card__tools--hint': true }">
        <el-icon><SetUp /></el-icon>
        {{ toolCountText }}
        <span class="mcp-server-card__open-hint">查看</span>
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  CircleCheckFilled,
  CircleCloseFilled,
  Connection,
  Cpu,
  Link,
  MoreFilled,
  Monitor,
  RemoveFilled,
  SetUp,
} from '@element-plus/icons-vue'

/**
 * MCP 服务卡片：展示名称、描述、传输类型、探测状态与工具数量。
 */
const props = defineProps({
  /** @type {import('vue').PropType<Record<string, unknown>>} */
  item: { type: Object, required: true },
})

const emit = defineEmits(['open', 'test', 'edit', 'delete'])

const transportLabel = computed(() => {
  const map = {
    STDIO: '本地',
    SSE: '联网',
    STREAMABLE_HTTP: 'MCP',
  }
  return map[props.item.transport] || props.item.transport || '—'
})

const iconType = computed(() => {
  if (props.item.transport === 'STDIO') return 'stdio'
  if (props.item.transport === 'SSE') return 'sse'
  return 'http'
})

const iconComponent = computed(() => {
  if (props.item.transport === 'STDIO') return Cpu
  if (props.item.transport === 'SSE') return Connection
  return Monitor
})

const statusType = computed(() => {
  if (props.item.status !== 0) return 'off'
  if (props.item.lastTestStatus === 'SUCCESS') return 'ok'
  if (props.item.lastTestStatus === 'FAILED') return 'fail'
  return 'unknown'
})

const statusTitle = computed(() => {
  if (props.item.status !== 0) return '已停用'
  if (props.item.lastTestStatus === 'SUCCESS') return '连接正常'
  if (props.item.lastTestStatus === 'FAILED') return props.item.lastTestMsg || '连接失败'
  return '未测试'
})

const toolCountText = computed(() => {
  const n = props.item.toolCount
  if (n == null || n < 0) return '未检测'
  return `${n} 个工具`
})

function onCommand(cmd) {
  if (cmd === 'test') emit('test', props.item)
  else if (cmd === 'edit') emit('edit', props.item)
  else if (cmd === 'delete') emit('delete', props.item)
}
</script>

<style scoped>
.mcp-server-card {
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
  cursor: pointer;
}

.mcp-server-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.mcp-server-card--disabled {
  opacity: 0.72;
}

.mcp-server-card__head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.mcp-server-card__icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.mcp-server-card__icon--stdio {
  background: linear-gradient(135deg, #606266, #909399);
}

.mcp-server-card__icon--sse {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.mcp-server-card__icon--http {
  background: linear-gradient(135deg, #0a2463, #409eff);
}

.mcp-server-card__title-wrap {
  flex: 1;
  min-width: 0;
}

.mcp-server-card__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mcp-server-card__more {
  flex-shrink: 0;
  padding: 4px;
  margin: -4px -4px 0 0;
}

.mcp-server-card__desc {
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

.mcp-server-card__foot {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 10px;
}

.mcp-server-card__tag,
.mcp-server-card__tools {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.mcp-server-card__tools {
  margin-left: auto;
}

.mcp-server-card__tools--hint {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
}

.mcp-server-card:hover .mcp-server-card__tools--hint {
  background: var(--el-color-primary);
  color: #fff;
}

.mcp-server-card__open-hint {
  margin-left: 2px;
  font-size: 11px;
  opacity: 0.9;
}

.mcp-server-card__status {
  display: inline-flex;
  font-size: 16px;
}

.mcp-server-card__status--ok {
  color: var(--el-color-success);
}

.mcp-server-card__status--fail {
  color: var(--el-color-danger);
}

.mcp-server-card__status--off,
.mcp-server-card__status--unknown {
  color: var(--el-text-color-placeholder);
}

.mcp-server-card__danger {
  color: var(--el-color-danger);
}
</style>
