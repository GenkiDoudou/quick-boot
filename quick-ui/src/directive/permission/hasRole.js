/**
 * v-hasRole 角色指令：当前用户不含指定角色时从 DOM 移除元素。
 *
 * 用法：`v-hasRole="['admin']"`；超级管理员角色 `admin` 始终通过。
 */
import useUserStore from '@/store/modules/user'

export default {
  mounted(el, binding) {
    const { value } = binding
    const super_admin = 'admin'
    const roles = useUserStore().roles

    if (value && value instanceof Array && value.length > 0) {
      const roleFlag = value
      const hasRole = roles.some(role => {
        return super_admin === role || roleFlag.includes(role)
      })
      if (!hasRole) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error('请设置角色权限标签值')
    }
  }
}
