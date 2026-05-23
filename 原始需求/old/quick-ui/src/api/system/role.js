import request, {downloadRequest} from '@/utils/request'

// 查询角色列表
export function listRole(query) {
    return request({
        url: '/system/role/list',
        method: 'get',
        params: query
    })
}

// 查询角色详细
export function getRole(roleId) {
    return request({
        url: '/system/role/' + roleId,
        method: 'get'
    })
}

// 新增角色
export function addRole(data) {
    return request({
        url: '/system/role',
        method: 'post',
        data: data
    })
}

// 修改角色
export function updateRole(data) {
    return request({
        url: '/system/role/update',
        method: 'post',
        data: data
    })
}

// 角色数据权限
export function dataScope(data) {
    return request({
        url: '/system/role/dataScope/update',
        method: 'post',
        data: data
    })
}

// 角色状态修改
export function changeRoleStatus(roleId, status) {
    const data = {
        roleId,
        status
    }
    return request({
        url: '/system/role/changeStatus/update',
        method: 'post',
        data: data
    })
}

// 删除角色
export function delRole(roleIds) {
    return request({
        url: '/system/role/delete',
        method: 'post',
        data: roleIds

    })
}

// 查询角色已授权用户列表
export function allocatedUserList(query) {
    return request({
        url: '/system/role/authUser/allocatedList',
        method: 'get',
        params: query
    })
}

// 查询角色未授权用户列表
export function unallocatedUserList(query) {
    return request({
        url: '/system/role/authUser/unallocatedList',
        method: 'get',
        params: query
    })
}

// 取消用户授权角色
export function authUserCancel(data) {
    return request({
        url: '/system/role/authUser/cancel',
        method: 'post',
        data: data
    })
}

// 批量取消用户授权角色
export function authUserCancelAll(data) {
    return request({
        url: '/system/role/authUser/cancelAll',
        method: 'post',
        params: data
    })
}

// 授权用户选择
export function authUserSelectAll(data) {
    return request({
        url: '/system/role/authUser/selectAll',
        method: 'post',
        params: data
    })
}

// 根据角色ID查询部门树结构
export function deptTreeSelect(roleId) {
    return request({
        url: '/system/role/deptTree/' + roleId,
        method: 'get'
    })
}

// 导出用户
export function exportRole(queryParams) {
    return downloadRequest('/system/role/export', queryParams)
}

// 根据角色获取菜单列表
export function roleMenuTreeselect(roleId) {
    return request({
        url: `system/menu/roleMenuTreeselect/${roleId}`,
        method: 'get'
    })
}

// 获取菜单树结构
export  function  treeselect(){
    return request({
        url: '/system/menu/treeselect',
        method: 'get'
    })
}