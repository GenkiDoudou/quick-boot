import {provide} from "vue";
import baseService from "./baseService.js";
import {useDict} from '@/utils/dict'

/**
 * 发送get请求
 * @param path 路径
 * @param params 参数
 * @param headers header
 * @returns {Promise | Promise<unknown>}
 */
export function getRequest(path, params, headers) {
    return baseService.get(path, params, headers)
}


/**
 * 发送get请求
 * @param path 路径
 * @param params 参数
 * @param headers header
 * @returns {Promise | Promise<unknown>}
 */
export function postRequest(path, params, headers) {
    return baseService.post(path, params, headers)
}

/**
 * 发送delete请求
 * @param path 路径
 * @param params 参数
 * @param headers header
 * @returns {Promise | Promise<unknown>}
 */
export function deleteRequest(path, params, headers) {
    return baseService.delete(path, params)
}



export function openNewTab(path) {

}

export default {
    getRequest,
    deleteRequest,
    openNewTab
}





