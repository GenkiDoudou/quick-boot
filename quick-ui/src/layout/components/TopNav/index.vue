<template>
  <el-menu
    :default-active="activeMenu"
    mode="horizontal"
    class="topmenu-menu"
    :ellipsis="false"
    @select="handleSelect"
  >
    <template v-for="(item, index) in topMenus" :key="item.path + index">
      <el-menu-item v-if="index < visibleNumber" :style="{ '--theme': theme }" :index="item.path">
        <svg-icon
          v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
          :icon-class="item.meta.icon"
        />
        {{ item.meta?.title }}
      </el-menu-item>
    </template>

    <el-sub-menu v-if="topMenus.length > visibleNumber" :style="{ '--theme': theme }" index="more">
      <template #title>更多菜单</template>
      <template v-for="(item, index) in topMenus" :key="'more-' + item.path + index">
        <el-menu-item v-if="index >= visibleNumber" :index="item.path">
          <svg-icon
            v-if="item.meta && item.meta.icon && item.meta.icon !== '#'"
            :icon-class="item.meta.icon"
          />
          {{ item.meta?.title }}
        </el-menu-item>
      </template>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
/**
 * 混合导航顶栏（navType=2）：一级菜单横向展示，选中后联动侧栏子菜单。
 */
import { isHttp } from '@/utils/validate'
import {
  applyMixSidebar,
  buildChildrenMenus,
  buildTopMenus,
  resolveMixTopPath
} from '@/utils/navLayout'
import { parseRouteQuery } from '@/utils/ruoyi'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

const visibleNumber = ref(6)

const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

const theme = computed(() => settingsStore.theme)

const routers = computed(() => {
  if (permissionStore.topbarRouters?.length) {
    return permissionStore.topbarRouters
  }
  return permissionStore.defaultRoutes || []
})

const topMenus = computed(() => buildTopMenus(routers.value))
const childrenMenus = computed(() => buildChildrenMenus(routers.value))

const activeMenu = computed(() => {
  if (route.meta?.link) {
    return route.path
  }
  return resolveMixTopPath(route)
})

/** 根据当前路由同步混合模式下侧栏显示的子菜单 */
function syncSidebarByRoute() {
  if (!routers.value.length) {
    return
  }
  applyMixSidebar({
    activeTopPath: activeMenu.value,
    permissionStore,
    appStore,
    routers: routers.value
  })
}

/** 顶栏可见一级菜单数量（随窗口宽度自适应） */
function setVisibleNumber() {
  const width = document.body.getBoundingClientRect().width / 3
  visibleNumber.value = Math.max(3, parseInt(width / 85, 10))
}

/** 选中顶栏一级菜单：有子菜单则展开侧栏，无子菜单则直接跳转并隐藏侧栏 */
function handleSelect(key) {
  const matched = routers.value.find((item) => item.path === key)
  if (isHttp(key)) {
    window.open(key, '_blank')
    return
  }
  if (!matched || !matched.children?.length) {
    const routeMenu = childrenMenus.value.find((item) => item.path === key)
    const query = parseRouteQuery(routeMenu?.query)
    if (query) {
      router.push({ path: key, query })
    } else {
      router.push({ path: key })
    }
    appStore.toggleSideBarHide(true)
    return
  }
  applyMixSidebar({
    activeTopPath: key,
    permissionStore,
    appStore,
    routers: routers.value
  })
  appStore.toggleSideBarHide(false)
}

watch(
  () => [route.path, routers.value.length],
  () => {
    syncSidebarByRoute()
  },
  { immediate: true }
)

onMounted(() => {
  setVisibleNumber()
  window.addEventListener('resize', setVisibleNumber)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setVisibleNumber)
})
</script>

<style lang="scss">
.topmenu-menu.el-menu--horizontal {
  width: 100%;
  height: 50px !important;
  border-bottom: none;
  background: transparent;
}

.topmenu-menu.el-menu--horizontal > .el-menu-item {
  height: 50px !important;
  line-height: 50px !important;
  color: #303133 !important;
  padding: 0 12px !important;
  margin: 0 4px !important;
}

.topmenu-menu.el-menu--horizontal > .el-menu-item.is-active,
.topmenu-menu.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title {
  border-bottom: 2px solid var(--theme, var(--el-color-primary)) !important;
  color: #303133 !important;
}

.topmenu-menu.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  height: 50px !important;
  line-height: 50px !important;
  color: #303133 !important;
  padding: 0 12px !important;
}

.topmenu-menu.el-menu--horizontal > .el-menu-item:not(.is-disabled):hover,
.topmenu-menu.el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.topmenu-menu .svg-icon {
  margin-right: 4px;
}
</style>
