import useUserStore from '@/store/modules/user'

export default {
  mounted(el, binding) {
    checkPermission(el, binding)
  },
  updated(el, binding) {
    checkPermission(el, binding)
  }
}

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
