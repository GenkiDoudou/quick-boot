/**
 * Monaco 代码格式化辅助：为 Python 注册基础整理，并提供轻量缩进修复。
 */

let pythonProviderReady = false

/**
 * 注册 Python 文档格式化 Provider（Monaco 默认不提供 Python formatter）。
 * @param {typeof import('monaco-editor')} monaco
 */
export function ensurePythonFormatProvider(monaco) {
  if (pythonProviderReady || !monaco?.languages?.registerDocumentFormattingEditProvider) {
    return
  }
  pythonProviderReady = true
  monaco.languages.registerDocumentFormattingEditProvider('python', {
    provideDocumentFormattingEdits(model) {
      const formatted = basicFormatPython(model.getValue())
      return [{
        range: model.getFullModelRange(),
        text: formatted
      }]
    }
  })
}

/**
 * Python 轻量格式化：统一缩进、去除行尾空白、压缩多余空行。
 * @param {string} code
 * @returns {string}
 */
export function basicFormatPython(code) {
  if (!code) return ''
  const normalized = code.replace(/\t/g, '    ').replace(/\r\n/g, '\n')
  const lines = normalized.split('\n')
  const result = []
  let blankPending = false

  for (const rawLine of lines) {
    const trimmedEnd = rawLine.replace(/\s+$/g, '')
    if (!trimmedEnd.trim()) {
      if (result.length > 0 && !blankPending) {
        blankPending = true
        result.push('')
      }
      continue
    }
    blankPending = false
    const indentMatch = trimmedEnd.match(/^(\s*)/)
    const indent = indentMatch ? indentMatch[1] : ''
    const spaces = indent.replace(/\s/g, ' ').length
    const level = Math.floor(spaces / 4)
    const content = trimmedEnd.trim()
    result.push(`${'    '.repeat(Math.max(0, level))}${content}`)
  }

  return `${result.join('\n').replace(/\n+$/g, '\n')}\n`
}
