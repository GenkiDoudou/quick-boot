<template>
  <el-dialog v-model="visible" title="预览代码" width="900px" destroy-on-close @open="load">
    <el-tabs v-if="items.length">
      <el-tab-pane v-for="item in items" :key="item.templateName" :label="tabLabel(item.templateName)">
        <div class="preview-toolbar">
          <el-button size="small" @click="copy(item.content)">复制</el-button>
        </div>
        <pre class="gen-preview-code">{{ item.content }}</pre>
      </el-tab-pane>
    </el-tabs>
    <el-empty v-else description="暂无预览" />
  </el-dialog>
</template>

<script setup>
/**
 * 代码预览弹窗。
 */
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { previewTable } from '@/api/tool/gen'

const props = defineProps({
  modelValue: Boolean,
  tableId: [Number, String]
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const items = ref([])

function tabLabel(name) {
  const i = name.lastIndexOf('/')
  return i >= 0 ? name.substring(i + 1) : name
}

async function load() {
  if (!props.tableId) return
  const res = await previewTable(props.tableId)
  items.value = res.data || []
}

async function copy(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

watch(() => props.tableId, () => {
  if (visible.value) load()
})
</script>

<style scoped>
.gen-preview-code {
  max-height: 420px;
  overflow: auto;
  background: var(--el-fill-color-light);
  padding: 12px;
  font-size: 12px;
  border-radius: 6px;
}
.preview-toolbar {
  margin-bottom: 8px;
}
</style>
