<template>
  <C7Dialog v-model="visible" :title="form.menuId ? '修改菜单' : '新增菜单'" width="880px" :on-confirm="submit">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="menu-form">
      <el-form-item prop="parentId">
        <template #label>
          <MenuFormLabel text="上级菜单" tip="选择所属上级；顶级选「主类目」。" />
        </template>
        <C7TreeSelect
          v-model="form.parentId"
          :data-list="treeData"
          value-key="id"
          label-key="label"
          children-key="children"
          :check-strictly="true"
          :default-expand-all="false"
          filterable
          placeholder="请选择上级菜单"
          value-type="string"
        />
      </el-form-item>

      <el-form-item prop="menuType">
        <template #label>
          <MenuFormLabel text="菜单类型" tip="M=目录，C=菜单，F=按钮。" />
        </template>
        <el-radio-group v-model="form.menuType" @change="onTypeChange">
          <el-radio value="M">目录</el-radio>
          <el-radio value="C">菜单</el-radio>
          <el-radio value="F">按钮</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item prop="menuName">
            <template #label>
              <MenuFormLabel :text="form.menuType === 'F' ? '按钮名称' : '菜单名称'" tip="侧栏或按钮展示名称。" />
            </template>
            <el-input v-model="form.menuName" maxlength="64" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="orderNum">
            <template #label>
              <MenuFormLabel text="显示排序" tip="同级数字越小越靠前。" />
            </template>
            <el-input-number v-model="form.orderNum" :min="0" :max="9999" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">
        <el-col :span="12">
          <el-form-item prop="icon">
            <template #label>
              <MenuFormLabel text="菜单图标" tip="侧栏图标名，与 SvgIcon 一致。" />
            </template>
            <el-popover v-model:visible="iconPopoverVisible" placement="bottom-start" :width="540" trigger="click">
              <template #reference>
                <el-input v-model="form.icon" placeholder="点击选择图标" readonly>
                  <template #prefix>
                    <svg-icon v-if="form.icon" :icon-class="form.icon" />
                  </template>
                </el-input>
              </template>
              <IconSelect @selected="onIconSelected" />
            </el-popover>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="path">
            <template #label>
              <MenuFormLabel text="路由地址" tip="目录/菜单 path；外链填完整 http(s) URL。" />
            </template>
            <el-input v-model="form.path" placeholder="如 role 或 https://..." />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="form.menuType === 'C'" :gutter="20">
        <el-col :span="12">
          <el-form-item prop="component">
            <template #label>
              <MenuFormLabel text="组件路径" tip="views 下路径，如 system/role/index；外链可空。" />
            </template>
            <el-input v-model="form.component" :disabled="form.isFrame === '1'" placeholder="system/xxx/index" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="routeName">
            <template #label>
              <MenuFormLabel text="路由名称" tip="唯一；缓存时需与页面 name 一致。" />
            </template>
            <el-input v-model="form.routeName" placeholder="可选" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="form.menuType === 'C'" :gutter="20">
        <el-col :span="12">
          <el-form-item prop="query">
            <template #label>
              <MenuFormLabel text="路由参数" tip="可选 query 串，如 id=1 或 JSON。" />
            </template>
            <el-input v-model="form.query" placeholder="可选" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="isFrame">
            <template #label>
              <MenuFormLabel text="是否外链" tip="是则内嵌 InnerLink，path 须为 http(s)。" />
            </template>
            <el-radio-group v-model="form.isFrame">
              <el-radio value="0">否</el-radio>
              <el-radio value="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row v-if="form.menuType !== 'F'" :gutter="20">
        <el-col :span="12">
          <el-form-item prop="visible">
            <template #label>
              <MenuFormLabel text="显示状态" tip="隐藏则不出现在侧栏，路由仍可能被访问。" />
            </template>
            <el-radio-group v-model="form.visible">
              <el-radio value="0">显示</el-radio>
              <el-radio value="1">隐藏</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col v-if="form.menuType === 'C'" :span="12">
          <el-form-item prop="isCache">
            <template #label>
              <MenuFormLabel text="是否缓存" tip="0=缓存，1=不缓存（keep-alive）。" />
            </template>
            <el-radio-group v-model="form.isCache">
              <el-radio value="0">缓存</el-radio>
              <el-radio value="1">不缓存</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item prop="perms">
            <template #label>
              <MenuFormLabel
                text="权限字符"
                tip="如 system:menu:list；按钮类型可填多个，保存时英文逗号分隔。"
              />
            </template>
            <template v-if="form.menuType === 'F'">
              <div class="perms-dynamic">
                <div v-for="(_, index) in permsList" :key="index" class="perms-dynamic__row">
                  <el-input
                    v-model="permsList[index]"
                    placeholder="如 system:xxx:list"
                    maxlength="128"
                    @blur="syncPermsFromList"
                  />
                  <el-button type="danger" link :disabled="permsList.length <= 1" @click="removePermsRow(index)">
                    删除
                  </el-button>
                </div>
                <el-button type="primary" link @click="addPermsRow">添加权限</el-button>
                <div class="perms-dynamic__hint">保存时合并为英文逗号并去重；中文逗号会转为英文逗号。</div>
              </div>
            </template>
            <el-input v-else v-model="form.perms" placeholder="可选" maxlength="500" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item prop="status">
            <template #label>
              <MenuFormLabel text="菜单状态" tip="停用后不参与路由与授权。" />
            </template>
            <el-radio-group v-model="form.status">
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item prop="remark">
        <template #label>
          <MenuFormLabel text="备注" tip="可选说明。" />
        </template>
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>
    </el-form>
  </C7Dialog>
</template>

<script setup>
/**
 * 菜单新增/修改弹窗。不含积木/BI；含 visible / isFrame / isCache / query。
 */
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { addMenu, getMenu, treeselectMenu, updateMenu } from '@/api/system/menu'
import IconSelect from '@/components/IconSelect/index.vue'
import MenuFormLabel from './MenuFormLabel.vue'

const emit = defineEmits(['success'])

const visible = ref(false)
const formRef = ref(null)
const iconPopoverVisible = ref(false)
const treeRaw = ref([])
/** 按钮类型多权限行编辑 */
const permsList = ref([''])

const form = reactive({
  menuId: undefined,
  parentId: '0',
  menuName: '',
  menuType: 'M',
  orderNum: 0,
  icon: '',
  path: '',
  component: '',
  routeName: '',
  query: '',
  isFrame: '0',
  isCache: '0',
  visible: '0',
  status: '0',
  perms: '',
  remark: ''
})

const rules = computed(() => {
  const req = (msg, trigger = 'blur') => [{ required: true, message: msg, trigger }]
  const permsRules = [{ max: 500, message: '权限字符总长度不超过 500', trigger: 'blur' }]
  if (form.menuType === 'F') {
    permsRules.unshift({
      validator: (_rule, _value, callback) => {
        const s = normalizePermsStr(permsList.value.join(','))
        if (!s) callback(new Error('请填写权限字符'))
        else callback()
      },
      trigger: 'blur'
    })
  }
  const base = {
    parentId: req('请选择上级菜单', 'change'),
    menuType: req('请选择菜单类型', 'change'),
    menuName: req('请输入名称'),
    orderNum: req('请输入排序', 'change'),
    status: req('请选择状态', 'change'),
    perms: permsRules
  }
  if (form.menuType !== 'F') {
    base.path = req('请输入路由地址')
    base.visible = req('请选择显示状态', 'change')
  }
  if (form.menuType === 'C') {
    base.isFrame = req('请选择是否外链', 'change')
    base.isCache = req('请选择是否缓存', 'change')
    if (form.isFrame !== '1') {
      base.component = req('请输入组件路径')
    }
  }
  return base
})

/**
 * 树节点 id 统一为字符串，与 C7TreeSelect valueType=string、表单 parentId 一致。
 * @param {Array} nodes
 * @param {string|number|null|undefined} excludeId 编辑时排除自身及子树
 */
function normalizeTreeIds(nodes, excludeId) {
  if (!Array.isArray(nodes)) return []
  const ex = excludeId != null && excludeId !== '' ? String(excludeId) : null
  const out = []
  for (const n of nodes) {
    if (!n) continue
    const id = n.id != null ? String(n.id) : ''
    if (ex && id === ex) continue
    out.push({
      id,
      label: n.label,
      children: normalizeTreeIds(n.children, excludeId)
    })
  }
  return out
}

const treeData = computed(() => [
  { id: '0', label: '主类目', children: normalizeTreeIds(treeRaw.value, form.menuId) }
])

/**
 * @param {string} raw
 */
function normalizePermsStr(raw) {
  if (!raw || typeof raw !== 'string') return ''
  const parts = raw
    .replace(/，/g, ',')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
  return [...new Set(parts)].join(',')
}

/**
 * @param {string} raw
 */
function permsStrToList(raw) {
  const normalized = normalizePermsStr(raw)
  if (!normalized) return ['']
  return normalized.split(',')
}

function syncPermsFromList() {
  form.perms = normalizePermsStr(permsList.value.join(','))
}

function addPermsRow() {
  permsList.value.push('')
}

/**
 * @param {number} index
 */
function removePermsRow(index) {
  permsList.value.splice(index, 1)
  if (permsList.value.length === 0) {
    permsList.value.push('')
  }
  syncPermsFromList()
}

function resetForm() {
  form.menuId = undefined
  form.parentId = '0'
  form.menuName = ''
  form.menuType = 'C'
  form.orderNum = 0
  form.icon = ''
  form.path = ''
  form.component = ''
  form.routeName = ''
  form.query = ''
  form.isFrame = '0'
  form.isCache = '0'
  form.visible = '0'
  form.status = '0'
  form.perms = ''
  form.remark = ''
  permsList.value = ['']
}

function onTypeChange() {
  if (form.menuType === 'F') {
    form.path = ''
    form.component = ''
    form.routeName = ''
    form.query = ''
    form.isFrame = '0'
    form.isCache = '0'
    form.visible = '0'
    form.icon = ''
    if (!permsList.value.length) {
      permsList.value = ['']
    }
  } else {
    syncPermsFromList()
  }
}

/**
 * @param {string} name
 */
function onIconSelected(name) {
  form.icon = name || ''
  iconPopoverVisible.value = false
}

async function loadTree() {
  const res = await treeselectMenu({ excludeButton: true })
  treeRaw.value = res.data || []
}

/**
 * @param {{ menuId?: string|number, parentId?: string|number }} opts
 */
async function open(opts = {}) {
  resetForm()
  await loadTree()
  if (opts.menuId != null) {
    const res = await getMenu(opts.menuId)
    const data = res.data || {}
    Object.assign(form, {
      menuId: data.menuId != null ? String(data.menuId) : undefined,
      parentId: data.parentId != null ? String(data.parentId) : '0',
      menuName: data.menuName || '',
      menuType: data.menuType || 'C',
      orderNum: data.orderNum ?? 0,
      icon: data.icon || '',
      path: data.path || '',
      component: data.component || '',
      routeName: data.routeName || '',
      query: data.query || '',
      isFrame: data.isFrame != null ? String(data.isFrame) : '0',
      isCache: data.isCache != null ? String(data.isCache) : '0',
      visible: data.visible != null ? String(data.visible) : '0',
      status: data.status != null ? String(data.status) : '0',
      perms: data.perms || '',
      remark: data.remark || ''
    })
    permsList.value = permsStrToList(form.perms)
  } else if (opts.parentId != null) {
    form.parentId = String(opts.parentId)
    form.menuType = 'C'
  }
  visible.value = true
  // 树数据与 model 就绪后再触达 TreeSelect，避免首帧类型不一致不反显
  await nextTick()
}

function submit() {
  return new Promise((resolve, reject) => {
    if (form.menuType === 'F') {
      syncPermsFromList()
    }
    formRef.value?.validate(async (ok) => {
      if (!ok) {
        reject(new Error('校验未通过'))
        return
      }
      try {
        const payload = {
          menuId: form.menuId,
          parentId: form.parentId === '' || form.parentId == null ? 0 : form.parentId,
          menuName: form.menuName,
          menuType: form.menuType,
          orderNum: form.orderNum,
          icon: form.icon || null,
          path: form.path || null,
          component: form.component || null,
          routeName: form.routeName || null,
          query: form.query || null,
          isFrame: form.isFrame,
          isCache: form.isCache,
          visible: form.visible,
          status: form.status,
          perms: normalizePermsStr(form.perms) || null,
          remark: form.remark || null
        }
        if (form.menuId) {
          await updateMenu(payload)
          ElMessage.success('修改成功')
        } else {
          delete payload.menuId
          await addMenu(payload)
          ElMessage.success('新增成功')
        }
        emit('success')
        resolve()
      } catch (e) {
        reject(e)
      }
    })
  })
}

defineExpose({ open })
</script>

<style scoped>
.menu-form {
  padding-right: 8px;
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
}
.perms-dynamic__hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
