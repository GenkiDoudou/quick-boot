<template>
  <div class="app-container">
    <el-page-header class="mb-4" content="编辑生成配置" @back="goBack" />

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="基本信息" name="basic">
        <el-form ref="basicRef" :model="form" :rules="basicRules" label-width="120px" class="gen-edit-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="表名称" prop="tableName">
                <el-input v-model="form.tableName" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="表描述" prop="tableComment">
                <el-input v-model="form.tableComment" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="实体类名" prop="className">
                <el-input v-model="form.className" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="作者" prop="functionAuthor">
                <el-input v-model="form.functionAuthor" placeholder="可在参数设置 qc.gen.author 配置默认值" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="生成包路径" prop="packageName">
                <el-input v-model="form.packageName" placeholder="qc.gen.package-name" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="生成模块名" prop="moduleName">
                <el-input v-model="form.moduleName" placeholder="qc.gen.module-name" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="业务名" prop="businessName">
                <el-input v-model="form.businessName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="功能名" prop="functionName">
                <el-input v-model="form.functionName" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模板类型" prop="tplCategory">
                <el-select v-model="form.tplCategory" style="width: 100%">
                  <el-option label="单表 CRUD" value="crud" />
                  <el-option label="树表（尚未开放）" value="tree" disabled />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="前端类型" prop="tplWebType">
                <el-select v-model="form.tplWebType" style="width: 100%">
                  <el-option label="C7 组件" value="c7" />
                  <el-option label="Element Plus 原生" value="element-plus" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="生成代码方式" prop="genType">
                <el-select v-model="form.genType" style="width: 100%">
                  <el-option label="Zip 压缩包" value="0" />
                  <el-option label="自定义路径" value="1" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col v-if="form.genType === '1'" :span="12">
              <el-form-item label="自定义路径" prop="genPath">
                <el-input v-model="form.genPath" placeholder="如 D:/codegen 或 E:/workspace/my-gen" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="上级菜单" prop="parentMenuId">
                <c7-tree-select
                  :key="menuTreeKey"
                  v-model="form.parentMenuId"
                  :data-list="menuTree"
                  value-key="id"
                  label-key="label"
                  children-key="children"
                  :check-strictly="true"
                  :default-expand-all="true"
                  value-type="string"
                  placeholder="选择挂载目录（仅目录类型）"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="form.remark" type="textarea" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="字段信息" name="columns">
        <el-table :data="form.columns" border max-height="480">
          <el-table-column prop="columnName" label="列名" width="140" />
          <el-table-column label="列描述" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.columnComment" size="small" :placeholder="row.columnName" />
            </template>
          </el-table-column>
          <el-table-column label="字段类型" width="130">
            <template #default="{ row }">
              <el-select v-model="row.columnType" size="small" filterable allow-create default-first-option style="width: 100%">
                <el-option v-for="t in columnTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="Java类型" width="110">
            <template #default="{ row }">
              <el-input v-model="row.javaType" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="Java属性" width="120">
            <template #default="{ row }">
              <el-input v-model="row.javaField" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="插入" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.isInsert" true-value="1" false-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="编辑" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.isEdit" true-value="1" false-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="列表" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.isList" true-value="1" false-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="查询" width="70" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.isQuery" true-value="1" false-value="0" />
            </template>
          </el-table-column>
          <el-table-column label="查询方式" width="100">
            <template #default="{ row }">
              <el-select v-model="row.queryType" size="small">
                <el-option label="=" value="EQ" />
                <el-option label="LIKE" value="LIKE" />
                <el-option label="BETWEEN" value="BETWEEN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="显示类型" width="120">
            <template #default="{ row }">
              <el-select v-model="row.htmlType" size="small" style="width: 100%">
                <el-option v-for="h in htmlTypeOptions" :key="h.value" :label="h.label" :value="h.value" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="字典类型" width="120">
            <template #default="{ row }">
              <el-input v-model="row.dictType" size="small" />
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <div class="gen-edit-actions">
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      <el-button @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 代码生成编辑页：基本信息 + 字段信息。
 */
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { treeselectMenu } from '@/api/system/menu'
import { getGenDefaults, getGenTable, updateGenTable } from '@/api/tool/gen'

const route = useRoute()
const router = useRouter()
const activeTab = ref('basic')
const saving = ref(false)
const basicRef = ref()
const menuTree = ref([])
const menuTreeKey = ref(0)

/** 数据库字段类型（下拉可选，支持 filterable 自定义输入） */
const columnTypeOptions = [
  'varchar', 'char', 'text', 'mediumtext', 'longtext',
  'int', 'bigint', 'tinyint', 'smallint', 'mediumint',
  'decimal', 'double', 'float',
  'datetime', 'date', 'timestamp', 'time', 'year',
  'bit', 'boolean', 'json', 'blob', 'longblob', 'enum'
]

/** 表单显示类型 */
const htmlTypeOptions = [
  { label: '文本框', value: 'input' },
  { label: '文本域', value: 'textarea' },
  { label: '下拉框', value: 'select' },
  { label: '单选框', value: 'radio' },
  { label: '复选框', value: 'checkbox' },
  { label: '日期时间', value: 'datetime' },
  { label: '图片上传', value: 'image' },
  { label: '文件上传', value: 'upload' },
  { label: '富文本', value: 'editor' }
]

const form = reactive({
  tableId: null,
  tableName: '',
  tableComment: '',
  className: '',
  remark: '',
  packageName: '',
  moduleName: '',
  businessName: '',
  functionName: '',
  functionAuthor: '',
  tplCategory: 'crud',
  tplWebType: 'c7',
  genType: '0',
  genPath: '',
  parentMenuId: null,
  columns: []
})

const basicRules = {
  tableName: [{ required: true, message: '必填', trigger: 'blur' }],
  className: [{ required: true, message: '必填', trigger: 'blur' }],
  packageName: [{ required: true, message: '必填', trigger: 'blur' }],
  moduleName: [{ required: true, message: '必填', trigger: 'blur' }],
  businessName: [{ required: true, message: '必填', trigger: 'blur' }],
  functionName: [{ required: true, message: '必填', trigger: 'blur' }],
  functionAuthor: [{ required: true, message: '必填', trigger: 'blur' }],
  tplWebType: [{ required: true, message: '请选择前端类型', trigger: 'change' }],
  genType: [{ required: true, message: '请选择生成方式', trigger: 'change' }],
  parentMenuId: [{ required: true, message: '请选择上级目录', trigger: 'change' }],
  genPath: [{
    validator: (_rule, value, callback) => {
      if (form.genType === '1' && !value) {
        callback(new Error('请填写自定义路径'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

/**
 * 树节点 id 统一为字符串，与 c7-tree-select value-type=string 一致，避免反显为纯数字 id。
 * @param {Array} nodes
 * @returns {Array}
 */
function normalizeMenuTreeForSelect(nodes) {
  if (!Array.isArray(nodes)) return []
  return nodes.map((node) => ({
    ...node,
    id: node.id != null ? String(node.id) : node.id,
    children: normalizeMenuTreeForSelect(node.children)
  }))
}

/**
 * @param {string} tableName
 * @returns {string}
 */
function deriveBusinessName(tableName) {
  if (!tableName) return ''
  const raw = tableName.startsWith('sys_') ? tableName.slice(4) : tableName
  const parts = raw.split('_').filter(Boolean)
  if (!parts.length) return ''
  return parts
    .map((p, i) => (i === 0 ? p.toLowerCase() : p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()))
    .join('')
}

/**
 * @param {Record<string, unknown>} defaults
 */
function applyDefaults(defaults) {
  if (!form.packageName && defaults.packageName) form.packageName = defaults.packageName
  if (!form.moduleName && defaults.moduleName) form.moduleName = defaults.moduleName
  if (!form.functionAuthor && defaults.functionAuthor) form.functionAuthor = defaults.functionAuthor
  if (!form.tplCategory && defaults.tplCategory) form.tplCategory = defaults.tplCategory
  if (!form.tplWebType) form.tplWebType = defaults.tplWebType || 'c7'
  if (!form.genType) form.genType = '0'
  if ((form.parentMenuId == null || form.parentMenuId === '') && defaults.parentMenuId != null) {
    form.parentMenuId = String(defaults.parentMenuId)
  }
  if (!form.businessName) form.businessName = deriveBusinessName(form.tableName)
  if (!form.functionName) form.functionName = form.tableComment || form.tableName
}

async function load() {
  const tableId = route.query.tableId
  if (!tableId) {
    ElMessage.error('缺少 tableId')
    return
  }
  const [tableRes, defaultsRes, menuRes] = await Promise.all([
    getGenTable(tableId),
    getGenDefaults(),
    treeselectMenu({ directoryOnly: true })
  ])
  menuTree.value = normalizeMenuTreeForSelect(menuRes.data || [])
  menuTreeKey.value += 1

  const info = tableRes.data?.info || {}
  Object.assign(form, info, { columns: tableRes.data?.columns || [] })
  if (form.parentMenuId != null && form.parentMenuId !== '') {
    form.parentMenuId = String(form.parentMenuId)
  }
  if (!form.tplWebType) {
    form.tplWebType = 'c7'
  }
  if (!form.genType) form.genType = '0'
  applyDefaults(defaultsRes.data || {})
  await nextTick()
}

async function submit() {
  await basicRef.value?.validate?.()
  const payload = {
    ...form,
    parentMenuId: form.parentMenuId != null && form.parentMenuId !== '' ? Number(form.parentMenuId) : null
  }
  saving.value = true
  try {
    await updateGenTable(payload)
    ElMessage.success('保存成功')
    goBack()
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push({ name: 'ToolGen' })
}

onMounted(load)
</script>

<style scoped>
.gen-edit-form {
  max-width: 960px;
  padding: 12px 0;
}
.gen-edit-actions {
  margin-top: 16px;
}
</style>
