import request from '@/utils/request'

export function pageOperlog(pageRequest) {
  return request({ url: '/monitor/operlog/page', method: 'post', data: pageRequest })
}

export function getOperlog(operId) {
  return request({ url: `/monitor/operlog/${encodeURIComponent(operId)}`, method: 'get' })
}

export function removeOperlog(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/monitor/operlog/remove', method: 'post', data: list })
}

export function cleanOperlog() {
  return request({ url: '/monitor/operlog/clean', method: 'post' })
}

export function exportOperlog(snapshot) {
  return request({
    url: '/monitor/operlog/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}
