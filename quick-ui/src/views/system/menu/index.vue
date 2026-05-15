<template>
  <div class="app-container">
    <el-form :inline="true" :model="query" class="mb-2">
      <el-form-item label="菜单名称">
        <el-input v-model="query.menuName" placeholder="请输入菜单名称" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData" v-hasPermi="['system:menu:list']">查询</el-button>
        <el-button @click="resetQuery" v-hasPermi="['system:menu:list']">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row class="menu-toolbar" :gutter="8" align="middle">
      <el-col :span="12">
        <el-button type="primary" plain @click="openAdd()" v-hasPermi="['system:menu:add']">新增</el-button>
        <el-button
          type="success"
          plain
          :disabled="selection.length !== 1"
          @click="toolbarEdit"
          v-hasPermi="['system:menu:edit']"
        >修改</el-button>
        <el-button
          type="danger"
          plain
          :disabled="!selection.length"
          @click="toolbarBatchRemove"
          v-hasPermi="['system:menu:remove']"
        >删除</el-button>
        <el-tooltip content="菜单树暂不支持导出" placement="top">
          <span class="menu-toolbar__export-wrap">
            <el-button type="warning" plain disabled>导出</el-button>
          </span>
        </el-tooltip>
        <el-button type="info" plain @click="toggleExpand">{{ expandAll ? '折叠' : '展开' }}</el-button>
      </el-col>
    </el-row>

    <el-table
      :key="String(expandAll)"
      ref="tableRef"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      border
      :tree-props="{ children: 'children' }"
      :default-expand-all="expandAll"
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="48" align="center" reserve-selection />
      <el-table-column prop="menuName" label="菜单名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="icon" label="图标" width="80" align="center">
        <template #default="{ row }">
          <svg-icon v-if="row.icon" :icon-class="row.icon" />
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序" width="80" align="center" />
      <el-table-column prop="perms" label="权限标识" min-width="140" show-overflow-tooltip />
      <el-table-column prop="component" label="组件路径" min-width="160" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <c7-dict-tag :model-value="row.status" :options="sys_normal_disable" dict-type="success" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAdd(row)" v-hasPermi="['system:menu:add']">新增</el-button>
          <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['system:menu:edit']">修改</el-button>
          <c7-button
            btn-type="delete"
            link
            confirm
            :confirm-message="`确认删除「${row.menuName}」吗？`"
            :click-function="() => removeRow(row)"
            v-hasPermi="['system:menu:remove']"
          />
        </template>
      </el-table-column>
    </el-table>

    <add-or-update ref="formRef" @success="loadData" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import { listMenu, delMenu } from '@/api/system/menu'
import AddOrUpdate from './add-or-update.vue'

defineOptions({ name: 'SysMenu' })

const { sys_normal_disable } = useDict('sys_normal_disable')
const loading = ref(false)
const menuList = ref([])
const expandAll = ref(true)
const tableRef = ref(null)
const formRef = ref(null)
const query = ref({ menuName: '', status: '' })
const selection = ref([])

function onSelectionChange(rows) {
  selection.value = rows || []
}

function toolbarEdit() {
  const row = selection.value[0]
  if (row) openEdit(row)
}

function toolbarBatchRemove() {
  const rows = selection.value
  if (!rows.length) return
  ElMessageBox.confirm(`确认删除选中的 ${rows.length} 条菜单吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
    .then(async () => {
      for (const row of rows) {
        await delMenu(row.menuId)
      }
      ElMessage.success('删除成功')
      tableRef.value?.clearSelection?.()
      selection.value = []
      loadData()
    })
    .catch(() => {})
}

function loadData() {
  loading.value = true
  listMenu(query.value)
    .then((res) => {
      menuList.value = res.data || []
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  query.value = { menuName: '', status: '' }
  loadData()
}

function toggleExpand() {
  expandAll.value = !expandAll.value
  loadData()
}

function openAdd(row) {
  formRef.value?.open({ parentId: row?.menuId ?? -1 })
}

function openEdit(row) {
  formRef.value?.open({ menuId: row.menuId })
}

function removeRow(row) {
  return delMenu(row.menuId).then(() => loadData())
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.menu-toolbar {
  margin-bottom: 12px;
}
.menu-toolbar :deep(.el-button + .el-button),
.menu-toolbar .menu-toolbar__export-wrap + .el-button {
  margin-left: 8px;
}
.menu-toolbar__export-wrap {
  display: inline-block;
  margin-left: 8px;
  vertical-align: middle;
}
</style>
