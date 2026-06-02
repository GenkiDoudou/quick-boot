import axios, { AxiosHeaders } from 'axios'
import {ElMessageBox, ElMessage, ElLoading} from 'element-plus'
import {getToken, removeToken} from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import {tansParams, blobValidate} from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import {saveAs} from 'file-saver'
import {applyClientSignHeaders} from '@/utils/clientSign'
import { isMonitorEnabled } from '@/monitor/config'
import {
  beginRequestObservation,
  finalizeRequestObservationSuccess,
  finalizeRequestObservationError
} from '@/monitor/requestObservation'

let downloadLoadingInstance;
let isShowReloginDialog = false;

/** 模块加载时读取一次，避免每个请求重复解析 env */
const monitorEnabled = isMonitorEnabled()

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 10000
})

service.interceptors.request.use(async (config) => {
    if (monitorEnabled) {
        beginRequestObservation(config)
    }
    // FormData：去掉 application/json（否则 axios 会转成 {"file":{}}）；勿手写 multipart/form-data（无 boundary 后端解析不到 file 部件）
    if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
        const headers = AxiosHeaders.from(config.headers)
        headers.delete('Content-Type')
        config.headers = headers
    }
    const isToken = (config.headers || {}).isToken === false
    if (getToken() && !isToken) {
        config.headers['Authorization'] = 'Bearer ' + getToken()
    }
    return applyClientSignHeaders(config)
}, error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
})

function handleUnauthorized(msg = '登录状态已过期，请重新登录') {
    if (window.location.pathname === '/login' || window.location.pathname.endsWith('/login')) {
        // 登录页：展示后端业务文案（如「用户名或密码错误」），不再静默 reject
        ElMessage({ message: msg, type: 'error', duration: 5 * 1000 })
        return Promise.reject(msg)
    }
    if (!isShowReloginDialog) {
        isShowReloginDialog = true
        removeToken()
        ElMessageBox.close()
        ElMessageBox.confirm(msg, '系统提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
            showClose: false
        }).then(() => {
            isShowReloginDialog = false
            window.location.replace('/login')
        }).catch(() => {
            isShowReloginDialog = false
            window.location.replace('/login')
        })
    }
    return Promise.reject(msg)
}

/**
 * 解析 axios 响应体为统一 R 形态对象（JSON 对象或 JSON 字符串）。
 *
 * @param {unknown} data `response.data`
 * @returns {Record<string, unknown> | null}
 */
function parseJsonResponseBody(data) {
    if (data == null) {
        return null
    }
    if (typeof data === 'object' && !Array.isArray(data)) {
        return /** @type {Record<string, unknown>} */ (data)
    }
    if (typeof data === 'string') {
        try {
            const o = JSON.parse(data)
            return typeof o === 'object' && o !== null && !Array.isArray(o) ? o : null
        } catch {
            return null
        }
    }
    return null
}

/**
 * HTTP 非 2xx 时走 axios 失败分支：若响应体为 R，则优先展示服务端 `msg`，
 * 业务码处理与成功分支对齐（含 401 登录失败 / 会话失效）。
 *
 * @param {import('axios').AxiosError} error
 * @returns {Promise<never> | null} 已提示并终结链路时返回 Promise，否则 null
 */
function tryHandleAxiosErrorResponseBody(error) {
    const res = error.response
    if (!res) {
        return null
    }
    const body = parseJsonResponseBody(res.data)
    if (!body) {
        return null
    }
    const serverMsg = typeof body.msg === 'string' ? body.msg.trim() : ''
    const bizCodeRaw = body.code
    const bizCode = bizCodeRaw !== undefined && bizCodeRaw !== null ? Number(bizCodeRaw) : NaN
    const hasBizCode = Number.isFinite(bizCode)
    const tip = serverMsg !== ''
        ? body.msg
        : (hasBizCode ? (errorCode[String(bizCode)] || errorCode['default']) : '')

    if (hasBizCode && bizCode === 401) {
        const msg = serverMsg !== '' ? body.msg : (errorCode['401'] || errorCode['default'])
        return handleUnauthorized(msg)
    }
    if (hasBizCode && bizCode === 500) {
        const msg = tip || errorCode['500']
        ElMessage({ message: msg, type: 'error' })
        return Promise.reject(new Error(msg))
    }
    if (hasBizCode && bizCode !== 200) {
        const msg = tip || errorCode[String(bizCode)] || errorCode['default']
        ElMessage({ message: msg, type: 'error', duration: 5 * 1000 })
        return Promise.reject(error)
    }
    if (!hasBizCode && res.status >= 400 && serverMsg !== '') {
        if (res.status === 401) {
            return handleUnauthorized(body.msg)
        }
        ElMessage({ message: body.msg, type: 'error', duration: 5 * 1000 })
        return Promise.reject(error)
    }
    return null
}

service.interceptors.response.use(async (res) => {
        if (monitorEnabled) {
            finalizeRequestObservationSuccess(res)
        }
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            // 导出等二进制场景：若后端返回的是 JSON 错误体，需要提前识别 401 并跳转登录。
            if (res.data && res.data.type && String(res.data.type).includes('application/json')) {
                try {
                    const text = await res.data.text()
                    const json = JSON.parse(text || '{}')
                    const code = Number(json.code || 200)
                    if (code === 401) {
                        return handleUnauthorized(json.msg || '登录状态已过期，请重新登录')
                    }
                } catch (e) {
                    // ignore: 保持原有 blob 返回行为
                }
            }
            /** @type {import('axios').AxiosRequestConfig & { returnBlobWithHeaders?: boolean }} */
            const cfg = res.config || {}
            if (cfg.returnBlobWithHeaders === true) {
                return { data: res.data, headers: res.headers }
            }
            return res.data
        }
        const code = Number(res.data.code || 200);
        const serverMsg = res.data.msg
        // 401 优先使用服务端 msg（登录失败等），避免被 errorCode['401'] 固定文案覆盖
        const msg =
            code === 401 && serverMsg
                ? serverMsg
                : (errorCode[String(code)] || serverMsg || errorCode['default'])

        if (code === 401) {
            return handleUnauthorized(msg)
        } else if (code === 500) {
            ElMessage({message: msg, type: 'error'})
            return Promise.reject(new Error(msg))
        } else if (code !== 200) {
            ElMessage.error({message: msg, type: 'error'})
            return Promise.reject('error')
        } else {
            return Promise.resolve(res.data)
        }
    },
    error => {
        console.error('响应拦截器错误:', error)
        if (monitorEnabled) {
            finalizeRequestObservationError(error)
        }
        const fromBody = tryHandleAxiosErrorResponseBody(error)
        if (fromBody != null) {
            return fromBody
        }
        let {message} = error;
        if (message == "Network Error") {
            message = "后端接口连接异常";
        } else if (message.includes("timeout")) {
            message = "系统接口请求超时";
        } else if (message.includes("Request failed with status code")) {
            message = "系统接口" + message.substr(message.length - 3) + "异常";
        }
        ElMessage({message: message, type: 'error', duration: 5 * 1000})
        return Promise.reject(error)
    }
)

/**
 * 表单 POST 导出：响应体为 **Blob**（`responseType: 'blob'`）。
 *
 * - **默认**：Promise resolve 值为 **`Blob`**（与历史行为一致）。
 * - **`config.returnBlobWithHeaders === true`**：resolve 值为 **`{ data: Blob, headers }`**，
 *   便于解析 **`Content-Disposition`**（如 **`C7ExcelDownload`** 与 **`filename*`**）。
 *
 * @param {string} url 相对 `baseURL` 的路径
 * @param {Record<string, unknown>} [params] 查询/表单参数（经 `tansParams` 序列化）
 * @param {import('axios').AxiosRequestConfig & { returnBlobWithHeaders?: boolean }} [config] 合并到 axios 请求配置；可传 **`returnBlobWithHeaders`**
 * @returns {Promise<Blob | { data: Blob, headers: import('axios').AxiosResponse['headers'] }>}
 */
export function downloadRequest(url, params, config) {
    return service.post(url, params, {
        transformRequest: [(params) => {
            if (!params) params = {}
            return tansParams(params)
        }],
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        responseType: 'blob',
        ...config
    })
}

export function download(url, params, filename, config) {
    downloadLoadingInstance = ElLoading.service({text: "正在下载数据，请稍候", background: "rgba(0, 0, 0, 0.7)",})
    return service.post(url, params, {
        transformRequest: [(params) => {
            return tansParams(params)
        }],
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        responseType: 'blob',
        ...config
    }).then(async (data) => {
        let fname = data.filename;
        if (!fname) {
            fname = filename;
        }
        const isBlob = blobValidate(data);
        if (isBlob) {
            const blob = new Blob([data])
            saveAs(blob, fname)
        } else {
            const resText = await data.text();
            const rspObj = JSON.parse(resText);
            const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
            ElMessage.error(errMsg);
        }
        downloadLoadingInstance.close();
    }).catch((r) => {
        console.error(r)
        ElMessage.error('下载文件出现错误，请联系管理员！')
        downloadLoadingInstance.close();
    })
}

export default service
