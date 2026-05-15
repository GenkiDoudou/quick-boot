<template>
  <div class="app-container">
    <!-- 工具栏：增删改、树展开折叠 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" @click="handleEdit">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" @click="handleDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpand">{{ expandAll ? '折叠' : '展开' }}</el-button>
      </el-col>
    </el-row>

    <!-- 部门树：节点展示名称 + 可选负责人 -->
    <el-tree
      :key="treeRenderKey"
      ref="treeRef"
      :data="deptTree"
      :props="{ label: 'deptName', children: 'children' }"
      node-key="deptId"
      :default-expand-all="expandAll"
      highlight-current
      @node-click="handleNodeClick"
    >
      <template #default="{ data }">
        <span class="dept-tree-node">
          <span>{{ data.deptName }}</span>
          <span v-if="data.leader" class="leader">（{{ data.leader }}）</span>
        </span>
      </template>
    </el-tree>

    <!-- 新增/编辑：上级为 el-tree-select，含虚拟根 deptId=0 与后端 parentId=0 对齐 -->
    <el-dialog v-model="dialogOpen" :title="dialogTitle" width="520px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            placeholder="选择上级部门（空为根）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="可选" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * 系统管理 — 部门维护页。
 * - 数据：@/api/system/dept（树 treeselect、详情、增删改）
 * - 路由：/system/dept（见 src/router/index.js）
 */
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import type ElTree from 'element-plus/es/components/tree/index'
import { listTreeDept, getDept, addDept, updateDept, delDept } from '@/api/system/dept'

/** 与后端 DeptTreeVo / SysDept 字段对齐的树节点 */
interface DeptNode {
  deptId: number
  parentId?: number
  deptName: string
  orderNum?: number
  leader?: string | null
  children?: DeptNode[]
}

const treeRef = ref<InstanceType<typeof ElTree>>()
const formRef = ref<FormInstance>()
const deptTree = ref<DeptNode[]>([])
const expandAll = ref(true)
/** 切换展开/折叠时重挂树，避免依赖 el-tree 内部 store */
const treeRenderKey = ref(0)
const dialogOpen = ref(false)
const isEdit = ref(false)
const currentDeptId = ref<number | null>(null)

const form = reactive({
  deptId: undefined as number | undefined,
  parentId: undefined as number | undefined,
  deptName: '',
  orderNum: 0,
  leader: '' as string | undefined
})

const dialogTitle = computed(() => (isEdit.value ? '修改部门' : '新增部门'))

const rules: FormRules = {
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }]
}

/** 上级选择：带虚拟根 0，避免 el-tree-select 与后端 parentId=0 对齐 */
const parentOptions = computed(() => {
  const root: DeptNode = { deptId: 0, deptName: '顶级部门', children: deptTree.value }
  return [root]
})

/** 拉取部门树并写入 deptTree */
async function loadTree() {
  const res = await listTreeDept()
  deptTree.value = (res as { data?: DeptNode[] }).data ?? []
}

/** 切换全部展开/折叠（通过 treeRenderKey 重挂组件） */
function toggleExpand() {
  expandAll.value = !expandAll.value
  treeRenderKey.value += 1
}

/** 记录当前选中部门，供修改/删除 */
function handleNodeClick(data: DeptNode) {
  currentDeptId.value = data.deptId
}

/** 关闭弹窗时由 el-dialog @closed 调用，清空表单模型 */
function resetForm() {
  form.deptId = undefined
  form.parentId = undefined
  form.deptName = ''
  form.orderNum = 0
  form.leader = ''
  formRef.value?.resetFields()
}

/** 打开新增；若树上已选中节点则默认作为父级 */
function handleAdd() {
  isEdit.value = false
  resetForm()
  if (currentDeptId.value != null) {
    form.parentId = currentDeptId.value
  }
  dialogOpen.value = true
}

/** 打开编辑并回填详情 */
async function handleEdit() {
  if (currentDeptId.value == null) {
    ElMessage.warning('请先在树中选择一个部门')
    return
  }
  isEdit.value = true
  resetForm()
  const res = await getDept(currentDeptId.value)
  const row = (res as { data?: DeptNode }).data
  if (!row) return
  form.deptId = row.deptId
  form.parentId = row.parentId === 0 ? 0 : row.parentId
  form.deptName = row.deptName
  form.orderNum = row.orderNum ?? 0
  form.leader = row.leader ?? ''
  dialogOpen.value = true
}

/** 删除前确认，成功后刷新树 */
async function handleDelete() {
  if (currentDeptId.value == null) {
    ElMessage.warning('请先在树中选择一个部门')
    return
  }
  await ElMessageBox.confirm('是否确认删除该部门？', '提示', { type: 'warning' })
  await delDept(currentDeptId.value)
  ElMessage.success('删除成功')
  currentDeptId.value = null
  await loadTree()
}

/** 校验通过后提交新增或修改，关闭弹窗并刷新树（无整页刷新） */
async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate()
  const payload = {
    deptId: form.deptId,
    parentId: form.parentId === undefined || form.parentId === null ? 0 : form.parentId,
    deptName: form.deptName.trim(),
    orderNum: form.orderNum ?? 0,
    leader: form.leader?.trim() || undefined
  }
  if (isEdit.value) {
    await updateDept(payload)
    ElMessage.success('修改成功')
  } else {
    await addDept(payload)
    ElMessage.success('新增成功')
  }
  dialogOpen.value = false
  await loadTree()
}

onMounted(() => {
  loadTree()
})
</script>

<script lang="ts">
export default {
  name: 'SystemDept'
}
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
.dept-tree-node {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}
.leader {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
