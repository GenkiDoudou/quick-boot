/**
 * 工作流节点分类标签。
 */
import { getVariableTreeType, inferFieldTypeFromLegacy } from './components/forms/startFieldTypes'
import { hasIfElseValidationWarning, resolveIfElseBranchHandles } from './utils/ifElseBranchUtils'
import { resolveAggregatorOutputKeys } from './utils/variableAggregatorUtils'

export const CATEGORY_LABELS = {
  basic: '基础',
  logic: '逻辑',
  ai: 'AI',
  knowledge: '知识库',
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
    label: '开始',
    category: 'basic',
    description: '工作流入口，定义运行入参（内置节点，不可删除）',
    example: '配置 question 等运行变量',
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
    type: 'end',
    label: '结束',
    category: 'basic',
    description: '工作流出口，配置 API 最终返回（内置节点，不可删除）',
    example: '返回变量 result ← {{answer_1.xxx}} 或返回文本',
    color: '#909399',
    icon: 'CircleCheck',
    defaults: {
      outputMode: 'variables',
      outputVariables: [],
      output: '',
      streaming: false
    },
    outputs: [],
    summarize(data) {
      if (data?.outputMode === 'text') {
        return (data?.output || '').trim() ? '返回文本' : '未配置返回内容'
      }
      const vars = data?.outputVariables
      if (Array.isArray(vars) && vars.some((v) => v?.key && v?.value)) {
        const keys = vars.map((v) => v.key).filter(Boolean)
        return keys.length ? `返回变量: ${keys.join(', ')}` : '未配置返回变量'
      }
      return '未配置返回变量'
    },
    hasValidationWarning(data) {
      const vars = data?.outputVariables
      const hasValidVar = Array.isArray(vars) && vars.some((v) => v?.key && v?.value)
      if (data?.outputMode === 'text') {
        return !(data?.output || '').trim() && !hasValidVar
      }
      return !hasValidVar
    }
  },
  {
    type: 'answer',
    label: '输出',
    category: 'basic',
    description: '配置返回变量或文本，可添加多个',
    example: '返回 res ← {{llm_1.output}}；选择器各分支可接不同输出节点',
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
      const vars = data?.outputVariables
      const hasValidVar = Array.isArray(vars) && vars.some((v) => v?.key && v?.value)
      if (data?.outputMode === 'text') {
        return !(data?.output || '').trim() && !hasValidVar
      }
      return !hasValidVar
    }
  },
  {
    type: 'llm',
    label: '大模型',
    category: 'ai',
    description: '调用大语言模型，支持文本 / Markdown / JSON 输出',
    example: '用户提示词 {{start_1.question}} → 输出 output',
    color: '#0a2463',
    icon: 'Cpu',
    defaults: {
      chatModelId: null,
      inputVariables: [{ key: 'question', value: '' }],
      outputVariables: [{ key: 'output', type: 'string', description: '' }],
      systemPrompt: '你是企业助手。',
      systemPromptId: null,
      userPrompt: '{{question}}',
      temperature: 0.3,
      streaming: false,
      outputFormat: 'text',
      useMcpTools: false,
      mcpIds: []
    },
    outputs: [
      { key: 'text', label: '生成文本', type: 'string' },
      { key: 'json', label: 'JSON 结果', type: 'object' },
      { key: 'mcpToolsUsed', label: 'MCP 工具调用', type: 'array', description: '启用 MCP 时记录本次调用的工具名称' },
      { key: 'mcpToolResults', label: 'MCP 工具结果', type: 'array', description: '每次工具调用的入参与原始返回，未经大模型改写' }
    ],
    fields: [
      { key: 'chatModelId', label: '大模型', input: 'select' },
      { key: 'systemPrompt', label: '系统提示词', input: 'textarea' },
      { key: 'userPrompt', label: '用户提示词', input: 'textarea' },
      { key: 'outputFormat', label: '输出格式', input: 'select' },
      { key: 'temperature', label: '温度', input: 'number' },
      { key: 'streaming', label: '流式输出', input: 'switch' }
    ],
    summarize(data) {
      const fmt = data?.outputFormat || 'text'
      const fmtLabel = fmt === 'json' ? 'JSON' : fmt === 'markdown' ? 'Markdown' : '文本'
      const stream = data?.streaming ? '流式' : '非流式'
      const temp = data?.temperature ?? 0.3
      const mcpCount = data?.useMcpTools && Array.isArray(data?.mcpIds) ? data.mcpIds.length : 0
      const mcp = mcpCount > 0 ? ` · MCP×${mcpCount}` : ''
      return `${fmtLabel} · T=${temp} · ${stream}${mcp}`
    },
    hasValidationWarning(data) {
      return !data?.userPrompt
    }
  },
  {
    type: 'knowledge-retrieval',
    label: '知识库检索',
    category: 'knowledge',
    description: '基于 Query 在指定知识库中检索，召回最匹配片段并以列表返回',
    example: 'Query={{start_1.question}} → outputList、contextText',
    color: '#e6a23c',
    icon: 'Collection',
    defaults: {
      kbId: '',
      query: '{{start_1.question}}',
      searchMode: 'VECTOR',
      topK: 8,
      similarityThreshold: 0.5,
      saveHistory: false
    },
    outputs: [
      { key: 'outputList', label: '召回列表', type: 'array', description: '含 output、documentId 等字段' },
      { key: 'contextText', label: '上下文文本', type: 'string' },
      { key: 'chunks', label: '检索片段', type: 'array' },
      { key: 'citations', label: '引用列表', type: 'array' }
    ],
    fields: [
      { key: 'query', label: 'Query', input: 'textarea' },
      { key: 'kbId', label: '知识库', input: 'select' },
      { key: 'searchMode', label: '搜索策略', input: 'select' },
      { key: 'topK', label: '最大召回数量', input: 'number' },
      { key: 'similarityThreshold', label: '最小匹配度', input: 'number' }
    ],
    summarize(data, kbNameMap = {}) {
      const kbId = data?.kbId
      const mode = data?.searchMode === 'VECTOR' ? '语义' : '混合'
      if (!kbId) return '未选择知识库'
      const name = kbNameMap[kbId] || kbNameMap[String(kbId).replace(/\{\{|\}\}/g, '')]
      const kbLabel = name || String(kbId).replace(/\{\{sys\.kbId\}\}/, 'sys.kbId')
      const q = preview(data?.query, 12)
      return q ? `${kbLabel} · ${mode} · ${q}` : `知识库: ${preview(kbLabel, 16)} · ${mode}`
    },
    hasValidationWarning(data) {
      return !data?.kbId || !data?.query
    }
  },
  {
    type: 'if-else',
    label: '选择器',
    category: 'logic',
    description: '连接多个下游分支；条件成立则走对应分支，均不满足则走「否则」',
    example: 'input=true → 如果；input=false → 否则如果；否则 → 否则',
    color: '#909399',
    icon: 'Switch',
    defaults: {
      branches: [
        {
          id: 'true',
          name: '如果',
          logic: 'AND',
          conditions: [{ left: '{{start_1.question}}', operator: 'not_empty', right: '' }]
        }
      ]
    },
    outputs: [{ key: 'branch', label: '命中分支', type: 'string' }],
    summarize(data) {
      const branches = resolveIfElseBranchHandles(data)
      const totalConds = (data?.branches || []).reduce(
        (n, b) => n + (Array.isArray(b?.conditions) ? b.conditions.length : 0),
        data?.conditions?.length || 0
      )
      return `${branches.length} 个条件分支 · ${totalConds} 条判断`
    },
    hasValidationWarning(data) {
      return hasIfElseValidationWarning(data)
    }
  },
  {
    type: 'template-transform',
    label: '模板转换',
    category: 'logic',
    description: '用 {{变量}} 模板拼接或格式化字符串',
    example: '模板：问题：{{start_1.question}}，上下文：{{kb_1.contextText}}',
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
    type: 'text-process',
    label: '文本处理',
    category: 'logic',
    description: '字符串拼接或按分隔符拆分为数组，适用于总结拼接、段落拆分、转义组合等',
    example: '拼接多轮摘要为 prompt；或按「...」拆分段落为 items 数组',
    color: '#626aef',
    icon: 'EditPen',
    defaults: {
      processMode: 'join',
      inputVariables: [{ key: 'String1', value: '' }],
      template: '',
      source: '{{String1}}',
      delimiter: ','
    },
    outputs: [
      { key: 'output', label: 'output', type: 'string' },
      { key: 'items', label: 'items', type: 'array' },
      { key: 'count', label: 'count', type: 'number' }
    ],
    summarize(data) {
      const inputs = Array.isArray(data?.inputVariables)
        ? data.inputVariables.filter((p) => p?.key).length
        : 0
      if (data?.processMode === 'split') {
        const delim = data?.delimiter ?? ''
        return `字符串分隔 · ${inputs} 个输入 · 「${preview(delim, 12) || '分隔符'}」`
      }
      return data?.template ? `字符串拼接 · ${preview(data.template)}` : `字符串拼接 · ${inputs} 个输入`
    },
    hasValidationWarning(data) {
      const inputs = Array.isArray(data?.inputVariables) ? data.inputVariables : []
      const hasInput = inputs.some((p) => (p?.key || '').trim() && (p?.value || '').trim())
      if (!hasInput) return true
      if (data?.processMode === 'split') {
        return !(data?.source || '').trim()
      }
      return !(data?.template || '').trim()
    }
  },
  {
    type: 'variable-assign',
    label: '变量赋值',
    category: 'logic',
    description: '将上游变量或模板值写入新的变量名',
    example: 'summary ← {{llm_1.output}}，供下游节点引用',
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
    description: '多路分支输出按分组取第一个非空值，输出 Group1、Group2…',
    example: '语文/数学分支的 answer → Group1；type → Group2',
    color: '#626aef',
    icon: 'Connection',
    defaults: {
      groups: [
        {
          id: 'group_1',
          name: 'Group1',
          strategy: 'first_non_empty',
          variables: []
        }
      ]
    },
    outputs: [{ key: 'Group1', label: 'Group1', type: 'any' }],
    summarize(data) {
      const keys = resolveAggregatorOutputKeys(data)
      if (!keys.length) return '未配置分组'
      const total = (data?.groups || []).reduce(
        (n, g) => n + (Array.isArray(g?.variables) ? g.variables.length : 0),
        Array.isArray(data?.variables) ? data.variables.length : 0
      )
      return `${keys.length} 个分组 · ${total} 个候选变量`
    },
    hasValidationWarning(data) {
      const groups = data?.groups
      if (Array.isArray(groups) && groups.length) {
        return !groups.some(
          (g) => Array.isArray(g?.variables) && g.variables.some((v) => (typeof v === 'string' ? v : v?.value)?.trim())
        )
      }
      return !Array.isArray(data?.variables) || !data.variables.some((v) => String(v || '').trim())
    }
  },
  {
    type: 'batch',
    label: '批处理',
    category: 'logic',
    description: '按数组元素分批并行执行批处理体，汇总输出为数组',
    example: '多图并行处理；体内引用 {{batch_1.item}}、{{batch_1.index}}',
    color: '#67c23a',
    icon: 'Grid',
    defaults: {
      bodyId: '',
      parallelLimit: 10,
      parallelLimitSource: '',
      maxRuns: 100,
      inputParameters: [{ key: '', source: '' }],
      outputParameters: [{ key: '', nodeId: '', field: 'result' }]
    },
    outputs: [
      { key: 'count', label: '运行次数', type: 'number' },
      { key: 'index', label: '末项索引', type: 'number' }
    ],
    summarize(data) {
      const parallel = data?.parallelLimit ?? 10
      const inputs = Array.isArray(data?.inputParameters) ? data.inputParameters.filter((p) => p?.key).length : 0
      return `并行 ${parallel} · ${inputs} 个输入`
    },
    hasValidationWarning(data) {
      if (!data?.bodyId) return true
      const inputs = Array.isArray(data?.inputParameters) ? data.inputParameters : []
      return !inputs.some((p) => (p?.key || '').trim() && (p?.source || '').trim())
    }
  },
  {
    type: 'batch-body',
    label: '批处理体',
    category: 'logic',
    description: '批处理节点内部容器，不在节点库展示',
    color: '#f0f9eb',
    icon: 'Grid',
    defaults: { batchNodeId: '' },
    outputs: [],
    summarize() {
      return '批处理体容器'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'loop',
    label: '循环',
    category: 'logic',
    description: '数组循环 / 指定次数 / 无限循环，串行执行循环体内节点',
    example: '数组遍历 item/index；无限循环配合终止循环节点',
    color: '#409eff',
    icon: 'Refresh',
    defaults: {
      loopType: 'count',
      count: 10,
      countSource: '',
      arraySource: '',
      arrayParameters: [{ key: 'item', source: '' }],
      bodyId: '',
      outputMode: 'results',
      outputVariableName: 'results',
      outputNodeId: '',
      outputField: 'text',
      outputVariableKey: '',
      intermediateVariables: [{ key: '', initialValue: '', type: 'any' }]
    },
    outputs: [
      { key: 'results', label: '结果数组', type: 'array' },
      { key: 'outputVariable', label: '中间变量最终值', type: 'any' },
      { key: 'item', label: '当前元素', type: 'any' },
      { key: 'index', label: '当前索引', type: 'number' },
      { key: 'count', label: '迭代次数', type: 'number' }
    ],
    summarize(data) {
      const typeMap = { array: '数组', count: `${data?.count ?? 10} 次`, infinite: '无限' }
      const mode = typeMap[data?.loopType] || typeMap.count
      const out = data?.outputNodeId ? ` → ${data.outputNodeId}` : ''
      return `${mode}${out}`
    },
    hasValidationWarning(data) {
      if (!data?.bodyId) return true
      if (data?.loopType === 'array') {
        const params = Array.isArray(data?.arrayParameters) ? data.arrayParameters : []
        const hasParam = params.some((p) => (p?.source || '').trim())
        if (hasParam) return false
        return !(data?.arraySource || '').trim()
      }
      if (data?.loopType === 'infinite') return false
      return false
    }
  },
  {
    type: 'loop-body',
    label: '循环体',
    category: 'logic',
    description: '循环节点内部容器，不在节点库展示',
    color: '#ecf5ff',
    icon: 'Refresh',
    defaults: { loopNodeId: '' },
    outputs: [],
    summarize() {
      return '循环体容器'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'loop-body-start',
    label: '开始',
    category: 'logic',
    description: '循环体入口锚点，不在节点库展示',
    color: '#67c23a',
    icon: 'VideoPlay',
    defaults: {},
    outputs: [],
    summarize() {
      return '循环体入口'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'loop-body-end',
    label: '结束',
    category: 'logic',
    description: '循环体出口锚点，不在节点库展示',
    color: '#909399',
    icon: 'CircleCheck',
    defaults: {},
    outputs: [],
    summarize() {
      return '循环体出口'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'break-loop',
    label: '终止循环',
    category: 'logic',
    description: '立即跳出循环，常用于无限循环模式',
    example: '插件报错时 if-else → 终止循环',
    color: '#f56c6c',
    icon: 'CircleClose',
    defaults: {},
    outputs: [{ key: 'broken', label: '已终止', type: 'boolean' }],
    summarize() {
      return '终止循环'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'continue-loop',
    label: '继续循环',
    category: 'logic',
    description: '跳过本轮剩余节点，进入下一轮迭代',
    example: '条件不满足时继续下一轮',
    color: '#e6a23c',
    icon: 'DArrowRight',
    defaults: {},
    outputs: [{ key: 'continued', label: '已继续', type: 'boolean' }],
    summarize() {
      return '继续循环'
    },
    hasValidationWarning() {
      return false
    }
  },
  {
    type: 'loop-set-variable',
    label: '设置变量',
    category: 'logic',
    description: '更新循环中间变量，供下一轮迭代引用（仅循环体内）',
    example: 'last_paragraph ← {{llm_1.output}}',
    color: '#626aef',
    icon: 'EditPen',
    defaults: { target: '', value: '' },
    outputs: [],
    summarize(data) {
      return data?.target ? `设置 ${data.target}` : '未配置中间变量'
    },
    hasValidationWarning(data) {
      return !(data?.target || '').trim()
    }
  },
  {
    type: 'code',
    label: '代码',
    category: 'tool',
    description: '通过 JavaScript / Python 编写自定义逻辑，处理输入并返回对象',
    example: 'params.input → 处理后 return { result }',
    color: '#409eff',
    icon: 'Edit',
    defaults: {
      language: 'javascript',
      code: "function main({ params }) {\n  return {\n    result: params.input\n  };\n}",
      timeoutSec: 60,
      errorMode: 'abort',
      inputVariables: [{ key: 'input', value: '' }],
      outputVariables: [{ key: 'result', type: 'string' }],
      fallbackOutputs: { result: '' }
    },
    outputs: [
      { key: 'result', label: 'result', type: 'string' },
      { key: 'isSuccess', label: '是否成功', type: 'boolean' },
      { key: 'errorBody', label: '错误信息', type: 'string' }
    ],
    summarize(data) {
      const lang = data?.language === 'python' ? 'Python' : 'JavaScript'
      const inputs = Array.isArray(data?.inputVariables)
        ? data.inputVariables.filter((p) => p?.key).length
        : 0
      const outputs = Array.isArray(data?.outputVariables)
        ? data.outputVariables.filter((p) => p?.key).length
        : 0
      return `${lang} · ${inputs} 入参 · ${outputs} 出参`
    },
    hasValidationWarning(data) {
      const hasCode = !!(data?.code || '').trim()
      const hasInput = Array.isArray(data?.inputVariables)
        && data.inputVariables.some((p) => (p?.key || '').trim())
      const hasOutput = Array.isArray(data?.outputVariables)
        && data.outputVariables.some((p) => (p?.key || '').trim())
      return !hasCode || !hasInput || !hasOutput
    }
  },
  {
    type: 'json-serialize',
    label: 'JSON 序列化',
    category: 'tool',
    description: '将 Object、Array 等结构转为 JSON 字符串，便于下游存储或传输',
    example: '{{llm_1.output}} → output 字符串',
    color: '#409eff',
    icon: 'Document',
    defaults: {
      inputVariables: [{ key: 'input', value: '' }]
    },
    outputs: [{ key: 'output', label: 'output', type: 'string' }],
    summarize(data) {
      const row = Array.isArray(data?.inputVariables) ? data.inputVariables[0] : null
      const key = (row?.key || '').trim()
      const value = (row?.value || '').trim()
      if (!key || !value) return '序列化 · 未配置输入'
      return `序列化 · ${preview(value, 24) || key}`
    },
    hasValidationWarning(data) {
      const row = Array.isArray(data?.inputVariables) ? data.inputVariables[0] : null
      return !(row?.key || '').trim() || !(row?.value || '').trim()
    }
  },
  {
    type: 'json-deserialize',
    label: 'JSON 反序列化',
    category: 'tool',
    description: '将 JSON 字符串解析为对象，支持按字段表提取子字段',
    example: '{{http_1.body}} → output 对象',
    color: '#67c23a',
    icon: 'DocumentCopy',
    defaults: {
      inputVariables: [{ key: 'input', value: '' }],
      outputFields: []
    },
    outputs: [{ key: 'output', label: 'output', type: 'object' }],
    summarize(data) {
      const row = Array.isArray(data?.inputVariables) ? data.inputVariables[0] : null
      const key = (row?.key || '').trim()
      const value = (row?.value || '').trim()
      if (!key || !value) return '反序列化 · 未配置输入'
      const fields = Array.isArray(data?.outputFields)
        ? data.outputFields.filter((f) => (f?.key || '').trim())
        : []
      if (!fields.length) return '反序列化 · 整包输出'
      return `反序列化 · ${fields.length} 个字段`
    },
    hasValidationWarning(data) {
      const row = Array.isArray(data?.inputVariables) ? data.inputVariables[0] : null
      if (!(row?.key || '').trim() || !(row?.value || '').trim()) return true
      const fields = Array.isArray(data?.outputFields) ? data.outputFields : []
      const keys = fields.map((f) => (f?.key || '').trim()).filter(Boolean)
      if (keys.length !== new Set(keys).size) return true
      return fields.some((f) => (f?.key || '').trim() === '' && ((f?.path || '').trim() || (f?.type || '').trim()))
    }
  },
  {
    type: 'http-request',
    label: 'HTTP 请求',
    category: 'tool',
    description: '调用外部 HTTP 接口并返回响应',
    example: 'GET https://api.example.com/data? q={{start_1.question}}',
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
    label: '意图识别',
    category: 'logic',
    description: '用 LLM 识别用户意图并分支路由',
    example: '文本 {{query}} → 意图 1/2/… 或兜底「其他」',
    color: '#b88230',
    icon: 'Grid',
    defaults: {
      chatModelId: null,
      inputVariables: [{ key: 'query', value: '{{start_1.question}}' }],
      query: '{{query}}',
      systemPrompt: '',
      outputVariables: [
        { key: 'index', type: 'number', description: '命中意图序号（1..N），未命中为 0' },
        { key: 'reason', type: 'string', description: '分类依据说明' }
      ],
      intents: [{ name: '意图1', examples: [] }]
    },
    outputs: [
      { key: 'index', label: 'index', type: 'number' },
      { key: 'reason', label: 'reason', type: 'string' }
    ],
    fields: [
      { key: 'chatModelId', label: '大模型', input: 'select' },
      { key: 'inputVariables', label: '输入参数', input: 'table' },
      { key: 'systemPrompt', label: '系统提示词', input: 'textarea' }
    ],
    summarize(data) {
      const n = Array.isArray(data?.intents)
        ? data.intents.length
        : (Array.isArray(data?.classes) ? data.classes.length : 0)
      return `${n} 个意图`
    },
    hasValidationWarning(data) {
      const normalized = data || {}
      const inputs = normalized.inputVariables
      const hasInput = Array.isArray(inputs) && inputs.some((i) => i?.key && i?.value)
      const intents = normalized.intents
      const legacy = normalized.classes
      const count = Array.isArray(intents) ? intents.length : (Array.isArray(legacy) ? legacy.length : 0)
      return !hasInput && !normalized.query || count < 1
    }
  },
  {
    type: 'parameter-extractor',
    label: '参数提取',
    category: 'logic',
    description: '从自然语言文本中提取结构化字段',
    example: '从「张三，电话138…」提取 name、phone 等 schema 字段',
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
    description: '对数组执行 first / filter / map 等操作',
    example: 'operation=first，listRef={{kb_1.chunks}}，取首条 content',
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
 * 批处理节点对外输出（配置的 outputParameters 汇总数组 + count）。
 * @param {object} [batchData] 批处理节点 data
 * @returns {Array<{ key: string, label: string, type: string, description?: string }>}
 */
export function resolveBatchNodeOutputs(batchData) {
  const outputs = []
  const seen = new Set()
  const params = Array.isArray(batchData?.outputParameters) ? batchData.outputParameters : []
  params.forEach((p) => {
    const key = (p?.key || '').trim()
    if (!key || seen.has(key)) return
    seen.add(key)
    outputs.push({
      key,
      label: key,
      type: 'array',
      description: '批处理汇总数组'
    })
  })
  outputs.push({ key: 'count', label: 'count', type: 'number', description: '实际运行次数' })
  return outputs
}

/**
 * 解析节点可输出的变量列表（供变量选择器与模板引用）。
 * start 节点从 data.inputs 动态生成；其他节点使用 nodeMeta.outputs。
 * @param {string} wfType 节点类型
 * @param {object} [nodeData] 节点 data
 * @returns {Array<{ key: string, label: string, type: string }>}
 */
export function resolveNodeOutputs(wfType, nodeData) {
  if (wfType === 'answer' || wfType === 'end') {
    if (nodeData?.outputMode === 'text') {
      return [{ key: 'text', label: wfType === 'end' ? '返回文本' : '回答内容', type: 'string' }]
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
  if (wfType === 'variable-aggregator') {
    const keys = resolveAggregatorOutputKeys(nodeData)
    return keys.map((key) => ({
      key,
      label: key,
      type: 'any'
    }))
  }
  if (wfType === 'llm') {
    const mcpOutput = nodeData?.useMcpTools
      ? [
        {
          key: 'mcpToolsUsed',
          label: 'MCP 工具调用',
          type: 'array',
          description: '本次调用的 MCP 工具名称列表'
        },
        {
          key: 'mcpToolResults',
          label: 'MCP 工具结果',
          type: 'array',
          description: '工具入参与原始返回（bing_search 等），未经大模型改写'
        }
      ]
      : []
    const vars = nodeData?.outputVariables
    if (Array.isArray(vars) && vars.length) {
      return [
        ...vars
          .filter((item) => item?.key)
          .map((item) => ({
            key: item.key,
            label: item.key,
            type: item.type || 'string',
            description: item.description?.trim() || ''
          })),
        ...mcpOutput
      ]
    }
    if (nodeData?.outputFormat === 'json') {
      return [
        { key: 'json', label: 'JSON', type: 'object' },
        { key: 'text', label: '原始文本', type: 'string' },
        ...mcpOutput
      ]
    }
    return [
      { key: 'output', label: 'output', type: 'string' },
      { key: 'text', label: 'text', type: 'string' },
      ...mcpOutput
    ]
  }
  if (wfType === 'loop') {
    const outputVarName = String(nodeData?.outputVariableName || 'results').trim() || 'results'
    const outputs = [
      { key: outputVarName, label: outputVarName, type: 'array' },
      { key: 'count', label: 'count', type: 'number' }
    ]
    if (nodeData?.outputMode === 'variable' && nodeData?.outputVariableKey) {
      outputs.push({
        key: 'outputVariable',
        label: '中间变量最终值',
        type: 'any'
      })
    }
    const intermediates = nodeData?.intermediateVariables
    if (Array.isArray(intermediates)) {
      intermediates.forEach((item) => {
        if (item?.key) {
          outputs.push({ key: item.key, label: item.key, type: 'string' })
        }
      })
    }
    return outputs
  }
  if (wfType === 'loop-set-variable' && nodeData?.target) {
    return [{ key: nodeData.target, label: nodeData.target, type: 'string' }]
  }
  if (wfType === 'batch') {
    return resolveBatchNodeOutputs(nodeData)
  }
  if (wfType === 'text-process') {
    if (nodeData?.processMode === 'split') {
      return [
        { key: 'items', label: 'items', type: 'array' },
        { key: 'count', label: 'count', type: 'number' },
        { key: 'text', label: 'text', type: 'string' }
      ]
    }
    return [
      { key: 'output', label: 'output', type: 'string' },
      { key: 'text', label: 'text', type: 'string' }
    ]
  }
  if (wfType === 'json-deserialize') {
    const outputs = [{ key: 'output', label: 'output', type: 'object' }]
    const fields = nodeData?.outputFields
    if (Array.isArray(fields)) {
      fields
        .filter((item) => (item?.key || '').trim())
        .forEach((item) => {
          outputs.push({
            key: `output.${item.key}`,
            label: item.key,
            type: item.type || 'string'
          })
        })
    }
    return outputs
  }
  if (wfType === 'code') {
    const vars = nodeData?.outputVariables
    const outputs = Array.isArray(vars) && vars.length
      ? vars
          .filter((item) => item?.key)
          .map((item) => ({
            key: item.key,
            label: item.key,
            type: item.type || 'string'
          }))
      : [{ key: 'result', label: 'result', type: 'string' }]
    return [
      ...outputs,
      { key: 'isSuccess', label: 'isSuccess', type: 'boolean' },
      { key: 'errorBody', label: 'errorBody', type: 'string' }
    ]
  }
  if (wfType === 'question-classifier') {
    const vars = nodeData?.outputVariables
    if (Array.isArray(vars) && vars.length) {
      return vars
        .filter((item) => item?.key)
        .map((item) => ({
          key: item.key,
          label: item.key,
          type: item.type || 'string',
          description: item.description?.trim() || ''
        }))
    }
    return NODE_META_MAP[wfType]?.outputs || []
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
 * 获取节点图标名（Element Plus 图标组件名）。
 * @param {string} type 节点类型
 * @returns {string}
 */
export function getNodeIcon(type) {
  return NODE_META_MAP[type]?.icon || 'Box'
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
