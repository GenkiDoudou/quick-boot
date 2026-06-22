<template>
  <el-config-provider :locale="locale" :size="elementSize" :z-index="2000">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { onMounted, nextTick } from 'vue'
import Cookies from 'js-cookie'
import locale from 'element-plus/es/locale/lang/zh-cn'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

/** 与历史 Cookie 约定一致，供 Element Plus 全局默认尺寸 */
const elementSize = Cookies.get('size') || 'default'

onMounted(() => {
  nextTick(() => {
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>
