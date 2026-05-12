/**
 * 工具函数 Composable
 * 提供常用的工具方法
 */
import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels } from '@/utils/ruoyi'
import * as validate from '@/utils/validate'
import { download } from '@/utils/request'
import { checkPermission } from '@/directive/permission/permissionUtils'

/**
 * 工具函数 Composable
 * 提供全局工具方法的统一访问入口
 */
export function useUtils() {
  return {
    // 日期时间格式化
    parseTime,
    // 表单重置（需要传入 ref）
    resetForm,
    // 添加日期范围
    addDateRange,
    // 树形结构处理
    handleTree,
    // 字典标签选择
    selectDictLabel,
    selectDictLabels,
    // 验证函数
    validate,
    // 文件下载
    download,
    // 权限检查
    checkPermission
  }
}

