<template>
  <div class="app-container">
    <c7-json-table
        ref="tableRef"
        :listFunction="listSysOauthClient"
        :tableColumns="tableColumns"
        :delete-function="delSysOauthClient"
        :searchColumns="searchColumns"
        :tableProps="tableProps"
        rowsKey="data.records"
        totalKey="data.total"
    >
      <!-- 操作列 -->
      <template #table-operate="{ row }">
        <C7ButtonGroup :max-visible=5>
          <C7Button
              type="primary"
              v-if="row.status == '1'"
              link
              icon="View"
              @click="updateStatus(row,'0')"
              v-hasPermi="['system:sysoauthclient:edit']"
          >
            上架
          </C7Button>
          <C7Button
              v-if="row.status == '0'"
              type="primary"
              link
              icon="View"
              @click="updateStatus(row,'1')"
              v-hasPermi="['system:sysoauthclient:edit']"
          >
            下架
          </C7Button>
          <C7Button
              type="primary"
              link
              icon="View"
              @click="handlerView(row)"
              v-hasPermi="['system:sysoauthclient:edit']"
          >
            查看
          </C7Button>
          <C7Button
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
              v-hasPermi="['system:sysoauthclient:edit']"
          >
            编辑
          </C7Button>

          <C7Button
              type="danger"
              link
              icon="Delete"
              @click="tableRef.handleDelete(row.id)"
              v-hasPermi="['system:sysoauthclient:remove']"
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

    <C7Dialog
        :visible="visibleRef"
        mode="dialog"
        title="客户端信息"
        width="600px"
        :footer="false"
        @close="handleDialogClose"
    >

      <div class="view-dialog-content">
        <!-- 客户端信息 -->
        <div class="info-section">
          <h3 class="section-title">客户端信息</h3>
          <div class="info-item">
            <label>客户端ID：</label>
            <div class="info-value mono-font">{{ viewForm.clientId || '暂无' }}</div>
          </div>
          <div class="info-item">
            <label>客户端密钥：</label>
            <div class="info-value mono-font">{{ viewForm.clientSecret || '暂无' }}</div>
          </div>
          <div class="info-actions">
            <el-button
                type="primary"
                :icon="CopyDocument"
                :loading="copyLoading.client"
                @click="handleCopyClientInfo"
            >
              复制客户端信息
            </el-button>
          </div>
        </div>

        <!-- 加解密信息 -->
        <div class="info-section">
          <div class="section-header">
            <h3 class="section-title">加解密信息</h3>
            <el-button
                type="warning"
                size="small"
                :loading="regenerateLoading"
                @click="handleRegenerateKey"
            >
              重新生成
            </el-button>
          </div>
          <div class="info-item">
            <label>公钥：</label>
            <div class="info-value mono-font">{{ viewForm.publicKey || '暂无' }}</div>
          </div>
          <div class="info-item">
            <label>私钥：</label>
            <div class="info-value mono-font">{{ viewForm.privateKey || '暂无' }}</div>
          </div>
          <div class="info-actions">
            <el-button
                type="primary"
                :icon="CopyDocument"
                :loading="copyLoading.crypto"
                @click="handleCopyCryptoInfo"
            >
              复制加解密信息
            </el-button>
          </div>
        </div>
      </div>
    </C7Dialog>
  </div>
</template>

<script setup name="sysoauthclient">
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import AddOrUpdate from './add-or-update.vue'
import {
  listSysOauthClient,
  delSysOauthClient,
  updateSysOauthClientStatus,
  getSysOauthClient,
  generateEncryptionKey
} from '@/api/system/sysoauthclient'

import { C7JsonTable, C7Button, C7ButtonGroup, C7Dialog } from '@c7-plus'
import {CopyDocument} from "@element-plus/icons-vue";
import {useDict} from '@/composables/useDict';
import {useUtils} from '@/composables/useUtils';

/* =====================================================
 *  dictType 自动收集（listFields + searchFields）
 * ===================================================== */

const { sys_verify_type, COMMON_STATUS } = useDict("sys_verify_type", "COMMON_STATUS")
const utils = useUtils();


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
  { prop: "clientName", label: "客户端名称", type: "input" },
  { prop: "clientId", label: "客户端id", type: "input" },
  { label: "ip白名单", prop: "whitelistIp", showOverflowTooltip: true },
  { prop: "verifyType", label: "校验类型", type: "select", dataList: sys_verify_type },
  { label: "状态", prop: "status", type: "select", dataList: COMMON_STATUS }
])

const tableColumns = ref([
  { label: "客户端名称", prop: "clientName" },
  { label: "客户端id", prop: "clientId" },
  { label: "接口授权", prop: "authorities" },
  { label: "令牌有效时间", prop: "accessTokenValidity" },
  { label: "刷新令牌有效时间", prop: "refreshTokenValidity" },
  { label: "ip白名单", prop: "whitelistIp", showOverflowTooltip: true },
  { label: "校验类型", prop: "verifyType", width: '150px', columnType: 'tag', dictList: sys_verify_type },
  { label: "状态", prop: "status", width: '60px', columnType: 'tag', dictList: COMMON_STATUS },
  { label: "创建时间", prop: "createTime", showOverflowTooltip: true },
  { label: "操作", prop: "table-operate", width: 160, fixed: "right" }
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
  showAdd: utils.checkPermission('system:sysoauthclient:add'),
  showEdit: utils.checkPermission('system:sysoauthclient:edit'),
  showDelete: utils.checkPermission('system:sysoauthclient:remove'),
  showExport: utils.checkPermission('system:sysoauthclient:export')
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
    addOrUpdateRef.value.init(row.id)
  })
}
// 修改状态  二次确认
const updateStatus =  (row, status) => {
  // 根据状态值确定操作文本：'0'=上架(启用)，'1'=下架(禁用)
  const actionText = status === '0' ? '上架' : '下架'
  try {
     ElMessageBox.confirm(`确定要${actionText}【${row.clientName}】吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      updateSysOauthClientStatus(row.id, status).then(() => {
        ElMessage.success(`${actionText}成功`)
        tableRef.value.refreshData()
      })
     })

  } catch (error) {
    if (error !== 'cancel') {
      console.error('状态更新失败:', error)
      ElMessage.error(`${actionText}失败`)

    } else {
      ElMessage.info('已取消')
    }
  }
}

// 查看
const viewForm = ref({})
const visibleRef = ref(false)
const handlerView = (row) => {
  getSysOauthClient(row.id).then(res => {
    visibleRef.value = true
    viewForm.value = res.data || {}
  }).catch(() => {
    // 忽略错误，组件可能已卸载
  })
}

// 对话框关闭处理
const handleDialogClose = () => {
  visibleRef.value = false
  viewForm.value = {}
}

// 复制功能相关状态
const copyLoading = reactive({
  client: false,
  crypto: false
})

// 重新生成密钥加载状态
const regenerateLoading = ref(false)

// 复制客户端信息（客户端ID和客户端密钥）
const handleCopyClientInfo = async () => {
  if (!viewForm.value?.clientId && !viewForm.value?.clientSecret) {
    ElMessage.warning('暂无客户端数据')
    return
  }

  const text = `客户端ID: ${viewForm.value?.clientId || ''}\n客户端密钥: ${viewForm.value?.clientSecret || ''}`
  
  copyLoading.client = true
  try {
    // 优先使用现代 Clipboard API
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      // 降级方案：使用传统方法
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  } finally {
    // 延迟重置 loading 状态，提升用户体验
    setTimeout(() => {
      copyLoading.client = false
    }, 300)
  }
}

// 复制加解密信息（公钥和私钥）
const handleCopyCryptoInfo = async () => {
  if (!viewForm.value?.publicKey && !viewForm.value?.privateKey) {
    ElMessage.warning('暂无加解密数据')
    return
  }

  const text = `公钥:\n${viewForm.value?.publicKey || ''}\n\n私钥:\n${viewForm.value?.privateKey || ''}`
  
  copyLoading.crypto = true
  try {
    // 优先使用现代 Clipboard API
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      // 降级方案：使用传统方法
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('复制成功')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  } finally {
    // 延迟重置 loading 状态，提升用户体验
    setTimeout(() => {
      copyLoading.crypto = false
    }, 300)
  }
}

// 重新生成加解密密钥
const handleRegenerateKey = async () => {
  if (!viewForm.value?.id) {
    ElMessage.warning('客户端ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm(
      '重新生成密钥后，旧的密钥将失效，确定要继续吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    regenerateLoading.value = true
    const res = await generateEncryptionKey(viewForm.value.id)

    // 更新 viewForm 中的公钥和私钥
    if (res.data) {
      viewForm.value.publicKey = res.data.publicKey
      viewForm.value.privateKey = res.data.privateKey
      ElMessage.success('重新生成成功')
    } else {
      ElMessage.error('重新生成失败，请稍后重试')
    }
  } catch (error) {
    // 用户取消操作
    if (error === 'cancel') {
      return
    }
    console.error('重新生成密钥失败:', error)
    ElMessage.error('重新生成失败，请稍后重试')
  } finally {
    regenerateLoading.value = false
  }
}
</script>
