import { ElMessage } from 'element-plus'

/**
 * 错误处理选项
 */
export interface ErrorHandlerOptions {
  showToast?: boolean
  logError?: boolean
  defaultMessage?: string
}

/**
 * 统一错误处理函数
 * @param error 错误对象
 * @param options 处理选项
 */
export function handleError(
  error: any,
  options: ErrorHandlerOptions = {}
): void {
  const {
    showToast = true,
    logError = true,
    defaultMessage = '操作失败，请稍后重试'
  } = options

  if (logError) {
    console.error('[C7-Plus Error]:', error)
  }

  if (showToast) {
    let message = defaultMessage
    
    // 优先使用响应中的错误信息
    if (error?.response?.data?.msg) {
      message = error.response.data.msg
    } else if (error?.message) {
      message = error.message
    } else if (typeof error === 'string') {
      message = error
    }

    ElMessage.error(message)
  }
}

