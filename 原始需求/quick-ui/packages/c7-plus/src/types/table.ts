import { IObject } from './form'

export enum ColumnEnumType {
  // 文本
  TEXT = 'text',
  // 图片
  IMAGE = 'image',
  // 标签
  TAG = 'tag',
  // 插槽
  SLOT = 'slot'
}

/**
 * 列配置属性定义
 */
export interface TableColumnProps {
  // 列类型
  columnType: ColumnEnumType
  // 是否显示
  visible?: boolean
  // 排序
  order?: number
  // 列的类型
  type?: 'selection' | 'index' | 'expand'
  // 自定义索引值
  index?: number | ((row: any, rowIndex: number) => number)
  // 列标题显示文本
  label?: string
  // 列的唯一标识key
  columnKey?: string
  // 字段名称
  prop?: string
  // 列宽度
  width?: string | number
  // 列最小宽度
  minWidth?: string | number
  // 列固定位置
  fixed?: boolean | 'left' | 'right'
  // 自定义表头渲染函数
  renderHeader?: (h: any, column: any, $index: number) => any
  // 排序配置
  sortable?: boolean | 'custom'
  // 自定义排序方法
  sortMethod?: (a: any, b: any) => number
  // 排序字段
  sortBy?: string | string[] | Record<string, any>
  // 排序策略轮转顺序
  sortOrders?: ('ascending' | 'descending' | null)[]
  // 是否允许调整列宽
  resizable?: boolean
  // 内容格式化函数
  formatter?: (row: any, column: any, cellValue: any) => string
  // 内容过长时显示tooltip
  showOverflowTooltip?: boolean | Record<string, any>
  // 列对齐方式
  align?: 'left' | 'center' | 'right'
  // 表头对齐方式
  headerAlign?: 'left' | 'center' | 'right'
  // 列自定义类名
  className?: string
  // 列标题自定义类名
  labelClassName?: string
  // 多选列的可选状态判断函数
  selectable?: (row: any, index: number) => boolean
  // 数据刷新后是否保留已选项
  reserveSelection?: boolean
  // 数据过滤选项配置
  filters?: { text: string; value: any }[]
  // 过滤弹出框定位策略
  filterPlacement?: string
  // 过滤弹出框自定义类名
  filterClassName?: string
  // 过滤是否支持多选
  filterMultiple?: boolean
  // 自定义过滤方法
  filterMethod?: (value: any, row: any, column: any) => boolean
  // 已选中的过滤值
  filteredValue?: Record<string, any>
  // 自定义tooltip内容
  tooltipFormatter?: (row: any) => string
  // 数据列表
  dictList?: IObject[]
  // 插槽名称
  slotName?: string
  // 是否可排序（前端排序）
  sortable?: boolean | 'custom'
  // 后端排序字段名（与 prop 不同时使用）
  sortProp?: string
  // 是否可筛选
  filterable?: boolean
  // 筛选类型：text（文本）、number（数字）、date（日期）、select（下拉选择）
  filterType?: 'text' | 'number' | 'date' | 'select'
  // 筛选选项（filterType 为 select 时使用）
  filterOptions?: Array<{ label: string; value: any }>
  // 是否可调整列宽
  resizable?: boolean
  // 是否可拖拽排序
  draggable?: boolean
  // 是否默认隐藏
  hidden?: boolean
  // 列顺序（用于列排序）
  order?: number
}

/**
 * 表格排序配置
 */
export interface TableSortConfig {
  // 排序字段
  prop: string
  // 排序方向：ascending（升序）、descending（降序）
  order: 'ascending' | 'descending' | null
}

/**
 * 表格筛选配置
 */
export interface TableFilterConfig {
  // 筛选字段
  prop: string
  // 筛选值
  value: any
  // 筛选类型
  type: 'text' | 'number' | 'date' | 'select'
}

/**
 * 表格列配置（用于列显示/隐藏控制）
 */
export interface TableColumnConfig {
  // 列标识
  prop: string
  // 是否显示
  visible: boolean
  // 列顺序
  order: number
  // 列宽度
  width?: string | number
}

