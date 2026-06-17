<template>
  <c7-dialog v-model="visible" title="发布与嵌入" width="720px" :on-confirm="submit">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="应用发布" name="publish">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="发布后将快照当前草稿配置，演示与嵌入均使用已发布版本。"
          style="margin-bottom: 16px"
        />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="应用名称">{{ appName }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="status === 'published' ? 'success' : 'info'">{{ statusLabel }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="嵌入配置" name="embed" :disabled="status !== 'published'">
        <el-form :model="form" label-width="120px">
          <el-form-item label="启用嵌入">
            <el-switch v-model="form.enabled" />
          </el-form-item>
          <el-form-item label="嵌入令牌">
            <el-input v-model="form.embedToken" readonly>
              <template #append>
                <el-button @click="copyText(form.embedToken)">复制</el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="域名白名单">
            <el-input
              v-model="form.allowedOrigins"
              type="textarea"
              :rows="2"
              placeholder="逗号分隔，如 https://example.com；留空不限制"
            />
          </el-form-item>
          <el-form-item label="iframe 代码">
            <el-input v-model="iframeCode" type="textarea" :rows="3" readonly />
            <el-button link type="primary" @click="copyText(iframeCode)">复制 iframe</el-button>
          </el-form-item>
          <el-form-item label="script 代码">
            <el-input v-model="scriptCode" type="textarea" :rows="4" readonly />
            <el-button link type="primary" @click="copyText(scriptCode)">复制 script</el-button>
          </el-form-item>
          <el-form-item label="菜单路由">
            <el-input v-model="form.menuPath" placeholder="可选，如 /ai/app/chat/1" />
          </el-form-item>
          <el-form-item label="组件路径">
            <el-input v-model="form.menuComponent" placeholder="可选，如 ai/app/chat/index" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </c7-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiAppEmbedInfo, publishAiApp, saveAiAppEmbed } from '@/api/ai/app'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  appId: { type: [Number, String], default: null },
  appName: { type: String, default: '' },
  status: { type: String, default: 'draft' }
})

const emit = defineEmits(['update:modelValue', 'published'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const activeTab = ref('publish')
const form = ref({
  appId: null,
  embedToken: '',
  allowedOrigins: '',
  menuPath: '',
  menuComponent: '',
  enabled: false
})

const statusLabel = computed(() => (props.status === 'published' ? '已发布' : '草稿'))

const embedPageUrl = computed(() => {
  if (!form.value.embedToken) return ''
  const origin = window.location.origin
  return `${origin}/ai/embed/${form.value.embedToken}`
})

const iframeCode = computed(() => {
  if (!embedPageUrl.value) return ''
  return `<iframe src="${embedPageUrl.value}" width="100%" height="600" frameborder="0"></iframe>`
})

const scriptCode = computed(() => {
  if (!embedPageUrl.value) return ''
  return `<div id="ai-app-embed"></div>\n<script>\n(function(){\n  var f=document.createElement('iframe');\n  f.src='${embedPageUrl.value}';\n  f.width='100%';f.height='600';f.frameBorder='0';\n  document.getElementById('ai-app-embed').appendChild(f);\n})();\n<\/script>`
})

watch(
  () => props.modelValue,
  async (open) => {
    if (!open || !props.appId) return
    activeTab.value = props.status === 'published' ? 'embed' : 'publish'
    const res = await getAiAppEmbedInfo(props.appId)
    const data = res.data || {}
    form.value = {
      appId: props.appId,
      embedToken: data.embedToken || '',
      allowedOrigins: data.allowedOrigins || '',
      menuPath: data.menuPath || '',
      menuComponent: data.menuComponent || '',
      enabled: !!data.enabled
    }
  }
)

async function submit() {
  if (activeTab.value === 'publish' && props.status !== 'published') {
    await publishAiApp({ appId: props.appId })
    ElMessage.success('发布成功')
    emit('published')
    activeTab.value = 'embed'
    const res = await getAiAppEmbedInfo(props.appId)
    form.value.embedToken = res.data?.embedToken || ''
    return
  }
  if (activeTab.value === 'embed') {
    await saveAiAppEmbed({ ...form.value, appId: props.appId })
    ElMessage.success('嵌入配置已保存')
  }
}

function copyText(text) {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制'))
}
</script>
