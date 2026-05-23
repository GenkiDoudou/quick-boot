<template>
  <div class="button-group-demo">
    <h2>C7ButtonGroup 组件演示</h2>
    
    <div class="demo-section">
      <h3>1. 自动模式 - 点击触发（默认）</h3>
      <p>当按钮数量超过 maxVisible 时，自动显示"更多"按钮</p>
      <c7-button-group :maxVisible="2" mode="auto" trigger="click" size="small">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
        <c7-button type="warning" icon="View" @click="handleAction('view')">查看</c7-button>
        <c7-button type="info" icon="CopyDocument" @click="handleAction('copy')">复制</c7-button>
      </c7-button-group>
    </div>

    <div class="demo-section">
      <h3>2. 自动模式 - 悬停触发</h3>
      <p>鼠标悬停在"更多"按钮上时显示下拉菜单</p>
      <c7-button-group :maxVisible="2" mode="auto" trigger="hover" size="default">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
        <c7-button type="warning" icon="View" @click="handleAction('view')">查看</c7-button>
        <c7-button type="info" icon="CopyDocument" @click="handleAction('copy')">复制</c7-button>
      </c7-button-group>
    </div>

    <div class="demo-section">
      <h3>3. 内联模式</h3>
      <p>所有按钮都显示在一行，不显示"更多"按钮</p>
      <c7-button-group mode="inline" size="small">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
        <c7-button type="warning" icon="View" @click="handleAction('view')">查看</c7-button>
        <c7-button type="info" icon="CopyDocument" @click="handleAction('copy')">复制</c7-button>
      </c7-button-group>
    </div>

    <div class="demo-section">
      <h3>4. 不同大小</h3>
      <p>小按钮</p>
      <c7-button-group :maxVisible="2" mode="auto" size="small">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
      
      <p>默认按钮</p>
      <c7-button-group :maxVisible="2" mode="auto" size="default">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
      
      <p>大按钮</p>
      <c7-button-group :maxVisible="2" mode="auto" size="large">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
    </div>

    <div class="demo-section">
      <h3>5. 不同间距</h3>
      <p>紧密间距</p>
      <c7-button-group :maxVisible="2" mode="auto" spacing="tight">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
      
      <p>正常间距</p>
      <c7-button-group :maxVisible="2" mode="auto" spacing="normal">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
      
      <p>宽松间距</p>
      <c7-button-group :maxVisible="2" mode="auto" spacing="loose">
        <c7-button btnType="edit" @click="handleAction('edit')" />
        <c7-button type="success" icon="Plus" @click="handleAction('add')">新增</c7-button>
        <c7-button btnType="delete" @click="handleAction('delete')" />
      </c7-button-group>
    </div>

    <div class="demo-section">
      <h3>6. 实际应用场景 - 表格操作列</h3>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <c7-button-group :maxVisible="2" mode="auto" trigger="click" size="small">
              <c7-button btnType="edit" @click="handleEdit(scope.row)" />
              <c7-button type="success" icon="Plus" @click="handleAdd(scope.row)">新增</c7-button>
              <c7-button btnType="delete" @click="handleDelete(scope.row)" />
              <c7-button type="warning" icon="View" @click="handleView(scope.row)">查看</c7-button>
              <c7-button type="info" icon="CopyDocument" @click="handleCopy(scope.row)">复制</c7-button>
              <c7-button type="primary" icon="Download" @click="handleDownload(scope.row)">下载</c7-button>
            </c7-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
// C7 组件已通过全局注册，无需单独导入
// import { C7ButtonGroup, C7Button } from '@/components/c7'

// 处理按钮点击
const handleAction = (action) => {
  ElMessage.success(`执行操作: ${action}`)
}

// 表格数据
const tableData = ref([
  { name: '用户管理', status: '启用' },
  { name: '角色管理', status: '启用' },
  { name: '菜单管理', status: '禁用' },
  { name: '字典管理', status: '启用' }
])

// 表格操作函数
const handleEdit = (row) => {
  ElMessage.info(`编辑: ${row.name}`)
}

const handleAdd = (row) => {
  ElMessage.success(`新增: ${row.name}`)
}

const handleDelete = (row) => {
  ElMessage.warning(`删除: ${row.name}`)
}

const handleView = (row) => {
  ElMessage.info(`查看: ${row.name}`)
}

const handleCopy = (row) => {
  ElMessage.success(`复制: ${row.name}`)
}

const handleDownload = (row) => {
  ElMessage.success(`下载: ${row.name}`)
}
</script>

<style scoped>
.button-group-demo {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.demo-section {
  margin-bottom: 40px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.demo-section h3 {
  margin-top: 0;
  color: #303133;
}

.demo-section p {
  margin: 10px 0;
  color: #606266;
  font-size: 14px;
}

.demo-section + .demo-section {
  margin-top: 20px;
}
</style>
