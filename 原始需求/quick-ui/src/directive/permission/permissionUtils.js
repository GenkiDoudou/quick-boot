/**
 * 权限工具类
 * 提供权限检查相关的工具函数
 * Copyright (c) 2024 quick-boot
 */

import useUserStore from '@/store/modules/user'

/**
 * 检查用户是否拥有指定权限
 * @param {string|Array} permission 权限标识，可以是字符串（用逗号分隔）或字符串数组
 * @returns {boolean} 是否有权限
 */
export function checkPermission(permission) {
  if (!permission) {
    return false
  }

  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  const all_permission = "*:*:*"

  // 如果用户有超级管理员权限，直接返回 true
  if (permissions.includes(all_permission)) {
    return true
  }

  let permissionList = []

  // 处理字符串输入（用逗号分隔）
  if (typeof permission === 'string') {
    permissionList = permission.split(',').map(p => p.trim()).filter(p => p)
  }
  // 处理数组输入
  else if (Array.isArray(permission)) {
    permissionList = permission.filter(p => p)
  }

  // 如果没有有效权限，返回 false
  if (permissionList.length === 0) {
    return false
  }

  // 检查是否拥有任意一个权限
  return permissionList.some(perm => permissions.includes(perm))
}

// 默认导出
export default {
  checkPermission
}