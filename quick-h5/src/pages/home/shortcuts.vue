<script setup lang="ts">
/**
 * 首页快捷设置：从候选池勾选/排序，最多 8；保存走 POST；空数组恢复默认。
 */
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  fetchH5HomeShortcutCandidates,
  fetchH5HomeShortcuts,
  saveH5HomeShortcuts,
  type H5WorkbenchItem,
} from '@/api/system/menu'
import { toastErr, toastInfo, toastOk } from '@/utils/toast'
import { qbPrimaryBtnStyle } from '@/utils/formStyle'

const MAX = 8

const loading = ref(false)
const submitting = ref(false)
const candidates = ref<H5WorkbenchItem[]>([])
/** 已选有序 id 列表 */
const selectedIds = ref<string[]>([])

const selectedSet = computed(() => new Set(selectedIds.value))

const selectedItems = computed(() => {
  const map = new Map(candidates.value.map((c) => [String(c.id), c]))
  return selectedIds.value
    .map((id) => map.get(id))
    .filter((x): x is H5WorkbenchItem => !!x)
})

function shortOf(label?: string, icon?: string) {
  const s = (icon && icon.trim()) || (label && label.trim()) || '?'
  return s.slice(0, 1)
}

function isChecked(id: string) {
  return selectedSet.value.has(String(id))
}

/** 点击候选：已选则移除；未选则追加（满 8 提示） */
function toggleCandidate(item: H5WorkbenchItem) {
  const id = String(item.id)
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
    return
  }
  if (selectedIds.value.length >= MAX) {
    toastInfo(`最多选择 ${MAX} 个`)
    return
  }
  selectedIds.value.push(id)
}

function moveUp(index: number) {
  if (index <= 0) return
  const arr = selectedIds.value
  const t = arr[index - 1]
  arr[index - 1] = arr[index]
  arr[index] = t
}

function moveDown(index: number) {
  const arr = selectedIds.value
  if (index < 0 || index >= arr.length - 1) return
  const t = arr[index + 1]
  arr[index + 1] = arr[index]
  arr[index] = t
}

async function load() {
  loading.value = true
  try {
    const [cands, current] = await Promise.all([
      fetchH5HomeShortcutCandidates(),
      fetchH5HomeShortcuts(),
    ])
    candidates.value = Array.isArray(cands) ? cands : []
    // 初始勾选用当前最终列表（含默认解析结果），便于在默认基础上微调
    selectedIds.value = (Array.isArray(current) ? current : [])
      .map((x) => String(x.id))
      .filter(Boolean)
      .slice(0, MAX)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    loading.value = false
  }
}

async function onSave() {
  submitting.value = true
  try {
    await saveH5HomeShortcuts([...selectedIds.value])
    toastOk('已保存')
    setTimeout(() => uni.navigateBack(), 400)
  }
  catch (e) {
    toastErr(e)
  }
  finally {
    submitting.value = false
  }
}

/** 恢复默认：空 menuIds，后端删偏好行 */
async function onRestoreDefault() {
  uni.showModal({
    title: '恢复默认',
    content: '清除个人快捷，恢复系统默认？',
    success: async (r) => {
      if (!r.confirm) return
      submitting.value = true
      try {
        await saveH5HomeShortcuts([])
        toastOk('已恢复默认')
        setTimeout(() => uni.navigateBack(), 400)
      }
      catch (e) {
        toastErr(e)
      }
      finally {
        submitting.value = false
      }
    },
  })
}

onLoad(() => {
  uni.setNavigationBarTitle({ title: '编辑快捷入口' })
  load()
})
</script>

<template>
  <view class="qb-page qb-crud-page shortcuts">
    <view v-if="loading" class="qb-muted shortcuts__hint">加载中…</view>
    <template v-else>
      <view class="card">
        <view class="card__hd">
          <text class="card__title">已选（{{ selectedIds.length }}/{{ MAX }}）</text>
          <text class="card__link" @click="onRestoreDefault">恢复默认</text>
        </view>
        <view v-if="!selectedItems.length" class="qb-muted shortcuts__hint">尚未选择，可从下方候选添加</view>
        <view
          v-for="(item, index) in selectedItems"
          :key="String(item.id)"
          class="row"
        >
          <view class="row__ico">{{ shortOf(item.label, item.icon) }}</view>
          <text class="row__label">{{ item.label }}</text>
          <text class="row__act" @click="moveUp(index)">上移</text>
          <text class="row__act" @click="moveDown(index)">下移</text>
          <text class="row__act row__act--danger" @click="toggleCandidate(item)">移除</text>
        </view>
      </view>

      <view class="card">
        <view class="card__hd">
          <text class="card__title">候选（有权限的工作台入口）</text>
        </view>
        <view v-if="!candidates.length" class="qb-muted shortcuts__hint">暂无可用入口</view>
        <view
          v-for="item in candidates"
          :key="String(item.id)"
          class="row"
          @click="toggleCandidate(item)"
        >
          <view class="row__check" :class="{ on: isChecked(String(item.id)) }" />
          <view class="row__ico">{{ shortOf(item.label, item.icon) }}</view>
          <text class="row__label">{{ item.label }}</text>
        </view>
      </view>

      <u-button
        type="primary"
        :loading="submitting"
        :custom-style="qbPrimaryBtnStyle"
        @click="onSave"
      >
        保存
      </u-button>
    </template>
  </view>
</template>

<style scoped lang="scss">
.shortcuts__hint {
  padding: 16rpx 0 8rpx;
  text-align: center;
}

.card {
  margin-bottom: 24rpx;
  padding: 24rpx 28rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.card__hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.card__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #111827;
}

.card__link {
  font-size: 24rpx;
  color: #059669;
}

.row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.row:last-child {
  border-bottom: 0;
}

.row__check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 8rpx;
  border: 2rpx solid #d1d5db;
  box-sizing: border-box;
  flex-shrink: 0;
}

.row__check.on {
  border-color: #059669;
  background: #059669;
  box-shadow: inset 0 0 0 6rpx #fff;
}

.row__ico {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
  flex-shrink: 0;
}

.row__label {
  flex: 1;
  min-width: 0;
  font-size: 28rpx;
  color: #111827;
}

.row__act {
  font-size: 24rpx;
  color: #059669;
  flex-shrink: 0;
}

.row__act--danger {
  color: #e11d48;
}
</style>
