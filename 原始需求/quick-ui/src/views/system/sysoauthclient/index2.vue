<template>
  <!-- 唯一根节点 -->
  <div class="app-wrapper">
    <div class="app-container">
      <c7-json-table
          ref="tableRef"
          :listFunction="listSysOauthClient"
          :tableColumns="tableColumns"
          :delete-function="delSysOauthClient"
          :searchColumns="searchColumns"
          :tableProps="tableProps"
          @addBtnHandle="handleAdd"
          rowsKey="data.records"
          totalKey="data.total"
      >
        <template #table-operate="{ row }">
        <C7ButtonGroup :max-visible="5">

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

      <add-or-update
          :key="addKey"
          ref="addOrUpdateRef"
          @refreshDataList="tableRef.refreshData()"
      />

      <!-- 查看详情弹窗 -->
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
                  @click="copySection('client')"
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
                  @click="copySection('crypto')"
              >
                复制加解密信息
              </el-button>
            </div>
          </div>
        </div>
      </C7Dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick, reactive, onUnmounted, onBeforeUnmount, defineOptions } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'

// 定义组件名称，用于 keep-alive 缓存
defineOptions({
  name: 'sysoauthclient'
})

import AddOrUpdate from './add-or-update.vue'
import {
  listSysOauthClient,
  delSysOauthClient,
  getSysOauthClient,
  updateSysOauthClientStatus,
  generateEncryptionKey
} from '@/api/system/sysoauthclient'

import { C7JsonTable, C7Button, C7ButtonGroup, C7Dialog } from '@c7-plus'

const { proxy } = getCurrentInstance()
defineEmits(["refreshDataList"])

const { sys_verify_type, COMMON_STATUS } = proxy.useDict("sys_verify_type", "COMMON_STATUS")

// ====================== refs 全量定义 ======================
const tableRef = ref()
const addOrUpdateRef = ref()
const dataFormRef = ref() // 🔥 这个是你之前缺的，必报错
const addKey = ref(0)

// 弹窗
const visibleRef = ref(false)
const viewForm = ref({})

// 复制
const copyLoading = reactive({
  client: false,
  crypto: false
})

// 重新生成密钥加载状态
const regenerateLoading = ref(false)

// 存储所有需要清理的定时器（必须在钩子之前定义）
const timers = []

// 组件是否已卸载的标志（用于防止异步操作在组件卸载后执行）
const isUnmounted = ref(false)

// ====================== 搜索/表格配置 ======================
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

const tableProps = ref({
  selection: true,
  border: true,
  stripe: true,
  height: 'auto',
  showRefresh: true,
  showAdd: proxy.checkPermission('system:sysoauthclient:add'),
  showEdit: proxy.checkPermission('system:sysoauthclient:edit'),
  showDelete: proxy.checkPermission('system:sysoauthclient:remove'),
  showExport: proxy.checkPermission('system:sysoauthclient:export')
})

// ====================== 方法 ======================
const handleAdd = () => {
  addKey.value++
  nextTick(() => addOrUpdateRef.value?.init())
}

const handleEdit = (row) => {
  addKey.value++
  nextTick(() => addOrUpdateRef.value?.init(row.id))
}

const handlerView = (row) => {
  if (isUnmounted.value) return
  visibleRef.value = true
  getSysOauthClient(row.id).then(res => {
    if (isUnmounted.value) return
    viewForm.value = res.data || {}
  }).catch(() => {
    // 忽略错误，组件可能已卸载
  })
}

// 弹窗关闭 + 路由离开必须清空
const handleDialogClose = () => {
  visibleRef.value = false
  viewForm.value = {}
}

// 路由离开前清理资源（在组件卸载前执行）
onBeforeUnmount(() => {
  try {
    // 标记组件已卸载，防止异步操作继续执行
    isUnmounted.value = true

    // 强制关闭弹窗
    visibleRef.value = false
    viewForm.value = {}

    // 清理所有定时器
    timers.forEach(timer => {
      try {
        clearTimeout(timer)
      } catch (e) {
        // 忽略定时器清理错误
      }
    })
    timers.length = 0

    // 重置加载状态
    copyLoading.client = false
    copyLoading.crypto = false
    regenerateLoading.value = false
  } catch (error) {
    // 捕获所有错误，防止阻塞路由切换
    console.warn('sysoauthclient onBeforeUnmount error:', error)
  }
})

// 路由离开时强制销毁弹窗状态（解决白屏/盖住问题）
onUnmounted(() => {
  try {
    // 确保标记已卸载
    isUnmounted.value = true

    visibleRef.value = false
    viewForm.value = {}

    // 再次清理定时器（双重保险）
    timers.forEach(timer => {
      try {
        clearTimeout(timer)
      } catch (e) {
        // 忽略定时器清理错误
      }
    })
    timers.length = 0
  } catch (error) {
    // 捕获所有错误，防止阻塞路由切换
    console.warn('sysoauthclient onUnmounted error:', error)
  }
})

// 复制
const copyToClipboard = async (text, section) => {
  if (isUnmounted.value) return
  if (!text?.trim()) {
    ElMessage.warning(`暂无${section === 'client' ? '客户端' : '加解密'}数据`)
    return
  }
  copyLoading[section] = true
  try {
    await navigator.clipboard.writeText(text)
    if (isUnmounted.value) return
    ElMessage.success('复制成功')
  } catch (e) {
    if (isUnmounted.value) return
    ElMessage.error('复制失败')
  } finally {
    if (isUnmounted.value) {
      copyLoading[section] = false
      return
    }
    const timer = setTimeout(() => {
      if (!isUnmounted.value) {
        copyLoading[section] = false
      }
    }, 300)
    timers.push(timer)
  }
}

const copySection = (section) => {
  if (section === 'client') {
    const text = `客户端ID: ${viewForm.value?.clientId || ''}\n客户端密钥: ${viewForm.value?.clientSecret || ''}`
    copyToClipboard(text, 'client')
  } else {
    const text = `公钥:\n${viewForm.value?.publicKey || ''}\n\n私钥:\n${viewForm.value?.privateKey || ''}`
    copyToClipboard(text, 'crypto')
  }
}

// 重新生成加解密密钥
const handleRegenerateKey = async () => {
  if (isUnmounted.value) return
  if (!viewForm.value?.id) {
    ElMessage.warning('客户端ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm('重新生成密钥后，旧的密钥将失效，确定要继续吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (isUnmounted.value) return
    regenerateLoading.value = true
    const res = await generateEncryptionKey(viewForm.value.id)

    if (isUnmounted.value) return

    // 更新 viewForm 中的公钥和私钥
    if (res.data) {
      viewForm.value.publicKey = res.data.publicKey
      viewForm.value.privateKey = res.data.privateKey
      ElMessage.success('重新生成成功')
    } else {
      ElMessage.success(res.msg || '重新生成成功')
      // 重新获取客户端详情以更新密钥
      const detailRes = await getSysOauthClient(viewForm.value.id)
      if (!isUnmounted.value && detailRes.data) {
        viewForm.value.publicKey = detailRes.data.publicKey
        viewForm.value.privateKey = detailRes.data.privateKey
      }
    }
  } catch (error) {
    if (isUnmounted.value) return
    if (error !== 'cancel') {
      console.error('重新生成密钥失败:', error)
      ElMessage.error('重新生成失败')
    }
  } finally {
    if (!isUnmounted.value) {
      regenerateLoading.value = false
    }
  }
}

//
// 修改状态  二次确认
const updateStatus = async (row, status) => {
  if (isUnmounted.value) return
  // 根据状态值确定操作文本：'0'=上架(启用)，'1'=下架(禁用)
  const actionText = status === '0' ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${actionText}【${row.clientName}】吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (isUnmounted.value) return
    const res = await updateSysOauthClientStatus(row.id, status)

    if (isUnmounted.value) return
    ElMessage.success(res.msg || `${actionText}成功`)

    // 立即更新本地状态，确保 UI 立即响应
    if (!isUnmounted.value) {
      row.status = status
    }

    // 然后刷新数据，确保与服务器同步
    if (!isUnmounted.value && tableRef.value) {
      await tableRef.value.refreshData()
    }
  } catch (error) {
    if (isUnmounted.value) return
    if (error !== 'cancel') {
      console.error('状态更新失败:', error)
      ElMessage.error(`${actionText}失败`)
      // 如果更新失败，恢复原状态
      if (!isUnmounted.value) {
        row.status = row.status === '0' ? '1' : '0'
      }
    } else {
      ElMessage.info('已取消')
    }
  }
}
</script>

<style scoped>
.app-wrapper {
  width: 100%;
  height: 100%;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-weight: 500;
}
.mono-font {
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

/* 查看弹窗样式 */
.view-dialog-content {
  padding: 20px 0;
}

.info-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.info-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-item {
  margin-bottom: 15px;
}

.info-item label {
  display: inline-block;
  width: 100px;
  font-weight: 500;
  color: #606266;
  vertical-align: top;
}

.info-value {
  display: inline-block;
  width: calc(100% - 120px);
  padding: 8px 12px;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  word-break: break-all;
  min-height: 40px;
  line-height: 1.5;
}

.info-actions {
  margin-top: 15px;
  text-align: right;
}
</style>
