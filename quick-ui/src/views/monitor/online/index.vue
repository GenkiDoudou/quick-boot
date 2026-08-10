<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="tokenId"
      :show-index="false"
      :show-selection="false"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :show-add-button="false"
      :show-edit-button="false"
      :show-delete-button="false"
      :show-export-button="false"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #action="{ row }">
        <c7-button
          btn-type="delete"
          link
          confirm
          confirm-message="确认强退该用户吗？"
          :click-function="() => handleForceLogout(row)"
          v-hasPermi="['monitor:online:forceLogout']"
        >
          强退
        </c7-button>
      </template>
    </C7JsonTable>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { forceLogout, listOnline } from '@/api/monitor/online'

/**
 * 在线用户：查询 Sa-Token 会话列表并支持强退。
 */
defineOptions({ name: 'SysUserOnline' })

const tableRef = ref(null)

const defaultSearchParam = {
  ipaddr: '',
  userName: '',
}

const searchColumns = computed(() => [
  { prop: 'ipaddr', label: '登录地址', type: 'input', span: 8, props: { placeholder: '请输入登录地址', clearable: true } },
  { prop: 'userName', label: '用户名称', type: 'input', span: 8, props: { placeholder: '请输入用户名称', clearable: true } },
])

const tableColumns = computed(() => [
  { prop: 'tokenId', label: '会话编号', minWidth: 200, showOverflowTooltip: true },
  { prop: 'userName', label: '登录名称', width: 120 },
  { prop: 'deptName', label: '部门', width: 140, showOverflowTooltip: true },
  { prop: 'ipaddr', label: '主机', width: 140 },
  { prop: 'loginLocation', label: '登录地点', width: 120 },
  { prop: 'browser', label: '浏览器', width: 120, showOverflowTooltip: true },
  { prop: 'os', label: '操作系统', width: 120, showOverflowTooltip: true },
  { prop: 'loginTime', label: '登录时间', width: 180 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 100, fixed: 'right' },
])

/** C7JsonTable 列表传 { current, size, param }；映射为扁平 GET query */
function toOnlineQuery(pageReq) {
  const raw = pageReq && typeof pageReq === 'object' ? pageReq : {}
  const nested =
    raw.param && typeof raw.param === 'object' && !Array.isArray(raw.param)
      ? { ...raw.param }
      : { ...raw }
  delete nested.current
  delete nested.size
  delete nested.param
  return {
    ...nested,
    pageNum: raw.current ?? raw.pageNum ?? 1,
    pageSize: raw.size ?? raw.pageSize ?? 10,
  }
}

function listFunction(pageReq) {
  return listOnline(toOnlineQuery(pageReq))
}

async function handleForceLogout(row) {
  await forceLogout(row.tokenId)
  ElMessage.success('强退成功')
  tableRef.value?.refreshData?.()
}
</script>
