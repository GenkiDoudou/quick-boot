<script setup lang="ts">
/**
 * 首页 Tab：问候 + 真快捷入口（API）+ 消息/待办 mock 壳。
 * 快捷失败不回退假入口；编辑进入 /pages/home/shortcuts。
 */
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  fetchH5HomeShortcuts,
  type H5WorkbenchItem,
} from '@/api/system/menu'
import { useUserStore } from '@/stores'
import { homeMessages, homeTodos, type TodoItem } from '@/mock/homeData'
import { toastErr, toastInfo } from '@/utils/toast'

const user = useUserStore()
const displayName = computed(() => user.nickName || user.username || '用户')

/** 真快捷；加载失败保持空数组（禁止 mock 回退） */
const shortcuts = ref<H5WorkbenchItem[]>([])
const shortcutsLoading = ref(false)

const todos = ref<TodoItem[]>(homeTodos.map((t) => ({ ...t })))
const openTodoCount = computed(() => todos.value.filter((t) => !t.done).length)

/** 图标短字：优先 icon 首字，否则 label 首字 */
function shortOf(label?: string, icon?: string) {
  const s = (icon && icon.trim()) || (label && label.trim()) || '?'
  return s.slice(0, 1)
}

async function loadShortcuts() {
  if (shortcutsLoading.value) return
  shortcutsLoading.value = true
  try {
    const list = await fetchH5HomeShortcuts()
    shortcuts.value = Array.isArray(list) ? list : []
  }
  catch (e) {
    shortcuts.value = []
    toastErr(e)
  }
  finally {
    shortcutsLoading.value = false
  }
}

function onShortcut(item: H5WorkbenchItem) {
  const path = String(item.path || '').trim()
  if (!path) {
    toastInfo('未配置跳转')
    return
  }
  uni.navigateTo({ url: path })
}

function goEditShortcuts() {
  uni.navigateTo({ url: '/pages/home/shortcuts' })
}

function onMessage() {
  toastInfo('消息详情待接入')
}

function toggleTodo(item: TodoItem) {
  item.done = !item.done
}

onShow(() => {
  loadShortcuts()
})
</script>

<template>
  <view class="qb-page home">
    <view class="home__hero">
      <text class="home__hi">Quick Boot</text>
      <text class="home__name">你好，{{ displayName }}</text>
    </view>

    <view class="card">
      <view class="card__hd">
        <text class="card__title">快捷入口</text>
        <text class="card__more" @click="goEditShortcuts">编辑</text>
      </view>
      <view v-if="shortcutsLoading && !shortcuts.length" class="qb-muted home__hint">加载中…</view>
      <view v-else-if="!shortcuts.length" class="qb-muted home__hint">暂无快捷，点击编辑添加</view>
      <view v-else class="grid">
        <view
          v-for="item in shortcuts"
          :key="String(item.id)"
          class="grid__item"
          @click="onShortcut(item)"
        >
          <view class="grid__ico">
            <text>{{ shortOf(item.label, item.icon) }}</text>
          </view>
          <text class="grid__label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card__hd">
        <text class="card__title">消息</text>
        <text class="card__more">全部</text>
      </view>
      <view
        v-for="msg in homeMessages"
        :key="msg.id"
        class="msg"
        :class="{ 'msg--unread': msg.unread }"
        @click="onMessage"
      >
        <text class="msg__tag" :class="`msg__tag--${msg.tagType}`">{{ msg.tag }}</text>
        <text class="msg__body">{{ msg.title }}</text>
        <text class="msg__time">{{ msg.time }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card__hd">
        <text class="card__title">今天待办</text>
        <text class="card__more">{{ openTodoCount }}</text>
      </view>
      <view
        v-for="todo in todos"
        :key="todo.id"
        class="todo"
        :class="{ 'todo--done': todo.done }"
        @click="toggleTodo(todo)"
      >
        <view class="todo__check" />
        <text class="todo__title">{{ todo.title }}</text>
        <text class="todo__prio" :class="{ 'todo__prio--high': todo.priority === '高' }">
          {{ todo.priority }}
        </text>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.home {
  padding: 24rpx 28rpx calc(24rpx + env(safe-area-inset-bottom));
  background: #f3f5f7;
}

.home__hero {
  margin-bottom: 24rpx;
  padding: 32rpx;
  border-radius: 28rpx;
  color: #fff;
  background: linear-gradient(135deg, #064e3b 0%, #059669 70%, #34d399 100%);
  box-shadow: 0 12rpx 28rpx rgba(5, 150, 105, 0.22);
}

.home__hi {
  display: block;
  font-size: 24rpx;
  opacity: 0.85;
}

.home__name {
  display: block;
  margin-top: 8rpx;
  font-size: 40rpx;
  font-weight: 700;
}

.home__hint {
  padding: 12rpx 0 4rpx;
  text-align: center;
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

.card__more {
  font-size: 24rpx;
  color: #059669;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx 8rpx;
}

.grid__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.grid__ico {
  position: relative;
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
  font-size: 22rpx;
  color: #374151;
  text-align: center;
}

.msg {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
  font-size: 26rpx;
}

.msg:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.msg:first-of-type {
  padding-top: 0;
}

.msg--unread .msg__body {
  font-weight: 600;
  color: #111827;
}

.msg__tag {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  font-size: 20rpx;
  background: #dbeafe;
  color: #1d4ed8;
}

.msg__tag--sys {
  background: #ecfdf5;
  color: #047857;
}

.msg__tag--warn {
  background: #fef3c7;
  color: #b45309;
}

.msg__body {
  flex: 1;
  min-width: 0;
  color: #374151;
  line-height: 1.4;
}

.msg__time {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #9ca3af;
}

.todo {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.todo:last-child {
  border-bottom: 0;
}

.todo__check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 3rpx solid #d1d5db;
  flex-shrink: 0;
}

.todo--done .todo__check {
  background: #059669;
  border-color: #059669;
}

.todo__title {
  flex: 1;
  font-size: 26rpx;
  color: #111827;
}

.todo--done .todo__title {
  color: #9ca3af;
  text-decoration: line-through;
}

.todo__prio {
  font-size: 22rpx;
  color: #6b7280;
}

.todo__prio--high {
  color: #dc2626;
}
</style>
