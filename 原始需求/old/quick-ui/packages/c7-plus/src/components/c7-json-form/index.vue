<template>
  <el-form
    ref="formRef"
    :model="formData"
  >
    <el-row :gutter="20">
      <el-col
        :span="getColumnSpan(item)"
        v-for="(item, index) in sortedColumns"
        :key="index"
      >
        <el-form-item
          :label="item.label"
          :prop="item.prop"
          :required="item.required ?? false"
          :rules="item.rules"
        >
        <!-- 自定义插槽 -->
        <template v-if="item.type === 'slot'">
          <slot
              :name="item.slotName || ('slot_' + item.prop)"
              :item="item"
              :modelValue="formData[item.prop]"
              @change="handleChange(item, $event)"
          ></slot>
        </template>
        
        <!-- 输入框 -->
        <el-input
            v-else-if="(item.type === 'input' || !item.type) && item.display"
            v-model="formData[item.prop]"
            :placeholder="getPlaceholder(item)"
            :disabled="isFieldDisabled(item)"
            @input="(value) => handleInput(item, value)"
        />

        <!-- 数字输入框 -->
        <el-input-number
            v-else-if="item.type === 'input-number' && item.display"
            v-model="formData[item.prop]"
            :min="item.min"
            :max="item.max"
            :precision="item.precision"
            :placeholder="getPlaceholder(item)"
            :disabled="isFieldDisabled(item)"
            @input="(value) => handleInput(item, value)"
        />

        <!-- 下拉选择 -->
        <c7-select
            v-else-if="item.type === 'select'"
            v-model="formData[item.prop]"
            :placeholder="item.placeholder || '请选择' + item.label"
            :options="item.dataList"
            :label-key="item.labelKey || 'label'"
            :value-key="item.valueKey || 'value'"
            :multiple="item.multiple || false"
            :fetch-data="item.fetchData"
            :fetch-params="item.fetchParams"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 日期选择器 -->
        <c7-date-picker
            v-else-if="datePickerTypes.includes(item.type)"
            v-model="formData[item.prop]"
            :type="item.type"
            :value-format="item.valueFormat"
            :placeholder="getDatePickerPlaceholder(item)"
            :format="item.format"
            :start-placeholder="getDatePickerStartPlaceholder(item)"
            :end-placeholder="getDatePickerEndPlaceholder(item)"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 复选框 -->
        <c7-checkbox
            v-else-if="item.type === 'checkbox'"
            v-model="formData[item.prop]"
            :data-list="item.dataList"
            :label-key="item.labelKey || 'label'"
            :value-key="item.valueKey || 'value'"
            :separator="item.separator !== false"
            :fetch-data="item.fetchData"
            :fetch-params="item.fetchParams"
            :indeterminate="item.indeterminate || false"
            :button="item.button || false"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 单选框 -->
        <c7-radio
            v-else-if="item.type === 'radio'"
            v-model="formData[item.prop]"
            :data-list="item.dataList"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 文件上传 -->
        <c7-upload
            v-else-if="item.type === 'upload'"
            v-model="formData[item.prop]"
            :upload-url="item.uploadUrl"
            :limit="item.limit"
            :file-type="item.fileType"
            :file-size="item.fileSize"
            @change="handleChange(item, formData[item.prop])"
        />
      </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { computed, ref, watch, defineOptions, onUnmounted, nextTick } from 'vue'
import C7Select from '../c7-select/index.vue'
import C7Upload from '../c7-upload/index.vue'
import C7DatePicker from '../c7-date-picker/index.vue'
import C7Checkbox from '../c7-checkbox/index.vue'
import C7Radio from '../c7-radio/index.vue'
import type { FormColumn } from '../../types/form'

defineOptions({
  name: 'C7JsonForm'
})

/**
 * 组件属性接口
 */
interface Props {
  columns?: FormColumn[]                // 表单列配置
  modelValue?: Record<string, any>      // 表单数据
}

const props = withDefaults(defineProps<Props>(), {
  columns: () => [],
  modelValue: () => ({})
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
  'validate': [valid: boolean, fields?: any]
}>()

// 表单引用
const formRef = ref()

/**
 * 表单数据存储
 * 注意：必须使用 ref 而不是 shallowRef，因为 v-model 需要深度响应式
 */
const formData = ref<Record<string, any>>({})

// 防止循环更新的标志（使用 ref 确保响应式）
const isUpdatingFromProps = ref(false)
const isUpdatingFromForm = ref(false)

/**
 * 监听 formData 的变化，触发 update:modelValue 事件
 */
const stopWatchFormData = watch(
    formData,
    (newVal) => {
      // 如果正在从 props 更新，跳过
      if (isUpdatingFromProps.value) {
        return
      }
      
      isUpdatingFromForm.value = true
      try {
        emit('update:modelValue', { ...newVal })
      } finally {
        // 使用 nextTick 而不是 setTimeout，避免定时器泄漏
        nextTick(() => {
          isUpdatingFromForm.value = false
        })
      }
    },
    { deep: true }
)

/**
 * 监听 props.modelValue 的变化，同步到 formData
 */
const stopWatchModelValue = watch(
    () => props.modelValue,
    (newVal) => {
      // 如果正在从 formData 更新，跳过
      if (isUpdatingFromForm.value) {
        return
      }
      
      isUpdatingFromProps.value = true
      try {
        if (!newVal || Object.keys(newVal).length === 0) {
          if (Object.keys(formData.value).length > 0) {
            formData.value = {}
          }
        } else {
          formData.value = { ...newVal }
        }
      } finally {
        // 使用 nextTick 而不是 setTimeout，避免定时器泄漏
        nextTick(() => {
          isUpdatingFromProps.value = false
        })
      }
    },
    { immediate: true, deep: true }
)

/**
 * 组件卸载时清理所有资源
 */
onUnmounted(() => {
  // 停止所有 watch
  stopWatchFormData()
  stopWatchModelValue()
  
  // 重置状态
  isUpdatingFromProps.value = false
  isUpdatingFromForm.value = false
  formData.value = {}
})

/**
 * 输入处理
 * @param column 列配置
 * @param value 输入值
 */
const handleInput = (column: FormColumn, value: any) => {
  formData.value[column.prop] = value
  
  if (typeof column.change === 'function') {
    column.change({
      prop: column.prop,
      value: value,
      formData: formData.value
    })
  }
}

/**
 * 计算属性：排序后的列配置（包含联动逻辑）
 */
const sortedColumns = computed(() => {
  return props.columns
      .filter(column => {
        // 基础可见性过滤
        if (column.visible === false) {
          return false
        }
        
        // 联动可见性判断
        if (column.visibleWhen && typeof column.visibleWhen === 'function') {
          return column.visibleWhen(formData.value)
        }
        
        return true
      })
      .map((item, index) => {
        // 应用联动配置
        let linkedConfig: Partial<FormColumn> = {}
        if (item.dependsOn && item.linkage && typeof item.linkage === 'function') {
          const dependsValue = Array.isArray(item.dependsOn)
            ? item.dependsOn.map(prop => formData.value[prop])
            : formData.value[item.dependsOn]
          linkedConfig = item.linkage(dependsValue, formData.value) || {}
        }
        
        // 动态选项
        let dynamicOptions: IObject[] | undefined = undefined
        if (item.optionsWhen && typeof item.optionsWhen === 'function') {
          dynamicOptions = item.optionsWhen(formData.value)
        }
        
        return {
          ...item,
          ...linkedConfig,
          ...(dynamicOptions ? { dataList: dynamicOptions } : {}),
          order: item.order ?? index,
          span: item.span ?? 8,
          display: item.display ?? true,
        }
      })
      .sort((a, b) => a.order - b.order)
})

/**
 * 计算属性：字段是否禁用（考虑联动）
 */
const isFieldDisabled = (column: FormColumn): boolean => {
  if (column.disabled === true) {
    return true
  }
  
  // 联动禁用判断
  if (column.disabledWhen && typeof column.disabledWhen === 'function') {
    return column.disabledWhen(formData.value)
  }
  
  return false
}

/**
 * 获取列跨度
 * @param column 列配置
 */
const getColumnSpan = (column: FormColumn): number => {
  return column.span ?? 8
}

/**
 * 获取占位符文本
 * @param column 列配置
 */
const getPlaceholder = (column: FormColumn): string => {
  return column.placeholder ?? `请输入${column.label}`
}

/**
 * 获取日期选择器的占位符（处理范围类型的数组占位符）
 * @param column 列配置
 */
const getDatePickerPlaceholder = (column: FormColumn): string | undefined => {
  const isRangeType = ['daterange', 'datetimerange', 'monthrange', 'yearrange'].includes(column.type || '')
  
  // 如果是范围类型且 placeholder 是数组，返回 undefined（使用 start-placeholder 和 end-placeholder）
  if (isRangeType && Array.isArray(column.placeholder)) {
    return undefined
  }
  
  // 其他情况返回字符串占位符
  return typeof column.placeholder === 'string' ? column.placeholder : (column.placeholder ? undefined : `请选择${column.label}`)
}

/**
 * 获取日期选择器的开始占位符
 * @param column 列配置
 */
const getDatePickerStartPlaceholder = (column: FormColumn): string | undefined => {
  const isRangeType = ['daterange', 'datetimerange', 'monthrange', 'yearrange'].includes(column.type || '')
  
  // 如果明确指定了 startPlaceholder，使用它
  if (column.startPlaceholder) {
    return column.startPlaceholder
  }
  
  // 如果是范围类型且 placeholder 是数组，使用第一个元素
  if (isRangeType && Array.isArray(column.placeholder) && column.placeholder.length > 0) {
    return column.placeholder[0]
  }
  
  return undefined
}

/**
 * 获取日期选择器的结束占位符
 * @param column 列配置
 */
const getDatePickerEndPlaceholder = (column: FormColumn): string | undefined => {
  const isRangeType = ['daterange', 'datetimerange', 'monthrange', 'yearrange'].includes(column.type || '')
  
  // 如果明确指定了 endPlaceholder，使用它
  if (column.endPlaceholder) {
    return column.endPlaceholder
  }
  
  // 如果是范围类型且 placeholder 是数组，使用第二个元素
  if (isRangeType && Array.isArray(column.placeholder) && column.placeholder.length > 1) {
    return column.placeholder[1]
  }
  
  return undefined
}

/**
 * 日期选择器支持的类型列表
 */
const datePickerTypes = ref([
  'year', 'years', 'month', 'months', 'date', 'dates', 'datetime', 
  'week', 'datetimerange', 'daterange', 'monthrange', 'yearrange'
])

/**
 * change 事件处理
 * @param item 列配置
 * @param value 变更值
 */
const handleChange = (item: FormColumn, value: any) => {
  if (item?.change && typeof item.change === 'function') {
    item.change({
      prop: item.prop,
      value: value,
      formData: formData.value
    })
  }
}
</script>

<style scoped>
.form-item {
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .el-col {
    width: 100%;
    margin-bottom: 12px;
  }
}
</style>

