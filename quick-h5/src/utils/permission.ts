/**
 * H5 权限校验：对齐 PC `$auth` / v-hasPermi。
 * - 含 `*:*:*` 视为全部权限
 * - 角色含 `admin` 视为超级角色（hasRole）
 */

import { useUserStore } from '@/stores/user'

/** 是否具备任一权限码 */
export function hasPermi(codes: string | string[]): boolean {
  const list = Array.isArray(codes) ? codes : [codes]
  if (!list.length) return false
  const permissions = useUserStore().permissions || []
  return permissions.some(
    (p) => p === '*:*:*' || list.includes(p),
  )
}

/** 是否具备任一角色键 */
export function hasRole(roles: string | string[]): boolean {
  const list = Array.isArray(roles) ? roles : [roles]
  if (!list.length) return false
  const mine = useUserStore().roles || []
  return mine.some((r) => r === 'admin' || list.includes(r))
}
