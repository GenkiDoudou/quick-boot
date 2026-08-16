<script setup lang="ts">
import { useTheme } from 'uview-pro'
import { computed } from 'vue'

const { darkMode, themes, currentTheme } = useTheme()
const currentThemeName = computed(() => currentTheme.value?.name)
</script>

<template>
  <u-config-provider
    :dark-mode="darkMode"
    :themes="themes"
    :current-theme="currentThemeName"
  >
    <KuRootView />
    <u-toast global />
  </u-config-provider>
</template>

<style lang="scss"></style>
