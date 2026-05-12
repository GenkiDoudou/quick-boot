<template>
  <div>
    <!-- 遍历所有选项，显示匹配的标签 -->
    <template v-for="(item, index) in options" :key="item.value">
      <template v-if="values.includes(String(item.value))">
        <!-- 纯文本模式：当 elTagType 为 default 或空，且没有自定义类名时 -->
        <span
            v-if="(item.elTagType == 'default' || item.elTagType == '') && (item.elTagClass == '' || item.elTagClass == null)"
            :class="item.elTagClass"
        >{{ item.label + " " }}</span>
        
        <!-- 标签模式：使用 el-tag 组件 -->
        <el-tag
            v-else
            :disable-transitions="true"
            :type="item.elTagType === 'primary' ? '' : item.elTagType"
            :class="item.elTagClass"
            :size="size"
        >{{ item.label + " " }}
        </el-tag>
      </template>
    </template>
    
    <!-- 显示未匹配的原始值 -->
    <template v-if="unmatch && showValue">
      {{ unmatchArray.join(' ') }}
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, defineOptions } from 'vue'

defineOptions({
  name: 'C7DictTag'
})

/**
 * 字典选项接口
 */
interface DictOption {
  label: string              // 显示文本
  value: string | number     // 选项值
  elTagType?: string         // 标签类型：success/info/warning/danger/primary/default
  elTagClass?: string        // 自定义类名
}

/**
 * 组件属性接口
 */
interface Props {
  options?: DictOption[]                           // 字典选项列表
  modelValue?: number | string | (string | number)[] // 当前值，支持单值或多值
  showValue?: boolean                              // 未匹配时是否显示原始值
  separator?: string                               // 多值分隔符
  size?: 'large' | 'default' | 'small'            // 标签大小
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  modelValue: undefined,
  showValue: true,
  separator: ',',
  size: 'default'
})

// 记录未匹配的项
const unmatchArray = ref<(string | number)[]>([])

/**
 * 计算属性：将 modelValue 转换为字符串数组
 * 支持数组、字符串、数字类型的输入
 */
const values = computed(() => {
  // 空值处理
  if (props.modelValue === null || typeof props.modelValue === 'undefined' || props.modelValue === '') {
    return []
  }
  
  // 数组类型：转换为字符串数组
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.map(item => String(item))
  }
  
  // 字符串类型：按分隔符拆分
  return String(props.modelValue).split(props.separator)
})

/**
 * 计算属性：检查是否有未匹配的值
 * 未匹配的值会被记录到 unmatchArray 中
 */
const unmatch = computed(() => {
  unmatchArray.value = []
  
  // 没有 value 或没有 options 时不显示
  if (props.modelValue === null || 
      typeof props.modelValue === 'undefined' || 
      props.modelValue === '' || 
      props.options.length === 0) {
    return false
  }
  
  let hasUnmatch = false
  
  // 检查每个值是否在 options 中存在
  values.value.forEach(item => {
    const found = props.options.some(v => String(v.value) === item)
    if (!found) {
      unmatchArray.value.push(item)
      hasUnmatch = true
    }
  })
  
  return hasUnmatch
})
</script>

<style scoped>
/* 标签之间的间距 */
.el-tag + .el-tag {
  margin-left: 10px;
}
</style>

