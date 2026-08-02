import request from '@/utils/request'

export function listType(params) { return request({ url: '/system/dict/type/list', method: 'get', params }) }
export function getType(dictId) { return request({ url: '/system/dict/type/' + dictId, method: 'get' }) }
export function addType(data) { return request({ url: '/system/dict/type', method: 'post', data }) }
export function updateType(data) { return request({ url: '/system/dict/type/update', method: 'post', data }) }
export function removeType(dictId) { return request({ url: '/system/dict/type/remove/' + dictId, method: 'post' }) }
export function refreshType(dictType) { return request({ url: '/system/dict/type/refresh/' + dictType, method: 'post' }) }
export function refreshAllType() { return request({ url: '/system/dict/type/refresh', method: 'post' }) }
