/**
 * C7MessageBox - 确认框组件
 * 
 * 基于 Element Plus 的 ElMessageBox 封装，提供统一的确认框、提示框、输入框功能
 */

import { ElMessageBox, ElMessage } from 'element-plus'
import type { MessageBoxOptions } from 'element-plus'
import { logger } from '../../utils/logger'

/**
 * 确认框选项接口
 */
export interface C7MessageBoxOptions {
  // 标题
  title?: string
  // 确认按钮文本
  confirmButtonText?: string
  // 取消按钮文本
  cancelButtonText?: string
  // 类型
  type?: 'success' | 'warning' | 'info' | 'error'
  // 是否显示取消按钮
  showCancelButton?: boolean
  // 是否显示确认按钮
  showConfirmButton?: boolean
  // 是否显示关闭按钮
  showClose?: boolean
  // 是否可拖拽
  draggable?: boolean
  // 自定义类名
  customClass?: string
  // 确认前的回调（返回 false 或 Promise<false> 可阻止确认）
  beforeClose?: (action: string, instance: any, done: () => void) => void | Promise<void>
  // 是否显示输入框
  input?: boolean
  // 输入框类型
  inputType?: string
  // 输入框占位符
  inputPlaceholder?: string
  // 输入框初始值
  inputValue?: string
  // 输入框验证函数
  inputValidator?: (value: string) => boolean | string | Promise<boolean | string>
  // 输入框错误提示
  inputErrorMessage?: string
  // 是否显示输入框错误提示
  showInputErrorMessage?: boolean
  // 是否危险操作（红色确认按钮）
  dangerouslyUseHTMLString?: boolean
  // 自定义内容
  message?: string
  // 是否居中布局
  center?: boolean
  // 是否可关闭遮罩层
  closeOnClickModal?: boolean
  // 是否可按 ESC 关闭
  closeOnPressEscape?: boolean
  // 是否可关闭遮罩层
  lockScroll?: boolean
  // 是否显示加载状态
  loading?: boolean
  // 异步确认函数（返回 Promise，支持 loading 状态）
  asyncConfirm?: () => Promise<any>
}

/**
 * 确认框结果
 */
export interface C7MessageBoxResult {
  value?: string
  action: 'confirm' | 'cancel' | 'close'
}

/**
 * 显示确认框
 * 
 * @param message 提示信息
 * @param title 标题
 * @param options 选项
 * @returns Promise<C7MessageBoxResult>
 */
export function c7Confirm(
  message: string,
  title: string = '提示',
  options: C7MessageBoxOptions = {}
): Promise<C7MessageBoxResult> {
  const {
    confirmButtonText = '确定',
    cancelButtonText = '取消',
    type = 'warning',
    showCancelButton = true,
    showConfirmButton = true,
    showClose = true,
    draggable = false,
    customClass = '',
    beforeClose,
    center = false,
    closeOnClickModal = false,
    closeOnPressEscape = true,
    lockScroll = true,
    loading = false,
    asyncConfirm,
    dangerouslyUseHTMLString = false
  } = options

  const messageBoxOptions: MessageBoxOptions = {
    title,
    message,
    type,
    confirmButtonText,
    cancelButtonText,
    showCancelButton,
    showConfirmButton,
    showClose,
    draggable,
    customClass,
    beforeClose,
    center,
    closeOnClickModal,
    closeOnPressEscape,
    lockScroll,
    dangerouslyUseHTMLString
  }

  // 如果有异步确认函数，包装 beforeClose
  if (asyncConfirm) {
    messageBoxOptions.beforeClose = async (action: string, instance: any, done: () => void) => {
      if (action === 'confirm') {
        instance.confirmButtonLoading = true
        try {
          await asyncConfirm()
          done()
        } catch (error: any) {
          instance.confirmButtonLoading = false
          logger.error('确认操作失败:', error)
          ElMessage.error(error?.message || '操作失败')
        }
      } else {
        done()
      }
    }
  }

  return ElMessageBox.confirm(message, title, messageBoxOptions)
    .then(() => ({ action: 'confirm' as const }))
    .catch((error: any) => {
      if (error === 'cancel' || error === 'close') {
        return { action: error as 'cancel' | 'close' }
      }
      throw error
    })
}

/**
 * 显示提示框
 * 
 * @param message 提示信息
 * @param title 标题
 * @param options 选项
 * @returns Promise<void>
 */
export function c7Alert(
  message: string,
  title: string = '提示',
  options: Omit<C7MessageBoxOptions, 'showCancelButton' | 'asyncConfirm'> = {}
): Promise<void> {
  const {
    confirmButtonText = '确定',
    type = 'info',
    showConfirmButton = true,
    showClose = true,
    draggable = false,
    customClass = '',
    center = false,
    closeOnClickModal = false,
    closeOnPressEscape = true,
    lockScroll = true,
    dangerouslyUseHTMLString = false
  } = options

  return ElMessageBox.alert(message, title, {
    confirmButtonText,
    type,
    showConfirmButton,
    showClose,
    draggable,
    customClass,
    center,
    closeOnClickModal,
    closeOnPressEscape,
    lockScroll,
    dangerouslyUseHTMLString
  })
}

/**
 * 显示输入框
 * 
 * @param message 提示信息
 * @param title 标题
 * @param options 选项
 * @returns Promise<C7MessageBoxResult>
 */
export function c7Prompt(
  message: string,
  title: string = '提示',
  options: C7MessageBoxOptions = {}
): Promise<C7MessageBoxResult> {
  const {
    confirmButtonText = '确定',
    cancelButtonText = '取消',
    type = 'info',
    showCancelButton = true,
    showConfirmButton = true,
    showClose = true,
    draggable = false,
    customClass = '',
    input = true,
    inputType = 'text',
    inputPlaceholder = '请输入',
    inputValue = '',
    inputValidator,
    inputErrorMessage = '输入格式不正确',
    showInputErrorMessage = true,
    center = false,
    closeOnClickModal = false,
    closeOnPressEscape = true,
    lockScroll = true,
    dangerouslyUseHTMLString = false
  } = options

  return ElMessageBox.prompt(message, title, {
    confirmButtonText,
    cancelButtonText,
    type,
    showCancelButton,
    showConfirmButton,
    showClose,
    draggable,
    customClass,
    input,
    inputType,
    inputPlaceholder,
    inputValue,
    inputValidator,
    inputErrorMessage,
    showInputErrorMessage,
    center,
    closeOnClickModal,
    closeOnPressEscape,
    lockScroll,
    dangerouslyUseHTMLString
  })
    .then(({ value }) => ({ value, action: 'confirm' as const }))
    .catch((error: any) => {
      if (error === 'cancel' || error === 'close') {
        return { action: error as 'cancel' | 'close' }
      }
      throw error
    })
}

/**
 * 危险操作确认框（红色确认按钮）
 * 
 * @param message 提示信息
 * @param title 标题
 * @param options 选项
 * @returns Promise<C7MessageBoxResult>
 */
export function c7DangerConfirm(
  message: string,
  title: string = '危险操作',
  options: C7MessageBoxOptions = {}
): Promise<C7MessageBoxResult> {
  return c7Confirm(message, title, {
    ...options,
    type: 'warning',
    confirmButtonText: options.confirmButtonText || '确定删除',
    customClass: options.customClass || 'c7-message-box-danger'
  })
}

/**
 * 默认导出对象（提供统一 API）
 */
export default {
  confirm: c7Confirm,
  alert: c7Alert,
  prompt: c7Prompt,
  dangerConfirm: c7DangerConfirm
}

