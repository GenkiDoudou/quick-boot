<script setup lang="ts">
/**
 * 工作台 Tab：从 /system/menu/h5Workbench 按角色渲染；有 path 则 navigateTo。
 * 失败时 toast + 空态，不回退全量 mock（避免暴露未授权入口）。
 */
import { onShow } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { fetchH5Workbench, type H5WorkbenchGroup } from '@/api/system/menu'
import { toastErr } from '@/utils/toast'

const groups = ref<H5WorkbenchGroup[]>([])
const loading = ref(false)
const loadError = ref(false)

/** 取展示用短字：优先 icon 首字，否则 label 首字 */
function shortOf(label?: string, icon?: string) {
  const s = (icon && icon.trim()) || (label && label.trim()) || '?'
  return s.slice(0, 1)
}

async function load() {
  if (loading.value) return
  loading.value = true
  loadError.value = false
  try {
    const data = await fetchH5Workbench()
    groups.value = Array.isArray(data) ? data : []
  }
  catch (e) {
    loadError.value = true
    groups.value = []
    toastErr(e)
  }
  finally {
    loading.value = false
  }
}

function onMenu(path?: string, label?: string) {
  if (!path) {
    toastErr(new Error(`${label || '菜单'}未配置跳转`))
    return
  }
  uni.navigateTo({ url: path })
}

onShow(() => {
  load()
})
</script>

<template>
  <view class="qb-page workbench">
    <view class="workbench__tip">
      工作台菜单由后台按角色下发（sys_menu，path 以 /pages/ 开头）。
    </view>

    <view v-if="loading" class="workbench__empty qb-muted">加载中…</view>
    <view v-else-if="loadError" class="workbench__empty qb-muted">菜单加载失败，请下拉重进本页</view>
    <view v-else-if="!groups.length" class="workbench__empty qb-muted">暂无可用菜单，请联系管理员授权</view>

    <view v-for="group in groups" :key="group.id" class="card">
      <view class="card__hd">
        <text class="card__title">{{ group.title }}</text>
      </view>
      <view class="grid">
        <view
          v-for="item in group.items"
          :key="item.id"
          class="grid__item"
          @click="onMenu(item.path, item.label)"
        >
          <view class="grid__ico">
            <text>{{ shortOf(item.label, item.icon) }}</text>
          </view>
          <text class="grid__label">{{ item.label }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.workbench {
  padding: 24rpx 28rpx calc(24rpx + env(safe-area-inset-bottom));
  background: #f3f5f7;
}

.workbench__tip {
  margin-bottom: 24rpx;
  padding: 20rpx 24rpx;
  border-radius: 20rpx;
  border: 2rpx dashed #a7f3d0;
  background: #fff;
  font-size: 24rpx;
  color: #6b7280;
  line-height: 1.5;
}

.workbench__empty {
  text-align: center;
  padding: 48rpx 0;
}

.card {
  margin-bottom: 24rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: #fff;
  box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.04);
}

.card__hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.card__title {
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 28rpx 8rpx;
}

.grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.grid__ico {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
}

.grid__label {
  font-size: 24rpx;
  color: #374151;
  text-align: center;
}
</style>
