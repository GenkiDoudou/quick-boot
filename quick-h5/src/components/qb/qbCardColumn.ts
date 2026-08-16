/**
 * QbJsonCardFields 列配置类型（对齐 C7 tableColumns 思路的 H5 精简版）。
 */
import type { DictOption } from '@/api/system/dict'

/** 卡片 meta 区单列配置 */
export type QbCardColumn = {
  /** 行数据字段名 */
  prop: string
  /** 展示标签 */
  label: string
  /** 24 栅格宽度：6|8|12|16|24，默认 12 */
  span?: number
  /** 标签与值：row 同行 / stack 上下，默认 row */
  kv?: 'row' | 'stack'
  /** 渲染类型，默认 text */
  type?: 'text' | 'dict' | 'slot'
  /** type=dict 时的选项（由页面传入） */
  options?: DictOption[]
  /** type=slot 时插槽名，默认等于 prop */
  slotName?: string
  /** 空值占位，默认 — */
  emptyText?: string
  /** true 时 prop 为空则不渲染该列 */
  showIfProp?: boolean
}
