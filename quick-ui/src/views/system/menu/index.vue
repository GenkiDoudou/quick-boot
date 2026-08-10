<template>
  <div class="app-container">
    <el-form v-show="showSearch" :inline="true" :model="query" class="mb-2" @submit.prevent>
      <el-form-item label="菜单名称">
        <el-input
          v-model="query.menuName"
          placeholder="请输入菜单名称"
          clearable
          style="width: 200px"
          @keyup.enter="loadData"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="菜单状态" clearable style="width: 200px">
          <el-option v-for="d in (sys_normal_disable || [])" :key="d.value" :label="d.label" :value="d.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" v-hasPermi="['system:menu:list']" @click="loadData">搜索</el-button>
        <el-button :icon="Refresh" v-hasPermi="['system:menu:list']" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="menu-toolbar mb8" align="middle">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" v-hasPermi="['system:menu:add']" @click="openAdd()">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <C7ExcelDownload
          type="warning"
          plain
          :download-fn="handleExport"
          default-file-name="menu.xlsx"
          v-hasPermi="['system:menu:export']"
        >导出</C7ExcelDownload>
      </el-col>

      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Check" v-hasPermi="['system:menu:edit']" @click="handleSaveSort">
          保存排序
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain :icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <el-col :span="1.5" class="menu-toolbar__search-toggle">
        <el-tooltip :content="showSearch ? '隐藏搜索' : '显示搜索'" placement="top">
          <el-button circle :icon="Search" @click="showSearch = !showSearch" />
        </el-tooltip>
      </el-col>
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      border
      :default-expand-all="expandAll"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="菜单名称" :show-overflow-tooltip="true" min-width="220">
        <template #default="{ row }">
          <svg-icon v-if="row.icon" :icon-class="row.icon" class="menu-name__icon" />
          <span>{{ row.menuName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isFrame === '1' || isExternalLinkMenu(row)" type="danger" size="small">外链</el-tag>
          <C7DictTag v-else :model-value="row.menuType" :options="sys_menu_menu_type" />
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序" width="120" align="center">
        <template #default="{ row }">
          <el-input-number
            v-model="row.orderNum"
            controls-position="right"
            :min="0"
            :max="9999"
            size="small"
            style="width: 88px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="perms" label="权限标识" min-width="140" :show-overflow-tooltip="true" />
      <el-table-column prop="component" label="组件路径" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column prop="visible" label="显示" width="80" align="center">
        <template #default="{ row }">
          <C7DictTag :model-value="row.visible" :options="sys_show_hide" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" v-hasPermi="['system:menu:edit']" @click="openEdit(row)">
            修改
          </el-button>
          <el-button link type="primary" :icon="Plus" v-hasPermi="['system:menu:add']" @click="openAdd(row)">
            新增
          </el-button>
          <el-button link type="primary" :icon="Delete" v-hasPermi="['system:menu:remove']" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <add-or-update ref="formRef" @success="loadData" />

    <el-dialog v-model="importVisible" title="导入菜单" width="520px" destroy-on-close @closed="onImportDialogClosed">
      <C7ExcelUpload
        ref="importUploadRef"
        :upload-fn="importMenu"
        :template-download-fn="downloadMenuImportTemplate"
        template-file-name="menu-import-template.xlsx"
        @success="onImportSuccess"
        @cancel="importVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 系统菜单管理：树表 + 排序 + 增删改（对齐 bak，裁剪积木/BI）。
 */
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Edit, Plus, Refresh, Search, Sort } from '@element-plus/icons-vue'
import { useDict } from '@/utils/dict'
import {
  delMenu,
  listMenu,
  updateMenuSort,
  exportMenu,
  downloadMenuImportTemplate,
  importMenu
} from '@/api/system/menu'
import { isExternal } from '@/utils/validate'
import AddOrUpdate from './add-or-update.vue'

defineOptions({ name: 'SysMenu' })

const { sys_normal_disable, sys_menu_menu_type, sys_show_hide } = useDict(
  'sys_normal_disable',
  'sys_menu_menu_type',
  'sys_show_hide'
)

const loading = ref(false)
const menuList = ref([])
const expandAll = ref(false)
const refreshTable = ref(true)
const showSearch = ref(true)
const formRef = ref(null)
const importVisible = ref(false)
const importUploadRef = ref(null)
const query = ref({ menuName: '', status: '' })
const originalOrders = ref({})

/**
 * 新标签页外链：path 为 http(s) 且非 iframe 标记时的展示辅助。
 * @param {Record<string, any>} row
 */
function isExternalLinkMenu(row) {
  return row?.menuType === 'C' && isExternal(String(row?.path || ''))
}

/**
 * @param {Array} list
 */
function recordOriginalOrders(list) {
  if (!Array.isArray(list)) return
  for (const item of list) {
    if (item?.menuId != null) {
      originalOrders.value[item.menuId] = item.orderNum
    }
    if (item.children?.length) {
      recordOriginalOrders(item.children)
    }
  }
}

function loadData() {
  loading.value = true
  listMenu(query.value)
    .then((res) => {
      menuList.value = res.data || []
      originalOrders.value = {}
      recordOriginalOrders(menuList.value)
    })
    .finally(() => {
      loading.value = false
    })
}

function resetQuery() {
  query.value = { menuName: '', status: '' }
  loadData()
}

function toggleExpandAll() {
  refreshTable.value = false
  expandAll.value = !expandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/**
 * @param {Array} list
 * @param {Array} menuIds
 * @param {Array} orderNums
 */
function collectSortChanges(list, menuIds, orderNums) {
  if (!Array.isArray(list)) return
  for (const item of list) {
    const id = item.menuId
    if (id != null && String(originalOrders.value[id]) !== String(item.orderNum)) {
      menuIds.push(id)
      orderNums.push(item.orderNum)
    }
    if (item.children?.length) {
      collectSortChanges(item.children, menuIds, orderNums)
    }
  }
}

function handleSaveSort() {
  const menuIds = []
  const orderNums = []
  collectSortChanges(menuList.value, menuIds, orderNums)
  if (!menuIds.length) {
    ElMessage.warning('未检测到排序修改')
    return
  }
  updateMenuSort({ menuIds, orderNums }).then(() => {
    ElMessage.success('排序保存成功')
    recordOriginalOrders(menuList.value)
  })
}

function handleExport() {
  return exportMenu({
    menuName: query.value.menuName || undefined,
    status: query.value.status || undefined
  })
}

function onImportSuccess(result) {
  const failCount = Number(result?.failCount) || 0
  if (failCount === 0) {
    importVisible.value = false
  }
  loadData()
}

function onImportDialogClosed() {
  importUploadRef.value?.reset?.()
}

/**
 * @param {Record<string, any>} [row]
 */
function openAdd(row) {
  formRef.value?.open({ parentId: row?.menuId ?? 0 })
}

/**
 * @param {Record<string, any>} row
 */
function openEdit(row) {
  formRef.value?.open({ menuId: row.menuId })
}

/**
 * @param {Record<string, any>} row
 */
function handleDelete(row) {
  ElMessageBox.confirm(`是否确认删除名称为「${row.menuName}」的数据项？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => delMenu(row.menuId))
    .then(() => {
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.menu-toolbar {
  margin-bottom: 12px;
  flex-wrap: nowrap;
}
.menu-toolbar__search-toggle {
  margin-left: auto;
}
.menu-name__icon {
  margin-right: 6px;
  vertical-align: -0.15em;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
