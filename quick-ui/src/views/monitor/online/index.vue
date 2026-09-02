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
import { ElMessage } from 'element-plus'
import { forceLogout, listOnline } from '@/api/monitor/online'
import { useCrudListPage } from '@/composables/useCrudPage'
import * as schema from '@/views/_schemas/tier-a/online.schema'
import { toLegacyPageQuery } from '@/views/_schemas/tier-a/_shared'

/** 在线用户：查询 Sa-Token 会话列表并支持强退。 */
defineOptions({ name: 'SysUserOnline' })

const { tableRef, refreshTable } = useCrudListPage()

const defaultSearchParam = schema.defaultSearch
const searchColumns = schema.searchColumns
const tableColumns = schema.tableColumns

function listFunction(pageReq) {
  return listOnline(toLegacyPageQuery(pageReq))
}

async function handleForceLogout(row) {
  await forceLogout(row.tokenId)
  ElMessage.success('强退成功')
  refreshTable()
}
</script>
