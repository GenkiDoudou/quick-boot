<template>
  <section class="app-main" :class="{ 'app-main--fullscreen': route.meta?.fullScreen }">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="tagsViewStore.cachedViews">
          <component v-if="!route.meta.link" :is="Component" :key="route.path" />
        </keep-alive>
      </transition>
    </router-view>
    <iframe-toggle />
  </section>
</template>

<script setup>
import IframeToggle from './IframeToggle/index.vue'
import useTagsViewStore from '@/store/modules/tagsView'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

/** 外链/积木 iframe 不依赖 TagsView 是否开启，进入路由即登记 */
watch(
  () => route.path,
  () => {
    if (route.meta?.link) {
      tagsViewStore.addIframeView(route)
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.app-main {
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow-y: auto;
  overflow-x: hidden;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.app-main--fullscreen {
  min-height: 100vh;
  padding-top: 0 !important;
  overflow: hidden;
}

.hasTagsView {
  .app-main {
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}
</style>
