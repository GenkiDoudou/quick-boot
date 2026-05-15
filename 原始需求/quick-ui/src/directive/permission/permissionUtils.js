import useUserStore from '@/store/modules/user'

/**
 * 检查权限
 * @param {Array} permissions 需要的权限列表
 */
export function checkPermission(permissions) {
  const all_permission = '*:*:*'
  const userPermissions = useUserStore().permissions || []
  if (permissions && permissions.length > 0) {
    return userPermissions.some(p => all_permission === p || permissions.includes(p))
  }
  return false
}
