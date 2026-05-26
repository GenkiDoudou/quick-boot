<template>
  <inner-link
    v-for="(item, index) in tagsViewStore.iframeViews"
    :key="item.path"
    :iframeId="'iframe' + index"
    v-show="route.path === item.path"
    :src="iframeUrl(item.meta.link, item.query)"
  ></inner-link>
</template>

<script setup>
import InnerLink from '../InnerLink/index.vue'
import useTagsViewStore from '@/store/modules/tagsView'
import { getToken } from '@/utils/auth'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

function iframeUrl(url, query) {
  const params = { ...query }
  const token = getToken()
  if (token && !params.token) {
    params.token = token
  }
  const keys = Object.keys(params)
  if (keys.length > 0) {
    const qs = keys.map((key) => key + '=' + encodeURIComponent(params[key])).join('&')
    const sep = url.includes('?') ? '&' : '?'
    return url + sep + qs
  }
  return url
}
</script>
