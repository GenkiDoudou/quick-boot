/**
 * 输入节点变量类型（对标 Dify：String / Integer / Number / Boolean / Time / Object / Array / File）。
 * `fieldType` 为 DSL 存储值；`backendType` 写入 `type` 字段供运行引擎使用。
 */
export const START_FIELD_TYPES = [
  { fieldType: 'string', label: 'String', labelZh: '字符串', typeTag: 'String', backendType: 'string' },
  { fieldType: 'integer', label: 'Integer', labelZh: '整数', typeTag: 'Integer', backendType: 'integer' },
  { fieldType: 'number', label: 'Number', labelZh: '数字', typeTag: 'Number', backendType: 'number' },
  { fieldType: 'boolean', label: 'Boolean', labelZh: '布尔值', typeTag: 'Boolean', backendType: 'boolean' },
  { fieldType: 'time', label: 'Time', labelZh: '时间', typeTag: 'Time', backendType: 'string' },
  { fieldType: 'object', label: 'Object', labelZh: '对象', typeTag: 'Object', backendType: 'string' },
  { fieldType: 'array', label: 'Array', labelZh: '数组', typeTag: 'Array', backendType: 'array' },
  { fieldType: 'file', label: 'File', labelZh: '文件', typeTag: 'File', backendType: 'string' }
]

/** @type {Record<string, typeof START_FIELD_TYPES[number]>} */
export const START_FIELD_TYPE_MAP = Object.fromEntries(
  START_FIELD_TYPES.map((item) => [item.fieldType, item])
)

/** 旧版 fieldType → 新版映射（兼容历史 DSL） */
const LEGACY_FIELD_TYPE_MAP = {
  text: 'string',
  paragraph: 'string',
  select: 'string',
  number: 'number',
  boolean: 'boolean',
  file: 'file',
  'array[file]': 'array',
  object: 'object'
}

/**
 * 将 fieldType 规范为新版枚举。
 * @param {string} [fieldType]
 * @returns {string}
 */
export function migrateFieldType(fieldType) {
  if (fieldType && START_FIELD_TYPE_MAP[fieldType]) {
    return fieldType
  }
  if (fieldType && LEGACY_FIELD_TYPE_MAP[fieldType]) {
    return LEGACY_FIELD_TYPE_MAP[fieldType]
  }
  return 'string'
}

/**
 * 根据字段定义解析展示用类型标签。
 * @param {object} field
 * @returns {string}
 */
export function resolveFieldTypeTag(field) {
  const ft = migrateFieldType(field?.fieldType)
  if (START_FIELD_TYPE_MAP[ft]) {
    return START_FIELD_TYPE_MAP[ft].typeTag
  }
  const t = field?.type || 'string'
  if (t === 'integer') return 'Integer'
  if (t === 'number') return 'Number'
  if (t === 'boolean') return 'Boolean'
  if (t === 'array') return 'Array'
  return 'String'
}

/**
 * 格式化类型标签为界面展示文案（变量树等）。
 * @param {string} tag
 * @returns {string}
 */
export function formatTypeTagLabel(tag) {
  const map = {
    String: 'String',
    Integer: 'Integer',
    Number: 'Number',
    Boolean: 'Boolean',
    Time: 'Time',
    Object: 'Object',
    Array: 'Array',
    File: 'File'
  }
  return map[tag] || tag
}

/**
 * 类型下拉展示文案。
 * @param {string} fieldType
 * @returns {string}
 */
export function formatTypeSelectLabel(fieldType) {
  const meta = START_FIELD_TYPE_MAP[migrateFieldType(fieldType)]
  return meta ? meta.label : 'String'
}

/**
 * 类型下拉副标题（中文说明）。
 * @param {string} fieldType
 * @returns {string}
 */
export function formatTypeSelectSublabel(fieldType) {
  const meta = START_FIELD_TYPE_MAP[migrateFieldType(fieldType)]
  return meta?.labelZh || '字符串'
}

/**
 * 创建默认输入字段。
 * @param {string} [fieldType]
 * @returns {object}
 */
export function createDefaultInputField(fieldType = 'string') {
  const ft = migrateFieldType(fieldType)
  const meta = START_FIELD_TYPE_MAP[ft] || START_FIELD_TYPE_MAP.string
  let defaultValue = ''
  if (meta.backendType === 'boolean') {
    defaultValue = false
  } else if (meta.backendType === 'integer' || meta.backendType === 'number') {
    defaultValue = 0
  } else if (meta.backendType === 'array') {
    defaultValue = '[]'
  } else if (ft === 'object') {
    defaultValue = '{}'
  }
  return {
    key: '',
    label: '',
    description: '',
    type: meta.backendType,
    fieldType: meta.fieldType,
    required: true,
    maxLength: null,
    defaultValue,
    hidden: false
  }
}

/**
 * 变量选择器 / 变量树展示用类型（使用 fieldType，不用 backendType）。
 * Object、Time、File 的 backendType 均为 string，但展示类型应区分。
 * @param {object} field 输入字段定义
 * @returns {string}
 */
export function getVariableTreeType(field) {
  return inferFieldTypeFromLegacy(field)
}

/**
 * 从历史字段推断 fieldType。
 * @param {object} field
 * @returns {string}
 */
export function inferFieldTypeFromLegacy(field) {
  if (field?.fieldType) {
    return migrateFieldType(field.fieldType)
  }
  if (field?.type === 'integer') return 'integer'
  if (field?.type === 'number') return 'number'
  if (field?.type === 'boolean') return 'boolean'
  if (field?.type === 'array') return 'array'
  return 'string'
}

/**
 * 是否为文件类字段（运行面板暂不支持上传）。
 * @param {object} field
 * @returns {boolean}
 */
export function isFileInputField(field) {
  return migrateFieldType(field?.fieldType) === 'file'
}

/**
 * 是否为 JSON 类字段（object / array）。
 * @param {object} field
 * @returns {boolean}
 */
export function isJsonInputField(field) {
  const ft = migrateFieldType(field?.fieldType)
  return ft === 'object' || ft === 'array'
}

/**
 * 尝试将字符串解析为 JSON 对象/数组。
 * @param {string} text
 * @returns {object|Array|null}
 */
function tryParseJson(text) {
  const trimmed = text.trim()
  if (!trimmed) return null
  if (!(trimmed.startsWith('{') || trimmed.startsWith('['))) return null
  try {
    return JSON.parse(trimmed)
  } catch {
    return null
  }
}

/**
 * 递归解析对象/数组中「看起来像 JSON 的字符串」为嵌套结构。
 * @param {unknown} value
 * @returns {unknown}
 */
export function deepParseJsonStringValues(value) {
  if (Array.isArray(value)) {
    return value.map((item) => deepParseJsonStringValues(item))
  }
  if (value && typeof value === 'object') {
    const out = {}
    Object.entries(value).forEach(([k, v]) => {
      out[k] = deepParseJsonStringValue(v)
    })
    return out
  }
  return deepParseJsonStringValue(value)
}

/**
 * @param {unknown} value
 * @returns {unknown}
 */
function deepParseJsonStringValue(value) {
  if (value === null || value === undefined) {
    return value
  }
  if (typeof value !== 'string') {
    if (Array.isArray(value) || typeof value === 'object') {
      return deepParseJsonStringValues(value)
    }
    return value
  }
  const parsed = tryParseJson(value)
  if (parsed !== null) {
    return deepParseJsonStringValues(parsed)
  }
  return value
}

/**
 * 运行调试入参：按字段类型解析（Object/Array 解析 JSON 并深度展开嵌套 JSON 字符串）。
 * @param {object} field 字段定义
 * @param {unknown} raw 原始输入
 * @returns {unknown}
 */
export function parseRunInputValue(field, raw) {
  const ft = migrateFieldType(field?.fieldType)
  if (ft === 'object') {
    if (raw === undefined || raw === null || raw === '') return {}
    if (typeof raw === 'object' && !Array.isArray(raw)) {
      return deepParseJsonStringValues(raw)
    }
    const parsed = tryParseJson(String(raw))
    if (parsed !== null && typeof parsed === 'object' && !Array.isArray(parsed)) {
      return deepParseJsonStringValues(parsed)
    }
    return raw
  }
  if (ft === 'array') {
    if (raw === undefined || raw === null || raw === '') return []
    if (Array.isArray(raw)) return deepParseJsonStringValues(raw)
    const parsed = tryParseJson(String(raw))
    if (parsed !== null && Array.isArray(parsed)) {
      return deepParseJsonStringValues(parsed)
    }
    return raw
  }
  if (ft === 'integer' || ft === 'number') {
    if (raw === '' || raw === null || raw === undefined) return 0
    const num = Number(raw)
    return Number.isNaN(num) ? raw : num
  }
  if (ft === 'boolean') {
    return !!raw
  }
  return raw
}
