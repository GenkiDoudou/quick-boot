<template>
  <div class="app-container">
    <c7-json-table
        ref="tableRef"
        :listFunction="listSysConfig"
        :tableColumns="tableColumns"
        :delete-function="delSysConfig"
        :searchColumns="searchColumns"
        :tableProps="tableProps"
        rowsKey="data.records"
        totalKey="data.total"
        @addBtnHandle="handleAdd"
        @editBtnHandle="handleEdit"
    >
      <!-- 操作列 -->
      <template #table-operate="{ row }">
        <C7ButtonGroup>
          <C7Button
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
              v-hasPermi="['system:sysconfig:edit']"
          >
            编辑
          </C7Button>

          <C7Button
              type="danger"
              link
              icon="Delete"
              @click="tableRef.handleDelete(row.configId)"
              v-hasPermi="['system:sysconfig:remove']"
          >
            删除
          </C7Button>
        </C7ButtonGroup>
      </template>
    </c7-json-table>

    <!-- 新增 / 修改 -->
    <add-or-update
        :key="addKey"
        ref="addOrUpdateRef"
        @refreshDataList="tableRef.refreshData()"
    />
  </div>
</template>

<script setup name="sysconfig">
import {ref, getCurrentInstance, nextTick} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'

import AddOrUpdate from './add-or-update.vue'
import {
  listSysConfig,
  delSysConfig
} from '@/api/system/sysconfig'

import {C7JsonTable, C7Button, C7ButtonGroup} from '@/components/c7'

const {proxy} = getCurrentInstance()

/* =====================================================
 *  dictType 自动收集（listFields + searchFields）
 * ===================================================== */

const {
  YES_NO
} = proxy.useDict(
    "YES_NO"
)

/* =====================================================
 * refs
 * ===================================================== */
const tableRef = ref()
const addOrUpdateRef = ref()
const addKey = ref(0)

/* =====================================================
 * searchColumns
 * ===================================================== */
const searchColumns = ref([
  {
    prop: "configName",
    label: "参数名称",
    type: "input"
  },
  {
    prop: "configKey",
    label: "参数键名",
    type: "input"
  },
  {
    prop: "configValue",
    label: "参数键值",
    type: "input"
  },
  {
    prop: "configType",
    label: "系统内置",
    type: "select",
    dataList: YES_NO
  }
])

/* =====================================================
 * tableColumns
 * ===================================================== */
const tableColumns = ref([
  {
    label: "参数名称",
    prop: "configName",
    showOverflowTooltip: true
  },
  {
    label: "参数键名",
    prop: "configKey",
    showOverflowTooltip: true
  },
  {
    label: "参数键值",
    prop: "configValue",
    showOverflowTooltip: true
  },
  {
    label: "系统内置",
    prop: "configType",
    showOverflowTooltip: true
    ,
    columnType: 'tag',
    dictList: YES_NO
  },
  {
    label: "创建时间",
    prop: "createTime",
    showOverflowTooltip: true
  },
  {
    label: "操作",
    prop: "table-operate",
    width: 160,
    fixed: "right"
  }
])

/* =====================================================
 * tableProps
 * ===================================================== */
const tableProps = ref({
  selection: true,
  border: true,
  stripe: true,
  height: 'auto',
  showRefresh: true,
  showAdd: proxy.checkPermission('system:sysconfig:add'),
  showEdit: proxy.checkPermission('system:sysconfig:edit'),
  showDelete: proxy.checkPermission('system:sysconfig:remove'),
  showExport: proxy.checkPermission('system:sysconfig:export')
})

/* =====================================================
 * handlers
 * ===================================================== */
const handleAdd = () => {
  addKey.value++
  nextTick(() => {
    addOrUpdateRef.value.init()
  })
}

const handleEdit = (row) => {
  addKey.value++
  nextTick(() => {
    addOrUpdateRef.value.init(row.configId)
  })
}

</script>
