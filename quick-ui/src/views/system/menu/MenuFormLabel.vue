<template>
  <span class="menu-form-label">
    <el-tooltip v-if="tip" :content="tip" placement="top">
      <el-icon class="menu-form-label__icon"><QuestionFilled /></el-icon>
    </el-tooltip>
    {{ text }}
  </span>
</template>

<script setup>
/**
 * 菜单表单项标签：文字 + 可选问号 tooltip 说明（对齐 RuoYi 菜单表单体验）。
 */
import { QuestionFilled } from '@element-plus/icons-vue'

defineProps({
  /** 标签文字 */
  text: { type: String, required: true },
  /** 悬停说明；为空则不显示问号图标 */
  tip: { type: String, default: '' }
})
</script>

<style scoped>
.menu-form-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.menu-form-label__icon {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  cursor: help;
  vertical-align: middle;
}
</style>
