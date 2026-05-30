<template>
  <el-menu
    class="topbar-menu"
    mode="horizontal"
    :ellipsis="false"
    :default-active="activeMenu"
    :active-text-color="theme"
  >
    <sidebar-item
      v-for="(item, index) in topMenus"
      :key="item.path + index"
      :item="item"
      :base-path="item.path"
    />
    <el-sub-menu v-if="moreRoutes.length > 0" index="more" class="el-sub-menu__hide-arrow">
      <template #title><span>更多菜单</span></template>
      <sidebar-item
        v-for="(item, index) in moreRoutes"
        :key="'more-' + item.path + index"
        :item="item"
        :base-path="item.path"
      />
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import SidebarItem from '../Sidebar/SidebarItem.vue'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const route = useRoute()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()

const theme = computed(() => settingsStore.theme)
const visibleNumber = ref(5)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) {
    return meta.activeMenu
  }
  return path
})

const topMenus = computed(() =>
  permissionStore.sidebarRouters.filter((f) => !f.hidden).slice(0, visibleNumber.value)
)

const moreRoutes = computed(() =>
  permissionStore.sidebarRouters.filter((f) => !f.hidden).slice(visibleNumber.value)
)

function setVisibleNumber() {
  const width = document.body.getBoundingClientRect().width / 3
  visibleNumber.value = Math.max(1, parseInt(width / 85, 10))
}

onMounted(() => {
  setVisibleNumber()
  window.addEventListener('resize', setVisibleNumber)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setVisibleNumber)
})
</script>

<style lang="scss">
.topbar-menu.el-menu--horizontal > .el-menu-item {
  height: 50px !important;
  line-height: 50px !important;
  color: #303133 !important;
  padding: 0 12px !important;
  margin: 0 4px !important;
}

.topbar-menu.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  line-height: 50px !important;
  color: #303133 !important;
}

.topbar-menu .svg-icon {
  margin-right: 4px;
}
</style>
