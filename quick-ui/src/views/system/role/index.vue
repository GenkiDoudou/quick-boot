<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      row-key="roleId"
      export-default-file-name="role-export.xlsx"
      :show-index="false"
      :show-selection="true"
      :list-function="listFunction"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearchParam"
      :delete-function="batchDeleteFunction"
      :export-function="exportFunction"
      :show-add-button="true"
      :show-edit-button="true"
      :show-delete-button="true"
      :show-export-button="true"
      :on-add="openAdd"
      :on-edit="openEdit"
      :check-delete-success="() => true"
      rows-key="data.records"
      total-key="data.total"
    >
      <template #status="{ row }">
        <c7-dict-tag :model-value="row.status" :options="sys_normal_disable" dict-type="success" />
      </template>

      <template #dataScope="{ row }">
        <span>{{ dataScopeText(row.dataScope) }}</span>
      </template>

      <template #action="{ row }">
        <el-button link type="primary" @click="openEdit(row)" v-hasPermi="['system:role:edit']">修改</el-button>
        <el-button link type="primary" @click="openMenuDialog(row)" v-hasPermi="['system:role:edit']">
          菜单权限
        </el-button>
        <el-button link type="primary" @click="openAuthUserDialog(row)" v-hasPermi="['system:role:edit']">
          分配用户
        </el-button>
        <c7-button
          v-if="row.roleId !== 1"
          btn-type="delete"
          link
          confirm
          :confirm-message="`确认删除角色「${row.roleName}」吗？`"
          :click-function="() => removeRow(row)"
          v-hasPermi="['system:role:remove']"
        />
      </template>
    </C7JsonTable>

    <c7-dialog v-model="formVisible" :title="form.roleId ? '修改角色' : '新增角色'" :on-confirm="submitForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="如 common、admin" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="opt in sys_normal_disable" :key="opt.value" :label="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
        <template v-if="form.roleId !== 1">
          <el-form-item label="数据权限" prop="dataScope">
            <el-select v-model="form.dataScope" placeholder="请选择" style="width: 100%">
              <el-option v-for="opt in DATA_SCOPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.dataScope === '2'" label="自定义部门" prop="deptIds">
            <el-scrollbar max-height="260px">
              <el-tree
                v-if="formVisible && form.dataScope === '2'"
                :key="'form-dept-' + deptTreeMountKey"
                ref="formDeptTreeRef"
                :data="deptTreeData"
                show-checkbox
                node-key="id"
                :props="{ label: 'label', children: 'children' }"
                check-strictly
                default-expand-all
                :default-checked-keys="formDeptDefaultKeys"
              />
            </el-scrollbar>
          </el-form-item>
        </template>
      </el-form>
    </c7-dialog>

    <!-- 菜单权限：父子联动；提交时只收集叶子节点 id（与后端 expandGrantedMenuIds 向上扩展一致） -->
    <c7-dialog v-model="menuVisible" title="菜单权限" width="520px" :on-confirm="submitMenu">
      <el-scrollbar max-height="420px">
        <el-tree
          v-if="menuVisible"
          :key="menuTreeRenderKey"
          ref="menuTreeRef"
          :data="menuTreeData"
          show-checkbox
          node-key="id"
          :props="{ label: 'label', children: 'children' }"
          :check-strictly="false"
          default-expand-all
          :default-checked-keys="menuDefaultCheckedKeys"
        />
      </el-scrollbar>
    </c7-dialog>

    <c7-dialog v-model="authUserVisible" title="分配用户" width="900px" :footer="false">
      <el-tabs v-model="authTab" @tab-change="onAuthTabChange">
        <el-tab-pane label="已分配" name="allocated">
          <el-form :inline="true" class="mb-2">
            <el-form-item label="账号">
              <el-input v-model="allocatedQuery.userName" clearable placeholder="模糊查询" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="allocatedQuery.nickName" clearable placeholder="模糊查询" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAllocated(1)">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="allocatedRows" border size="small">
            <el-table-column prop="userName" label="账号" />
            <el-table-column prop="nickName" label="昵称" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="danger" @click="handleCancelOne(row)" v-hasPermi="['system:role:edit']">
                  取消授权
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="mt-2"
            background
            layout="prev, pager, next, ->, total"
            :total="allocatedTotal"
            v-model:current-page="allocatedQuery.pageNum"
            v-model:page-size="allocatedQuery.pageSize"
            @current-change="loadAllocated"
          />
        </el-tab-pane>
        <el-tab-pane label="未分配" name="unallocated">
          <el-form :inline="true" class="mb-2">
            <el-form-item label="账号">
              <el-input v-model="unallocatedQuery.userName" clearable placeholder="模糊查询" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="unallocatedQuery.nickName" clearable placeholder="模糊查询" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadUnallocated(1)">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table ref="unallocatedTableRef" :data="unallocatedRows" border size="small" row-key="userId">
            <el-table-column type="selection" width="48" reserve-selection />
            <el-table-column prop="userName" label="账号" />
            <el-table-column prop="nickName" label="昵称" />
          </el-table>
          <div class="mt-2">
            <el-button type="primary" @click="handleGrantSelected" v-hasPermi="['system:role:edit']">批量授权</el-button>
          </div>
          <el-pagination
            class="mt-2"
            background
            layout="prev, pager, next, ->, total"
            :total="unallocatedTotal"
            v-model:current-page="unallocatedQuery.pageNum"
            v-model:page-size="unallocatedQuery.pageSize"
            @current-change="loadUnallocated"
          />
        </el-tab-pane>
      </el-tabs>
    </c7-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useDict } from '@/utils/dict'
import { listTreeDept } from '@/api/system/dept'
import {
  addRole,
  cancelRoleUser,
  exportRole,
  getRole,
  grantRoleUsers,
  listRole,
  listRoleAllocatedUsers,
  listRoleUnallocatedUsers,
  removeRole,
  roleMenuTreeselect,
  updateRole,
  updateRoleMenu,
} from '@/api/system/role'

defineOptions({ name: 'SysRole' })

/** 数据范围选项（与后端 data_scope 枚举一致） */
const DATA_SCOPE_OPTIONS = [
  { label: '全部', value: '1' },
  { label: '自定义', value: '2' },
  { label: '本部门', value: '3' },
  { label: '本部门及以下', value: '4' },
  { label: '仅本人', value: '5' },
]

const { sys_normal_disable } = useDict('sys_normal_disable')

const tableRef = ref(null)
const formRef = ref(null)
const formDeptTreeRef = ref(null)
const formVisible = ref(false)
const deptTreeData = ref([])
const deptTreeMountKey = ref(0)
const formDeptDefaultKeys = ref([])
const form = ref({
  roleId: null,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: '',
  dataScope: '1',
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }],
  roleSort: [{ required: true, message: '请输入显示顺序', trigger: 'change' }],
}

const defaultSearchParam = {
  roleName: '',
  roleKey: '',
  status: '',
  createTimeRange: [],
}

const searchColumns = computed(() => [
  { prop: 'roleName', label: '角色名称', type: 'input', span: 8, props: { placeholder: '请输入', clearable: true } },
  { prop: 'roleKey', label: '权限字符', type: 'input', span: 8, props: { placeholder: '请输入', clearable: true } },
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    span: 8,
    options: sys_normal_disable.value,
    props: { placeholder: '请选择', clearable: true },
  },
  {
    prop: 'createTimeRange',
    label: '创建时间',
    type: 'daterange',
    span: 8,
    props: { 'value-format': 'YYYY-MM-DD', 'range-separator': '-', 'start-placeholder': '开始', 'end-placeholder': '结束' },
  },
])

const tableColumns = computed(() => [
  { prop: 'roleName', label: '角色名称', minWidth: 140 },
  { prop: 'roleKey', label: '权限字符', minWidth: 120 },
  { prop: 'roleSort', label: '顺序', width: 80 },
  { prop: 'dataScope', label: '数据权限', width: 120, columnType: 'slot', slotName: 'dataScope' },
  { prop: 'status', label: '状态', width: 100, columnType: 'slot', slotName: 'status' },
  { prop: 'createTime', label: '创建时间', width: 170 },
  { prop: 'action', label: '操作', columnType: 'slot', slotName: 'action', width: 300, fixed: 'right' },
])

function dataScopeText(v) {
  const m = DATA_SCOPE_OPTIONS.find((o) => o.value === v)
  return m ? m.label : v || '-'
}

/** 将接口返回的 id 统一为数字，避免与 el-tree 的 node-key 比较不一致 */
function normalizeLongIds(keys) {
  if (!Array.isArray(keys)) return []
  return keys
    .map((k) => {
      if (k == null || k === '') return null
      if (typeof k === 'number' && !Number.isNaN(k)) return k
      const n = Number(k)
      return Number.isNaN(n) ? null : n
    })
    .filter((k) => k != null && k >= 1)
}

/** 将菜单树节点 id 规范为数字，与 el-tree node-key、勾选状态比较一致 */
function normalizeMenuTreeNodes(nodes) {
  if (!Array.isArray(nodes)) return []
  return nodes.map((n) => ({
    ...n,
    id: n?.id == null || n.id === '' ? n?.id : Number(n.id),
    children: normalizeMenuTreeNodes(n.children || []),
  }))
}

/** 深拷贝菜单树，避免与 axios 响应式对象共享引用导致 el-tree 深度 watch 重置勾选 */
function cloneMenuTreeForRole(raw) {
  try {
    return JSON.parse(JSON.stringify(raw || []))
  } catch {
    return []
  }
}

function ensureDeptTree() {
  if (deptTreeData.value?.length) return Promise.resolve()
  return listTreeDept({}).then((res) => {
    deptTreeData.value = res.data || []
  })
}

function listFunction(params) {
  const [beginTime, endTime] = params.createTimeRange || []
  const req = { ...params, beginTime, endTime }
  delete req.createTimeRange
  return listRole(req)
}

function openAdd() {
  deptTreeMountKey.value += 1
  formDeptDefaultKeys.value = []
  form.value = {
    roleId: null,
    roleName: '',
    roleKey: '',
    roleSort: 0,
    status: '0',
    remark: '',
    dataScope: '1',
  }
  formVisible.value = true
  ensureDeptTree()
}

function openEdit(row) {
  if (!row) return
  getRole(row.roleId).then((res) => {
    const d = res.data || {}
    deptTreeMountKey.value += 1
    formDeptDefaultKeys.value = normalizeLongIds(d.deptIds || [])
    ensureDeptTree().then(() => {
      form.value = {
        roleId: d.roleId,
        roleName: d.roleName,
        roleKey: d.roleKey,
        roleSort: d.roleSort,
        status: d.status || '0',
        remark: d.remark || '',
        dataScope: d.dataScope || '1',
      }
      formVisible.value = true
    })
  })
}

function submitForm() {
  return new Promise((resolve, reject) => {
    formRef.value?.validate((valid) => {
      if (!valid) return reject(new Error('校验失败'))
      const payload = { ...form.value }
      if (payload.roleId === 1) {
        delete payload.dataScope
        delete payload.deptIds
      } else if (payload.dataScope === '2') {
        const checked = formDeptTreeRef.value?.getCheckedKeys(false) || []
        if (!checked.length) {
          ElMessage.warning('自定义数据权限请至少选择一个部门')
          return reject(new Error('validation'))
        }
        payload.deptIds = normalizeLongIds(checked)
      } else {
        payload.deptIds = []
      }
      const req = payload.roleId ? updateRole(payload) : addRole(payload)
      req
        .then(() => {
          ElMessage.success('操作成功')
          formVisible.value = false
          tableRef.value?.refreshData()
          resolve()
        })
        .catch(reject)
    })
  })
}

function removeRow(row) {
  return removeRole([row.roleId]).then(() => {
    ElMessage.success('删除成功')
    return tableRef.value?.refreshData()
  })
}

function batchDeleteFunction(ids) {
  if (ids?.includes(1)) {
    ElMessage.error('不能删除内置超级管理员角色')
    return Promise.reject(new Error('invalid'))
  }
  return removeRole(ids || []).then(() => {
    ElMessage.success('删除成功')
  })
}

function exportFunction(searchParam) {
  const req = { ...searchParam }
  const [beginTime, endTime] = req.createTimeRange || []
  req.beginTime = beginTime
  req.endTime = endTime
  delete req.createTimeRange
  return exportRole(req)
}

/** ---------- 菜单权限 ---------- */
const menuVisible = ref(false)
const menuTreeRef = ref(null)
const menuTreeData = ref([])
const menuTreeRenderKey = ref(0)
const menuRoleId = ref(null)
const menuDefaultCheckedKeys = ref([])

function openMenuDialog(row) {
  menuRoleId.value = row.roleId
  roleMenuTreeselect(row.roleId).then((res) => {
    const d = res.data || {}
    menuDefaultCheckedKeys.value = normalizeLongIds(d.checkedKeys || [])
    menuTreeData.value = normalizeMenuTreeNodes(cloneMenuTreeForRole(d.menus))
    menuTreeRenderKey.value += 1
    menuVisible.value = true
  })
}

function submitMenu() {
  return nextTick().then(() => {
    const tree = menuTreeRef.value
    if (!tree || !menuRoleId.value) return Promise.reject(new Error('no tree'))
    /**
     * 仅提交「勾选叶子」节点 id：后端 expandGrantedMenuIds 会从每条记录向上补祖先，
     * 与 RuoYi 习惯一致；getCheckedKeys(false)+半选父级易与子级取消勾选不同步。
     */
    const leafKeys = typeof tree.getCheckedKeys === 'function' ? tree.getCheckedKeys(true) || [] : []
    const menuIds = [...new Set(normalizeLongIds(leafKeys))]
    return updateRoleMenu({ roleId: menuRoleId.value, menuIds }).then(() => {
      ElMessage.success('菜单权限已保存')
      menuVisible.value = false
      tableRef.value?.refreshData()
    })
  })
}

/** ---------- 分配用户 ---------- */
const authUserVisible = ref(false)
const authTab = ref('allocated')
const authRoleId = ref(null)
const unallocatedTableRef = ref(null)

const allocatedQuery = ref({ pageNum: 1, pageSize: 10, roleId: null, userName: '', nickName: '' })
const allocatedRows = ref([])
const allocatedTotal = ref(0)

const unallocatedQuery = ref({ pageNum: 1, pageSize: 10, roleId: null, userName: '', nickName: '' })
const unallocatedRows = ref([])
const unallocatedTotal = ref(0)

function openAuthUserDialog(row) {
  authRoleId.value = row.roleId
  allocatedQuery.value = { pageNum: 1, pageSize: 10, roleId: row.roleId, userName: '', nickName: '' }
  unallocatedQuery.value = { pageNum: 1, pageSize: 10, roleId: row.roleId, userName: '', nickName: '' }
  authTab.value = 'allocated'
  authUserVisible.value = true
  loadAllocated(1)
}

function onAuthTabChange(name) {
  if (name === 'unallocated') loadUnallocated(1)
  if (name === 'allocated') loadAllocated(1)
}

function loadAllocated(page) {
  if (page) allocatedQuery.value.pageNum = page
  return listRoleAllocatedUsers(allocatedQuery.value).then((res) => {
    const d = res.data || {}
    allocatedRows.value = d.records || []
    allocatedTotal.value = Number(d.total || 0)
  })
}

function loadUnallocated(page) {
  if (page) unallocatedQuery.value.pageNum = page
  return listRoleUnallocatedUsers(unallocatedQuery.value).then((res) => {
    const d = res.data || {}
    unallocatedRows.value = d.records || []
    unallocatedTotal.value = Number(d.total || 0)
  })
}

function handleCancelOne(row) {
  return cancelRoleUser({ roleId: authRoleId.value, userId: row.userId }).then(() => {
    ElMessage.success('已取消授权')
    loadAllocated(1)
  })
}

function handleGrantSelected() {
  const rows = unallocatedTableRef.value?.getSelectionRows() || []
  if (!rows.length) {
    ElMessage.warning('请先勾选用户')
    return
  }
  const userIds = rows.map((r) => r.userId)
  return grantRoleUsers({ roleId: authRoleId.value, userIds }).then(() => {
    ElMessage.success('授权成功')
    unallocatedTableRef.value?.clearSelection()
    loadUnallocated(unallocatedQuery.value.pageNum)
    loadAllocated(1)
  })
}
</script>

<style scoped>
.mb-2 {
  margin-bottom: 8px;
}
.mt-2 {
  margin-top: 8px;
}
</style>
