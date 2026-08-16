/**
 * 权限校验工具：供 v-hasPermi、C7JsonTable 按钮权限等复用。
 * 用户 permissions 含 `*:*:*` 或包含任一指定字符即通过。
 */
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
