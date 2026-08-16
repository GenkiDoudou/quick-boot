<template>
  <component :is="type" v-bind="linkProps()">
    <slot />
  </component>
</template>

<script setup>
/**
 * 菜单链接包装：外链用 <a> 新标签打开；meta.link 内嵌页强制 router-link。
 */
import { isExternal } from '@/utils/validate'

const props = defineProps({
  to: {
    type: [String, Object],
    required: true
  },
  /** 侧栏菜单 meta；含 link 时强制 router 内嵌，不走新标签页 */
  meta: {
    type: Object,
    default: null
  }
})

const isExt = computed(() => {
  if (props.meta?.link) {
    return false
  }
  return isExternal(props.to)
})

const type = computed(() => {
  if (isExt.value) {
    return 'a'
  }
  return 'router-link'
})

function linkProps() {
  if (isExt.value) {
    return {
      href: props.to,
      target: '_blank',
      rel: 'noopener'
    }
  }
  return {
    to: props.to
  }
}
</script>
