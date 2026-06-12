/** 大模型类型（与后端 ai_model.model_type 一致） */
export const AI_MODEL_TYPES = [
  { value: 'LANGUAGE', label: '语言模型' },
  { value: 'IMAGE', label: '图像模型' },
  { value: 'VECTOR', label: '向量模型' }
]

/** 厂商预设：选择后反显 Base URL 等连接参数 */
export const AI_PROVIDER_PRESETS = [
  {
    value: 'DEEPSEEK',
    label: 'DeepSeek',
    baseUrl: 'https://api.deepseek.com',
    completionsPath: '/chat/completions',
    embeddingsPath: '',
    apiKeyType: 'SECRET',
    modelNameHint: 'deepseek-chat'
  },
  {
    value: 'OPENAI',
    label: 'OpenAI',
    baseUrl: 'https://api.openai.com',
    completionsPath: '/v1/chat/completions',
    embeddingsPath: '/v1/embeddings',
    apiKeyType: 'SECRET',
    modelNameHint: 'gpt-4o-mini'
  },
  {
    value: 'OLLAMA',
    label: 'Ollama',
    baseUrl: 'http://localhost:11434',
    completionsPath: '',
    embeddingsPath: '',
    apiKeyType: 'PLAIN',
    apiKeyOptional: true,
    modelNameHint: 'llama3.2'
  },
  {
    value: 'TONGYI',
    label: '通义千问',
    baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode',
    completionsPath: '',
    embeddingsPath: '',
    apiKeyType: 'SECRET',
    modelNameHint: 'qwen-plus'
  }
]

/** @deprecated 兼容旧数据展示 */
export const LEGACY_MODEL_TYPE_LABELS = {
  CHAT: '语言模型',
  EMBEDDING: '向量模型'
}

/** @deprecated 兼容旧数据展示 */
export const LEGACY_PROVIDER_LABELS = {
  OPENAI_COMPAT: 'OpenAI 兼容'
}

/**
 * 按厂商 code 查找预设。
 * @param {string} provider
 */
export function findProviderPreset(provider) {
  if (!provider) return null
  return AI_PROVIDER_PRESETS.find((p) => p.value === provider) || null
}

/**
 * 模型类型中文标签（含旧值 CHAT/EMBEDDING）。
 * @param {string} modelType
 */
export function modelTypeLabel(modelType) {
  const found = AI_MODEL_TYPES.find((t) => t.value === modelType)
  if (found) return found.label
  return LEGACY_MODEL_TYPE_LABELS[modelType] || modelType || '—'
}

/**
 * 厂商中文标签。
 * @param {string} provider
 */
export function providerLabel(provider) {
  const found = AI_PROVIDER_PRESETS.find((p) => p.value === provider)
  if (found) return found.label
  return LEGACY_PROVIDER_LABELS[provider] || provider || '—'
}

/**
 * 是否语言模型（含旧 CHAT）。
 * @param {string} modelType
 */
export function isLanguageModel(modelType) {
  return modelType === 'LANGUAGE' || modelType === 'CHAT'
}

/**
 * 是否向量模型（含旧 EMBEDDING）。
 * @param {string} modelType
 */
export function isVectorModel(modelType) {
  return modelType === 'VECTOR' || modelType === 'EMBEDDING'
}

/**
 * 选择厂商后填充默认连接信息。
 * @param {Record<string, any>} form 表单对象
 * @param {string} provider 厂商
 */
export function applyProviderPreset(form, provider) {
  const preset = findProviderPreset(provider)
  if (!preset || !form) return
  form.baseUrl = preset.baseUrl
  if (preset.completionsPath !== undefined) {
    form.completionsPath = preset.completionsPath
  }
  if (preset.embeddingsPath !== undefined && isVectorModel(form.modelType)) {
    form.embeddingsPath = preset.embeddingsPath
  }
  if (preset.apiKeyType) {
    form.apiKeyType = preset.apiKeyType
  }
}
