<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="88px">
<#list queryColumns as col>
      <el-form-item label="${col.columnComment!}" prop="${col.javaField}">
        <el-input v-model="queryParams.${col.javaField}" placeholder="璇疯緭鍏?{col.columnComment!}" clearable @keyup.enter="handleQuery" />
      </el-form-item>
</#list>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">鎼滅储</el-button>
        <el-button icon="Refresh" @click="resetQuery">閲嶇疆</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['${permissionPrefix}:add']">鏂板</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['${permissionPrefix}:remove']">鍒犻櫎</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
<#list listColumns as col>
      <el-table-column label="${col.columnComment!}" prop="${col.javaField}" align="center" show-overflow-tooltip />
</#list>
      <el-table-column label="鎿嶄綔" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['${permissionPrefix}:edit']">淇敼</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['${permissionPrefix}:remove']">鍒犻櫎</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup>
/**
 * ${tableComment!} 鍒楄〃锛堜唬鐮佺敓鎴?路 Element Plus 鍘熺敓锛夈€?
 */
import { ref } from 'vue'
import { list${className}, del${className} } from '@/api/${moduleName}/${businessName}'

defineOptions({ name: '${className}List' })

const loading = ref(false)
const showSearch = ref(true)
const dataList = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
<#list queryColumns as col>
  ${col.javaField}: undefined,
</#list>
})

function getList() {
  loading.value = true
  list${className}(queryParams.value).then((res) => {
    dataList.value = res.data?.records || res.data?.rows || []
    total.value = res.data?.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { pageNum: 1, pageSize: 10 }
  getList()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.${pkField!"id"})
  multiple.value = !selection.length
}

function handleAdd() {
  // TODO: 鎵撳紑鏂板寮圭獥
}

function handleUpdate(row) {
  // TODO: 鎵撳紑缂栬緫寮圭獥
}

function handleDelete(row) {
  const delIds = row?.${pkField!"id"} ? [row.${pkField!"id"}] : ids.value
  del${className}(delIds).then(() => getList())
}

getList()
</script>
