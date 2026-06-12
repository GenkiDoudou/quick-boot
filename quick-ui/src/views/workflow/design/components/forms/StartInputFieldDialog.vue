<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑变量' : '添加变量'"
    width="480px"
    destroy-on-close
    append-to-body
    class="start-field-dialog"
    @closed="onClosed"
  >
    <el-form label-position="top" size="default" class="start-field-dialog__form">
      <el-form-item label="字段类型" required>
        <el-select
          v-model="form.fieldType"
          placeholder="选择字段类型"
          style="width: 100%"
          popper-class="start-field-dialog__popper"
          @change="onFieldTypeChange"
        >
          <template #label="{ label, value }">
            <div v-if="selectedTypeMeta" class="start-field-dialog__type-selected">
              <span class="start-field-dialog__type-left">
                <el-icon><component :is="iconMap[selectedTypeMeta.icon]" /></el-icon>
                <span>{{ selectedTypeMeta.label }}</span>
              </span>
              <span class="start-field-dialog__type-tag">{{ formatTypeTagLabel(selectedTypeMeta.typeTag) }}</span>
            </div>
            <span v-else>{{ label || value }}</span>
          </template>
          <el-option
            v-for="item in START_FIELD_TYPES"
            :key="item.fieldType"
            :label="item.label"
            :value="item.fieldType"
          >
            <div class="start-field-dialog__type-option">
              <span class="start-field-dialog__type-left">
                <el-icon><component :is="iconMap[item.icon]" /></el-icon>
                <span>{{ item.label }}</span>
              </span>
              <span class="start-field-dialog__type-tag">{{ formatTypeTagLabel(item.typeTag) }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="变量名称" required>
        <el-input v-model="form.key" placeholder="请输入" />
      </el-form-item>

      <el-form-item label="显示名称">
        <el-input v-model="form.label" placeholder="请输入" />
      </el-form-item>

      <el-form-item v-if="showMaxLength" label="最大长度">
        <el-input v-model="form.maxLength" placeholder="请输入" type="number" />
      </el-form-item>

      <el-form-item v-if="showDefaultValue" :label="defaultValueLabel">
        <el-input-number
          v-if="form.type === 'number'"
          v-model="form.defaultValue"
          controls-position="right"
          style="width: 100%"
        />
        <el-switch v-else-if="form.type === 'boolean'" v-model="form.defaultValue" />
        <el-input
          v-else-if="form.fieldType === 'object'"
          v-model="form.defaultValue"
          type="textarea"
          :rows="3"
          placeholder="JSON 对象"
        />
        <el-input v-else v-model="form.defaultValue" placeholder="请输入" />
      </el-form-item>

      <div class="start-field-dialog__checks">
        <el-checkbox v-model="form.required">必填</el-checkbox>
        <el-checkbox v-model="form.hidden">
          隐藏并预填
          <el-tooltip content="运行时不展示该字段，使用默认值预填" placement="top">
            <el-icon class="start-field-dialog__help"><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-checkbox>
      </div>
    </el-form>

    <template #footer>
      <div class="start-field-dialog__footer">
        <el-button v-if="isEdit" type="danger" link @click="handleDelete">删除</el-button>
        <div class="start-field-dialog__footer-right">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  CircleCheck,
  Coin,
  Document,
  EditPen,
  Folder,
  Memo,
  Odometer,
  QuestionFilled
} from '@element-plus/icons-vue'
import {
  START_FIELD_TYPES,
  START_FIELD_TYPE_MAP,
  createDefaultInputField,
  formatTypeTagLabel
} from './startFieldTypes'

defineOptions({ name: 'StartInputFieldDialog' })

const props = defineProps({
  visible: { type: Boolean, default: false },
  field: { type: Object, default: null },
  isEdit: { type: Boolean, default: false },
  existingKeys: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:visible', 'save', 'delete'])

const iconMap = {
  EditPen,
  Memo,
  ArrowDown,
  Odometer,
  CircleCheck,
  Document,
  Folder,
  Coin
}

const form = reactive(createDefaultInputField())

const dialogVisible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v)
})

const selectedTypeMeta = computed(() => START_FIELD_TYPE_MAP[form.fieldType])

const showMaxLength = computed(() =>
  ['text', 'paragraph', 'select'].includes(form.fieldType)
)

const showDefaultValue = computed(() => !['file', 'array[file]'].includes(form.fieldType))

const defaultValueLabel = computed(() => (form.fieldType === 'object' ? '默认值 (JSON)' : '默认值'))

watch(
  () => [props.visible, props.field],
  ([visible, field]) => {
    if (!visible) return
    Object.assign(form, createDefaultInputField())
    if (field) {
      Object.assign(form, JSON.parse(JSON.stringify(field)))
      if (!form.fieldType) {
        form.fieldType = inferFieldType(field)
      }
    }
  },
  { immediate: true }
)

function inferFieldType(field) {
  if (field.fieldType) return field.fieldType
  if (field.type === 'number') return 'number'
  if (field.type === 'boolean') return 'boolean'
  return 'text'
}

function onFieldTypeChange(fieldType) {
  const meta = START_FIELD_TYPE_MAP[fieldType]
  if (!meta) return
  form.type = meta.backendType
  if (meta.backendType === 'boolean') {
    form.defaultValue = false
  } else if (meta.backendType === 'number') {
    form.defaultValue = 0
  } else {
    form.defaultValue = ''
  }
}

function onClosed() {
  Object.assign(form, createDefaultInputField())
}

function handleSave() {
  const key = form.key?.trim()
  if (!key) {
    ElMessage.warning('请填写变量名称')
    return
  }
  if (!/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(key)) {
    ElMessage.warning('变量名称仅支持字母、数字、下划线，且不能包含 . 或节点 ID')
    return
  }
  if (key.includes('.')) {
    ElMessage.warning('变量名称不要带点号；引用路径会自动生成为 节点ID.变量名')
    return
  }
  if (props.existingKeys.includes(key)) {
    ElMessage.warning('变量名称已存在')
    return
  }
  const payload = {
    key,
    label: form.label?.trim() || key,
    type: form.type,
    fieldType: form.fieldType,
    required: !!form.required,
    hidden: !!form.hidden,
    maxLength: form.maxLength ? Number(form.maxLength) : null,
    defaultValue: form.defaultValue
  }
  emit('save', payload)
  dialogVisible.value = false
}

function handleDelete() {
  ElMessageBox.confirm('确认删除该输入字段？', '删除字段', { type: 'warning' })
    .then(() => {
      emit('delete')
      dialogVisible.value = false
    })
    .catch(() => {})
}
</script>

<style scoped lang="scss">
.start-field-dialog__form {
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: #303133;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    background: #f5f7fa;
    box-shadow: 0 0 0 1px #ebeef5 inset;
  }
}

.start-field-dialog__type-selected,
.start-field-dialog__type-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.start-field-dialog__type-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.start-field-dialog__type-tag {
  font-size: 12px;
  color: #909399;
  background: #f2f4f7;
  padding: 2px 8px;
  border-radius: 4px;
}

.start-field-dialog__checks {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 4px;
}

.start-field-dialog__help {
  margin-left: 4px;
  color: #c0c4cc;
  vertical-align: middle;
}

.start-field-dialog__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.start-field-dialog__footer-right {
  display: flex;
  gap: 8px;
  margin-left: auto;
}
</style>

<style lang="scss">
.start-field-dialog__popper {
  .el-select-dropdown__item {
    height: auto;
    padding: 10px 12px;
  }
}
</style>
