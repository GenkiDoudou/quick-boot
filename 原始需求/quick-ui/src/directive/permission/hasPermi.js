/**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2019 ruoyi
 */

import useUserStore from '@/store/modules/user'

export default {
    mounted(el, binding, vnode) {
        checkPermission(el, binding)
    },
    updated(el, binding, vnode) {
        checkPermission(el, binding)
    }
}

/**
 * 检查权限并控制元素显示/隐藏
 * @param {HTMLElement} el - DOM元素
 * @param {Object} binding - 指令绑定对象
 */
function checkPermission(el, binding) {
    const {value} = binding
    const all_permission = "*:*:*"
    const userStore = useUserStore()
    const permissions = userStore.permissions || []
    

    
    if (value && value instanceof Array && value.length > 0) {
        const permissionFlag = value
        const hasPermissions = permissions.some(permission => {
            return all_permission === permission || permissionFlag.includes(permission)
        })


        if (!hasPermissions) {
            // 隐藏元素
            el.style.display = 'none'
            el.setAttribute('data-has-permi', 'false')
        } else {
            // 显示元素
            el.style.display = ''
            el.setAttribute('data-has-permi', 'true')
        }
    } else {

        // 如果没有权限参数，默认隐藏
        el.style.display = 'none'
        el.setAttribute('data-has-permi', 'no-permission')
    }
}
