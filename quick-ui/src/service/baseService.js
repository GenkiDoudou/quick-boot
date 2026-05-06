import request from '../utils/request'

export default {
  get(path, params, headers) {
    return new Promise((resolve, reject) => {
      request({ url: path, params, headers, method: 'GET' })
        .then(resolve)
        .catch((error) => {
          if (error !== '-999') reject(error)
        })
    })
  },
  post(path, body, headers) {
    return request({
      url: path,
      method: 'POST',
      headers: { 'Content-Type': 'application/json;charset=UTF-8', ...headers },
      data: body
    })
  },
  put(path, params, headers) {
    return request({
      url: path,
      data: params,
      headers: { 'Content-Type': 'application/json;charset=UTF-8', ...headers },
      method: 'PUT'
    })
  },
  delete(path, params) {
    return request({ url: path, data: params, method: 'DELETE' })
  }
}
