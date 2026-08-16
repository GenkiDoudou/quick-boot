<script setup lang="ts">
/**
 * 用户新增/编辑/查看表单。
 * - 新增：必选部门、角色；密码可选。
 * - 编辑：账号只读；须始终回传 deptId / roleIds（后端 update 未传会清空）。
 * - 查看（mode=view）：全部只读，隐藏保存。
 * - 补齐 email / sex / remark；提交前用 validate.assert 做必填与格式校验。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addUser, getUser, updateUser } from '@/api/system/user'
import { pageRole, type SysRole } from '@/api/system/role'
import {
  treeselectDept,
  type DeptTreeNode,
  type SysDept,
} from '@/api/system/dept'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { assert, email, mobile, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

type DeptOption = {
  id: string
  label: string
}

const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')
/** 查看模式：路由 mode=view，控件只读且不可保存 */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:user:edit') : hasPermi('system:user:add')),
)

const userId = ref('')
const isEdit = computed(() => !!userId.value)
const loading = ref(false)
const submitting = ref(false)
const roleOptions = ref<SysRole[]>([])
const deptOptions = ref<DeptOption[]>([])
const deptIndex = ref(0)

const form = reactive({
  userName: '',
  nickName: '',
  password: '',
  phonenumber: '',
  email: '',
  sex: '0',
  remark: '',
  status: '0',
  deptId: '',
  roleIds: [] as string[],
})

/** 编辑时从详情带回的角色，提交兜底用（界面未改角色时仍须回传） */
const preservedRoleIds = ref<Array<number | string>>([])

function nodeId(n: DeptTreeNode | SysDept): string {
  const raw = (n as DeptTreeNode).deptId ?? (n as DeptTreeNode).id
  return String(raw ?? '')
}

function nodeLabel(n: DeptTreeNode | SysDept): string {
  return (n as DeptTreeNode).deptName || (n as DeptTreeNode).label || '—'
}

/** 部门树拍平为 picker 选项（带层级前缀） */
function flattenDepts(nodes: DeptTreeNode[], depth: number, out: DeptOption[]) {
  for (const n of nodes || []) {
    const id = nodeId(n)
    if (!id) continue
    const pad = depth > 0 ? `${'· '.repeat(depth)}` : ''
    out.push({ id, label: `${pad}${nodeLabel(n)}` })
    if (n.children?.length) {
      flattenDepts(n.children, depth + 1, out)
    }
  }
}

async function loadDepts() {
  const tree = (await treeselectDept()) || []
  const opts: DeptOption[] = []
  flattenDepts(tree, 0, opts)
  deptOptions.value = opts
  syncDeptIndex()
}

function syncDeptIndex() {
  const i = deptOptions.value.findIndex((o) => o.id === String(form.deptId))
  deptIndex.value = i >= 0 ? i : 0
  if (i < 0 && deptOptions.value.length) {
    // 未匹配时不强制改 deptId（编辑可能存在已删部门）
    if (!isEdit.value) {
      form.deptId = ''
    }
  }
}

function onDeptChange(e: { detail: { value: number | string } }) {
  const idx = Number(e.detail.value)
  const opt = deptOptions.value[idx]
  if (!opt) {
    syncDeptIndex()
    return
  }
  deptIndex.value = idx
  form.deptId = opt.id
}

const deptLabel = computed(() => {
  if (!form.deptId) return '请选择部门'
  const hit = deptOptions.value.find((o) => o.id === String(form.deptId))
  return hit?.label || `部门 #${form.deptId}`
})

/** 多选角色：点击切换选中态；查看模式不响应 */
function toggleRole(roleId: number | string) {
  if (isView.value) return
  const id = String(roleId)
  const i = form.roleIds.indexOf(id)
  if (i >= 0) form.roleIds.splice(i, 1)
  else form.roleIds.push(id)
}

function isRoleChecked(roleId: number | string) {
  return form.roleIds.includes(String(roleId))
}

async function loadRoles() {
  const data = await pageRole({ current: 1, size: 100, param: {} })
  roleOptions.value = data?.records || []
}

async function loadDetail(id: string) {
  const d = await getUser(id)
  form.userName = d.userName || ''
  form.nickName = d.nickName || ''
  form.phonenumber = d.phonenumber || ''
  form.email = d.email || ''
  form.sex = d.sex ?? '0'
  form.remark = d.remark || ''
  form.status = d.status ?? '0'
  form.password = ''
  form.deptId = d.deptId != null && d.deptId !== '' ? String(d.deptId) : ''
  preservedRoleIds.value = (d.roleIds || []).map((x) => x)
  form.roleIds = preservedRoleIds.value.map(String)
}

/**
 * 角色必选：新增须勾选；编辑允许沿用详情 roleIds（界面未改时 preserved 兜底）。
 * @returns 通过为 true（失败时已 toast）
 */
function assertRoles(): boolean {
  if (form.roleIds.length > 0) return true
  if (isEdit.value && preservedRoleIds.value.length > 0) return true
  toastInfo('请选择角色')
  return false
}

onLoad(async (query) => {
  const id = query?.userId ? String(query.userId) : ''
  userId.value = id
  // mode=view：只读查看，不展示保存
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看用户' : id ? '编辑用户' : '新增用户',
  })
  loading.value = true
  try {
    await loadDepts()
    if (!id) {
      await loadRoles()
    }
    else {
      await Promise.all([loadDetail(id), loadRoles()])
      syncDeptIndex()
    }
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
  }
})

function goBack() {
  uni.navigateBack()
}

/** 校验必填与手机/邮箱格式后新增或更新；编辑始终带 deptId/roleIds */
async function onSubmit() {
  if (!assert([
    () => required('用户账号', form.userName),
    () => required('用户昵称', form.nickName),
    () => required('所属部门', form.deptId),
    assertRoles,
    () => mobile(form.phonenumber),
    () => email(form.email),
  ])) {
    return
  }
  submitting.value = true
  try {
    // 雪花 ID 须以字符串提交，禁止 Number()（超 MAX_SAFE_INTEGER 会丢精度）
    // 编辑：界面清空角色时回退到详情带回的 roleIds，避免后端清空关联
    const roleIds = isEdit.value
      ? (form.roleIds.length
          ? form.roleIds.map((x) => String(x))
          : preservedRoleIds.value.map((x) => String(x)))
      : form.roleIds.map((x) => String(x))

    const payload = {
      userName: form.userName.trim(),
      nickName: form.nickName.trim(),
      phonenumber: form.phonenumber.trim() || undefined,
      email: form.email.trim() || undefined,
      sex: form.sex,
      remark: form.remark.trim() || undefined,
      status: form.status,
      deptId: String(form.deptId),
      roleIds,
    }

    if (isEdit.value) {
      await updateUser({
        ...payload,
        userId: userId.value,
      })
    }
    else {
      await addUser({
        ...payload,
        password: form.password.trim() || undefined,
      })
    }
    toastOk('保存成功')
    setTimeout(() => uni.navigateBack(), 400)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <view v-if="loading" class="qb-form-loading qb-muted">加载中…</view>
    <view v-else class="qb-form-panel">
      <view class="qb-form-field">
        <text class="qb-form-label">用户账号</text>
        <u-input
          v-model="form.userName"
          :disabled="isView || isEdit"
          placeholder="必填"
          border="surround"
          maxlength="64"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">用户昵称</text>
        <u-input
          v-model="form.nickName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view v-if="!isEdit && !isView" class="qb-form-field">
        <text class="qb-form-label">密码</text>
        <u-input
          v-model="form.password"
          type="password"
          placeholder="可空，默认 admin123"
          border="surround"
          clearable
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">所属部门</text>
        <picker
          v-if="deptOptions.length"
          mode="selector"
          :range="deptOptions"
          range-key="label"
          :value="deptIndex"
          :disabled="isView"
          @change="onDeptChange"
        >
          <view class="dept-picker">
            <text :class="{ placeholder: !form.deptId }">{{ deptLabel }}</text>
            <text class="dept-picker__arrow">›</text>
          </view>
        </picker>
        <view v-else class="qb-muted">暂无部门或无权限加载</view>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">手机号码</text>
        <u-input
          v-model="form.phonenumber"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">邮箱</text>
        <u-input
          v-model="form.email"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">性别</text>
        <QbStatusChips v-model="form.sex" :options="sys_user_sex" :disabled="isView" />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <QbStatusChips v-model="form.status" :options="sys_normal_disable" :disabled="isView" />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">备注</text>
        <u-input
          v-model="form.remark"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">角色（必选）</text>
        <view v-if="!roleOptions.length" class="qb-muted">暂无角色或无权限加载</view>
        <view
          v-for="r in roleOptions"
          :key="String(r.roleId)"
          class="role-item"
          :class="{ 'role-item--readonly': isView }"
          @click="toggleRole(r.roleId!)"
        >
          <view class="role-check" :class="{ on: isRoleChecked(r.roleId!) }" />
          <text>{{ r.roleName || r.roleId }}</text>
        </view>
      </view>

      <u-button
        v-if="canSave"
        type="primary"
        :loading="submitting"
        :custom-style="qbPrimaryBtnStyle"
        @click="onSubmit"
      >
        保存
      </u-button>
      <u-button
        v-else-if="isView"
        :custom-style="qbPrimaryBtnStyle"
        @click="goBack"
      >
        返回
      </u-button>
      <view v-else class="qb-muted" style="margin-top: 24rpx; text-align: center">无保存权限</view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.dept-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: #f1f5f9;
  font-size: 28rpx;
  color: #0f172a;
}

.dept-picker .placeholder {
  color: #94a3b8;
}

.dept-picker__arrow {
  color: #94a3b8;
  font-size: 36rpx;
  line-height: 1;
  transform: rotate(90deg);
}

.role-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
  font-size: 28rpx;
  color: #111827;
}

.role-item--readonly {
  pointer-events: none;
  opacity: 0.85;
}

.role-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid #d1d5db;
  box-sizing: border-box;
}

.role-check.on {
  border-color: #059669;
  background: #059669;
  box-shadow: inset 0 0 0 6rpx #fff;
}
</style>
