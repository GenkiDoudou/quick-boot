/**
 * 工作台菜单本地 mock（仅开发兜底，默认不启用）。
 * 正式环境以 GET /system/menu/h5Workbench 为准；勿在失败时回退本文件全量列表。
 *
 * H5 菜单约定：sys_menu.path 以 /pages/ 开头；M=分组，C=入口，F=按钮权限（不进工作台）。
 */

export interface WorkbenchMenuItem {
  id: string
  label: string
  short: string
  tone?: 'green' | 'blue' | 'amber' | 'rose'
  /** uni 页面 path */
  path?: string
}

export interface WorkbenchMenuGroup {
  id: string
  title: string
  items: WorkbenchMenuItem[]
}

/** @deprecated 勿作为工作台默认数据源 */
export const workbenchMenuGroups: WorkbenchMenuGroup[] = [
  {
    id: 'system',
    title: '系统管理',
    items: [
      { id: 'user', label: '用户', short: '用', path: '/pages/system/user/index' },
      { id: 'dept', label: '部门', short: '部', path: '/pages/system/dept/index' },
      { id: 'role', label: '角色', short: '角', tone: 'blue', path: '/pages/system/role/index' },
    ],
  },
]
