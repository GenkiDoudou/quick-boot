<template>
  <div class="c7-json-table" v-bind="$attrs">
    <!-- 搜索区 -->
    <el-form
        v-if="searchColumns.length"
        class="c7-json-table__search"
        :model="searchParam"
        label-width="auto"
        @submit.prevent="handleSearchSubmit"
        @keyup.enter="handleSearchSubmit"
    >
      <el-row :gutter="12">
        <el-col
            v-for="(col, idx) in sortedSearchColumns"
            :key="col.prop || col.label || idx"
            :span="col.span ?? 6"
        >
          <el-form-item :label="col.label" :prop="col.prop">
            <template v-if="col.type === 'input' || col.type === undefined || col.type === ''">
              <el-input
                  v-model="searchParam[col.prop]"
                  clearable
                  v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'select'">
              <C7Select
                  v-model="searchParam[col.prop]"
                  :data-list="col.dataList ?? col.options ?? []"
                  clearable
                  style="width: 100%"
                  v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'date'">
              <C7DatePicker
                  v-model="searchParam[col.prop]"
                  type="date"
                  clearable
                  style="width: 100%"
                  v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'daterange'">
              <C7DatePicker
                  v-model="searchParam[col.prop]"
                  type="daterange"
                  clearable
                  style="width: 100%"
                  v-bind="col.props || {}"
              />
            </template>
            <template v-else-if="col.type === 'slot'">
              <slot :name="col.prop" :form-data="searchParam" :column="col"/>
            </template>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" @click="handleSearchSubmit">搜索</el-button>
            <el-button @click="handleSearchReset">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
      <slot name="search-extra"/>
    </el-form>

    <!-- 工具栏 -->
    <el-row class="c7-json-table__toolbar" :gutter="8" align="middle">
      <el-col :span="12">
        <slot name="toolbar-left"/>
        <el-button
            v-if="deleteFunction"
            type="danger"
            plain
            :disabled="!selectedRows.length"
            @click="handleBatchDelete"
        >批量删除
        </el-button>
        <C7ExcelDownload
            v-if="exportFunction"
            :download-fn="exportDownloadFn"
            :default-file-name="exportDefaultFileName"
            @success="onExportBlobSuccess"
        >
          导出
        </C7ExcelDownload>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <slot name="toolbar-right"/>
        <el-button v-if="columnSettingKey" circle title="列设置" @click="columnPopoverVisible = true">
          <el-icon>
            <Setting/>
          </el-icon>
        </el-button>
        <el-button circle title="刷新" @click="refreshData">
          <el-icon>
            <Refresh/>
          </el-icon>
        </el-button>
      </el-col>
    </el-row>

    <el-drawer v-model="columnPopoverVisible" title="列设置" direction="rtl" size="280px">
      <div v-for="c in columnSettingItems" :key="c.prop" class="c7-json-table__col-setting-row">
        <el-checkbox v-model="columnCheck[c.prop]">{{ c.label || c.prop }}</el-checkbox>
      </div>
      <el-button type="primary" link style="margin-top: 12px" @click="resetColumnSettings">重置列设置</el-button>
    </el-drawer>

    <!-- 表格 -->
    <el-table
        ref="tableRef"
        v-loading="listLoading"
        :data="tableRows"
        :border="border"
        :stripe="stripe"
        :row-key="rowKey"
        :lazy="lazy"
        :load="load"
        :tree-props="treeProps"
        @selection-change="onSelectionChange"
        @sort-change="onSortChange"
    >
      <el-table-column v-if="showSelection" type="selection" width="48" align="center"/>
      <el-table-column v-if="showIndex" type="index" label="#" width="55" align="center"/>
      <slot
          v-if="$slots['table-columns']"
          name="table-columns"
          :table-columns="effectiveTableColumns"
          :search-param="searchParam"
          :selected-rows="selectedRows"
          :refresh-data="refreshData"
          :get-data-list="getDataList"
      />
      <C7JsonTableColumn v-else :columns="effectiveTableColumns" :empty-text="emptyText">
        <template v-for="name in forwardedSlotNames" :key="name" #[name]="scope">
          <slot :name="name" v-bind="scope || {}"/>
        </template>
      </C7JsonTableColumn>
    </el-table>

    <div class="c7-json-table__pagination">
      <C7Pagination
          v-model:current-page="currentPage"
          v-model:page-size="currentPageSize"
          :total="total"
          :page-sizes="pageSizes"
          :disabled="listLoading"
          @change="onPaginationChange"
      />
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref, useSlots, watch} from 'vue'
import {ElLoading, ElMessage, ElMessageBox} from 'element-plus'
import {Refresh, Setting} from '@element-plus/icons-vue'
import cloneDeep from 'lodash/cloneDeep'
import get from 'lodash/get'
import C7Pagination from '../C7Pagination/index.vue'
import C7JsonTableColumn from '../C7JsonTableColumn/index.vue'
import C7Select from '../C7Select/index.vue'
import C7DatePicker from '../C7DatePicker/index.vue'
import C7ExcelDownload from '../C7ExcelDownload/index.vue'

defineOptions({name: 'C7JsonTable', inheritAttrs: false})

const RESERVED_SLOTS = new Set(['search-extra', 'toolbar-left', 'toolbar-right', 'table-columns'])

/**
 * 一体化 JSON 配置列表：搜索区（子集）、工具栏、表格、分页、列设置、删除/导出。
 *
 * **取消列表请求**：请使用 **`beforeFetch` prop**，返回 **`false`**（支持 **`Promise`**）可阻止调用 **`listFunction`**。
 * **`before-fetch` 事件**仅用于监听参数，**返回值不参与拦截**（与项目内未见「emit 可取消」惯例一致）。
 *
 * @prop {function(object): Promise<unknown>} listFunction 列表请求；入参含 **`...searchParam`**、**`pageNum`**、**`pageSize`**、**`orderByColumn`**、**`isAsc`**（**`asc`/`desc`/''**）
 */
const props = defineProps({
  /** 列表请求函数 */
  listFunction: {type: Function, required: true},
  /** 表格列配置（同 C7JsonTableColumn） */
  tableColumns: {type: Array, required: true},
  /** 搜索列配置（JsonForm 字段子集） */
  searchColumns: {type: Array, default: () => []},
  /** 搜索默认值；重置时恢复此快照 */
  defaultSearchParam: {type: Object, default: () => ({})},
  /** 从列表响应取行数组的点路径（默认对齐 `request` 成功体 **`{ data: { records, total } }`**） */
  rowsKey: {type: String, default: 'data.records'},
  /** 从列表响应取总数的点路径 */
  totalKey: {type: String, default: 'data.total'},
  /** 行主键字段名，传给 `el-table` 的 `row-key` */
  rowKey: {type: String, default: 'id'},
  border: {type: Boolean, default: true},
  stripe: {type: Boolean, default: true},
  showSelection: {type: Boolean, default: true},
  showIndex: {type: Boolean, default: false},
  /** 列显隐持久化 key；不传则不展示列设置 */
  columnSettingKey: {type: String, default: ''},
  /** 批量删除 API：入参 id 数组 */
  deleteFunction: {type: Function, default: undefined},
  /** 导出：返回 Blob 或 `{ data, headers }`；入参为组件传入的 **`searchParam` 深拷贝快照**（见 **`exportDownloadFn`**） */
  exportFunction: {type: Function, default: undefined},
  /** 删除前钩子，返回 `false` 取消 */
  beforeDelete: {type: Function, default: undefined},
  /** 自定义删除成功判定 */
  checkDeleteSuccess: {type: Function, default: undefined},
  /** 导出默认文件名（当响应头无 Content-Disposition 时） */
  exportDefaultFileName: {type: String, default: 'export.xlsx'},
  /**
   * 为 **`false`** 时不使用全屏 **`ElLoading`**（仍保留 **`C7ExcelDownload`** 按钮 loading）。
   * 非 **`false`** 时导出过程中额外全屏 Loading（与原始需求「内置导出 loading」一致）。
   */
  exportLoadingOptions: {type: [Boolean, Object], default: true},
  deleteConfirmMessage: {type: String, default: '确认删除选中记录？'},
  /** 传给 C7JsonTableColumn 的表级空文案 */
  emptyText: {type: String, default: undefined},
  /** 分页可选条数 */
  pageSizes: {type: Array, default: () => [10, 20, 50, 100]},
  lazy: {type: Boolean, default: undefined},
  load: {type: Function, default: undefined},
  treeProps: {type: Object, default: undefined},
  /**
   * 列表请求前钩子；返回 **`false`** 或 **`Promise<false>`** 时**不**调用 **`listFunction`**。
   * @param {Record<string, unknown>} params 即将传给 listFunction 的参数
   */
  beforeFetch: {type: Function, default: undefined},
})

const emit = defineEmits([
  'update:searchParam',
  'before-fetch',
  'after-fetch',
  'fetch-error',
  'selection-change',
  'sort-change',
  'delete-success',
  'export-success',
])

const slots = useSlots()

/** 转发给 C7JsonTableColumn 的插槽名（排除本组件保留插槽） */
const forwardedSlotNames = computed(() =>
    Object.keys(slots).filter((name) => !RESERVED_SLOTS.has(name)),
)

const tableRef = ref(null)
const exportBtnRef = ref(null)
const listLoading = ref(false)
const tableRows = ref([])
const total = ref(0)
const currentPage = ref(1)
const currentPageSize = ref(10)
const selectedRows = ref([])
const searchParam = reactive({})
const orderByColumn = ref('')
const isAsc = ref('')
const columnPopoverVisible = ref(false)
/** 列设置勾选：prop -> 是否显示 */
const columnCheck = reactive({})

const STORAGE_PREFIX = 'c7-json-table:columns:'

function warnDev(msg) {
  if (import.meta.env.DEV) console.warn(`[C7JsonTable] ${msg}`)
}

function initSearchParam() {
  const base = cloneDeep(props.defaultSearchParam || {})
  Object.keys(searchParam).forEach((k) => {
    delete searchParam[k]
  })
  Object.assign(searchParam, base)
}

const sortedSearchColumns = computed(() => {
  const cols = [...(props.searchColumns || [])]
  for (const col of cols) {
    const t = col.type
    if (t && !['input', 'select', 'date', 'daterange', 'slot', undefined, ''].includes(t)) {
      warnDev(`searchColumns 未知 type="${t}"（prop=${col.prop}），已跳过`)
    }
  }
  return cols
      .filter((col) => {
        const t = col.type
        return !t || ['input', 'select', 'date', 'daterange', 'slot', '', undefined].includes(t)
      })
      .slice()
      .sort((a, b) => {
        const oa = a.order
        const ob = b.order
        if (oa == null && ob == null) return 0
        if (oa == null) return 1
        if (ob == null) return -1
        return Number(oa) - Number(ob)
      })
})

function loadColumnVisibilityFromStorage() {
  if (!props.columnSettingKey) return {}
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + props.columnSettingKey)
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function saveColumnVisibilityToStorage(map) {
  if (!props.columnSettingKey) return
  try {
    localStorage.setItem(STORAGE_PREFIX + props.columnSettingKey, JSON.stringify(map))
  } catch (e) {
    warnDev(`写入列设置失败: ${e}`)
  }
}

const columnSettingItems = computed(() =>
    (props.tableColumns || []).filter((c) => c && c.prop),
)

function syncColumnCheckFromStorage() {
  const stored = loadColumnVisibilityFromStorage()
  for (const c of columnSettingItems.value) {
    const prop = c.prop
    if (Object.prototype.hasOwnProperty.call(stored, prop)) {
      columnCheck[prop] = !!stored[prop]
    } else {
      const def = c._visible !== false && c.visible !== false
      columnCheck[prop] = def
    }
  }
}

watch(
    () => props.columnSettingKey,
    () => {
      syncColumnCheckFromStorage()
    },
)

watch(
    () => props.tableColumns,
    () => {
      syncColumnCheckFromStorage()
    },
    {deep: true},
)

watch(columnCheck, () => {
  if (!props.columnSettingKey) return
  const map = {}
  for (const c of columnSettingItems.value) {
    map[c.prop] = !!columnCheck[c.prop]
  }
  saveColumnVisibilityToStorage(map)
}, {deep: true})

const effectiveTableColumns = computed(() => {
  const cols = props.tableColumns || []
  return cols
      .filter((col) => col && typeof col === 'object')
      .map((col) => {
        if (!col.prop) {
          return col
        }
        const checked = columnCheck[col.prop]
        const visible = checked === undefined ? (col.visible !== false && col._visible !== false) : checked
        return {...col, visible}
      })
      .filter((col) => col.visible !== false)
})

function buildListParams() {
  return {
    ...searchParam,
    pageNum: currentPage.value,
    pageSize: currentPageSize.value,
    orderByColumn: orderByColumn.value,
    isAsc: isAsc.value,
  }
}

async function fetchList() {
  const params = buildListParams()
  emit('before-fetch', params)
  if (typeof props.beforeFetch === 'function') {
    const allow = await props.beforeFetch(params)
    if (allow === false) return
  }
  listLoading.value = true
  try {
    const res = await props.listFunction(params)
    const rows = get(res, props.rowsKey)
    const tot = get(res, props.totalKey)
    if (!Array.isArray(rows)) {
      warnDev(`rowsKey="${props.rowsKey}" 未解析到数组，已置空列表`)
      tableRows.value = []
    } else {
      tableRows.value = rows
    }
    const n = Number(tot)
    total.value = Number.isFinite(n) ? n : 0
    emit('after-fetch', tableRows.value, total.value)
  } catch (err) {
    emit('fetch-error', err)
    tableRows.value = []
    total.value = 0
  } finally {
    listLoading.value = false
  }
}

function refreshData() {
  return fetchList()
}

function getDataList() {
  currentPage.value = 1
  return fetchList()
}

function handleSearchSubmit() {
  currentPage.value = 1
  return fetchList()
}

function handleSearchReset() {
  initSearchParam()
  currentPage.value = 1
  return fetchList()
}

function onPaginationChange() {
  return fetchList()
}

function onSelectionChange(rows) {
  selectedRows.value = rows || []
  emit('selection-change', selectedRows.value)
}

function onSortChange(evt) {
  const {prop, order} = evt
  if (!order) {
    orderByColumn.value = ''
    isAsc.value = ''
  } else {
    orderByColumn.value = prop || ''
    isAsc.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  }
  emit('sort-change', evt)
  return fetchList()
}

async function handleBatchDelete() {
  const rows = selectedRows.value
  if (!rows.length) return
  const key = props.rowKey
  const ids = rows.map((r) => r[key]).filter((v) => v != null)
  if (typeof props.beforeDelete === 'function') {
    const ok = await props.beforeDelete(ids, rows)
    if (ok === false) return
  }
  try {
    await ElMessageBox.confirm(props.deleteConfirmMessage, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  if (!props.deleteFunction) return
  try {
    const res = await props.deleteFunction(ids)
    let success
    if (typeof props.checkDeleteSuccess === 'function') {
      success = props.checkDeleteSuccess(res)
    } else {
      success = !!res
    }
    if (!success) {
      ElMessage.error('删除失败')
      return
    }
    ElMessage.success('删除成功')
    emit('delete-success', ids)
    await refreshData()
  } catch (e) {
    emit('fetch-error', e)
  }
}

function resetColumnSettings() {
  if (props.columnSettingKey) {
    try {
      localStorage.removeItem(STORAGE_PREFIX + props.columnSettingKey)
    } catch {
      /* ignore */
    }
  }
  for (const c of columnSettingItems.value) {
    const def = c._visible !== false && c.visible !== false
    columnCheck[c.prop] = def
  }
  ElMessage.success('已重置列设置')
}

/**
 * 供 **`C7ExcelDownload`** 使用：点击时固定 **`searchParam`** 快照再调 **`exportFunction(快照)`**；
 * 若 **`exportLoadingOptions !== false`** 则叠加全屏 **`ElLoading`**（与按钮 **`downloading`** 并存，便于长耗时导出反馈）。
 */
function exportDownloadFn() {
  const snapshot = cloneDeep(searchParam)
  const run = async () => {
    if (typeof props.exportFunction !== 'function') {
      throw new Error('缺少 exportFunction')
    }
    return props.exportFunction(snapshot)
  }
  if (props.exportLoadingOptions === false) {
    return run()
  }
  const inst = ElLoading.service({fullscreen: true, text: '导出中…'})
  return run().finally(() => {
    inst.close()
  })
}

function onExportBlobSuccess() {
  emit('export-success')
}

watch(
    searchParam,
    () => {
      emit('update:searchParam', {...searchParam})
    },
    {deep: true},
)

onMounted(() => {
  initSearchParam()
  syncColumnCheckFromStorage()
  emit('update:searchParam', {...searchParam})
  fetchList()
})

defineExpose({
  refreshData,
  getDataList,
  selectedRows,
  searchParam,
  currentPage,
  currentPageSize,
  total,
  tableRef,
})
</script>

<style scoped>
.c7-json-table__search {
  margin-bottom: 12px;
}

.c7-json-table__search :deep(.el-select),
.c7-json-table__search :deep(.el-date-editor) {
  min-width: 180px;
}

.c7-json-table__toolbar {
  margin-bottom: 12px;
}

.c7-json-table__toolbar .el-button + .el-button {
  margin-left: 8px;
}

.c7-json-table__pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.c7-json-table__col-setting-row {
  padding: 6px 0;
}
</style>
