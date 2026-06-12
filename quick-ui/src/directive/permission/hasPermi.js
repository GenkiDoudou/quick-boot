/**
 * v-hasPermi 权限指令：无权限时隐藏 DOM（display:none）。
 *
 * 用法：v-hasPermi="['system:user:add']"
 * 校验逻辑与 plugins/auth.js 一致：permissions 含 *:*:* 或包含任一指定字符即通过。
 * 与路由级权限（后端菜单）互补：路由控制页面可见，指令控制按钮可见。
 */
import useUserStore from '@/store/modules/user'

export default {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

/**
 * @param {HTMLElement} el
 * @param {import('vue').DirectiveBinding<string[]>} binding value 为权限字符数组
 */
function checkPermission(el, binding) {
  const { value } = binding
  const all_permission = '*:*:*'
  const permissions = useUserStore().permissions || []

  if (value && value instanceof Array && value.length > 0) {
    const permissionFlag = value
    const hasPermissions = permissions.some(permission => {
      return all_permission === permission || permissionFlag.includes(permission)
    })
    if (!hasPermissions) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  } else {
    el.style.display = 'none'
  }
}
