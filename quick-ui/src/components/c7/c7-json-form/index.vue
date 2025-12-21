<template>
  <template v-if="sortedColumns.length > 0">
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
            :disabled="item.disabled"
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
            :disabled="item.disabled"
            @input="(value) => handleInput(item, value)"
        />

        <!-- 下拉选择 -->
        <C7Select
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
        <C7DatePicker
            v-else-if="datePickerTypes.includes(item.type)"
            v-model="formData[item.prop]"
            :type="item.type"
            :value-format="item.valueFormat"
            :placeholder="item.placeholder || '请选择' + item.label"
            :format="item.format"
            :start-placeholder="item.startPlaceholder"
            :end-placeholder="item.endPlaceholder"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 复选框 -->
        <C7Checkbox
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
        <C7Radio
            v-else-if="item.type === 'radio'"
            v-model="formData[item.prop]"
            :data-list="item.dataList"
            @change="handleChange(item, formData[item.prop])"
        />

        <!-- 文件上传 -->
        <C7Upload
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
  </template>
</template>

<script setup>
import { computed, ref, watch, defineEmits, defineProps } from 'vue'
import C7Select from '../c7-select/index.vue'
import C7Upload from '../c7-upload/index.vue'
import C7DatePicker from '../c7-date-picker/index.vue'
import C7Checkbox from '../c7-checkbox/index.vue'
import C7Radio from '../c7-radio/index.vue'

defineOptions({
  name: 'JsonForm'
})

const emit = defineEmits(['update:modelValue'])

// 定义表单数据模型
const props = defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: () => ({})
  },
})

// 表单数据存储
const formData = ref({})

// 监听 formData 的变化，触发 update:modelValue 事件
watch(
    formData,
    (newVal) => {
      if (JSON.stringify(newVal) !== JSON.stringify(props.modelValue)) {
        emit('update:modelValue', { ...newVal })
      }
    },
    { deep: true }
)

// 监听 props.modelValue 的变化，同步到 formData
watch(
    () => props.modelValue,
    (newVal) => {
      if (JSON.stringify(newVal) !== JSON.stringify(formData.value)) {
        formData.value = { ...newVal }
      }
    },
    { deep: true }
)

// 输入处理
const handleInput = (column, value) => {
  formData.value[column.prop] = value
  
  if (typeof column.change === 'function') {
    column.change({
      prop: column.prop,
      value: value,
      formData: formData.value
    })
  }
}

// 优化后的计算属性
const sortedColumns = computed(() => {
  return props.columns
      .filter(column => column.visible !== false)
      .map((item, index) => ({
        ...item,
        order: item.order ?? index,
        span: item.span ?? 8,
        display: item.display ?? true,
      }))
      .sort((a, b) => a.order - b.order)
})

// 获取列跨度
const getColumnSpan = (column) => {
  return column.span ?? 8
}

// 获取占位符文本
const getPlaceholder = (column) => {
  return column.placeholder ?? `请输入${column.label}`
}

// 日期选择器支持的类型列表
const datePickerTypes = ref([
  'year', 'years', 'month', 'months', 'date', 'dates', 'datetime', 
  'week', 'datetimerange', 'daterange', 'monthrange', 'yearrange'
])

// change 事件处理
const handleChange = (item, value) => {
  if (item?.change && typeof item.change === 'function') {
    item.change(item, value)
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
