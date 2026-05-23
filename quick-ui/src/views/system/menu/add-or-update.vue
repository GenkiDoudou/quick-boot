<template>

  <c7-dialog v-model="visible" :title="form.menuId ? '修改菜单' : '新增菜单'" width="880px" :on-confirm="submit">

    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="menu-form">

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item label="上级菜单" prop="parentId">

            <c7-tree-select

              v-model="form.parentId"

              :data-list="treeData"

              value-key="id"

              label-key="label"

              children-key="children"

              :check-strictly="true"

              :default-expand-all="true"

              value-type="string"

            />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item label="菜单类型" prop="menuType">

            <el-radio-group v-model="form.menuType" @change="onTypeChange">

              <el-radio label="M">目录</el-radio>

              <el-radio label="C">菜单</el-radio>

              <el-radio label="F">按钮</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="12">

          <el-form-item :label="form.menuType === 'F' ? '按钮名称' : '菜单名称'" prop="menuName">

            <el-input v-model="form.menuName" />

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item label="显示排序" prop="orderNum">

            <el-input-number v-model="form.orderNum" :min="0" :max="9999" style="width: 100%" />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">

        <el-col :span="12">

          <el-form-item label="菜单图标" prop="icon">

            <el-popover

              v-model:visible="iconPopoverVisible"

              placement="bottom-start"

              :width="540"

              trigger="click"

            >

              <template #reference>

                <el-input

                  v-model="form.icon"

                  placeholder="点击选择图标"

                  readonly

                  @blur="showSelectIcon"

                >

                  <template #prefix>

                    <svg-icon

                      v-if="form.icon"

                      :icon-class="form.icon"

                      style="width: 16px; height: 16px"

                    />

                    <el-icon v-else style="width: 16px; height: 16px"><Search /></el-icon>

                  </template>

                </el-input>

              </template>

              <icon-select ref="iconSelectRef" :active-icon="form.icon" @selected="onIconSelected" />

            </el-popover>

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item label="路由地址" prop="path">

            <el-input v-model="form.path" placeholder="如 system 或 /system" />

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'C' || form.menuType === 'M'" :gutter="20">

        <el-col :span="12">

          <el-form-item label="组件路径" prop="component">

            <el-input

              v-model="form.component"

              :placeholder="form.menuType === 'M' ? '目录一般为 Layout' : '如 system/menu/index'"

            />

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item label="路由名称" prop="routeName"><el-input v-model="form.routeName" /></el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">

        <el-col :span="12">

          <el-form-item label="是否外链" prop="isFrame">

            <el-radio-group v-model="form.isFrame">

              <el-radio label="0">否</el-radio>

              <el-radio label="1">是</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

        <el-col :span="12">

          <el-form-item label="显示状态" prop="visible">

            <el-radio-group v-model="form.visible">

              <el-radio label="0">显示</el-radio>

              <el-radio label="1">隐藏</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row v-if="form.menuType === 'C' || form.menuType === 'M'" :gutter="20">

        <el-col :span="12">

          <el-form-item label="是否缓存" prop="isCache">

            <el-radio-group v-model="form.isCache">

              <el-radio label="0">缓存</el-radio>

              <el-radio label="1">不缓存</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item label="权限字符" prop="perms">

            <div class="perms-dynamic">

              <div v-for="(_, index) in permsList" :key="index" class="perms-dynamic__row">

                <el-input

                  v-model="permsList[index]"

                  placeholder="如 system:menu:list"

                  clearable

                />

                <el-button type="danger" link @click="removePermsRow(index)">删除</el-button>

              </div>

              <el-button type="primary" link @click="addPermsRow">添加权限</el-button>

              <div class="perms-dynamic__hint">保存时合并为英文逗号并去重；中文逗号会转为英文逗号。</div>

            </div>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="12">

          <el-form-item :label="form.menuType === 'F' ? '按钮状态' : '菜单状态'" prop="status">

            <el-radio-group v-model="form.status">

              <el-radio v-for="d in sys_normal_disable" :key="d.value" :label="d.value">{{ d.label }}</el-radio>

            </el-radio-group>

          </el-form-item>

        </el-col>

      </el-row>

      <el-row :gutter="20">

        <el-col :span="24">

          <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>

        </el-col>

      </el-row>

    </el-form>

  </c7-dialog>

</template>



<script setup>

import { computed, nextTick, ref } from 'vue'

import { ElMessage } from 'element-plus'

import { Search } from '@element-plus/icons-vue'

import { addMenu, getMenu, treeselectMenu, updateMenu } from '@/api/system/menu'

import { useDict } from '@/utils/dict'

import IconSelect from '@/components/IconSelect/index.vue'



const ROOT = '-1'



const emit = defineEmits(['success'])

const { sys_normal_disable } = useDict('sys_normal_disable')



const visible = ref(false)

const formRef = ref(null)

const treeData = ref([])

const iconPopoverVisible = ref(false)

const iconSelectRef = ref(null)



const defaultForm = () => ({

  menuId: null,

  parentId: ROOT,

  menuType: 'M',

  menuName: '',

  orderNum: 0,

  path: '',

  component: 'Layout',

  query: '',

  routeName: '',

  isFrame: '0',

  isCache: '0',

  visible: '0',

  status: '0',

  perms: '',

  icon: '',

  remark: '',

})



const form = ref(defaultForm())

/** 权限字符多行编辑，提交前同步到 form.perms */
const permsList = ref([''])



function reqRule(message, trigger = 'blur') {

  return [{ required: true, message, trigger }]

}



const rules = computed(() => {

  const t = form.value.menuType

  const permsFieldRules = [

    { max: 500, message: '权限字符总长度不超过 500', trigger: 'blur' },

  ]

  if (t === 'F') {

    permsFieldRules.unshift({

      validator: (_rule, value, callback) => {

        const s = normalizePermsStr(typeof value === 'string' ? value : '')

        if (!s) {

          callback(new Error('请填写权限字符'))

        } else {

          callback()

        }

      },

      trigger: 'blur',

    })

  }



  const base = {

    parentId: reqRule('请选择上级菜单', 'change'),

    menuType: reqRule('请选择类型', 'change'),

    perms: permsFieldRules,

  }



  if (t === 'M') {

    return {

      ...base,

      menuName: reqRule('请输入菜单名称'),

      orderNum: reqRule('请输入显示排序'),

      path: reqRule('请输入路由地址'),

      isFrame: reqRule('请选择是否外链', 'change'),

      visible: reqRule('请选择显示状态', 'change'),

      isCache: reqRule('请选择是否缓存', 'change'),

      status: reqRule('请选择菜单状态', 'change'),

    }

  }



  if (t === 'C') {

    return {

      ...base,

      menuName: reqRule('请输入菜单名称'),

      orderNum: reqRule('请输入显示排序'),

      path: reqRule('请输入路由地址'),

      component: reqRule('请输入组件路径'),

      isFrame: reqRule('请选择是否外链', 'change'),

      visible: reqRule('请选择显示状态', 'change'),

      isCache: reqRule('请选择是否缓存', 'change'),

      status: reqRule('请选择菜单状态', 'change'),

    }

  }



  if (t === 'F') {

    return {

      ...base,

      menuName: reqRule('请输入按钮名称'),

      orderNum: reqRule('请输入显示排序'),

      status: reqRule('请选择按钮状态', 'change'),

    }

  }



  return base

})



function onIconSelected(name) {

  form.value.icon = name

  iconPopoverVisible.value = false

}



function showSelectIcon() {

  iconSelectRef.value?.reset?.()

}



function onTypeChange() {

  if (form.value.menuType === 'F') {

    iconPopoverVisible.value = false

    form.value.icon = ''

  }

  if (form.value.menuType === 'M') {

    form.value.component = form.value.component || 'Layout'

  }

  if (form.value.menuType === 'F') {

    form.value.path = ''

    form.value.component = ''

    form.value.visible = '0'

  }

  nextTick(() => formRef.value?.clearValidate?.())

}



function normalizePermsStr(raw) {

  if (raw == null || typeof raw !== 'string') return ''

  return raw

    .replace(/，/g, ',')

    .split(',')

    .map((s) => s.trim())

    .filter(Boolean)

    .filter((v, i, a) => a.indexOf(v) === i)

    .join(',')

}



/** 从已保存的 perms 拆成输入行（至少一行便于编辑） */
function permsStrToList(raw) {

  const normalized = normalizePermsStr(typeof raw === 'string' ? raw : '')

  if (!normalized) return ['']

  return normalized.split(',')

}



function addPermsRow() {

  permsList.value.push('')

}



function removePermsRow(index) {

  permsList.value.splice(index, 1)

  if (permsList.value.length === 0) {

    permsList.value.push('')

  }

}



/** 将多行输入写回 form.perms，供校验与提交 */
function syncPermsFromList() {

  form.value.perms = normalizePermsStr(permsList.value.join(','))

}



function normalizeParentId(v) {
  if (v == null || v === '') return ROOT
  const s = String(v).trim()
  if (s === ROOT || s === '-1' || s === '0') return ROOT
  return s
}

/**
 * 提交给后端的 Long 字段：安全整数用 number，雪花 ID 用 string（避免 JS Number 精度丢失）。
 * @param {string|number|null|undefined} value
 * @returns {number|string|null}
 */
function toApiLongId(value) {
  if (value == null || value === '') return null
  const s = String(value).trim()
  const n = Number(s)
  return Number.isSafeInteger(n) ? n : s
}

/** 上级菜单：顶级为 -1，其余走 {@link toApiLongId} */
function toApiParentId(value) {
  const normalized = normalizeParentId(value)
  if (normalized === ROOT) return -1
  return toApiLongId(normalized)
}



function open(payload = {}) {

  visible.value = true

  treeData.value = [{ id: ROOT, label: '主类目', children: [] }]

  form.value = defaultForm()

  permsList.value = ['']

  form.value.parentId = normalizeParentId(payload.parentId ?? ROOT)



  treeselectMenu().then((res) => {

    treeData.value = [{ id: ROOT, label: '主类目', children: res.data || [] }]

  })



  if (payload.menuId) {

    getMenu(payload.menuId).then((res) => {

      const row = res.data || {}

      form.value = {

        ...defaultForm(),

        ...row,

        parentId: normalizeParentId(row.parentId),

      }

      permsList.value = permsStrToList(form.value.perms)

    })

  }

}



function toPayload() {

  syncPermsFromList()

  const f = { ...form.value }

  f.parentId = toApiParentId(f.parentId)
  if (f.menuId != null && f.menuId !== '') {
    f.menuId = toApiLongId(f.menuId)
  }

  if (f.menuType === 'M' && !f.component) {

    f.component = 'Layout'

  }

  f.perms = normalizePermsStr(f.perms)

  return f

}



function submit() {

  syncPermsFromList()

  return new Promise((resolve, reject) => {

    formRef.value.validate((valid) => {

      if (!valid) {

        reject(new Error('校验未通过'))

        return

      }

      const body = toPayload()

      const req = body.menuId ? updateMenu(body) : addMenu(body)

      req

        .then(() => {

          ElMessage.success(body.menuId ? '修改成功' : '新增成功')

          emit('success')

          resolve()

        })

        .catch(reject)

    })

  })

}



defineExpose({ open })

</script>



<style scoped>

.menu-form :deep(.el-row) {

  margin-bottom: 4px;

}



.perms-dynamic {

  width: 100%;

}



.perms-dynamic__row {

  display: flex;

  align-items: center;

  gap: 8px;

  margin-bottom: 8px;

}



.perms-dynamic__row .el-input {

  flex: 1;

  min-width: 0;

}



.perms-dynamic__hint {

  margin-top: 4px;

  font-size: 12px;

  color: var(--el-text-color-secondary);

  line-height: 1.4;

}

</style>

