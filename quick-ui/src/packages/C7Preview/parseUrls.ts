/**
 * 将业务侧常见的「逗号分隔附件 URL」字符串规范为数组。
 *
 * @param urls - 原始字符串；**`undefined`/`null`** 按空串处理
 * @returns 非空 URL 片段列表（顺序与出现顺序一致；每项已 **trim**）
 */
export function parseUrls(urls: string | null | undefined): string[] {
  const raw = urls == null ? '' : String(urls)
  return raw
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0)
}
