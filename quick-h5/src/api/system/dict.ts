/**
 * 系统字典 API 与前端选项映射工具。
 */
import { request } from '../http'

/** 后端字典数据行 */
export type SysDictData = {
  dictCode?: number | string
  dictSort?: number
  dictLabel?: string
  dictValue?: string
  dictType?: string
  cssClass?: string
  listClass?: string
  status?: string
}

/** 前端消费用的字典选项 */
export type DictOption = {
  label: string
  value: string
  listClass?: string
  cssClass?: string
}

/** 按 dictType 拉取字典数据列表 */
export function getDicts(dictType: string) {
  return request<SysDictData[]>({
    url: `/sys/dict/data/type/${encodeURIComponent(dictType)}`,
    method: 'GET',
  })
}

/** 将 SysDictData[] 转为 label/value 选项，供 Select、Tag 使用 */
export function mapDictOptions(rows: SysDictData[] | null | undefined): DictOption[] {
  return (rows || []).map((p) => ({
    label: p.dictLabel || '',
    value: String(p.dictValue ?? ''),
    listClass: p.listClass,
    cssClass: p.cssClass,
  }))
}

/** 根据 value 反查 label，未命中返回 fallback（默认「—」） */
export function dictLabel(options: DictOption[] | undefined, value: unknown, fallback = '—') {
  const v = value == null ? '' : String(value)
  const hit = (options || []).find((o) => o.value === v)
  return hit?.label || fallback
}
