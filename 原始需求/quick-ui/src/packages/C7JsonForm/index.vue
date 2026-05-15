<template>
  <el-form
    ref="formRef"
    :model="formData"
    :label-width="labelWidth"
    :label-position="labelPosition"
    :size="size"
    :disabled="disabled"
    :gutter="gutter"
    v-bind="$attrs"
    @validate="handleValidate"
  >
    <el-row :gutter="gutter">
      <template v-for="col in sortedColumns" :key="col.prop">
        <!-- 联动显示控制 -->
        <el-col
          v-if="isVisible(col)"
          :span="col.span ?? 24"
        >
          <el-form-item
            :prop="col.prop"
            :rules="col.rules"
            :required="col.required"
          >
            <!-- 自定义 label slot：#label-[prop] -->
            <template #label>
              <slot :name="'label-' + col.prop">
                <span>{{ col.label }}</span>
                <!-- tooltip 提示 -->
                <el-tooltip v-if="col.tooltip" :content="col.tooltip" placement="top">
                  <el-icon style="margin-left:4px;vertical-align:middle;cursor:help;"><QuestionFilled /></el-icon>
                </el-tooltip>
              </slot>
            </template>

            <!-- input -->
            <template v-if="col.type === 'input' || !col.type">
              <el-input
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请输入' + col.label"
                :disabled="isDisabled(col)"
                :readonly="readonly || col.readonly"
                :prefix-icon="col.prefixIcon"
                :suffix-icon="col.suffixIcon"
                :clearable="col.clearable ?? true"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              >
                <template v-if="col.prefix" #prefix>{{ col.prefix }}</template>
                <template v-if="col.suffix" #suffix>{{ col.suffix }}</template>
              </el-input>
            </template>

            <!-- input-number -->
            <template v-else-if="col.type === 'input-number'">
              <el-input-number
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请输入' + col.label"
                :disabled="isDisabled(col)"
                :min="col.min"
                :max="col.max"
                :step="col.step"
                :precision="col.precision"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- select -->
            <template v-else-if="col.type === 'select'">
              <C7Select
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请选择' + col.label"
                :disabled="isDisabled(col)"
                :multiple="col.multiple"
                :fetch-data="col.fetchData"
                :data-list="getColumnOptions(col)"
                :label-key="col.labelKey ?? 'label'"
                :value-key="col.valueKey ?? 'value'"
                :filterable="col.filterable"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- date / daterange / datetime / datetimerange / year / month / monthrange -->
            <template v-else-if="col.type && col.type.startsWith('date')">
              <C7DatePicker
                v-model="formData[col.prop]"
                :type="col.type"
                :placeholder="col.placeholder ?? '请选择' + col.label"
                :disabled="isDisabled(col)"
                :format="col.format"
                :value-format="col.valueFormat"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- time -->
            <template v-else-if="col.type === 'time'">
              <C7TimePicker
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请选择' + col.label"
                :disabled="isDisabled(col)"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- checkbox -->
            <template v-else-if="col.type === 'checkbox'">
              <C7Checkbox
                v-model="formData[col.prop]"
                :disabled="isDisabled(col)"
                :fetch-data="col.fetchData"
                :data-list="getColumnOptions(col)"
                :label-key="col.labelKey ?? 'label'"
                :value-key="col.valueKey ?? 'value'"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- radio -->
            <template v-else-if="col.type === 'radio'">
              <C7Radio
                v-model="formData[col.prop]"
                :disabled="isDisabled(col)"
                :fetch-data="col.fetchData"
                :data-list="getColumnOptions(col)"
                :label-key="col.labelKey ?? 'label'"
                :value-key="col.valueKey ?? 'value'"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- switch -->
            <template v-else-if="col.type === 'switch'">
              <el-switch
                v-model="formData[col.prop]"
                :disabled="isDisabled(col)"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- upload -->
            <template v-else-if="col.type === 'upload'">
              <C7Upload
                v-model="formData[col.prop]"
                :http-request="col.httpRequest"
                :response-parser="col.responseParser"
                :upload-url="col.uploadUrl"
                :file-type="col.fileType"
                :file-size="col.fileSize"
                :limit="col.limit ?? 1"
                :list-type="col.listType ?? 'picture-card'"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- textarea -->
            <template v-else-if="col.type === 'textarea'">
              <el-input
                v-model="formData[col.prop]"
                type="textarea"
                :placeholder="col.placeholder ?? '请输入' + col.label"
                :disabled="isDisabled(col)"
                :readonly="readonly || col.readonly"
                :rows="col.rows ?? 3"
                :autosize="col.autosize"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- tree-select -->
            <template v-else-if="col.type === 'tree-select'">
              <C7TreeSelect
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请选择' + col.label"
                :disabled="isDisabled(col)"
                :fetch-data="col.fetchData"
                :data-list="col.dataList"
                :multiple="col.multiple"
                :label-key="col.labelKey ?? 'label'"
                :value-key="col.valueKey ?? 'value'"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- cascader -->
            <template v-else-if="col.type === 'cascader'">
              <C7Cascader
                v-model="formData[col.prop]"
                :placeholder="col.placeholder ?? '请选择' + col.label"
                :disabled="isDisabled(col)"
                :fetch-data="col.fetchData"
                :data-list="col.dataList"
                :label-key="col.labelKey ?? 'label'"
                :value-key="col.valueKey ?? 'value'"
                style="width:100%"
                v-bind="col.props"
                @change="handleFieldChange(col.prop, formData[col.prop])"
              />
            </template>

            <!-- slot 类型：完全自定义 -->
            <template v-else-if="col.type === 'slot'">
              <slot
                :name="col.prop"
                :col="col"
                :form-data="formData"
                :disabled="isDisabled(col)"
                :on-change="(val) => handleFieldChange(col.prop, val)"
              />
            </template>
          </el-form-item>
        </el-col>
      </template>

      <!-- 操作按钮 slot -->
      <el-col v-if="$slots.actions" :span="24">
        <el-form-item>
          <slot name="actions" :form-data="formData" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { QuestionFilled } from '@element-plus/icons-vue'
import C7Select from '../C7Select/index.vue'
import C7DatePicker from '../C7DatePicker/index.vue'
import C7TimePicker from '../C7TimePicker/index.vue'
import C7Checkbox from '../C7Checkbox/index.vue'
import C7Radio from '../C7Radio/index.vue'
import C7Upload from '../C7Upload/index.vue'
import C7TreeSelect from '../C7TreeSelect/index.vue'
import C7Cascader from '../C7Cascader/index.vue'

defineOptions({ name: 'C7JsonForm', inheritAttrs: false })

const props = defineProps({
  /** 字段配置数组 */
  columns: {
    type: Array,
    default: () => []
  },
  /** v-model 绑定的表单数据 */
  modelValue: {
    type: Object,
    default: () => ({})
  },
  /** 全局 label 宽度 */
  labelWidth: {
    type: [String, Number],
    default: ''
  },
  /** label 对齐方式 */
  labelPosition: {
    type: String,
    default: 'right',
    validator: (v) => ['left', 'right', 'top'].includes(v)
  },
  /** 全局表单尺寸 */
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['large', 'default', 'small'].includes(v)
  },
  /** 全局禁用 */
  disabled: {
    type: Boolean,
    default: false
  },
  /** 全局只读 */
  readonly: {
    type: Boolean,
    default: false
  },
  /** 栅格间距，默认 20 */
  gutter: {
    type: Number,
    default: 20
  }
})

const emit = defineEmits([
  'update:modelValue',
  'validate',
  'field-change'
])

// ── 表单 ref ──
const formRef = ref(null)

// ── 防循环更新标志 ──
let isUpdatingFromProps = false
let isUpdatingFromForm = false

// ── 内部表单数据（reactive，确保字段响应性）──
const formData = reactive({})

// ── 初始化 formData：从 columns 收集所有 prop 的默认值 ──
function initFormData(modelVal, cols) {
  cols.forEach(col => {
    if (!(col.prop in formData)) {
      formData[col.prop] = modelVal?.[col.prop] ?? col.defaultValue ?? null
    } else {
      formData[col.prop] = modelVal?.[col.prop] ?? col.defaultValue ?? null
    }
  })
}

// ── 首次初始化 ──
initFormData(props.modelValue, props.columns)

// ── 监听外部 modelValue 变化，同步到内部（防循环）──
watch(
  () => props.modelValue,
  (newVal) => {
    if (isUpdatingFromForm) return
    isUpdatingFromProps = true
    Object.keys(newVal || {}).forEach(key => {
      formData[key] = newVal[key]
    })
    isUpdatingFromProps = false
  },
  { deep: true }
)

// ── 监听内部 formData 变化，同步到外部（防循环）──
watch(
  formData,
  (newVal) => {
    if (isUpdatingFromProps) return
    isUpdatingFromForm = true
    emit('update:modelValue', { ...newVal })
    isUpdatingFromForm = false
  },
  { deep: true }
)

// ── 字段排序：按 order 升序，未设置排最后 ──
const sortedColumns = computed(() => {
  return [...props.columns].sort((a, b) => {
    const oa = a.order ?? 999
    const ob = b.order ?? 999
    return oa - ob
  })
})

// ── 动态联动选项（optionsWhen）──
const dynamicOptions = reactive({})

watch(
  formData,
  (newVal) => {
    props.columns.forEach(col => {
      if (col.optionsWhen) {
        dynamicOptions[col.prop] = col.optionsWhen(newVal)
      }
    })
  },
  { deep: true, immediate: true }
)

/** 获取字段的选项列表：优先 optionsWhen 动态结果，其次 dataList / options */
function getColumnOptions(col) {
  if (dynamicOptions[col.prop] !== undefined) {
    return dynamicOptions[col.prop]
  }
  return col.dataList ?? col.options ?? null
}

/** 判断字段是否显示（visibleWhen）*/
function isVisible(col) {
  if (typeof col.visibleWhen === 'function') {
    return col.visibleWhen(formData)
  }
  return col.visible !== false
}

/** 判断字段是否禁用（disabledWhen）*/
function isDisabled(col) {
  if (props.disabled) return true
  if (typeof col.disabledWhen === 'function') {
    return col.disabledWhen(formData)
  }
  return col.disabled === true
}

/** 字段变化处理：触发 linkage 联动 + emit field-change */
function handleFieldChange(prop, value) {
  // 触发联动
  const col = props.columns.find(c => c.prop === prop)
  if (col?.linkage) {
    col.linkage(value, formData)
  }
  emit('field-change', prop, value, { ...formData })
}

/** el-form validate 事件透传 */
function handleValidate(prop, isValid, message) {
  emit('validate', isValid, { prop, message })
}

// ── 暴露表单方法给父组件 ──
defineExpose({
  /** 触发表单校验 */
  validate: (callback) => formRef.value?.validate(callback),
  /** 重置表单字段 */
  resetFields: (props) => formRef.value?.resetFields(props),
  /** 清除校验结果 */
  clearValidate: (props) => formRef.value?.clearValidate(props),
  /** 滚动到指定字段 */
  scrollToField: (prop) => formRef.value?.scrollToField(prop),
  /** 暴露内部 formData，供外部直接读取 */
  formData
})
</script>

<style scoped>
/* el-form-item label 区域对齐优化 */
:deep(.el-form-item__label) {
  display: inline-flex;
  align-items: center;
}
</style>
