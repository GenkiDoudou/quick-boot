<template>
  <div>
    <el-radio-group
        v-model="innerValue"
        @change="handleChange"
    >
      <el-radio
          v-for="item in options"
          :key="item.value"
          :label="item.value"
      >
        {{ item.label }}
      </el-radio>
    </el-radio-group>
  </div>
</template>

<script setup lang="ts">
import { computed, defineOptions } from 'vue'

defineOptions({
  name: 'C7Radio'
})

/**
 * 选项接口
 */
interface Option {
  label: string              // 显示文本
  value: string | number     // 选项值
}

/**
 * 组件属性接口
 */
interface Props {
  modelValue?: string | number  // 绑定值，支持字符串或数字
  dataList?: Option[]           // 选项数据列表
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  dataList: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'change': [value: string | number]
}>()

/**
 * 内部绑定值
 * 使用计算属性实现双向绑定
 */
const innerValue = computed({
  get() {
    return props.modelValue
  },
  set(val: string | number) {
    emit('update:modelValue', val)
  }
})

/**
 * 选项列表
 * 直接使用 dataList
 */
const options = computed(() => props.dataList)

/**
 * 处理选中后触发 change 事件
 * @param val 选中的值
 */
function handleChange(val: string | number) {
  emit('change', val)
}
</script>

