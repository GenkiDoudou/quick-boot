<template>
  <div class="app-container kb-library">
    <el-row :gutter="16">
      <el-col :span="6">
        <c7-card label="目录">
          <div class="kb-library__tree-toolbar">
            <el-button type="primary" link @click="openFolderDialog()" v-hasPermi="['knowledge:library:add']">新建根目录</el-button>
            <el-button type="primary" link :disabled="!currentFolderId" @click="openFolderDialog(currentFolder)" v-hasPermi="['knowledge:library:add']">新建子目录</el-button>
          </div>
          <el-tree
            ref="treeRef"
            node-key="folderId"
            :data="folderTree"
            :props="{ label: 'name', children: 'children' }"
            highlight-current
            default-expand-all
            @node-click="onFolderClick"
          />
        </c7-card>
      </el-col>
      <el-col :span="18">
        <c7-card label="文件列表">
          <div class="kb-library__file-toolbar">
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              :on-change="onFilePick"
              v-hasPermi="['knowledge:library:upload']"
            >
              <el-button type="primary" plain :disabled="currentFolderId == null">上传文件</el-button>
            </el-upload>
            <span v-if="currentFolderId == null" class="kb-library__hint">请先选择左侧目录</span>
          </div>
          <el-table v-loading="fileLoading" :data="fileList" row-key="libFileId" border>
            <el-table-column prop="title" label="文件名" min-width="200" show-overflow-tooltip />
            <el-table-column prop="fileExt" label="类型" width="80" />
            <el-table-column prop="fileSize" label="大小" width="100">
              <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <c7-button
                  btn-type="delete"
                  link
                  confirm
                  :confirm-message="`确认删除「${row.title}」吗？`"
                  :click-function="() => removeFile(row)"
                  v-hasPermi="['knowledge:library:remove']"
                />
              </template>
            </el-table-column>
          </el-table>
          <div class="kb-library__pager">
            <el-pagination
              v-model:current-page="fileQuery.pageNum"
              v-model:page-size="fileQuery.pageSize"
              :total="fileTotal"
              layout="total, prev, pager, next"
              @current-change="loadFiles"
              @size-change="loadFiles"
            />
          </div>
        </c7-card>
      </el-col>
    </el-row>

    <c7-dialog v-model="folderVisible" :title="folderForm.folderId ? '修改目录' : '新建目录'" :on-confirm="submitFolder" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="folderForm.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="folderForm.orderNum" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
    </c7-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  addLibraryFolder,
  listLibraryFile,
  listLibraryFolderTree,
  removeLibraryFile,
  uploadLibraryFile
} from '@/api/knowledge/library'

defineOptions({ name: 'KbDocLibrary' })

const folderTree = ref([])
const currentFolderId = ref(null)
const currentFolder = ref(null)
const fileList = ref([])
const fileTotal = ref(0)
const fileLoading = ref(false)
const fileQuery = ref({ pageNum: 1, pageSize: 10, folderId: null })

const folderVisible = ref(false)
const folderForm = ref({ folderId: null, parentId: 0, name: '', orderNum: 0 })

function formatSize(bytes) {
  if (bytes == null || bytes < 1024) return `${bytes || 0} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function loadTree() {
  return listLibraryFolderTree().then((res) => {
    folderTree.value = Array.isArray(res?.data) ? res.data : []
  })
}

function onFolderClick(node) {
  currentFolderId.value = node.folderId
  currentFolder.value = node
  fileQuery.value.folderId = node.folderId
  fileQuery.value.pageNum = 1
  loadFiles()
}

function loadFiles() {
  if (fileQuery.value.folderId == null) {
    fileList.value = []
    fileTotal.value = 0
    return Promise.resolve()
  }
  fileLoading.value = true
  return listLibraryFile(fileQuery.value)
    .then((res) => {
      fileList.value = res?.data?.records || []
      fileTotal.value = res?.data?.total || 0
    })
    .finally(() => {
      fileLoading.value = false
    })
}

function openFolderDialog(parent) {
  if (parent) {
    folderForm.value = { folderId: null, parentId: parent.folderId, name: '', orderNum: 0 }
  } else {
    folderForm.value = { folderId: null, parentId: 0, name: '', orderNum: 0 }
  }
  folderVisible.value = true
}

function submitFolder() {
  const name = String(folderForm.value.name || '').trim()
  if (!name) {
    ElMessage.warning('请输入目录名称')
    return Promise.reject(new Error('no name'))
  }
  return addLibraryFolder({ ...folderForm.value, name }).then(() => {
    ElMessage.success('目录已创建')
    folderVisible.value = false
    return loadTree()
  })
}

function onFilePick(uploadFile) {
  if (currentFolderId.value == null) {
    ElMessage.warning('请先选择目录')
    return
  }
  const raw = uploadFile.raw
  if (!raw) return
  uploadLibraryFile(currentFolderId.value, raw)
    .then(() => {
      ElMessage.success('上传成功')
      loadFiles()
    })
    .catch(() => {})
}

function removeFile(row) {
  return removeLibraryFile([row.libFileId]).then(() => {
    ElMessage.success('已删除')
    return loadFiles()
  })
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.kb-library__tree-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.kb-library__file-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.kb-library__hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.kb-library__pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
