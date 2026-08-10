<template>
  <div class="app-container">
    <C7JsonTable
      ref="tableRef"
      :list-function="pageRole"
      :table-columns="tableColumns"
      :search-columns="searchColumns"
      :default-search-param="defaultSearch"
      :show-delete-button="true"
      :delete-function="removeRole"
      row-key="roleId"
      column-setting-key="sys-role"
      :show-add-button="true"
      :add-button-permi="['system:role:add']"
      :delete-button-permi="['system:role:remove']"
      :on-add="openAdd"
      :export-function="exportRole"
      :export-button-permi="['system:role:export']"
      export-default-file-name="role.xlsx"
      :import-function="importRole"
      :import-template-download-fn="downloadRoleImportTemplate"
      :import-button-permi="['system:role:import']"
      import-template-file-name="role-import-template.xlsx"
      :show-import-button="true"
    >
      <template #action="{ row }">
        <el-button link type="primary" v-hasPermi="['system:role:edit']" @click="openEdit(row)">修改</el-button>
        <el-button link type="primary" v-hasPermi="['system:role:menu']" @click="openMenuAuth(row)">菜单权限</el-button>
        <el-button link type="primary" v-hasPermi="['system:role:authUser']" @click="openUserAuth(row)">分配用户</el-button>
        <el-button link type="danger" v-hasPermi="['system:role:remove']" @click="removeRow(row)">删除</el-button>
      </template>
    </C7JsonTable>

    <C7Dialog
      v-model="formVisible"
      :title="isAdd ? '新增角色' : '修改角色'"
      width="560px"
      :on-confirm="submitForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="64"/>
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input
            v-model="form.roleKey"
            placeholder="如 admin、common"
            maxlength="64"
            :disabled="!isAdd && form.roleId === 1"
          />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" :controls="true" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <C7Select v-model="form.status" :data-list="sys_normal_disable" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="可选备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </C7Dialog>

    <C7Dialog
      v-model="menuVisible"
      title="分配菜单权限"
      width="480px"
      :on-confirm="submitMenuAuth"
    >
      <div class="menu-tree-toolbar">
        <el-checkbox v-model="menuExpand">展开/折叠</el-checkbox>
        <el-checkbox v-model="menuNodeAll">全选/全不选</el-checkbox>
      </div>
      <el-tree
        ref="menuTreeRef"
        class="menu-tree"
        :data="menuOptions"
        show-checkbox
        node-key="menuId"
        empty-text="加载中，请稍候"
        :props="{ label: 'menuName', children: 'children' }"
        :default-expand-all="menuExpand"
      />
    </C7Dialog>

    <C7Dialog v-model="userVisible" title="分配用户" width="860px">
      <el-tabs v-model="userTab">
        <el-tab-pane label="已分配" name="allocated">
          <div class="user-toolbar">
            <el-input
              v-model="allocatedQuery.userName"
              clearable
              placeholder="用户名"
              style="width: 180px"
              @keyup.enter="loadAllocated"
            />
            <el-button type="primary" @click="loadAllocated">搜索</el-button>
            <el-button
              type="danger"
              :disabled="!allocatedSelection.length"
              @click="cancelSelected"
            >批量取消授权
            </el-button>
          </div>
          <el-table
            v-loading="allocatedLoading"
            :data="allocatedRows"
            border
            row-key="userId"
            @selection-change="(rows) => (allocatedSelection = rows)"
          >
            <el-table-column type="selection" width="48"/>
            <el-table-column prop="userName" label="用户名" min-width="120"/>
            <el-table-column prop="nickName" label="昵称" min-width="120"/>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" @click="cancelOne(row)">取消授权</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              v-model:current-page="allocatedPage.current"
              v-model:page-size="allocatedPage.size"
              :total="allocatedPage.total"
              layout="total, prev, pager, next"
              @current-change="loadAllocated"
              @size-change="loadAllocated"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane label="未分配" name="unallocated">
          <div class="user-toolbar">
            <el-input
              v-model="unallocatedQuery.userName"
              clearable
              placeholder="用户名"
              style="width: 180px"
              @keyup.enter="loadUnallocated"
            />
            <el-button type="primary" @click="loadUnallocated">搜索</el-button>
            <el-button
              type="primary"
              :disabled="!unallocatedSelection.length"
              @click="grantSelected"
            >批量授权
            </el-button>
          </div>
          <el-table
            v-loading="unallocatedLoading"
            :data="unallocatedRows"
            border
            row-key="userId"
            @selection-change="(rows) => (unallocatedSelection = rows)"
          >
            <el-table-column type="selection" width="48"/>
            <el-table-column prop="userName" label="用户名" min-width="120"/>
            <el-table-column prop="nickName" label="昵称" min-width="120"/>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <C7DictTag :model-value="row.status" :options="sys_normal_disable" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="grantOne(row)">授权</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
              v-model:current-page="unallocatedPage.current"
              v-model:page-size="unallocatedPage.size"
              :total="unallocatedPage.total"
              layout="total, prev, pager, next"
              @current-change="loadUnallocated"
              @size-change="loadUnallocated"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="primary" @click="userVisible = false">关闭</el-button>
      </template>
    </C7Dialog>
  </div>
</template>

<script setup>
/**
 * 角色管理列表页：CRUD、菜单全量授权、角色内用户授权。
 * 交互对齐 oauthClient/index.vue（C7JsonTable + C7Dialog）。
 */
import {ElMessage, ElMessageBox} from 'element-plus'
import {useDict} from '@/utils/dict'
import {
  pageRole,
  getRole,
  addRole,
  updateRole,
  removeRole,
  updateRoleMenu,
  roleMenuTreeselect,
  allocatedUserList,
  unallocatedUserList,
  authUserSelectAll,
  authUserCancel,
  authUserCancelAll,
  exportRole,
  downloadRoleImportTemplate,
  importRole
} from '@/api/system/role'
import { toApiLongIds } from '@/utils/ruoyi'

const {sys_normal_disable} = useDict('sys_normal_disable')

const tableRef = ref(null)
const formRef = ref(null)
const menuTreeRef = ref(null)

const formVisible = ref(false)
const menuVisible = ref(false)
const userVisible = ref(false)
const isAdd = ref(true)

const defaultSearch = {
  roleName: '',
  roleKey: '',
  status: ''
}

const searchColumns = computed(() => [
  {prop: 'roleName', label: '角色名称', type: 'input', order: 1, span: 6},
  {prop: 'roleKey', label: '权限字符', type: 'input', order: 2, span: 6},
  {
    prop: 'status',
    label: '状态',
    type: 'select',
    order: 3,
    span: 6,
    options: [
      {label: '全部', value: ''},
      ...(sys_normal_disable.value || [])
    ]
  }
])

const tableColumns = computed(() => [
  {prop: 'roleName', label: '角色名称', columnType: 'text', minWidth: 120},
  {prop: 'roleKey', label: '权限字符', columnType: 'text', minWidth: 120},
  {prop: 'roleSort', label: '显示顺序', columnType: 'text', width: 100},
  {
    prop: 'status',
    label: '状态',
    columnType: 'tag',
    width: 90,
    options: sys_normal_disable.value || []
  },
  {prop: 'createTime', label: '创建时间', columnType: 'text', minWidth: 160},
  {prop: 'action', label: '操作', columnType: 'slot', width: 280, fixed: 'right', slotName: 'action'}
])

const form = reactive({
  roleId: undefined,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: '0',
  remark: ''
})

const rules = {
  roleName: [{required: true, message: '请输入角色名称', trigger: 'blur'}],
  roleKey: [{required: true, message: '请输入权限字符', trigger: 'blur'}],
  roleSort: [{required: true, message: '请输入显示顺序', trigger: 'change'}]
}

const menuRoleId = ref(null)
const menuOptions = ref([])
const menuExpand = ref(true)
const menuNodeAll = ref(false)
const menuCheckedKeys = ref([])

watch(menuExpand, (val) => {
  const nodes = menuTreeRef.value?.store?.nodesMap
  if (!nodes) return
  for (const key of Object.keys(nodes)) {
    nodes[key].expanded = val
  }
})

watch(menuNodeAll, (val) => {
  if (!menuTreeRef.value) return
  if (val) {
    menuTreeRef.value.setCheckedNodes(menuOptions.value)
  } else {
    menuTreeRef.value.setCheckedKeys([])
  }
})

const userRoleId = ref(null)
const userTab = ref('allocated')
const allocatedLoading = ref(false)
const unallocatedLoading = ref(false)
const allocatedRows = ref([])
const unallocatedRows = ref([])
const allocatedSelection = ref([])
const unallocatedSelection = ref([])
const allocatedQuery = reactive({userName: ''})
const unallocatedQuery = reactive({userName: ''})
const allocatedPage = reactive({current: 1, size: 10, total: 0})
const unallocatedPage = reactive({current: 1, size: 10, total: 0})

watch(userTab, (name) => {
  if (name === 'allocated') loadAllocated()
  else loadUnallocated()
})

function resetForm() {
  form.roleId = undefined
  form.roleName = ''
  form.roleKey = ''
  form.roleSort = 0
  form.status = '0'
  form.remark = ''
}

function openAdd() {
  isAdd.value = true
  resetForm()
  formVisible.value = true
}

async function openEdit(row) {
  isAdd.value = false
  try {
    const res = await getRole(row.roleId)
    const data = res.data || row
    form.roleId = data.roleId
    form.roleName = data.roleName || ''
    form.roleKey = data.roleKey || ''
    form.roleSort = data.roleSort ?? 0
    form.status = data.status != null ? String(data.status) : '0'
    form.remark = data.remark || ''
    formVisible.value = true
  } catch {
    /* request 已提示 */
  }
}

function removeRow(row) {
  ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', {type: 'warning'})
    .then(async () => {
      await removeRole([row.roleId])
      ElMessage.success('已删除')
      tableRef.value?.refreshData?.()
    })
    .catch(() => {
    })
}

function submitForm() {
  return new Promise((resolve, reject) => {
    formRef.value?.validate(async (ok) => {
      if (!ok) {
        reject(new Error('校验未通过'))
        return
      }
      try {
        const payload = {
          roleId: form.roleId,
          roleName: form.roleName,
          roleKey: form.roleKey,
          roleSort: form.roleSort,
          status: form.status,
          remark: form.remark
        }
        if (isAdd.value) {
          await addRole(payload)
          ElMessage.success('创建成功')
        } else {
          await updateRole(payload)
          ElMessage.success('更新成功')
        }
        tableRef.value?.refreshData?.()
        resolve()
      } catch (e) {
        reject(e)
      }
    })
  })
}

/**
 * 打开菜单授权弹窗并加载树与已选 keys。
 * @param {{ roleId: number }} row
 */
async function openMenuAuth(row) {
  menuRoleId.value = row.roleId
  menuNodeAll.value = false
  menuExpand.value = true
  menuOptions.value = []
  menuCheckedKeys.value = []
  menuVisible.value = true
  try {
    const res = await roleMenuTreeselect(row.roleId)
    const data = res.data || {}
    menuOptions.value = data.menus || []
    menuCheckedKeys.value = data.checkedKeys || []
    await nextTick()
    // 只勾选叶子：若把父节点也 setCheckedKeys，在 check-strictly=false 下会级联勾上全部子节点，导致「取消后保存再开又勾上」
    menuTreeRef.value?.setCheckedKeys(filterLeafMenuIds(menuCheckedKeys.value, menuOptions.value))
  } catch {
    /* request 已提示 */
  }
}

/**
 * 从树中收集叶子 menuId，并与已存 keys 求交（字符串比较，避免类型不一致）。
 * @param {Array<string|number>} keys
 * @param {Array} tree
 * @returns {Array<string|number>}
 */
function filterLeafMenuIds(keys, tree) {
  const leafSet = new Set()
  const walk = (nodes) => {
    if (!Array.isArray(nodes)) return
    for (const n of nodes) {
      if (n?.children?.length) {
        walk(n.children)
      } else if (n?.menuId != null) {
        leafSet.add(String(n.menuId))
      }
    }
  }
  walk(tree)
  return (keys || []).filter((id) => leafSet.has(String(id)))
}

function collectCheckedMenuIds() {
  const checked = menuTreeRef.value?.getCheckedKeys?.(false) || []
  const half = menuTreeRef.value?.getHalfCheckedKeys?.() || []
  // 禁止 Number(雪花ID)：会丢精度，导致新建目录勾选后写不进 sys_role_menu
  return toApiLongIds([...checked, ...half])
}

function submitMenuAuth() {
  return updateRoleMenu({
    roleId: menuRoleId.value,
    menuIds: collectCheckedMenuIds()
  }).then(() => {
    ElMessage.success('菜单权限已保存')
  })
}

/**
 * 打开用户授权弹窗。
 * @param {{ roleId: number }} row
 */
function openUserAuth(row) {
  userRoleId.value = row.roleId
  userTab.value = 'allocated'
  allocatedQuery.userName = ''
  unallocatedQuery.userName = ''
  allocatedPage.current = 1
  unallocatedPage.current = 1
  userVisible.value = true
  loadAllocated()
}

async function loadAllocated() {
  if (!userRoleId.value) return
  allocatedLoading.value = true
  try {
    const res = await allocatedUserList({
      roleId: userRoleId.value,
      current: allocatedPage.current,
      size: allocatedPage.size,
      param: {userName: allocatedQuery.userName || undefined}
    })
    const page = res.data || {}
    allocatedRows.value = page.records || []
    allocatedPage.total = Number(page.total) || 0
  } catch {
    allocatedRows.value = []
    allocatedPage.total = 0
  } finally {
    allocatedLoading.value = false
  }
}

async function loadUnallocated() {
  if (!userRoleId.value) return
  unallocatedLoading.value = true
  try {
    const res = await unallocatedUserList({
      roleId: userRoleId.value,
      current: unallocatedPage.current,
      size: unallocatedPage.size,
      param: {userName: unallocatedQuery.userName || undefined}
    })
    const page = res.data || {}
    unallocatedRows.value = page.records || []
    unallocatedPage.total = Number(page.total) || 0
  } catch {
    unallocatedRows.value = []
    unallocatedPage.total = 0
  } finally {
    unallocatedLoading.value = false
  }
}

async function grantOne(row) {
  await authUserSelectAll({roleId: userRoleId.value, userIds: [row.userId]})
  ElMessage.success('已授权')
  await Promise.all([loadAllocated(), loadUnallocated()])
}

async function grantSelected() {
  const ids = unallocatedSelection.value.map((r) => r.userId)
  if (!ids.length) return
  await authUserSelectAll({roleId: userRoleId.value, userIds: ids})
  ElMessage.success('已批量授权')
  await Promise.all([loadAllocated(), loadUnallocated()])
}

async function cancelOne(row) {
  await authUserCancel({roleId: userRoleId.value, userIds: [row.userId]})
  ElMessage.success('已取消授权')
  await Promise.all([loadAllocated(), loadUnallocated()])
}

async function cancelSelected() {
  const ids = allocatedSelection.value.map((r) => r.userId)
  if (!ids.length) return
  await authUserCancelAll({roleId: userRoleId.value, userIds: ids})
  ElMessage.success('已批量取消')
  await Promise.all([loadAllocated(), loadUnallocated()])
}
</script>

<style scoped>
.menu-tree-toolbar {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.menu-tree {
  max-height: 420px;
  overflow: auto;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px;
}

.user-toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
