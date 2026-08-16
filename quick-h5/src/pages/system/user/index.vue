<script setup lang="ts">
/**
 * 系统用户列表：分页搜索、状态筛选、查看/编辑、删除、启停、重置密码（弹窗）。
 * 卡片展示部门/角色/手机/邮箱/性别/备注；超级管理员（userId=1）禁止停用与删除。
 */
import { computed, ref } from 'vue'
import {
  pageUser,
  changeUserStatus,
  resetUserPwd,
  removeUser,
  type SysUser,
} from '@/api/system/user'
import { useDict } from '@/composables/useDict'
import { usePagedList } from '@/composables/usePagedList'
import { toastErr, toastOk, toastInfo } from '@/utils/toast'
import { hasPermi } from '@/utils/permission'
import { qbInputStyle } from '@/utils/formStyle'
import type { QbCardColumn } from '@/components/qb/qbCardColumn'

const { sys_normal_disable, sys_user_sex } = useDict('sys_normal_disable', 'sys_user_sex')
const canAdd = computed(() => hasPermi('system:user:add'))
const canEdit = computed(() => hasPermi('system:user:edit'))
const canRemove = computed(() => hasPermi('system:user:remove'))
/** 查看：query 或 list 任一即可 */
const canView = computed(() => hasPermi(['system:user:query', 'system:user:list']))
const canResetPwd = computed(() => hasPermi('system:user:resetPwd'))

/** 列表状态筛选；空串表示「全部」，不传给后端 */
const filters = ref({ status: '' })

/**
 * 卡片 meta 字段：性别走字典；邮箱/备注有值才展示。
 * options 取自 useDict 的响应式数组，故用 computed。
 */
const cardColumns = computed<QbCardColumn[]>(() => [
  { prop: 'deptName', label: '部门', span: 12, kv: 'row' },
  { prop: 'phonenumber', label: '手机', span: 12, kv: 'row' },
  { prop: 'roleNames', label: '角色', span: 24, kv: 'stack' },
  { prop: 'email', label: '邮箱', span: 24, kv: 'row', showIfProp: true },
  {
    prop: 'sex',
    label: '性别',
    span: 12,
    kv: 'row',
    type: 'dict',
    options: sys_user_sex.value,
    showIfProp: true,
  },
  { prop: 'remark', label: '备注', span: 24, kv: 'stack', showIfProp: true },
])

const {
  keyword,
  rows,
  loading,
  finished,
  onSearch,
  load,
} = usePagedList<SysUser>({
  filters,
  fetcher: ({ current, size, keyword: kw, filters: f }) =>
    pageUser({
      current,
      size,
      param: {
        userName: kw || undefined,
        // 仅在选中具体状态时传入，避免空串覆盖后端默认
        status: f.status || undefined,
      },
    }),
})

const pwdVisible = ref(false)
const pwdTarget = ref<SysUser | null>(null)
const newPassword = ref('')
const pwdSubmitting = ref(false)

/** 内置超级管理员，禁止停用 */
function isSuperAdmin(row: SysUser) {
  return String(row.userId) === '1'
}

function goAdd() {
  uni.navigateTo({ url: '/pages/system/user/form' })
}

/** 只读查看：复用 form + mode=view */
function goView(row: SysUser) {
  uni.navigateTo({
    url: `/pages/system/user/form?userId=${encodeURIComponent(String(row.userId))}&mode=view`,
  })
}

function goEdit(row: SysUser) {
  uni.navigateTo({ url: `/pages/system/user/form?userId=${encodeURIComponent(String(row.userId))}` })
}

/** 二次确认删除；userId=1 禁止 */
function onRemove(row: SysUser) {
  if (isSuperAdmin(row)) {
    toastInfo('不能删除超级管理员')
    return
  }
  uni.showModal({
    title: '确认删除',
    content: `删除用户「${row.userName || row.nickName || ''}」？`,
    success: async (r) => {
      if (!r.confirm) return
      try {
        await removeUser([row.userId!])
        toastOk('已删除')
        load(true)
      }
      catch (e) {
        toastErr(e)
      }
    },
  })
}

/** 切换用户启停状态，成功后就地更新行数据 */
async function onToggleStatus(row: SysUser) {
  if (isSuperAdmin(row)) {
    toastInfo('不能停用超级管理员')
    return
  }
  const next = row.status === '1' ? '0' : '1'
  try {
    await changeUserStatus({ userId: row.userId!, status: next })
    row.status = next
    toastOk('状态已更新')
  }
  catch (e) {
    toastErr(e)
  }
}

function openResetPwd(row: SysUser) {
  pwdTarget.value = row
  newPassword.value = ''
  pwdVisible.value = true
}

/** 提交重置密码：校验非空后调用接口并关闭弹窗 */
async function submitResetPwd() {
  if (!pwdTarget.value?.userId) return
  const pwd = newPassword.value.trim()
  if (!pwd) {
    toastInfo('请输入新密码')
    return
  }
  pwdSubmitting.value = true
  try {
    await resetUserPwd({ userId: pwdTarget.value.userId, password: pwd })
    toastOk('密码已重置')
    pwdVisible.value = false
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    pwdSubmitting.value = false
  }
}
</script>

<template>
  <view class="qb-page qb-crud-page">
    <QbSearchBar
      v-model="keyword"
      placeholder="用户账号"
      :show-add="canAdd"
      @search="onSearch"
      @add="goAdd"
    />
    <QbListFilters v-model="filters.status" label="状态" :options="sys_normal_disable" />

    <QbListCard
      v-for="row in rows"
      :key="String(row.userId)"
      :title="row.nickName || row.userName || '—'"
      :subtitle="`@${row.userName || '—'}`"
    >
      <template #status>
        <QbDictTag :value="row.status" :options="sys_normal_disable" />
      </template>
      <template #meta>
        <view class="qb-card-meta">
          <QbJsonCardFields :row="row" :columns="cardColumns" />
        </view>
      </template>
      <template #actions>
        <text v-if="canView" class="qb-link" @click="goView(row)">查看</text>
        <text v-if="canEdit" class="qb-link" @click="goEdit(row)">编辑</text>
        <text
          v-if="canEdit && !isSuperAdmin(row)"
          class="qb-link"
          @click="onToggleStatus(row)"
        >
          {{ row.status === '1' ? '启用' : '停用' }}
        </text>
        <text
          v-if="canResetPwd"
          class="qb-link qb-link--warn"
          @click="openResetPwd(row)"
        >
          重置密码
        </text>
        <text
          v-if="canRemove && !isSuperAdmin(row)"
          class="qb-link qb-link--danger"
          @click="onRemove(row)"
        >
          删除
        </text>
      </template>    </QbListCard>

    <QbListFooter
      :loading="loading"
      :finished="finished"
      :empty="!rows.length"
      :has-rows="!!rows.length"
      empty-text="暂无用户"
    />

    <u-popup :show="pwdVisible" mode="center" round="16" @close="pwdVisible = false">
      <view class="pwd-box">
        <text class="pwd-box__title">重置密码</text>
        <text class="pwd-box__sub">用户 {{ pwdTarget?.userName || '' }}</text>
        <u-input
          v-model="newPassword"
          type="password"
          placeholder="请输入新密码"
          border="surround"
          clearable
          :custom-style="qbInputStyle"
        />
        <view class="pwd-box__actions">
          <u-button size="small" @click="pwdVisible = false">取消</u-button>
          <u-button type="primary" size="small" :loading="pwdSubmitting" @click="submitResetPwd">确定</u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<style scoped lang="scss">
.pwd-box {
  width: 560rpx;
  padding: 40rpx 36rpx;
  border-radius: 24rpx;
  background: #fff;
}

.pwd-box__title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #0f172a;
}

.pwd-box__sub {
  display: block;
  margin: 8rpx 0 24rpx;
  font-size: 24rpx;
  color: #6b7280;
}

.pwd-box__actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 28rpx;
}
</style>
