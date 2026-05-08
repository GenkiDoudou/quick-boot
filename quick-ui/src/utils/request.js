import axios from 'axios'
import {ElMessageBox, ElMessage, ElLoading} from 'element-plus'
import {getToken, removeToken} from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import {tansParams, blobValidate} from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import {saveAs} from 'file-saver'

let downloadLoadingInstance;
let isShowReloginDialog = false;

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API,
    timeout: 10000
})

service.interceptors.request.use(config => {
    const isToken = (config.headers || {}).isToken === false
    if (getToken() && !isToken) {
        config.headers['Authorization'] = 'Bearer ' + getToken()
    }
    return config
}, error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
})

service.interceptors.response.use(res => {
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            /** @type {import('axios').AxiosRequestConfig & { returnBlobWithHeaders?: boolean }} */
            const cfg = res.config || {}
            if (cfg.returnBlobWithHeaders === true) {
                return { data: res.data, headers: res.headers }
            }
            return res.data
        }
        const code = res.data.code || 200;
        const msg = errorCode[code] || res.data.msg || errorCode['default']

        if (code === 401) {
            // 已在登录页则不处理
            if (window.location.pathname === '/login') {
                return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
            }
            if (!isShowReloginDialog) {
                isShowReloginDialog = true;
                // 立即清除token，阻止后续请求继续携带无效token触发401
                removeToken();
                ElMessageBox.close();
                ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose: false
                }).then(() => {
                    isShowReloginDialog = false;
                    window.location.replace('/login');
                }).catch(() => {
                    isShowReloginDialog = false;
                    window.location.replace('/login');
                });
            }
            return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
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
