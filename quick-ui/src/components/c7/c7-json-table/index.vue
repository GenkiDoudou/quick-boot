<template>
  <!-- C7JsonTable 表格组件容器 -->
  <div class="json-table">
    <!-- 搜索表单区域 -->
    <el-form v-if="showSearch" ref="searchForm" :model="searchParam">
      <el-row gutter="20">
        <!-- 动态搜索表单组件 -->
        <JsonForm
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
         v-if="$slots.operate || showAdd || showEdit || showDelete || showRefresh || showExport || showImport">
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
        <!-- 导出按钮 -->
        <el-button v-if="showExport" type="primary" icon="Download" @click="handleExport">导出</el-button>
        <!-- 导入按钮 -->
        <el-button v-if="showImport" type="primary" icon="Upload" @click="handleImport">导入</el-button>
      </slot>
    </div>

    <!-- 数据表格区域 -->
    <el-table
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
          v-for="column in tableColumns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'left'"
          :show-overflow-tooltip="column.showOverflowTooltip !== false"
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

<script setup>
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
 * @version 1.0.0
 */

import {ref, computed, onMounted, watch, defineOptions, defineProps, getCurrentInstance} from 'vue'
import {ElLoading, ElMessage, ElMessageBox} from 'element-plus'
import JsonForm from '../c7-json-form/index.vue'
import {blobValidate, tansParams} from "@/utils/ruoyi.js";
import errorCode from "@/utils/errorCode.js";
import service from "@/utils/request.js";
import {saveAs} from 'file-saver';
import {delUser} from "@/api/system/user.js";

// 定义组件名称
defineOptions({name: 'JsonTable'})

/**
 * C7JsonTable 组件 Props 定义
 * @typedef {Object} TableProps
 * @property {boolean} [showAdd=false] - 是否显示新增按钮
 * @property {boolean} [showEdit=false] - 是否显示编辑按钮
 * @property {boolean} [showDelete=false] - 是否显示删除按钮
 * @property {boolean} [showRefresh=false] - 是否显示刷新按钮
 * @property {boolean} [showExport=false] - 是否显示导出按钮
 * @property {boolean} [showImport=false] - 是否显示导入按钮
 * @property {boolean} [selection=false] - 是否显示多选框
 * @property {boolean} [border=false] - 是否显示表格边框
 * @property {boolean} [stripe=false] - 是否显示斑马纹
 * @property {string|number} [height='auto'] - 表格高度
 * @property {any} [treeProps] - 树形表格配置
 * @property {boolean} [defaultExpandAll=false] - 是否默认展开所有行
 * @property {boolean} [lazy=false] - 是否懒加载
 */

// 定义组件属性
const props = defineProps({
  // ==================== 数据相关 ====================
  /** 数据获取函数，用于从后端API获取数据 */
  listFunction: {type: Function, required: false},
  /** 静态表格数据，如果提供则不会调用listFunction */
  tableData: {type: Array, default: () => []},
  /** 行数据的唯一标识字段名 */
  rowKey: {type: String, default: 'id'},
  /** 响应数据中列表数据的路径，支持嵌套如 'data.records' */
  rowsKey: {type: String, default: 'data.records'},
  /** 响应数据中总数的路径，支持嵌套如 'data.total' */
  totalKey: {type: String, default: 'data.total'},

  // ==================== 搜索相关 ====================
  /** 是否显示搜索表单 */
  showSearch: {type: Boolean, default: true},
  /** 搜索表单列配置 */
  searchColumns: {type: Array, default: () => []},
  /** 搜索参数对象 */
  searchParam: {type: Object, default: () => ({})},


  // ==================== 删除 ====================
  deleteFunction: {type: Function, required: false},
  // ==================== 导入导出相关 ====================
  exportFunction: {type: Function, required: false},
  // ==================== 表格相关 ====================
  /** 表格列配置 */
  tableColumns: {type: Array, default: () => []},
  /** 表格属性配置对象 */
  tableProps: {
    type: Object,
    default: () => ({
      // 按钮显示控制
      showAdd: false,        // 是否显示新增按钮
      showEdit: false,       // 是否显示编辑按钮
      showDelete: false,     // 是否显示删除按钮
      showRefresh: false,    // 是否显示刷新按钮
      showExport: false,     // 是否显示导出按钮
      showImport: false,     // 是否显示导入按钮
      selection: false,      // 是否显示多选框
      // 表格样式
      border: false,         // 是否显示表格边框
      stripe: false,         // 是否显示斑马纹
      height: 'auto',        // 表格高度
      // 树形表格相关
      treeProps: undefined,  // 树形表格配置
      defaultExpandAll: false, // 是否默认展开所有行
      lazy: false            // 是否懒加载
    }),
    // 添加验证函数来确保传入的对象包含正确的属性
    validator: (value) => {
      if (!value || typeof value !== 'object') return true

      const validKeys = [
        'showAdd', 'showEdit', 'showDelete', 'showRefresh', 'showExport', 'showImport', 'selection',
        'border', 'stripe', 'height', 'treeProps', 'defaultExpandAll', 'lazy'
      ]

      return Object.keys(value).every(key => validKeys.includes(key))
    }
  },
  /** 是否显示序号列 */
  showIndex: {type: Boolean, default: true},

  // ==================== 分页相关 ====================
  /** 数据总数 */
  total: {type: Number, default: 0},
  /** 每页显示条数选项 */
  pageSizes: {type: Array, default: () => [10, 20, 30, 50]},
  /** 分页组件布局 */
  pageLayout: {type: String, default: 'total, sizes, prev, pager, next, jumper'},
  /** 分页组件背景色 */
  pageBackground: {type: Boolean, default: true},
  /** 是否隐藏分页组件 */
  pageHidden: {type: Boolean, default: false},

  // ==================== 初始化相关 ====================
  /** 组件挂载时是否自动获取数据 */
  init: {type: Boolean, default: true},
  /** 初始化参数，用于传递给数据获取函数 */
  initParam: {type: Object, default: () => ({})}
})

// 定义组件事件
const emit = defineEmits([
  'update:searchParam',    // 搜索参数更新事件
  'selection-change',      // 选择变化事件
  'addBtnHandle',          // 新增按钮点击事件
  'editBtnHandle',         // 编辑按钮点击事件
  'deleteBtnHandle',       // 删除按钮点击事件
  'refreshDataList',       // 刷新数据事件
  'exportBtnHandle',       // 导出按钮点击事件
  'importBtnHandle'        // 导入按钮点击事件
])

// ==================== 响应式数据 ====================
/** 表格加载状态 */
const loading = ref(false)
/** 内部表格数据（当使用listFunction时） */
const internalTableData = ref([])
/** 当前页码 */
const currentPage = ref(1)
/** 每页显示条数 */
const pageSize = ref(10)
/** 搜索参数 */
const searchParam = ref({...props.searchParam})
/** 数据总数 */
const total = ref(0)
/** 选中的行数据 */
const selectedRows = ref([])

// ==================== 计算属性 ====================
/** 表格数据源 - 优先使用传入的tableData，否则使用内部数据 */
const tableData = computed(() => {
  // 如果传入了 tableData，直接使用
  if (props.tableData && props.tableData.length > 0) {
    return props.tableData
  }
  // 否则使用内部数据
  return internalTableData.value
})

/** 计算后的总数 - 优先使用传入的total，否则使用内部解析的total */
const computedTotal = computed(() => {
  // 如果传入了 total 且大于0，优先使用传入的值
  if (props.total && props.total > 0) {
    return props.total
  }
  // 否则使用内部解析的 total
  return total.value
})

// ==================== 按钮显示控制 ====================
/** 是否显示新增按钮 */
const showAdd = computed(() => props.tableProps?.showAdd || false)
/** 是否显示编辑按钮 */
const showEdit = computed(() => props.tableProps?.showEdit || false)
/** 是否显示删除按钮 */
const showDelete = computed(() => props.tableProps?.showDelete || false)
/** 是否显示刷新按钮 */
const showRefresh = computed(() => props.tableProps?.showRefresh || false)
/** 是否显示导出按钮 */
const showExport = computed(() => props.tableProps?.showExport || false)
/** 是否显示导入按钮 */
const showImport = computed(() => props.tableProps?.showImport || false)
/** 是否显示多选框 */
const showSelection = computed(() => props.tableProps?.selection || false)
/** 是否有选中的数据 */
const hasSelection = computed(() => selectedRows.value.length > 0)

// ==================== 监听器 ====================
/** 监听搜索参数变化 */
watch(() => props.searchParam, (newVal) => {
  searchParam.value = {...newVal}
}, {deep: true})

/** 监听初始化参数变化 */
watch(() => props.initParam, (newVal, oldVal) => {
  // 只有当initParam真正发生变化时才重新获取数据
  if (newVal && Object.keys(newVal).length > 0) {
    const newValStr = JSON.stringify(newVal)
    const oldValStr = JSON.stringify(oldVal || {})

    if (newValStr !== oldValStr) {
      console.log('initParam changed, refetching data:', newVal)
      fetchData()
    }
  }
}, {deep: true, immediate: false})

// ==================== 核心方法 ====================
/**
 * 获取表格数据
 * 支持两种模式：
 * 1. 静态数据模式：直接使用传入的tableData
 * 2. 动态数据模式：调用listFunction获取数据
 */
const fetchData = async () => {
  // 如果传入了 tableData，不需要调用 API
  if (props.tableData && props.tableData.length > 0) {
    return
  }

  if (!props.listFunction) {
    console.warn('listFunction is required when tableData is not provided')
    return
  }

  loading.value = true
  try {
    // 构建请求参数
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchParam.value,
      ...props.initParam
    }

    // 调用数据获取函数
    const response = await props.listFunction(params)

    // 根据配置的键路径解析数据
    if (response) {
      // 解析数据列表
      const rowsPath = props.rowsKey.split('.')
      let rowsData = response
      for (const key of rowsPath) {
        rowsData = rowsData?.[key]
      }
      internalTableData.value = Array.isArray(rowsData) ? rowsData : []

      // 解析总数
      const totalPath = props.totalKey.split('.')
      let totalData = response
      for (const key of totalPath) {
        if (totalData && typeof totalData === 'object' && key in totalData) {
          totalData = totalData[key]
        } else {
          console.warn(`C7JsonTable: totalKey path "${props.totalKey}" not found in response, key "${key}" is missing`)
          totalData = undefined
          break
        }
      }

      // 尝试转换为数字
      if (totalData !== undefined && totalData !== null) {
        const numValue = Number(totalData)
        total.value = !isNaN(numValue) ? numValue : 0
      } else {
        total.value = 0
      }

    } else {
      internalTableData.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    internalTableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const buildExportPayload = () => ({
  ...searchParam.value,
  ...props.initParam
})

// ==================== 事件处理方法 ====================
/** 搜索事件处理 */
const handleSearch = () => {
  currentPage.value = 1
  emit('update:searchParam', {...searchParam.value})
  fetchData()
}

/** 重置搜索条件 */
const handleReset = () => {
  searchParam.value = {}
  currentPage.value = 1
  emit('update:searchParam', {})
  fetchData()
}

/** 分页大小变化处理 */
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

/** 当前页变化处理 */
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchData()
}

/** 表格选择变化处理 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

// ==================== 按钮事件处理 ====================
/** 新增按钮点击事件 */
const handleAdd = () => {
  emit('addBtnHandle')
}

/** 编辑按钮点击事件 - 需要选中一行数据 */
const handleEdit = () => {
  if (selectedRows.value.length === 1) {
    emit('editBtnHandle', selectedRows.value[0])
  }
}

/** 删除按钮点击事件 - 需要选中数据 */
const handleDelete = async (id) => {
  // console.log("handleDelete")
  // if (selectedRows.value.length > 0) {
  //   emit('deleteBtnHandle', selectedRows.value)
  // }

  try {
    let ids = [];
    let message = '';

    // 判断参数类型
    if (Array.isArray(selectedRows.value) && selectedRows.value.length > 0) {
      // 来自表格组件的批量删除
      ids = selectedRows.value.map(row => row[props.rowKey]);
      message = `是否确认删除选中的${ids.length}个数据？`;
    } else {
      // 来自操作列的单个删除
      ids = [id];
      message = `是否确认删除编号为"${id}"的数据项？`;
    }

    await ElMessageBox.confirm(message, '系统提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const response = await props.deleteFunction(ids)
    if (response){
      ElMessage.success('删除成功');
      handleRefresh();
    }else {
      ElMessage.error('删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
}

/** 刷新按钮点击事件 */
const handleRefresh = () => {
  emit('refreshDataList')
  fetchData()
}

let downloadLoadingInstance = null
const {proxy} = getCurrentInstance();
/** 导出按钮点击事件 */
const handleExport = async () => {
  if (typeof props.exportFunction !== 'function') {
    ElMessage.warning('未配置导出方法')
    return
  }

  downloadLoadingInstance = ElLoading.service({text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)",})
  const payload = buildExportPayload()

  try {
    const data = await props.exportFunction(payload)
    console.log('Export response:', data)

    let fname = data?.filename || `export_${new Date().getTime()}.xlsx`
    const isBlob = blobValidate(data)

    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, fname)
      ElMessage.success('导出成功')
    } else {
      const resText = await data.text()
      const rspObj = JSON.parse(resText)
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      ElMessage.error(errMsg)
    }
  } catch (error) {
    console.error('Export error:', error)
    ElMessage.error(error?.message || '下载文件出现错误，请联系管理员！')
  } finally {
    downloadLoadingInstance?.close()
    downloadLoadingInstance = null
  }

}

/** 导入按钮点击事件 */
const handleImport = () => {
  emit('importBtnHandle')
}

// ==================== 辅助函数 ====================
/**
 * 根据字典列表获取标签文本
 * @param {any} value - 要查找的值
 * @param {Array} dictList - 字典列表
 * @returns {string} 对应的标签文本
 */
const getDictLabel = (value, dictList) => {
  if (!dictList || !Array.isArray(dictList) || dictList.length === 0) return value
  const dictItem = dictList.find(item => item.value === value || item.value === String(value))
  return dictItem ? dictItem.label : value
}

/**
 * 根据字典列表获取标签类型
 * @param {any} value - 要查找的值
 * @param {Array} dictList - 字典列表
 * @returns {string} 标签类型（success、danger、info等）
 */
const getTagType = (value, dictList) => {
  if (!dictList || !Array.isArray(dictList) || dictList.length === 0) return ''
  const dictItem = dictList.find(item => item.value === value || item.value === String(value))
  if (dictItem && dictItem.elTagType) {
    return dictItem.elTagType
  }
  // 默认根据值判断类型
  if (value === '0' || value === 0) return 'success'
  if (value === '1' || value === 1) return 'danger'
  return 'info'
}

// ==================== 生命周期钩子 ====================
/** 组件挂载时获取数据 */
onMounted(() => {
  if (props.init) {
    fetchData()
  }
})

// ==================== 暴露给父组件的方法 ====================
/** 暴露方法给父组件调用 */
defineExpose({
  getDataList: fetchData,    // 获取数据列表
  refreshData: fetchData,
  handleDelete: handleDelete,
  // 刷新数据（别名）
})
</script>

<style scoped>
/* ==================== 组件样式 ==================== */


/* 搜索表单样式 */
.search-form {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

/* 操作按钮工具栏样式 */
.operate-bar {
  margin-bottom: 16px;
}

/* 分页组件样式 */
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

</style>
