import axios from 'axios'
import {ElNotification, ElMessageBox, ElMessage, ElLoading} from 'element-plus'
import {getToken} from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import {tansParams, blobValidate} from '@/utils/ruoyi'
import cache from '@/plugins/cache'
import {saveAs} from 'file-saver'
import useUserStore from '@/store/modules/user'
import {createTraceContext} from '@/utils/trace.js'
import {addSignature} from '@/utils/signature'

import {generateSignature, paramHandler, responseHandler} from '@/utils/secureEncryption.js'
import {checkRepeatSubmit} from '@/composables/useRepeatSubmit'

let downloadLoadingInstance;
// 是否显示重新登录
export let isRelogin = {show: false};

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
// 创建axios实例
const service = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: import.meta.env.VITE_APP_BASE_API,
    // 超时
    timeout: 10000
})

// request拦截器
service.interceptors.request.use(async config => {
    // 是否需要设置 token
    const isToken = (config.headers || {}).isToken === false
    // 是否需要防止数据重复提交
    const isRepeatSubmit = (config.headers || {}).repeatSubmit === false
    if (getToken() && !isToken) {
        config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    }

    generateSignature(config);
    paramHandler(config);


    const trace = createTraceContext();
    config.headers['traceparent'] = trace.traceparent;
    config.headers['x-trace-id'] = trace.traceId;

    // 防重复提交检查
    if (checkRepeatSubmit(config)) {
        return Promise.reject(new Error('数据正在处理，请勿重复提交'))
    }
    return config
}, error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
})


// 响应拦截器
service.interceptors.response.use(res => {

        // 二进制数据则直接返回
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            let disposition = res.headers['content-disposition'];
            // 判断,如果不为空 并且包含 filename= 的话,则根据  filename= 拆分, 后面的那一截
            if (disposition && disposition.indexOf('filename=') !== -1) {
                let filename = disposition.substring(disposition.indexOf('filename=') + 9, disposition.length);
                filename = decodeURI(filename);
                // 获取文件后缀
                console.log(filename)
                res.data.filename = filename;

            }


            return res.data
        }
        // 响应解密（基于国密SM2算法）
        // res = decryptResponseData(res);
        res = responseHandler(res);
        // 未设置状态码则默认成功状态
        const code = res.data.code || 200;
        // 获取错误信息
        const msg = errorCode[code] || res.data.msg || errorCode['default']

        if (code === 401) {
            if (!isRelogin.show) {
                isRelogin.show = true;
                ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', {
                    confirmButtonText: '重新登录',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    isRelogin.show = false;
                    useUserStore().logOut().then(() => {
                        location.href = '/index';
                    })
                }).catch(() => {
                    isRelogin.show = false;
                });
            }
            return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
        } else if (code === 500) {
            ElMessage({message: msg, type: 'error'})
            return Promise.reject(new Error(msg))
        } else if (code === 601) {
            ElMessage({message: msg, type: 'warning'})
            return Promise.reject(new Error(msg))
        } else if (code !== 200) {
            ElMessage.error({message: msg, type: 'error'})
            // ElNotification.error({title: msg})
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


// 通用下载方法
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
        console.log(data)
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

/**
 * 通用下载请求，返回 Promise<Blob>
 */
export function downloadRequest(url, params, config) {
    return service.post(url, params, {
        transformRequest: [(params) => {
            // 确保 params 不是 undefined 或 null
            if (!params) {
                params = {};
            }
            return tansParams(params);
        }],
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        responseType: 'blob',
        ...config
    })
}

export default service
