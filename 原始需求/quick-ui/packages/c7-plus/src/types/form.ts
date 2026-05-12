export interface IObject {
  [key: string]: any
}

export enum IColumnEnum {
  // 输入框类型
  INPUT = 'input',
  // 数字输入框
  INPUT_NUMBER = 'input-number',
  // 下拉选择
  SELECT = 'select',
  // 日期选择
  DATE_PICKER = 'date',
  // 日期范围
  DATE_RANGE = 'daterange',
  // 多选框
  CHECKBOX = 'checkbox',
  // 级联选择
  CASCADER = 'cascader',
  // 文件上传
  UPLOAD = 'upload',
  // 单选框
  RADIO = 'radio',
  // 插槽
  SLOT = 'slot'
}

/**
 * 字段配置
 */
export interface IColumn {
  // 标签文本
  label: string
  // model 的键名
  prop: string
  // 标签宽度
  labelWidth?: string
  // 是否为必填项
  required?: boolean
  // 是否显示
  display?: boolean
  // 排序
  order?: number
  // 表单验证规则
  rules?: IObject | {}
  // 输入框提示文字
  placeholder?: string
  // 输入框宽度
  columnsWidth?: string | number
  // 输入框类型
  type?: IColumnEnum
  // 栅格占位
  span?: number
  // 字典type
  dictType?: string
  // 字典数据
  dataList?: IObject[]
  // 函数
  change?: Function
  // 日期格式
  format?: string
  // 绑定值格式
  valueFormat?: string
  // 范围选择时开始日期的占位内容
  startPlaceholder?: string
  // 范围选择时结束日期的占位内容
  endPlaceholder?: string
  // 选择范围时的分隔符
  rangeSeparator?: string
  // 可选，选择器打开时默认显示的时间
  defaultValue?: string
  // 范围选择时选中日期所使用的当日内具体时刻
  defaultTime?: string
  // 组件名称
  component?: string
  // 组件的 props 对象
  props?: Record<string, any>
  // 动态插槽配置
  slots?: Record<string, any>
  // 是否使用插槽
  useSlot?: boolean
  // 插槽名称
  slotName?: string
  // 数值精度
  precision?: number
  // 最小值
  min?: number
  // 最大值
  max?: number
  // 返回结果类型
  resultType?: number
  // 分隔符
  separator?: string
  // 文件数量限制
  limit?: number
  // 文件大小限制
  fileSize?: number
  // 文件类型限制
  fileType?: string[]
  // 是否多选
  multiple?: boolean
  // 是否显示提示
  isShowTip?: boolean
  // 上传的url
  uploadUrl?: string
  // 删除的url
  deleteUrl?: string
  // c7-select 相关属性
  labelKey?: string
  valueKey?: string
  group?: boolean
  tag?: boolean
  remote?: boolean
  fetchData?: Function
  fetchParams?: Record<string, any>
  resultKey?: string
  dataFormatter?: Function
  autoLoad?: boolean
  // 表单联动配置
  // 依赖字段（当依赖字段变化时触发联动）
  dependsOn?: string | string[]
  // 联动函数（返回新的字段配置）
  linkage?: (value: any, formData: Record<string, any>) => Partial<FormColumn>
  // 是否根据依赖字段显示/隐藏
  visibleWhen?: (formData: Record<string, any>) => boolean
  // 是否根据依赖字段禁用/启用
  disabledWhen?: (formData: Record<string, any>) => boolean
  // 是否根据依赖字段动态更新选项
  optionsWhen?: (formData: Record<string, any>) => IObject[]
}

/**
 * 表单列类型
 */
export interface FormColumn extends IColumn {
  span?: number
  componentType?: string
  disabled?: boolean
  visible?: boolean
  change?: (payload: {
    prop: string
    value: any
    formData: Record<string, any>
  }) => void
}

