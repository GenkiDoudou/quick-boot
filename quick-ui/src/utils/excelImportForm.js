/**
 * 导入 multipart 公共字段（避免 api 与 excelImport 循环依赖）。
 * @param {FormData} formData
 * @param {boolean} updateSupport
 * @param {{ mode?: string, syncMaxRows?: number, forceAsync?: boolean, contextJson?: object|string }} [opts]
 */
export function appendImportFormFields(formData, updateSupport, opts = {}) {
  formData.append('updateSupport', String(!!updateSupport))
  const mode = opts.forceAsync ? 'async' : opts.mode
  if (mode) formData.append('mode', mode)
  if (opts.syncMaxRows != null) formData.append('syncMaxRows', String(opts.syncMaxRows))
  if (opts.contextJson != null) {
    const json = typeof opts.contextJson === 'string' ? opts.contextJson : JSON.stringify(opts.contextJson)
    if (json && json !== '{}') formData.append('contextJson', json)
  }
}
