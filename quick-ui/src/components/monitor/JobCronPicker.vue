<template>
  <el-dialog
    v-model="visible"
    title="Cron 表达式"
    width="720px"
    append-to-body
    destroy-on-close
    @closed="onClosed"
  >
    <Crontab :expression="inner" @fill="onFill" @hide="onHide" />
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import Crontab from '@/components/Crontab/index.vue'

/**
 * 定时任务 Cron 表达式选择弹窗（若依 Crontab 组件，避免 vue3-cron-plus 与 Vite 双 Vue 实例问题）。
 */
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  expression: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
const inner = ref('')

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v
    if (v) {
      inner.value = (props.expression || '').trim()
    }
  },
  { immediate: true }
)

watch(visible, (v) => emit('update:modelValue', v))

function onFill(value) {
  inner.value = (value || '').trim()
  emit('confirm', inner.value)
  visible.value = false
}

function onHide() {
  visible.value = false
}

function onClosed() {
  emit('update:modelValue', false)
}
</script>
