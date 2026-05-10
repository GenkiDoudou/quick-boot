<template>
  <c7-dialog v-model="visible" :title="form.deptId ? '修改部门' : '新增部门'" width="680px" :on-confirm="submit">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="dept-dialog-form">
      <el-form-item label="上级部门" prop="parentId">
        <c7-tree-select
          v-model="form.parentId"
          :data="treeData"
          value-key="id"
          label-key="label"
          children-key="children"
          :check-strictly="true"
          :default-expand-all="true"
        />
      </el-form-item>
      <el-form-item label="部门名称" prop="deptName"><el-input v-model="form.deptName" /></el-form-item>
      <el-form-item label="显示排序" prop="orderNum"><el-input-number v-model="form.orderNum" :min="0" :max="9999" /></el-form-item>
      <el-form-item label="负责人" prop="leader"><el-input v-model="form.leader" /></el-form-item>
      <el-form-item label="联系电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="d in sys_normal_disable" :key="d.value" :label="d.value">{{ d.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" /></el-form-item>
    </el-form>
  </c7-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addDept, getDept, listTreeDept, updateDept } from '@/api/system/dept'
import { useDict } from '@/utils/dict'

const emit = defineEmits(['success'])
const { sys_normal_disable } = useDict('sys_normal_disable')

const visible = ref(false)
const formRef = ref(null)
const treeData = ref([])
const form = ref({ deptId: null, parentId: -1, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0', remark: '' })

const rules = {
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function open(payload = {}) {
  visible.value = true
  form.value = { deptId: null, parentId: payload.parentId ?? -1, deptName: '', orderNum: 0, leader: '', phone: '', email: '', status: '0', remark: '' }
  listTreeDept().then(res => {
    treeData.value = [{ id: -1, label: '顶级部门', children: res.data || [] }]
  })
  if (payload.deptId) {
    getDept(payload.deptId).then(res => {
      form.value = { ...form.value, ...(res.data || {}) }
    })
  }
}

function submit() {
  return new Promise((resolve, reject) => {
    formRef.value.validate(valid => {
      if (!valid) {
        reject(new Error('表单校验未通过'))
        return
      }
      const req = form.value.deptId ? updateDept(form.value) : addDept(form.value)
      req.then(() => {
        ElMessage.success(form.value.deptId ? '修改成功' : '新增成功')
        emit('success')
        resolve()
      }).catch(reject)
    })
  })
}

defineExpose({ open })
</script>

<style scoped>
.dept-dialog-form {
  --dept-primary: #0a2463;
  --dept-accent: #409eff;
  --dept-border: #dce3eb;
  --dept-text: #233243;
  --dept-text-muted: #4f6175;
  font-family: "PingFang SC", "Helvetica Neue", sans-serif;
  color: var(--dept-text);
}

.dept-dialog-form :deep(.el-form-item__label) {
  color: var(--dept-text-muted);
  font-weight: 600;
}

.dept-dialog-form :deep(.el-input__wrapper),
.dept-dialog-form :deep(.el-textarea__inner),
.dept-dialog-form :deep(.el-input-number) {
  border-color: var(--dept-border);
}

.dept-dialog-form :deep(.el-input__wrapper.is-focus),
.dept-dialog-form :deep(.el-textarea__inner:focus),
.dept-dialog-form :deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--dept-accent) inset;
}

.dept-dialog-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  border-color: var(--dept-accent);
  background: var(--dept-accent);
}

.dept-dialog-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--dept-primary);
  font-weight: 600;
}
</style>
