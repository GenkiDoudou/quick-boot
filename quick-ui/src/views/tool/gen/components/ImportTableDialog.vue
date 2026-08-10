<template>
  <el-dialog v-model="visible" title="导入表" width="720px" destroy-on-close @open="loadDb">
    <el-form :inline="true" class="mb-2">
      <el-form-item label="表名称">
        <el-input v-model="query.tableName" clearable placeholder="表名称" @keyup.enter="loadDb" />
      </el-form-item>
      <el-form-item label="表描述">
        <el-input v-model="query.tableComment" clearable placeholder="表描述" @keyup.enter="loadDb" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadDb">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table ref="tableRef" :data="dbTables" height="360" @selection-change="onSelect">
      <el-table-column type="selection" width="48" />
      <el-table-column prop="tableName" label="表名称" />
      <el-table-column prop="tableComment" label="表描述" show-overflow-tooltip />
    </el-table>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 从数据库导入表弹窗。
 */
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { importTable, listDbTable } from '@/api/tool/gen'

const props = defineProps({
  modelValue: Boolean
})
const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const query = reactive({ tableName: '', tableComment: '' })
const dbTables = ref([])
const selected = ref([])
const loading = ref(false)

async function loadDb() {
  const res = await listDbTable({ ...query })
  dbTables.value = res.data || []
}

function onSelect(rows) {
  selected.value = rows || []
}

async function submit() {
  if (!selected.value.length) {
    ElMessage.warning('请选择要导入的表')
    return
  }
  loading.value = true
  try {
    await importTable(selected.value.map((r) => r.tableName))
    ElMessage.success('导入成功')
    visible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
