/**
 * 默认防抖时间（毫秒）
 */
export const DEFAULT_DEBOUNCE_TIME = 300

/**
 * 默认分页大小
 */
export const DEFAULT_PAGE_SIZE = 10

/**
 * 默认分页大小选项
 */
export const DEFAULT_PAGE_SIZES = [10, 20, 30, 50]

/**
 * 按钮预设类型常量
 */
export const BUTTON_TYPES = {
  ADD: 'add',
  EDIT: 'edit',
  DELETE: 'delete',
  QUERY: 'query',
  REFRESH: 'refresh',
  UPLOAD: 'upload',
  DOWNLOAD: 'download',
  SUBMIT: 'submit',
  CANCEL: 'cancel'
} as const

/**
 * 按钮类型
 */
export type ButtonType = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default'

/**
 * 按钮大小
 */
export type Size = 'large' | 'default' | 'small'

