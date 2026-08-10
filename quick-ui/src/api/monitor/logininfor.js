import request from '@/utils/request'

export function pageLogininfor(pageRequest) {
  return request({ url: '/monitor/logininfor/page', method: 'post', data: pageRequest })
}

export function removeLogininfor(ids) {
  const list = (Array.isArray(ids) ? ids : [ids]).map(String)
  return request({ url: '/monitor/logininfor/remove', method: 'post', data: list })
}

export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'post' })
}

export function unlockLogininfor(userName) {
  return request({
    url: `/monitor/logininfor/unlock/${encodeURIComponent(userName)}`,
    method: 'get'
  })
}

export function exportLogininfor(snapshot) {
  return request({
    url: '/monitor/logininfor/export',
    method: 'post',
    data: snapshot || {},
    responseType: 'blob',
    returnBlobWithHeaders: true,
    timeout: 120000
  })
}
