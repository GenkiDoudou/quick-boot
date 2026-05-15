<template>
  <div class="c7-json-table">
    <!-- ══════════════════════════════════════════
         搜索区域
    ══════════════════════════════════════════ -->
    <el-card v-if="searchColumns && searchColumns.length" class="c7-json-table__search" shadow="never">
      <el-form
        ref="searchFormRef"
        :model="searchParam"
        :inline="true"
        label-width="80px"
        @keyup.enter="handleSearch"
      >
        <el-form-item
          v-for="col in searchColumns"
          :key="col.prop"
          :label="col.label"
          :prop="col.prop"
        >
          <!-- 输入框 -->
          <template v-if="!col.type || col.type === 'input'">
            <el-input
              v-model="searchParam[col.prop]"
              :placeholder="col.placeholder ?? '请输入' + col.label"
              clearable
              style="width:200px"
              v-bind="col.props"
            />
          </template>
          <!-- 下拉选择 -->
          <template v-else-if="col.type === 'select'">
            <el-select
              v-model="searchParam[col.prop]"
              :placeholder="col.placeholder ?? '请选择' + col.label"
              clearable
              style="width:200px"
              v-bind="col.props"
            >
              <el-option
                v-for="opt in (col.dataList ?? [])"
                :key="opt[col.valueKey ?? 'value']"
                :label="opt[col.labelKey ?? 'label']"
                :value="opt[col.valueKey ?? 'value']"
              />
            </el-select>
          </template>
          <!-- 日期范围 -->
          <template v-else-if="col.type === 'daterange'">
            <el-date-picker
              v-model="searchParam[col.prop]"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width:240px"
              v-bind="col.props"
            />
          </template>
          <!-- 日期 -->
          <template v-else-if="col.type === 'date'">
            <el-date-picker
              v-model="searchParam[col.prop]"
              type="date"
              :placeholder="col.placeholder ?? '请选择' + col.label"
              value-format="YYYY-MM-DD"
              style="width:200px"
              v-bind="col.props"
            />
          </template>
          <!-- slot 类型搜索项 -->
          <template v-else-if="col.type === 'slot'">
            <slot :name="'search-' + col.prop" :param="searchParam" />
          </template>
        </el-form-item>

        <!-- 搜索按钮组 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <!-- 额外搜索内容 slot -->
          <slot name="search-extra" :param="searchParam" />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ══════════════════════════════════════════
         工具栏
    ══════════════════════════════════════════ -->
    <div class="c7-json-table__toolbar">
      <!-- 左侧：操作按钮 -->
      <div class="c7-json-table__toolbar-left">
        <slot name="toolbar-left" :selection="selectedRows" />
        <el-button
          v-if="deleteFunction"
          type="danger"
          plain
          :disabled="!selectedRows.length"
          @click="handleDelete"
        >
          <el-icon><Delete /></el-icon> 删除
        </el-button>
        <el-button
          v-if="exportFunction"
          type="success"
          plain
          @click="handleExport"
        >
          <el-icon><Download /></el-icon> 导出
        </el-button>
      </div>
      <!-- 右侧 -->
      <div class="c7-json-table__toolbar-right">
        <slot name="toolbar-right" />
        <!-- 列设置按钮 -->
        <el-tooltip content="列设置" placement="top">
          <el-button circle plain @click="columnSettingVisible = true">
            <el-icon><Setting /></el-icon>
          </el-button>
        </el-tooltip>
        <!-- 刷新按钮 -->
        <el-tooltip content="刷新" placement="top">
          <el-button circle plain :loading="loading" @click="refreshData">
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         数据表格
    ══════════════════════════════════════════ -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      :border="border"
      :stripe="stripe"
      :row-key="rowKey"
      :tree-props="treeProps"
      :lazy="lazy"
      :load="loadFunction"
      :empty-text="emptyText ?? '暂无数据'"
      v-bind="$attrs"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <!-- 多选列 -->
      <el-table-column v-if="selection" type="selection" width="50" fixed="left" />
      <!-- 序号列 -->
      <el-table-column v-if="showIndex" type="index" label="#" width="60" />

      <!-- 数据列（使用 C7JsonTableColumn 渲染）-->
      <C7JsonTableColumn :columns="visibleTableColumns">
        <!-- 透传所有具名插槽（列 slot） -->
        <template v-for="(_, name) in $slots" #[name]="slotProps">
          <slot :name="name" v-bind="slotProps ?? {}" />
        </template>
      </C7JsonTableColumn>

      <!-- 空数据 slot -->
      <template v-if="$slots.empty" #empty>
        <slot name="empty" />
      </template>
    </el-table>

    <!-- ══════════════════════════════════════════
         分页
    ══════════════════════════════════════════ -->
    <div class="c7-json-table__pagination">
      <slot name="pagination" :total="total" :page="currentPage" :page-size="currentPageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="currentPageSize"
          :total="total"
          :page-sizes="pageSizes"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </slot>
    </div>

    <!-- ══════════════════════════════════════════
         列设置对话框
    ══════════════════════════════════════════ -->
    <el-dialog
      v-model="columnSettingVisible"
      title="列设置"
      width="480px"
      append-to-body
      destroy-on-close
    >
      <div class="c7-json-table__col-setting">
        <el-checkbox
          v-for="col in settableColumns"
          :key="col.prop"
          v-model="col._visible"
          @change="saveColumnSetting"
        >
          {{ col.label }}
        </el-checkbox>
      </div>
      <template #footer>
        <el-button @click="resetColumnSetting">重置</el-button>
        <el-button type="primary" @click="columnSettingVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { Search, Refresh, Delete, Download, Setting, RefreshRight } from '@element-plus/icons-vue'
import C7JsonTableColumn from '../C7JsonTableColumn/index.vue'

defineOptions({ name: 'C7JsonTable', inheritAttrs: false })

// ────────────────────────────────────────────
// Props 定义
// ────────────────────────────────────────────
const props = defineProps({
  /** 数据获取函数：(params) => Promise<{ rows, total }> */
  listFunction: { type: Function, default: null },
  /** 表格列配置（TableColumnProps[]） */
  tableColumns: { type: Array, default: () => [] },
  /** 搜索表单列配置 */
  searchColumns: { type: Array, default: () => [] },
  /** 删除函数：(ids) => Promise<any> */
  deleteFunction: { type: Function, default: null },
  /** 导出函数：(params) => Promise<any> */
  exportFunction: { type: Function, default: null },
  /** 树形懒加载函数 */
  loadFunction: { type: Function, default: null },
  /** 是否开启多选 */
  selection: { type: Boolean, default: true },
  /** 是否显示序号列 */
  showIndex: { type: Boolean, default: false },
  /** 是否显示边框 */
  border: { type: Boolean, default: true },
  /** 是否斑马纹 */
  stripe: { type: Boolean, default: true },
  /** 行 key（树形/懒加载必填） */
  rowKey: { type: String, default: 'id' },
  /** 是否开启树形懒加载 */
  lazy: { type: Boolean, default: false },
  /** 树形 props */
  treeProps: { type: Object, default: () => ({ children: 'children', hasChildren: 'hasChildren' }) },
  /** 响应数据中 rows 的路径，支持 'data.records' 格式 */
  rowsKey: { type: String, default: 'data' },
  /** 响应数据中 total 的路径 */
  totalKey: { type: String, default: 'total' },
  /** 默认每页条数 */
  pageSize: { type: Number, default: 10 },
  /** 可选每页条数 */
  pageSizes: { type: Array, default: () => [10, 20, 50, 100] },
  /** 默认搜索参数（重置时恢复） */
  defaultSearchParam: { type: Object, default: () => ({}) },
  /** 删除前确认：(ids, rows) => Promise<boolean> | boolean，返回 false 取消 */
  beforeDelete: { type: Function, default: null },
  /** 判断删除是否成功：(response) => boolean */
  checkDeleteSuccess: { type: Function, default: null },
  /** 导出 loading 配置，false 不显示 loading */
  exportLoadingOptions: { type: [Object, Boolean], default: undefined },
  /** 列设置持久化的 localStorage key，不传则不持久化 */
  columnSettingKey: { type: String, default: '' },
  /** 空数据提示文字 */
  emptyText: { type: String, default: '' },
  /** 自定义通知函数，不传则使用 ElMessage */
  notify: { type: Function, default: null },
  /** 挂载后是否自动加载数据 */
  autoLoad: { type: Boolean, default: true },
})

const emit = defineEmits([
  'before-fetch',
  'after-fetch',
  'delete-success',
  'export-success',
  'selection-change',
  'sort-change',
])

// ────────────────────────────────────────────
// 内部状态
// ────────────────────────────────────────────
const tableRef = ref(null)
const searchFormRef = ref(null)
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const currentPageSize = ref(props.pageSize)
const selectedRows = ref([])
const columnSettingVisible = ref(false)

/** 搜索参数（响应式，初始为 defaultSearchParam 的副本） */
const searchParam = reactive({ ...props.defaultSearchParam })

/** 排序参数 */
const sortParam = reactive({ prop: '', order: '' })

// ────────────────────────────────────────────
// 列设置（_visible 字段控制可见性）
// ────────────────────────────────────────────

/** 可设置的列（排除 slot/index/selection 等系统列）*/
const settableColumns = ref([])

/** 初始化列设置，加载持久化状态 */
function initColumnSetting() {
  settableColumns.value = props.tableColumns
    .filter(col => col.prop)
    .map(col => ({
      ...col,
      _visible: true,
    }))

  // 读取持久化配置
  if (props.columnSettingKey) {
    try {
      const saved = JSON.parse(localStorage.getItem(props.columnSettingKey) || '{}')
      settableColumns.value.forEach(col => {
        if (col.prop in saved) {
          col._visible = saved[col.prop]
        }
      })
    } catch {
      // 忽略读取错误
    }
  }
}

/** 保存列设置到 localStorage */
function saveColumnSetting() {
  if (!props.columnSettingKey) return
  const map = {}
  settableColumns.value.forEach(col => { map[col.prop] = col._visible })
  localStorage.setItem(props.columnSettingKey, JSON.stringify(map))
}

/** 重置列设置 */
function resetColumnSetting() {
  settableColumns.value.forEach(col => { col._visible = true })
  saveColumnSetting()
}

/** 当前实际渲染的列（_visible 为 true）*/
const visibleTableColumns = computed(() =>
  settableColumns.value
    .filter(col => col._visible !== false)
    .map(col => {
      // 剥离内部控制字段，避免污染 el-table-column
      const { _visible, ...rest } = col
      return rest
    })
)

// ────────────────────────────────────────────
// 数据获取
// ────────────────────────────────────────────

/**
 * 按嵌套路径安全读取对象值
 * 例如 getByPath(res, 'data.records') => res.data.records
 */
function getByPath(obj, path) {
  if (!path) return obj
  return path.split('.').reduce((acc, key) => (acc != null ? acc[key] : undefined), obj)
}

/** 组装请求参数 */
function buildParams() {
  const params = {
    ...searchParam,
    pageNum: currentPage.value,
    pageSize: currentPageSize.value,
  }
  if (sortParam.prop) {
    params.orderByColumn = sortParam.prop
    params.isAsc = sortParam.order === 'ascending' ? 'asc' : 'desc'
  }
  return params
}

/** 获取数据 */
async function fetchData() {
  if (!props.listFunction) return
  loading.value = true
  try {
    const params = buildParams()
    emit('before-fetch', params)
    const res = await props.listFunction(params)
    const rows = getByPath(res, props.rowsKey) ?? []
    const tot = getByPath(res, props.totalKey) ?? 0
    tableData.value = rows
    total.value = tot
    emit('after-fetch', rows, tot)
  } catch (e) {
    console.error('[C7JsonTable] fetchData error:', e)
  } finally {
    loading.value = false
  }
}

// ────────────────────────────────────────────
// 搜索 / 重置
// ────────────────────────────────────────────

/** 点击搜索：回到第 1 页后拉数据 */
function handleSearch() {
  currentPage.value = 1
  fetchData()
}

/** 重置搜索：还原 defaultSearchParam，回第 1 页 */
function handleReset() {
  // 清空现有字段
  Object.keys(searchParam).forEach(k => delete searchParam[k])
  // 还原默认值
  Object.assign(searchParam, { ...props.defaultSearchParam })
  currentPage.value = 1
  fetchData()
}

// ────────────────────────────────────────────
// 分页
// ────────────────────────────────────────────
function handlePageChange(page) {
  currentPage.value = page
  fetchData()
}

function handlePageSizeChange(size) {
  currentPageSize.value = size
  currentPage.value = 1
  fetchData()
}

// ────────────────────────────────────────────
// 多选
// ────────────────────────────────────────────
function handleSelectionChange(rows) {
  selectedRows.value = rows
  emit('selection-change', rows)
}

// ────────────────────────────────────────────
// 排序
// ────────────────────────────────────────────
function handleSortChange({ prop, order }) {
  sortParam.prop = prop || ''
  sortParam.order = order || ''
  currentPage.value = 1
  fetchData()
  emit('sort-change', { prop, order })
}

// ────────────────────────────────────────────
// 删除
// ────────────────────────────────────────────
async function handleDelete() {
  if (!selectedRows.value.length) return
  const ids = selectedRows.value.map(row => row[props.rowKey])
  const rows = selectedRows.value

  // 前置确认
  if (props.beforeDelete) {
    const pass = await props.beforeDelete(ids, rows)
    if (pass === false) return
  } else {
    try {
      await ElMessageBox.confirm(
        `确认删除选中的 ${ids.length} 条数据吗？`,
        '删除确认',
        { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
      )
    } catch {
      return
    }
  }

  try {
    const res = await props.deleteFunction(ids)
    const ok = props.checkDeleteSuccess ? props.checkDeleteSuccess(res) : !!res
    if (ok) {
      showNotify('success', '删除成功')
      emit('delete-success', ids)
      refreshData()
    } else {
      showNotify('error', '删除失败')
    }
  } catch (e) {
    console.error('[C7JsonTable] handleDelete error:', e)
    showNotify('error', '删除失败')
  }
}

// ────────────────────────────────────────────
// 导出
// ────────────────────────────────────────────
async function handleExport() {
  if (!props.exportFunction) return
  let loadingInstance = null

  // 显示 loading
  if (props.exportLoadingOptions !== false) {
    const opts = typeof props.exportLoadingOptions === 'object'
      ? props.exportLoadingOptions
      : { text: '正在导出，请稍候...' }
    loadingInstance = ElLoading.service(opts)
  }

  try {
    const params = { ...searchParam }
    await props.exportFunction(params)
    showNotify('success', '导出成功')
    emit('export-success')
  } catch (e) {
    console.error('[C7JsonTable] handleExport error:', e)
    showNotify('error', '导出失败')
  } finally {
    loadingInstance?.close()
  }
}

// ────────────────────────────────────────────
// 通知工具
// ────────────────────────────────────────────
function showNotify(type, msg) {
  if (props.notify) {
    props.notify(type, msg)
  } else {
    ElMessage[type]?.(msg)
  }
}

// ────────────────────────────────────────────
// 刷新（保留当前页和搜索条件）
// ────────────────────────────────────────────
function refreshData() {
  fetchData()
}

/** 跳回第一页刷新 */
function getDataList() {
  currentPage.value = 1
  fetchData()
}

// ────────────────────────────────────────────
// 初始化
// ────────────────────────────────────────────
onMounted(() => {
  initColumnSetting()
  if (props.autoLoad) {
    fetchData()
  }
})

// tableColumns 变化时重新初始化列设置
watch(
  () => props.tableColumns,
  () => initColumnSetting(),
  { deep: true }
)

// ────────────────────────────────────────────
// 对外暴露
// ────────────────────────────────────────────
defineExpose({
  /** 重新加载（保留当前页） */
  refreshData,
  /** 重置到第 1 页加载 */
  getDataList,
  /** 当前选中行 */
  selectedRows,
  /** 搜索参数 */
  searchParam,
  /** 当前页码 */
  currentPage,
  /** 当前页尺寸 */
  currentPageSize,
  /** 总条数 */
  total,
  /** el-table 实例 */
  tableRef,
})
</script>

<style scoped>
.c7-json-table {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 搜索卡片 */
.c7-json-table__search {
  border-radius: 6px;
}
.c7-json-table__search :deep(.el-card__body) {
  padding: 16px 20px 4px;
}

/* 工具栏 */
.c7-json-table__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2px;
}
.c7-json-table__toolbar-left,
.c7-json-table__toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 分页 */
.c7-json-table__pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
}

/* 列设置对话框 */
.c7-json-table__col-setting {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 24px;
  padding: 8px 0;
}
</style>
