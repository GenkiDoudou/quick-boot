<script setup lang="ts">
/**
 * 部门新增/编辑/查看表单：上级部门 picker（编辑时排除自身及子孙节点防环）。
 * mode=view 时全部只读并隐藏保存。
 */
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  addDept,
  getDept,
  treeselectDept,
  updateDept,
  type DeptTreeNode,
  type SysDept,
} from '@/api/system/dept'
import { useDict } from '@/composables/useDict'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { assert, required } from '@/utils/validate'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle, qbPrimaryBtnStyle } from '@/utils/formStyle'

type ParentOption = {
  id: string
  label: string
  depth: number
}

const { sys_normal_disable } = useDict('sys_normal_disable')

const deptId = ref('')
const isEdit = computed(() => !!deptId.value)
/** 查看模式：路由 mode=view */
const isView = ref(false)
const canSave = computed(() =>
  !isView.value && (isEdit.value ? hasPermi('system:dept:edit') : hasPermi('system:dept:add')),
)
const loading = ref(false)
const submitting = ref(false)
const parentOptions = ref<ParentOption[]>([])
const parentIndex = ref(0)

const form = reactive({
  parentId: '0',
  deptName: '',
  orderNum: 0,
  leader: '',
  phone: '',
  status: '0',
})

function nodeId(n: DeptTreeNode | SysDept): string {
  const raw = (n as DeptTreeNode).deptId ?? (n as DeptTreeNode).id
  return String(raw ?? '')
}

function nodeLabel(n: DeptTreeNode | SysDept): string {
  return (n as DeptTreeNode).deptName || (n as DeptTreeNode).label || '—'
}

/** 在树中定位 targetId 并收集其全部子孙 id，用于上级部门不可选 */
function collectDescendantIds(nodes: DeptTreeNode[], targetId: string, acc: Set<string>) {
  for (const n of nodes || []) {
    const id = nodeId(n)
    if (id === targetId) {
      walkIds(n.children || [], acc)
      return true
    }
    if (n.children?.length && collectDescendantIds(n.children, targetId, acc)) {
      return true
    }
  }
  return false
}

function walkIds(nodes: DeptTreeNode[], acc: Set<string>) {
  for (const n of nodes || []) {
    const id = nodeId(n)
    if (id) acc.add(id)
    if (n.children?.length) walkIds(n.children, acc)
  }
}

/** 将部门树拍平为 picker 选项，跳过 disabledIds 中的节点 */
function flattenParents(
  nodes: DeptTreeNode[],
  depth: number,
  disabledIds: Set<string>,
  out: ParentOption[],
) {
  for (const n of nodes || []) {
    const id = nodeId(n)
    if (!id) continue
    if (!disabledIds.has(id)) {
      const pad = depth > 0 ? `${'· '.repeat(depth)}` : ''
      out.push({
        id,
        label: `${pad}${nodeLabel(n)}`,
        depth,
      })
    }
    if (n.children?.length) {
      flattenParents(n.children, depth + 1, disabledIds, out)
    }
  }
}

/** 加载上级部门候选：根部门 + 树选项；编辑时排除自身及子孙 */
async function loadParentOptions(selfId?: string) {
  const tree = (await treeselectDept()) || []
  const disabledIds = new Set<string>()
  if (selfId) {
    disabledIds.add(selfId)
    collectDescendantIds(tree, selfId, disabledIds)
  }
  const opts: ParentOption[] = [
    { id: '0', label: '根部门', depth: 0 },
  ]
  flattenParents(tree, 1, disabledIds, opts)
  parentOptions.value = opts
  syncParentIndex()
}

function syncParentIndex() {
  const i = parentOptions.value.findIndex((o) => o.id === String(form.parentId))
  parentIndex.value = i >= 0 ? i : 0
  if (i < 0) form.parentId = '0'
}

function onParentChange(e: { detail: { value: number | string } }) {
  const idx = Number(e.detail.value)
  const opt = parentOptions.value[idx]
  if (!opt) {
    syncParentIndex()
    return
  }
  parentIndex.value = idx
  form.parentId = opt.id
}

async function loadDetail(id: string) {
  const d = await getDept(id)
  form.parentId = d.parentId != null && d.parentId !== '' ? String(d.parentId) : '0'
  form.deptName = d.deptName || ''
  form.orderNum = Number(d.orderNum ?? 0)
  form.leader = d.leader || ''
  form.phone = d.phone || ''
  form.status = d.status ?? '0'
}

onLoad(async (query) => {
  const id = query?.deptId ? String(query.deptId) : ''
  const presetParent = query?.parentId != null ? String(query.parentId) : ''
  deptId.value = id
  isView.value = String(query?.mode || '') === 'view'
  uni.setNavigationBarTitle({
    title: isView.value ? '查看部门' : id ? '编辑部门' : '新增部门',
  })
  loading.value = true
  try {
    if (id) {
      await loadDetail(id)
      await loadParentOptions(id)
    }
    else {
      if (presetParent) form.parentId = presetParent
      await loadParentOptions()
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

/**
 * 部门名称必填；上级部门须已选（含根部门 '0'）。
 * required 对 '0' 视为非空，故可用；额外拦截 null/空串。
 */
async function onSubmit() {
  if (!assert([
    () => required('部门名称', form.deptName),
    () => {
      // '0' 表示根部门，合法；仅拒绝未选
      if (form.parentId == null || form.parentId === '') {
        toastInfo('请选择上级部门')
        return false
      }
      return true
    },
  ])) {
    return
  }
  submitting.value = true
  try {
    const payload: Partial<SysDept> = {
      parentId: form.parentId === '0' || !form.parentId ? 0 : form.parentId,
      deptName: form.deptName.trim(),
      orderNum: Number(form.orderNum) || 0,
      leader: form.leader.trim() || undefined,
      phone: form.phone.trim() || undefined,
      status: form.status,
    }
    if (isEdit.value) {
      await updateDept({ ...payload, deptId: deptId.value })
    }
    else {
      await addDept(payload)
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
        <text class="qb-form-label">上级部门</text>
        <picker
          mode="selector"
          :range="parentOptions"
          range-key="label"
          :value="parentIndex"
          :disabled="isView"
          @change="onParentChange"
        >
          <view class="picker-val">
            {{ parentOptions[parentIndex]?.label || '根部门' }}
          </view>
        </picker>
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">部门名称</text>
        <u-input
          v-model="form.deptName"
          :disabled="isView"
          placeholder="必填"
          border="surround"
          maxlength="64"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">显示排序</text>
        <u-input
          v-model="form.orderNum"
          type="number"
          :disabled="isView"
          placeholder="0"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">负责人</text>
        <u-input
          v-model="form.leader"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">联系电话</text>
        <u-input
          v-model="form.phone"
          :disabled="isView"
          placeholder="选填"
          border="surround"
          :custom-style="qbInputStyle"
        />
      </view>
      <view class="qb-form-field">
        <text class="qb-form-label">状态</text>
        <QbStatusChips v-model="form.status" :options="sys_normal_disable" :disabled="isView" />
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
.picker-val {
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  background: #f8fafc;
  font-size: 28rpx;
  color: #111827;
  border: 1rpx solid #e5e7eb;
}
</style>
