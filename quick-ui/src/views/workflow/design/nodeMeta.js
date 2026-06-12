/**
 * 工作流节点分类标签。
 */
import { getVariableTreeType, inferFieldTypeFromLegacy } from './components/forms/startFieldTypes'

export const CATEGORY_LABELS = {
  basic: '基础',
  logic: '逻辑',
  ai: 'AI',
  tool: '工具'
}

/**
 * 截取字符串预览。
 * @param {string} text
 * @param {number} max
 * @returns {string}
 */
function preview(text, max = 24) {
  if (!text) return ''
  const s = String(text)
  return s.length > max ? `${s.slice(0, max)}…` : s
}

/**
 * 工作流 12 种节点类型元数据。
 */
export const WORKFLOW_NODE_TYPES = [
  {
    type: 'start',
    label: '输入',
    category: 'basic',
    description: '工作流入口，定义用户输入变量',
    color: '#67c23a',
    icon: 'Upload',
    defaults: {
      inputs: []
    },
    /** start 节点输出由 inputs 动态决定，见 {@link resolveNodeOutputs} */
    outputs: [],
    summarize(data) {
      const inputs = data?.inputs
      if (!Array.isArray(inputs) || !inputs.length) return '未配置输入'
      const keys = inputs.map((i) => i.key).filter(Boolean)
      return keys.length ? `输入: ${keys.join(', ')}` : '未配置输入'
    },
    hasValidationWarning(data) {
      return !Array.isArray(data?.inputs) || !data.inputs.length
    }
  },
  {
    type: 'answer',
    label: '输出',
    category: 'basic',
    description: '将上游变量映射为工作流最终输出',
    color: '#e6a23c',
    icon: 'Flag',
    defaults: {
      outputMode: 'variables',
      outputVariables: [],
      output: '',
      streaming: false
    },
    outputs: [],
    summarize(data) {
      if (data?.outputMode === 'text') {
        return (data?.output || '').trim() ? '返回文本' : '未配置回答内容'
      }
      const vars = data?.outputVariables
      if (Array.isArray(vars) && vars.some((v) => v?.key && v?.value)) {
        const keys = vars.map((v) => v.key).filter(Boolean)
        return keys.length ? `返回变量: ${keys.join(', ')}` : '未配置输出变量'
      }
      return '未配置输出变量'
    },
    hasValidationWarning(data) {
      if (data?.outputMode === 'text') {
        return !(data?.output || '').trim()
      }
      const vars = data?.outputVariables
      return !Array.isArray(vars) || !vars.some((v) => v?.key && v?.value)
    }
  },
  {
    type: 'llm',
    label: 'LLM',
    category: 'ai',
    description: '调用大语言模型生成文本',
    color: '#0a2463',
    icon: 'Cpu',
    defaults: {
      systemPrompt: '你是企业助手。',
      userPrompt: '{{start_1.question}}',
      temperature: 0.3,
      streaming: true
    },
    outputs: [
      { key: 'text', label: '生成文本', type: 'string' },
      { key: 'usage', label: 'Token 用量', type: 'object' }
    ],
    fields: [
      { key: 'systemPrompt', label: '系统提示词', input: 'textarea' },
      { key: 'userPrompt', label: '用户提示词', input: 'textarea' },
      { key: 'temperature', label: '温度', input: 'number' },
      { key: 'streaming', label: '流式输出', input: 'switch' }
    ],
    summarize(data) {
      const temp = data?.temperature ?? 0.3
      const stream = data?.streaming ? '流式' : '非流式'
      return `temperature: ${temp} · ${stream}`
    },
    hasValidationWarning(data) {
      return !data?.userPrompt
    }
  },
  {
    type: 'knowledge-retrieval',
    label: '知识检索',
    category: 'ai',
    description: '从知识库检索相关片段',
    color: '#e6a23c',
    icon: 'Collection',
    defaults: {
      kbId: '{{sys.kbId}}',
      query: '{{start_1.question}}',
      topK: 5,
      similarityThreshold: 0.65
    },
    outputs: [
      { key: 'contextText', label: '上下文文本', type: 'string' },
      { key: 'chunks', label: '检索片段', type: 'array' },
      { key: 'citations', label: '引用列表', type: 'array' }
    ],
    fields: [
      { key: 'kbId', label: '知识库 ID', input: 'text' },
      { key: 'query', label: '检索问题', input: 'textarea' },
      { key: 'topK', label: 'Top K', input: 'number' },
      { key: 'similarityThreshold', label: '相似度阈值', input: 'number' }
    ],
    summarize(data, kbNameMap = {}) {
      const kbId = data?.kbId
      if (!kbId) return '未选择知识库'
      const name = kbNameMap[kbId] || kbNameMap[String(kbId).replace(/\{\{|\}\}/g, '')]
      if (name) return `知识库: ${name}`
      const id = String(kbId).replace(/\{\{sys\.kbId\}\}/, 'sys.kbId')
      return `知识库: ${preview(id, 16)}`
    },
    hasValidationWarning(data) {
      return !data?.kbId || !data?.query
    }
  },
  {
    type: 'if-else',
    label: '条件分支',
    category: 'logic',
    description: '根据条件走 IF / ELSE 分支',
    color: '#909399',
    icon: 'Switch',
    defaults: {
      conditions: [{ left: '{{start_1.question}}', operator: 'not-empty', right: '' }]
    },
    outputs: [],
    fields: [
      { key: 'conditionsJson', label: '条件 (JSON)', input: 'textarea', bind: 'conditions', json: true }
    ],
    summarize(data) {
      const n = Array.isArray(data?.conditions) ? data.conditions.length : 0
      return `条件: ${n} 条`
    },
    hasValidationWarning(data) {
      return !Array.isArray(data?.conditions) || !data.conditions.length
    }
  },
  {
    type: 'template-transform',
    label: '模板转换',
    category: 'logic',
    description: '用模板语法转换变量',
    color: '#626aef',
    icon: 'Document',
    defaults: { template: '{{start_1.question}}' },
    outputs: [{ key: 'output', label: '转换结果', type: 'string' }],
    fields: [{ key: 'template', label: '模板', input: 'textarea' }],
    summarize(data) {
      return data?.template ? preview(data.template) : '未配置模板'
    },
    hasValidationWarning(data) {
      return !data?.template
    }
  },
  {
    type: 'variable-assign',
    label: '变量赋值',
    category: 'logic',
    description: '为变量赋值',
    color: '#626aef',
    icon: 'EditPen',
    defaults: { assignments: [{ target: 'var1', value: '{{start_1.question}}' }] },
    outputs: [{ key: 'variables', label: '赋值结果', type: 'object' }],
    fields: [
      { key: 'assignmentsJson', label: '赋值列表 (JSON)', input: 'textarea', bind: 'assignments', json: true }
    ],
    summarize(data) {
      const n = Array.isArray(data?.assignments) ? data.assignments.length : 0
      return `${n} 个变量`
    },
    hasValidationWarning(data) {
      return !Array.isArray(data?.assignments) || !data.assignments.length
    }
  },
  {
    type: 'variable-aggregator',
    label: '变量聚合',
    category: 'logic',
    description: '合并多个变量为列表',
    color: '#626aef',
    icon: 'Connection',
    defaults: { variables: ['{{start_1.question}}'] },
    outputs: [{ key: 'output', label: '聚合结果', type: 'array' }],
    fields: [
      { key: 'variablesJson', label: '变量列表 (JSON)', input: 'textarea', bind: 'variables', json: true }
    ],
    summarize(data) {
      const n = Array.isArray(data?.variables) ? data.variables.length : 0
      return `合并 ${n} 项`
    },
    hasValidationWarning(data) {
      return !Array.isArray(data?.variables) || !data.variables.length
    }
  },
  {
    type: 'http-request',
    label: 'HTTP 请求',
    category: 'tool',
    description: '发起 HTTP 请求获取数据',
    color: '#f56c6c',
    icon: 'Link',
    defaults: { method: 'GET', url: 'https://example.com', headers: {}, body: '' },
    outputs: [
      { key: 'statusCode', label: '状态码', type: 'number' },
      { key: 'body', label: '响应体', type: 'string' },
      { key: 'headers', label: '响应头', type: 'object' }
    ],
    fields: [
      { key: 'method', label: '方法', input: 'text' },
      { key: 'url', label: 'URL', input: 'textarea' },
      { key: 'headersJson', label: 'Headers (JSON)', input: 'textarea', bind: 'headers', json: true },
      { key: 'body', label: 'Body', input: 'textarea' }
    ],
    summarize(data) {
      const method = data?.method || 'GET'
      const url = preview(data?.url || '', 28)
      return url ? `${method} ${url}` : '未配置 URL'
    },
    hasValidationWarning(data) {
      return !data?.url
    }
  },
  {
    type: 'question-classifier',
    label: '问题分类',
    category: 'logic',
    description: '将输入分类到不同分支',
    color: '#b88230',
    icon: 'Grid',
    defaults: { query: '{{start_1.question}}', classes: [{ id: 'a', name: '类别A', description: '' }] },
    outputs: [{ key: 'classId', label: '分类 ID', type: 'string' }],
    fields: [
      { key: 'query', label: '待分类文本', input: 'textarea' },
      { key: 'classesJson', label: '分类 (JSON)', input: 'textarea', bind: 'classes', json: true }
    ],
    summarize(data) {
      const n = Array.isArray(data?.classes) ? data.classes.length : 0
      return `${n} 个分类`
    },
    hasValidationWarning(data) {
      return !data?.query || !Array.isArray(data?.classes) || !data.classes.length
    }
  },
  {
    type: 'parameter-extractor',
    label: '参数提取',
    category: 'logic',
    description: '从文本中提取结构化参数',
    color: '#b88230',
    icon: 'Filter',
    defaults: {
      query: '{{start_1.question}}',
      schema: { fields: [{ key: 'name', type: 'string', description: '', required: true }] }
    },
    outputs: [{ key: 'parameters', label: '提取参数', type: 'object' }],
    fields: [
      { key: 'query', label: '待提取文本', input: 'textarea' },
      { key: 'schemaJson', label: 'JSON Schema', input: 'textarea', bind: 'schema', json: true }
    ],
    summarize(data) {
      const fields = data?.schema?.fields
      const n = Array.isArray(fields) ? fields.length : 0
      return `schema: ${n} 字段`
    },
    hasValidationWarning(data) {
      const fields = data?.schema?.fields
      return !data?.query || !Array.isArray(fields) || !fields.length
    }
  },
  {
    type: 'list-operator',
    label: '列表操作',
    category: 'logic',
    description: '对列表执行 filter / first 等操作',
    color: '#909399',
    icon: 'List',
    defaults: { operation: 'first', listRef: '{{kb_1.chunks}}', field: 'content' },
    outputs: [{ key: 'result', label: '操作结果', type: 'any' }],
    fields: [
      { key: 'operation', label: '操作', input: 'text' },
      { key: 'listRef', label: '列表引用', input: 'textarea' },
      { key: 'field', label: '字段名', input: 'text' }
    ],
    summarize(data) {
      const op = data?.operation || 'first'
      return `${op} / ${data?.field || 'content'}`
    },
    hasValidationWarning(data) {
      return !data?.listRef
    }
  }
]

/** @type {Record<string, typeof WORKFLOW_NODE_TYPES[number]>} */
export const NODE_META_MAP = Object.fromEntries(
  WORKFLOW_NODE_TYPES.map((item) => [item.type, item])
)

/**
 * 解析节点可输出的变量列表（供变量选择器与模板引用）。
 * start 节点从 data.inputs 动态生成；其他节点使用 nodeMeta.outputs。
 * @param {string} wfType 节点类型
 * @param {object} [nodeData] 节点 data
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
export function resolveNodeOutputs(wfType, nodeData) {
  if (wfType === 'answer') {
    if (nodeData?.outputMode === 'text') {
      return [{ key: 'text', label: '回答内容', type: 'string' }]
    }
    const vars = nodeData?.outputVariables
    if (Array.isArray(vars) && vars.length) {
      return vars
        .filter((item) => item?.key)
        .map((item) => ({
          key: item.key,
          label: item.key,
          type: 'string'
        }))
    }
    return []
  }
  if (wfType === 'start') {
    const inputs = nodeData?.inputs
    if (Array.isArray(inputs) && inputs.length) {
      return inputs
        .filter((item) => item?.key)
        .map((item) => ({
          key: item.key,
          label: item.label || item.key,
          type: getVariableTreeType(item),
          description: item.description?.trim() || ''
        }))
    }
    return []
  }
  return NODE_META_MAP[wfType]?.outputs || []
}

/**
 * 获取节点类型中文标签。
 * @param {string} type 节点类型
 * @returns {string}
 */
export function getNodeLabel(type) {
  return NODE_META_MAP[type]?.label || type
}

/**
 * 获取节点主题色。
 * @param {string} type 节点类型
 * @returns {string}
 */
export function getNodeColor(type) {
  return NODE_META_MAP[type]?.color || '#409eff'
}

/**
 * 获取节点摘要文本。
 * @param {string} type 节点类型
 * @param {object} data 节点 data
 * @param {Record<string, string>} [kbNameMap] 知识库 ID → 名称
 * @returns {string}
 */
export function summarizeNode(type, data, kbNameMap) {
  const meta = NODE_META_MAP[type]
  if (meta?.summarize) return meta.summarize(data || {}, kbNameMap)
  return data?.label || getNodeLabel(type)
}

/**
 * 节点是否存在必填项未填警告。
 * @param {string} type 节点类型
 * @param {object} data 节点 data
 * @returns {boolean}
 */
export function hasNodeValidationWarning(type, data) {
  const meta = NODE_META_MAP[type]
  if (meta?.hasValidationWarning) return meta.hasValidationWarning(data || {})
  return false
}

/**
 * 按分类分组节点类型。
 * @returns {Record<string, typeof WORKFLOW_NODE_TYPES>}
 */
export function groupNodeTypesByCategory() {
  const groups = { basic: [], logic: [], ai: [], tool: [] }
  WORKFLOW_NODE_TYPES.forEach((item) => {
    const cat = item.category || 'logic'
    if (groups[cat]) groups[cat].push(item)
  })
  return groups
}
