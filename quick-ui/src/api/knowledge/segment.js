/**
 * 知识库分段配置工具：构建 segmentConfig JSON（未自定义时返回 null）。
 *
 * @param {boolean} customized 是否覆盖知识库默认
 * @param {Record<string, any>} form 分段表单
 * @returns {Record<string, any>|null}
 */
export function buildSegmentConfig(customized, form) {
  if (!customized) return null
  return {
    segmentMode: form.segmentMode,
    chunkSize: form.chunkSize,
    chunkOverlap: form.chunkOverlap,
    chunkDelimiter: form.chunkDelimiter,
    preprocessNormalizeWs: form.preprocessNormalizeWs,
    preprocessRemoveUrl: form.preprocessRemoveUrl,
    preprocessRemoveEmail: form.preprocessRemoveEmail
  }
}

/** 默认分段表单值 */
export function defaultSegmentForm() {
  return {
    segmentMode: 'AUTO',
    chunkSize: 800,
    chunkOverlap: 120,
    chunkDelimiter: 'DOUBLE_NEWLINE',
    preprocessNormalizeWs: true,
    preprocessRemoveUrl: false,
    preprocessRemoveEmail: false
  }
}

/** 从知识库详情填充默认展示 */
export function segmentFormFromKb(kb) {
  const base = defaultSegmentForm()
  if (!kb) return base
  return {
    segmentMode: kb.segmentMode || base.segmentMode,
    chunkSize: kb.chunkSize ?? base.chunkSize,
    chunkOverlap: kb.chunkOverlap ?? base.chunkOverlap,
    chunkDelimiter: kb.chunkDelimiter || base.chunkDelimiter,
    preprocessNormalizeWs: kb.preprocessNormalizeWs === 0 ? false : kb.preprocessNormalizeWs === 1 ? true : base.preprocessNormalizeWs,
    preprocessRemoveUrl: kb.preprocessRemoveUrl === 1,
    preprocessRemoveEmail: kb.preprocessRemoveEmail === 1
  }
}
