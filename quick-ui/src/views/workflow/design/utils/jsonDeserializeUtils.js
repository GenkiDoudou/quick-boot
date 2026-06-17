/**
 * 从 JSON 示例生成 json-deserialize 节点的 outputFields。
 */

const TYPE_OPTIONS = ['string', 'number', 'boolean', 'object', 'array']

/**
 * 推断 JSON 值的字段类型。
 * @param {*} value
 * @returns {string}
 */
export function inferJsonFieldType(value) {
  if (value === null || value === undefined) return 'string'
  if (Array.isArray(value)) return 'array'
  const t = typeof value
  if (t === 'number') return 'number'
  if (t === 'boolean') return 'boolean'
  if (t === 'object') return 'object'
  return 'string'
}

/**
 * 遍历 object 树，收集深度 ≤ maxDepth 的叶子 primitive 字段。
 * @param {object} root JSON 根对象
 * @param {number} [maxDepth=3]
 * @returns {{ key: string, path: string, type: string }[]}
 */
export function collectLeafFields(root, maxDepth = 3) {
  /** @type {{ key: string, path: string, type: string }[]} */
  const fields = []

  /**
   * @param {object} obj
   * @param {string} prefix
   * @param {number} depth 当前 object 所在深度（根为 1）
   */
  function walk(obj, prefix, depth) {
    if (!obj || typeof obj !== 'object' || Array.isArray(obj)) return
    if (depth > maxDepth) return
    for (const [key, val] of Object.entries(obj)) {
      const path = prefix ? `${prefix}.${key}` : key
      if (val !== null && typeof val === 'object' && !Array.isArray(val)) {
        walk(val, path, depth + 1)
      } else {
        fields.push({
          key,
          path,
          type: inferJsonFieldType(val)
        })
      }
    }
  }

  walk(root, '', 1)
  return fields
}

/**
 * 解析 JSON 示例文本并生成 outputFields。
 * @param {string} jsonText
 * @returns {{ key: string, path: string, type: string }[]}
 */
export function generateFieldsFromJsonExample(jsonText) {
  const trimmed = (jsonText || '').trim()
  if (!trimmed) {
    throw new Error('请输入 JSON 示例')
  }
  let parsed
  try {
    parsed = JSON.parse(trimmed)
  } catch {
    throw new Error('JSON 格式无效，请检查后重试')
  }
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    throw new Error('根须为 JSON 对象')
  }
  const fields = collectLeafFields(parsed)
  if (!fields.length) {
    throw new Error('未找到可生成的叶子字段')
  }
  return fields
}

export { TYPE_OPTIONS }
