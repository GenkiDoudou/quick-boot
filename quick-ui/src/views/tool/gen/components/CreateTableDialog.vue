<template>
  <el-dialog v-model="visible" title="创建表" width="640px" destroy-on-close>
    <el-input v-model="sql" type="textarea" :rows="12" placeholder="请输入 CREATE TABLE 语句，支持多条" />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 执行建表 SQL 弹窗。
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTable } from '@/api/tool/gen'

const props = defineProps({
  modelValue: Boolean
})
const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const sql = ref('')
const loading = ref(false)

async function submit() {
  if (!sql.value?.trim()) {
    ElMessage.warning('请输入建表 SQL')
    return
  }
  loading.value = true
  try {
    await createTable(sql.value)
    ElMessage.success('建表成功，请通过「导入」选择新表')
    visible.value = false
    sql.value = ''
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
