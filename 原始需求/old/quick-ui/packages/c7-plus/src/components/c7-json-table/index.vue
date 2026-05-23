<template>
  <!-- C7JsonTable 表格组件容器 -->
  <div class="json-table">
    <!-- 搜索表单区域 -->
    <el-form v-if="showSearch" ref="searchFormRef" :model="searchParam">
      <el-row :gutter="20">
        <!-- 动态搜索表单组件 -->
        <c7-json-form
            :columns="searchColumns"
            v-model="searchParam"
        />
        <!-- 搜索操作按钮区域 -->
        <el-col :span="8">
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 操作按钮工具栏 -->
    <div class="operate-bar"
         v-if="$slots.operate || showAdd || showEdit || showDelete || showRefresh || showExport || showImport || showColumnSetting">
      <!-- 自定义操作按钮插槽 -->
      <slot name="operate">
        <!-- 新增按钮 -->
        <el-button v-if="showAdd" type="primary" icon="Plus" @click="handleAdd">新增</el-button>
        <!-- 修改按钮 - 需要选中数据才能操作 -->
        <el-button v-if="showEdit" type="success" icon="Edit" @click="handleEdit" :disabled="!hasSelection">修改
        </el-button>
        <!-- 删除按钮 - 需要选中数据才能操作 -->
        <el-button v-if="showDelete" type="danger" icon="Delete" @click="handleDelete" :disabled="!hasSelection">删除
        </el-button>
        <!-- 刷新按钮 -->
        <el-button v-if="showRefresh" type="primary" icon="Refresh" @click="handleRefresh">刷新</el-button>
        <!-- 导出按钮 -->
        <el-button v-if="showExport" type="primary" icon="Download" @click="handleExport">导出</el-button>
        <!-- 导入按钮 -->
        <el-button v-if="showImport" type="primary" icon="Upload" @click="handleImport">导入</el-button>
        <!-- 列设置按钮 -->
        <el-button v-if="showColumnSetting" type="default" icon="Setting" @click="showColumnSettingDialog = true">列设置</el-button>
      </slot>
    </div>

    <!-- 列设置对话框 -->
    <el-dialog
      v-model="showColumnSettingDialog"
      title="列设置"
      width="500px"
    >
      <div class="column-setting-content">
        <el-checkbox-group v-model="columnSettingChecked">
          <div
            v-for="column in props.tableColumns.filter(col => col.prop && col.prop !== 'table-operate')"
            :key="column.prop"
            class="column-setting-item"
          >
            <el-checkbox :label="column.prop">{{ column.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="showColumnSettingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleColumnSettingConfirm">确定</el-button>
        <el-button type="default" @click="handleColumnSettingReset">重置</el-button>
      </template>
    </el-dialog>

    <!-- 数据表格区域 -->
    <el-table
        :key="tableKey"
        :data="tableData"
        :row-key="rowKey"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        align="center"
        :tree-props="tableProps.treeProps"
        :default-expand-all="tableProps.defaultExpandAll"
        :lazy="tableProps.lazy"
    >
      <!-- 多选列 - 用于批量操作 -->
      <el-table-column
          v-if="showSelection"
          type="selection"
          width="55"
      />

      <!-- 序号列 - 显示行号 -->
      <el-table-column
          v-if="showIndex"
          type="index"
          label="序号"
          width="60"
      />

      <!-- 动态数据列 - 根据配置渲染列 -->
      <el-table-column
          v-for="column in visibleTableColumns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'left'"
          :header-align="column.headerAlign || column.align || 'left'"
          :show-overflow-tooltip="column.showOverflowTooltip !== false"
          :sortable="column.sortable || false"
          :sort-method="column.sortMethod"
          :sort-by="column.sortBy"
          :sort-orders="column.sortOrders"
          :filters="column.filters"
          :filter-method="column.filterMethod"
          :filter-multiple="column.filterMultiple"
          :filtered-value="getFilteredValue(column.prop)"
          :fixed="column.fixed"
          :resizable="column.resizable !== false"
      >
        <template #default="{ row, $index }">
          <!-- 操作列插槽 - 用于自定义操作按钮 -->
          <slot
              v-if="column.prop === 'table-operate'"
              name="table-operate"
              :row="row"
              :index="$index"
          />
          <!-- 自定义列插槽 - 用于自定义列内容 -->
          <slot
              v-else-if="column.slotName"
              :name="column.slotName"
              :row="row"
              :index="$index"
              :value="row[column.prop]"
          />

          <!-- 字典标签显示 - 将值转换为对应的标签 -->
          <el-tag
              v-else-if="column.columnType === 'tag' && column.dictList && Array.isArray(column.dictList) && column.dictList.length > 0"
              :type="getTagType(row[column.prop], column.dictList)"
              size="small"
          >
            {{ getDictLabel(row[column.prop], column.dictList) }}
          </el-tag>
          <!-- 默认文本显示 -->
          <span v-else>{{ row[column.prop] }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        v-if="!pageHidden"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="computedTotal"
        :layout="pageLayout"
        :background="pageBackground"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * C7JsonTable 表格组件
 *
 * 功能特性：
 * - 支持搜索表单
 * - 支持操作按钮（新增、编辑、删除、导出、导入等）
 * - 支持多选和序号列
 * - 支持字典标签显示
 * - 支持自定义列插槽
 * - 支持分页
 * - 支持树形表格
 * - 支持懒加载
 *
 * @author C7 Team
 * @version 2.0.0
 */

import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElLoading, ElMessage, ElMessageBox } from 'element-plus'
import C7JsonForm from '../c7-json-form/index.vue'
import type { TableColumnProps } from '../../types/table'
import type { FormColumn } from '../../types/form'
import { handleError } from '../../utils/errorHandler'
import { logger } from '../../utils/logger'

defineOptions({ name: 'C7JsonTable' })

/**
 * 表格属性配置接口
 */
interface TablePropsConfig {
  showAdd?: boolean
  showEdit?: boolean
  showDelete?: boolean
  showRefresh?: boolean
  showExport?: boolean
  showImport?: boolean
  showColumnSetting?: boolean
  selection?: boolean
  border?: boolean
  stripe?: boolean
  height?: string | number
  treeProps?: any
  defaultExpandAll?: boolean
  lazy?: boolean
}

/**
 * 组件属性接口
 */
interface Props {
  listFunction?: Function
  tableData?: any[]
  rowKey?: string
  rowsKey?: string
  totalKey?: string
  showSearch?: boolean
  searchColumns?: FormColumn[]
  searchParam?: Record<string, any>
  deleteFunction?: Function
  exportFunction?: Function
  tableColumns?: TableColumnProps[]
  tableProps?: TablePropsConfig
  showIndex?: boolean
  total?: number
  pageSizes?: number[]
  pageLayout?: string
  pageBackground?: boolean
  pageHidden?: boolean
  init?: boolean
  initParam?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  listFunction: undefined,
  tableData: () => [],
  rowKey: 'id',
  rowsKey: 'data.records',
  totalKey: 'data.total',
  showSearch: true,
  searchColumns: () => [],
  searchParam: () => ({}),
  deleteFunction: undefined,
  exportFunction: undefined,
  tableColumns: () => [],
  tableProps: () => ({
    showAdd: false,
    showEdit: false,
    showDelete: false,
    showRefresh: false,
    showExport: false,
    showImport: false,
    selection: false,
    border: false,
    stripe: false,
    height: 'auto',
    treeProps: undefined,
    defaultExpandAll: false,
    lazy: false
  }),
  showIndex: true,
  total: 0,
  pageSizes: () => [10, 20, 30, 50],
  pageLayout: 'total, sizes, prev, pager, next, jumper',
  pageBackground: true,
  pageHidden: false,
  init: true,
  initParam: () => ({})
})

const emit = defineEmits<{
  'update:searchParam': [params: Record<string, any>]
  'selection-change': [selection: any[]]
  'addBtnHandle': []
  'editBtnHandle': [row: any]
  'deleteBtnHandle': []
  'refreshDataList': []
  'exportBtnHandle': []
  'importBtnHandle': []
}>()

// ==================== 响应式数据 ====================
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const searchFormRef = ref() // 在模板中使用：ref="searchFormRef"
const loading = ref(false)
// 表格数据：使用 ref 而不是 shallowRef，因为 el-table 需要深度响应式来正确更新行数据
const internalTableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const searchParam = ref<Record<string, any>>({ ...props.searchParam })
const total = ref(0)
const selectedRows = ref<any[]>([])
// 用于强制 el-table 重新渲染的 key
const tableKey = ref(0)
// 组件是否已卸载的标志
const isUnmounted = ref(false)

// ==================== 计算属性 ====================
const tableData = computed(() => {
  if (props.tableData && props.tableData.length > 0) {
    return props.tableData
  }
  return internalTableData.value
})

const computedTotal = computed(() => {
  if (props.total && props.total > 0) {
    return props.total
  }
  return total.value
})

const showAdd = computed(() => props.tableProps?.showAdd || false)
const showEdit = computed(() => props.tableProps?.showEdit || false)
const showDelete = computed(() => props.tableProps?.showDelete || false)
const showRefresh = computed(() => props.tableProps?.showRefresh || false)
const showExport = computed(() => props.tableProps?.showExport || false)
const showImport = computed(() => props.tableProps?.showImport || false)
const showSelection = computed(() => props.tableProps?.selection || false)
const showColumnSetting = computed(() => props.tableProps?.showColumnSetting !== false)
const hasSelection = computed(() => selectedRows.value.length > 0)

// 列设置对话框
const showColumnSettingDialog = ref(false)
const columnSettingChecked = ref<string[]>([])

// ==================== 列控制相关 ====================
// 列显示/隐藏状态
const columnVisibility = ref<Record<string, boolean>>({})
// 列顺序
const columnOrder = ref<Record<string, number>>({})
// 筛选值
const filterValues = ref<Record<string, any>>({})
// 排序状态
const sortState = ref<{ prop: string; order: 'ascending' | 'descending' | null } | null>(null)

/**
 * 计算属性：可见的表格列（根据 visible 和 hidden 属性过滤）
 */
const visibleTableColumns = computed(() => {
  return props.tableColumns
    .filter(column => {
      // 如果设置了 visible: false，则隐藏
      if (column.visible === false) {
        return false
      }
      // 如果设置了 hidden: true，则隐藏
      if (column.hidden === true) {
        return false
      }
      // 如果列控制中有设置，使用列控制的值
      if (column.prop && columnVisibility.value.hasOwnProperty(column.prop)) {
        return columnVisibility.value[column.prop]
      }
      return true
    })
    .sort((a, b) => {
      // 根据 order 排序
      const orderA = columnOrder.value[a.prop || ''] ?? a.order ?? 0
      const orderB = columnOrder.value[b.prop || ''] ?? b.order ?? 0
      return orderA - orderB
    })
})

/**
 * 获取列的筛选值
 */
const getFilteredValue = (prop?: string): any[] | undefined => {
  if (!prop) return undefined
  return filterValues.value[prop]
}

// ==================== 监听器 ====================
const stopWatchSearchParam = watch(() => props.searchParam, (newVal) => {
  searchParam.value = { ...newVal }
}, { deep: true })

const stopWatchInitParam = watch(() => props.initParam, (newVal, oldVal) => {
  // 如果组件已卸载，不执行
  if (isUnmounted.value) {
    return
  }
  
  if (newVal && Object.keys(newVal).length > 0) {
    const newValStr = JSON.stringify(newVal)
    const oldValStr = JSON.stringify(oldVal || {})
    if (newValStr !== oldValStr) {
      fetchData()
    }
  }
}, { deep: true, immediate: false })

/**
 * 组件卸载时清理所有资源
 */
onUnmounted(() => {
  try {
    // 标记组件已卸载
    isUnmounted.value = true
    
    // 停止所有 watch
    try {
      stopWatchSearchParam()
    } catch (e) {
      // 忽略 watch 停止错误
    }
    try {
      stopWatchInitParam()
    } catch (e) {
      // 忽略 watch 停止错误
    }
    
    // 停止 loading
    loading.value = false
    
    // 清理数据
    internalTableData.value = []
    selectedRows.value = []
    searchParam.value = {}
    total.value = 0
  } catch (error) {
    // 捕获所有错误，防止阻塞路由切换
    logger.warn('C7JsonTable onUnmounted error:', error)
  }
})

// ==================== 核心方法 ====================
/**
 * 获取表格数据
 */
const fetchData = async () => {
  if (props.tableData && props.tableData.length > 0) {
    return
  }

  if (!props.listFunction) {
    logger.warn('listFunction is required when tableData is not provided')
    return
  }

  // 如果组件已卸载，不执行请求
  if (isUnmounted.value) {
    return
  }

  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchParam.value,
      ...props.initParam
    }

    const response = await props.listFunction(params)

    if (response) {
      // 解析数据列表
      const rowsPath = props.rowsKey.split('.')
      let rowsData: any = response
      for (const key of rowsPath) {
        rowsData = rowsData?.[key]
      }
      const newData = Array.isArray(rowsData) ? rowsData : []
      
      // 优化：使用浅拷贝创建新对象引用，避免深拷贝性能问题
      // 对于表格数据，浅拷贝已足够，因为 el-table 主要关注对象引用变化
      internalTableData.value = newData.map(item => ({ ...item }))
      
      // 更新 tableKey 以强制 el-table 重新渲染
      // 这样可以确保 slot 中的 row 对象引用被更新
      tableKey.value++

      // 解析总数
      const totalPath = props.totalKey.split('.')
      let totalData: any = response
      for (const key of totalPath) {
        if (totalData && typeof totalData === 'object' && key in totalData) {
          totalData = totalData[key]
        } else {
          totalData = undefined
          break
        }
      }

      if (totalData !== undefined && totalData !== null) {
        const numValue = Number(totalData)
        total.value = !isNaN(numValue) ? numValue : 0
      } else {
        total.value = 0
      }
    } else {
      // 如果组件已卸载，不更新数据
      if (!isUnmounted.value) {
        internalTableData.value = []
        total.value = 0
      }
    }
  } catch (error) {
    // 如果组件已卸载，不处理错误
    if (isUnmounted.value) {
      return
    }
    
    // 使用统一的错误处理
    handleError(error, {
      showToast: false, // 表格数据获取失败不显示 toast，避免干扰
      defaultMessage: '获取数据失败',
      logError: true
    })
    logger.error('获取表格数据失败:', error)
    internalTableData.value = []
    total.value = 0
  } finally {
    // 如果组件已卸载，不更新 loading 状态
    if (!isUnmounted.value) {
      loading.value = false
    }
  }
}

// ==================== 事件处理方法 ====================
const handleSearch = () => {
  currentPage.value = 1
  emit('update:searchParam', { ...searchParam.value })
  fetchData()
}

const handleReset = () => {
  searchParam.value = {}
  currentPage.value = 1
  emit('update:searchParam', {})
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handleAdd = () => {
  emit('addBtnHandle')
}

const handleEdit = () => {
  if (selectedRows.value.length === 1) {
    emit('editBtnHandle', selectedRows.value[0])
  }
}

const handleDelete = async (id?: any) => {
  try {
    let ids: any[] = []
    let message = ''

    if (Array.isArray(selectedRows.value) && selectedRows.value.length > 0) {
      ids = selectedRows.value.map(row => row[props.rowKey])
      message = `是否确认删除选中的${ids.length}个数据？`
    } else {
      ids = [id]
      message = `是否确认删除编号为"${id}"的数据项？`
    }

    await ElMessageBox.confirm(message, '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (props.deleteFunction) {
      const response = await props.deleteFunction(ids)
      if (response) {
        ElMessage.success('删除成功')
        handleRefresh()
      } else {
        ElMessage.error('删除失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleRefresh = () => {
  emit('refreshDataList')
  fetchData()
}

const handleExport = async () => {
  if (typeof props.exportFunction !== 'function') {
    ElMessage.warning('未配置导出方法')
    return
  }

  const downloadLoadingInstance = ElLoading.service({
    text: "正在下载数据，请稍候",
    background: "rgba(0, 0, 0, 0.7)"
  })

  try {
    const payload = {
      ...searchParam.value,
      ...props.initParam
    }
    await props.exportFunction(payload)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error?.message || '下载文件出现错误，请联系管理员！')
  } finally {
    downloadLoadingInstance?.close()
  }
}

const handleImport = () => {
  emit('importBtnHandle')
}

// ==================== 辅助函数 ====================
/**
 * 根据字典列表获取标签文本（支持逗号分隔的多值）
 */
const getDictLabel = (value: any, dictList: any[]): string => {
  if (!dictList || !Array.isArray(dictList) || dictList.length === 0) {
    return String(value)
  }

  const shouldSplit = typeof value === 'string' && value.includes(',')
  const values = shouldSplit
      ? value.split(',').map((v: string) => v.trim())
      : [value]

  const result = values.map((val: any) => {
    if (val === '') return ''
    const item = dictList.find(
        i => i.value === val || i.value === String(val)
    )
    return item ? item.label : String(val)
  })

  return result.join(',')
}

/**
 * 根据字典列表获取标签类型
 */
const getTagType = (value: any, dictList: any[]): string => {
  if (!dictList || !Array.isArray(dictList) || dictList.length === 0) return ''
  const dictItem = dictList.find(item => item.value === value || item.value === String(value))
  if (dictItem && dictItem.elTagType) {
    return dictItem.elTagType
  }
  if (value === '0' || value === 0) return 'success'
  if (value === '1' || value === 1) return 'danger'
  return 'info'
}

// ==================== 生命周期钩子 ====================
onMounted(() => {
  if (props.init) {
    fetchData()
  }
})

// ==================== 列控制方法 ====================
/**
 * 切换列显示/隐藏
 */
const toggleColumnVisibility = (prop: string, visible?: boolean) => {
  if (visible !== undefined) {
    columnVisibility.value[prop] = visible
  } else {
    columnVisibility.value[prop] = !columnVisibility.value[prop]
  }
  tableKey.value++ // 强制重新渲染
}

/**
 * 设置列顺序
 */
const setColumnOrder = (prop: string, order: number) => {
  columnOrder.value[prop] = order
  tableKey.value++ // 强制重新渲染
}

/**
 * 重置列配置
 */
const resetColumnConfig = () => {
  columnVisibility.value = {}
  columnOrder.value = {}
  filterValues.value = {}
  sortState.value = null
  tableKey.value++ // 强制重新渲染
}

/**
 * 获取列配置（用于保存）
 */
const getColumnConfig = () => {
  return {
    visibility: { ...columnVisibility.value },
    order: { ...columnOrder.value },
    filters: { ...filterValues.value },
    sort: sortState.value ? { ...sortState.value } : null
  }
}

/**
 * 恢复列配置（用于加载）
 */
const restoreColumnConfig = (config: {
  visibility?: Record<string, boolean>
  order?: Record<string, number>
  filters?: Record<string, any>
  sort?: { prop: string; order: 'ascending' | 'descending' | null } | null
}) => {
  if (config.visibility) {
    columnVisibility.value = { ...config.visibility }
  }
  if (config.order) {
    columnOrder.value = { ...config.order }
  }
  if (config.filters) {
    filterValues.value = { ...config.filters }
  }
  if (config.sort) {
    sortState.value = config.sort
  }
  tableKey.value++ // 强制重新渲染
}

/**
 * 处理列设置确认
 */
const handleColumnSettingConfirm = () => {
  // 更新列显示状态
  props.tableColumns.forEach(column => {
    if (column.prop) {
      columnVisibility.value[column.prop] = columnSettingChecked.value.includes(column.prop)
    }
  })
  showColumnSettingDialog.value = false
  tableKey.value++ // 强制重新渲染
}

/**
 * 处理列设置重置
 */
const handleColumnSettingReset = () => {
  // 重置为默认状态（所有列都显示）
  columnSettingChecked.value = props.tableColumns
    .filter(col => col.prop && col.prop !== 'table-operate')
    .map(col => col.prop!)
  columnVisibility.value = {}
  tableKey.value++ // 强制重新渲染
}

/**
 * 初始化列设置对话框
 */
watch(showColumnSettingDialog, (visible) => {
  if (visible) {
    // 初始化选中状态
    columnSettingChecked.value = props.tableColumns
      .filter(col => {
        if (!col.prop || col.prop === 'table-operate') return false
        // 如果列控制中有设置，使用列控制的值，否则使用 visible 属性
        if (columnVisibility.value.hasOwnProperty(col.prop)) {
          return columnVisibility.value[col.prop]
        }
        return col.visible !== false && col.hidden !== true
      })
      .map(col => col.prop!)
  }
})

// ==================== 暴露给父组件的方法 ====================
defineExpose({
  getDataList: fetchData,
  refreshData: fetchData,
  handleDelete: handleDelete,
  toggleColumnVisibility,
  setColumnOrder,
  resetColumnConfig,
  getColumnConfig,
  restoreColumnConfig
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.operate-bar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
</style>

